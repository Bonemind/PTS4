package com.proftaak.pts4.importers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.tables.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Justin on 22/06/2015
 */
public class RallyImporter {

    /**
     * While processing an import, these variables are set and used,
     * when an import is done, these values are returned back to default
     */
    private static Team team = null;
    private static Project newProject = null;
    private static Map<String, Object> newObjects_refObjectUUID = new HashMap<>();


    /**
     * Import function for a project from rally. (rally exports individual files).
     *
     * @param inputStreams A set of input streams which are directly rally XML export files.
     * @param team         The team to add the project in the import to.
     * @return The imported project
     */
    public synchronized static Project importRally(Collection<InputStream> inputStreams, Team team) {
        // Set the team available for all methods
        RallyImporter.team = team;

        // Create the iterations (and a project)
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        for (InputStream inputStream : inputStreams) {
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document dom = builder.parse(inputStream);
                Element document = dom.getDocumentElement();

                RallyImporter.createIterations(document);
            } catch (Exception exception) {
                System.err.println(exception.getClass() + " exception caught while importing iterations");
            }
        }

        // The import failed if no iterations file was found, thus no newProject was set.
        if (RallyImporter.newProject == null) {
            RallyImporter.reset();
            System.err.println("Found no iterations, and couldn't import the project");
            return null;
        }

        RallyImporter.newProject.setProductOwner(team.getScrumMaster());

        // Create the stories and defects
        for (InputStream inputStream : inputStreams) {
            try {
                inputStream.reset();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document dom = builder.parse(inputStream);
                Element document = dom.getDocumentElement();

                RallyImporter.createStories(document);
            } catch (Exception exception) {
                System.err.println(exception.getClass() + " exception caught while importing stories");
            }
        }

        // Create the tasks
        for (InputStream inputStream : inputStreams) {
            try {
                inputStream.reset();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document dom = builder.parse(inputStream);
                Element document = dom.getDocumentElement();

                RallyImporter.createTasks(document);
            } catch (Exception exception) {
                System.err.println(exception.getClass() + " exception caught while importing tasks");
            }
        }

        Ebean.beginTransaction();
        Ebean.save(RallyImporter.team.getIterations());
        Ebean.save(RallyImporter.team);
        Ebean.commitTransaction();

        System.out.println("Import succeeded");
        Project project = RallyImporter.newProject;
        RallyImporter.reset();
        return project;
    }

    private static void createIterations(Node node) {
        // If node represents an iteration
        if (node.getNodeName().equals("Iteration")) {
            // Import this iteration
            RallyImporter.createIteration(node);
        } else if (node.getNodeName().equals("HierarchicalRequirement") ||
                node.getNodeName().equals("Defect") ||
                node.getNodeName().equals("Task")) {
            // To make sure I don't get in <HierarchicalRequirement><Iteration></HierarchicalRequirement>
            return;
        } else {
            // Otherwise look deeper for iterations
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    RallyImporter.createIterations(child);
                }
            }
        }
    }

    private static void createIteration(Node iterationNode) {
        // Create the project, only for the first iteration
        if (RallyImporter.newProject == null) {
            System.out.println("Creating a project");
            RallyImporter.newProject = new Project();
            RallyImporter.team.getProjects().add(newProject);
            String projectName = RallyImporter.getNodeRefName(iterationNode, "Project");
            newProject.setName(projectName);
            newProject.setTeam(RallyImporter.team);
        }

        System.out.println("Creating an iteration");
        // Get iteration specific data from the node
        String iterationName = RallyImporter.getNodeText(iterationNode, "Name");
        String startDate = RallyImporter.getNodeText(iterationNode, "StartDate");
        startDate = startDate.substring(0, startDate.indexOf("T"));
        String endDate = RallyImporter.getNodeText(iterationNode, "EndDate");
        endDate = endDate.substring(0, endDate.indexOf("T"));

        // Create a new iteration object
        Iteration newIteration = new Iteration();
        newIteration.setName(iterationName);
        newIteration.setStart(LocalDate.parse(startDate));
        newIteration.setEnd(LocalDate.parse(endDate));
        RallyImporter.team.getIterations().add(newIteration);

        // Add the new iteration object to the given team
        newIteration.setTeam(RallyImporter.team);

        // Add the new iteration with his rally uuid to a map
        String uuid = RallyImporter.getNodeText(iterationNode, "ObjectUUID");
        newObjects_refObjectUUID.put(uuid, newIteration);
    }

    private static void createStories(Node node) {
        // If node represents a story
        if (node.getNodeName().equals("HierarchicalRequirement")) {// Story in rally xml
            // Import this story
            RallyImporter.createStory(node, false);
        } else if (node.getNodeName().equals("Defect")) {
            // Import this defect as a story with defect type
            RallyImporter.createStory(node, true);
        } else {
            // Otherwise look deeper for stories
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    RallyImporter.createStories(child);
                }
            }
        }
    }

    /**
     * Used for both a rally story and rally defect
     */
    private static void createStory(Node storyNode, boolean isDefect) {
        System.out.println("Creating a story");
        // Get story specific data from the node
        String storyName = RallyImporter.getNodeText(storyNode, "Name");
        String points = RallyImporter.getNodeText(storyNode, "PlanEstimate");
        String status = RallyImporter.getNodeText(storyNode, "ScheduleState");
        String description = RallyImporter.getNodeText(storyNode, "Description");

        // Clean the description of <div> tags
        description = description.replace("<div>", "\n").replace("</div>", "");

        // Set points to 0, if null
        if (points == null) {
            points = "0";
        }

        // Transform the rally string status to a Story.status instance
        Story.Status storyStatus = Story.Status.DEFINED;
        switch (status) {
            // Omitted the defined case and defaulted it
            case "In-Progress":
                storyStatus = Story.Status.IN_PROGRESS;
                break;
            case "Completed":
                storyStatus = Story.Status.DONE;
                break;
            case "Accepted":
                storyStatus = Story.Status.ACCEPTED;
                break;
        }

        // Get the type
        Story.Type type = Story.Type.USER_STORY;
        if (isDefect) {
            type = Story.Type.DEFECT;
        }

        // Get the iteration this story is in
        String iteration_uuid = RallyImporter.getNodeRefUuid(storyNode, "Iteration");
        Iteration iteration = (Iteration) RallyImporter.newObjects_refObjectUUID.get(iteration_uuid);

        // Create a new story object
        int defaultPriority = 1;
        int storypoints = (int) Math.round(Double.valueOf(points));
        Story newStory = new Story(RallyImporter.newProject, iteration, type, storyName, description, storyStatus, defaultPriority, storypoints);
        RallyImporter.newProject.getStories().add(newStory);

        // Add the new story with his rally uuid to a map
        String uuid = RallyImporter.getNodeText(storyNode, "ObjectUUID");
        newObjects_refObjectUUID.put(uuid, newStory);
    }

    private static void createTasks(Node node) {
        // If node represents a task
        if (node.getNodeName().equals("Task")) {
            // Import this task
            RallyImporter.createTask(node);
        } else {
            // Otherwise look deeper for tasks
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    RallyImporter.createTasks(child);
                }
            }
        }
    }

    private static void createTask(Node taskNode) {
        System.out.println("Creating a task");
        // Get task specific data from the node
        String taskName = RallyImporter.getNodeText(taskNode, "Name");
        String status = RallyImporter.getNodeText(taskNode, "State");
        String estimate = RallyImporter.getNodeText(taskNode, "Estimate");
        String timeTodo = RallyImporter.getNodeText(taskNode, "ToDo");
        String description = RallyImporter.getNodeText(taskNode, "Description");

        // Set estimate and timeTODO to 0, if null
        if (estimate == null) {
            estimate = "0.0";
        }
        if (timeTodo == null) {
            timeTodo = "0.0";
        }

        Double timeSpent = Double.valueOf(estimate) - Double.valueOf(timeTodo);


        // Transform the rally string status to a Story.status instance
        Task.Status taskStatus = Task.Status.DEFINED;
        switch (status) {
            // Omitted the defined case and defaulted it
            case "In-Progress":
                taskStatus = Task.Status.IN_PROGRESS;
                break;
            case "Completed":
                taskStatus = Task.Status.DONE;
                break;
        }

        // Get the story this task is in
        String iteration_uuid = RallyImporter.getNodeRefUuid(taskNode, "WorkProduct");
        Story story = (Story) RallyImporter.newObjects_refObjectUUID.get(iteration_uuid);

        // Create a new task object
        Task newTask = new Task(story, null, taskName, description, Double.valueOf(estimate), taskStatus);
        story.getTasks().add(newTask);
        newTask.setTimeSpent(timeSpent);
    }


    private static String getNodeText(Node parent, String childnodeName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(childnodeName)) {
                return child.getTextContent();
            }
        }

        return null;
    }

    private static String getNodeRefUuid(Node parent, String childnodeName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(childnodeName)) {
                return child.getAttributes().getNamedItem("refObjectUUID").getNodeValue();
            }
        }

        return null;
    }

    private static String getNodeRefName(Node parent, String childnodeName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals(childnodeName)) {
                return child.getAttributes().getNamedItem("refObjectName").getNodeValue();
            }
        }

        return null;
    }


    private static void reset() {
        RallyImporter.team = null;
        RallyImporter.newProject = null;
        newObjects_refObjectUUID = new HashMap<>();
    }

}
