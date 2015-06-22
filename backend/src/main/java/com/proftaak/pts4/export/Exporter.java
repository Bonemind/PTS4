package com.proftaak.pts4.export;

import com.proftaak.pts4.database.tables.*;

import javax.print.Doc;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.proftaak.pts4.rest.HTTPException;
import org.w3c.dom.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Created by Stan
 */
public class Exporter {
    public static String export(Team team) throws HTTPException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            // Write team into the document
            Element teamElement = doc.createElement("team");
            teamElement.setAttribute("name", team.getName());
            teamElement.setAttribute("effortTrackingEnabled", String.valueOf(team.isEffortTrackingEnabled()));

            // Make team the root element
            doc.appendChild(teamElement);

            // Write the scrum master into the document
            Exporter.writeScrumMaster(team.getScrumMaster(), teamElement, doc);

            // Write the users into the document
            Exporter.writeUsers(team.getUsers(), teamElement, doc);

            // Write projects into the document
            Exporter.writeProjects(team.getProjects(), teamElement, doc);

            // Write iterations into the document
            Exporter.writeIterations(team.getIterations(), teamElement, doc);

            // Write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);

            // Create a StringWriter for the output
            StringWriter outWriter = new StringWriter();
            StreamResult result = new StreamResult(outWriter);

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);

            StringBuffer sb = outWriter.getBuffer();
            return sb.toString();
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            throw HTTPException.ERROR_INTERNAL;
        }
    }

    private static void writeIterations(List<Iteration> iterations, Element teamElement, Document doc) {
        Element iterationsElement = doc.createElement("iterations");

        for (Iteration iteration : iterations) {
            Element iterationElement = Exporter.writeIteration(iteration, doc);

            iterationsElement.appendChild(iterationElement);
        }

        teamElement.appendChild(iterationsElement);
    }

    private static Element writeIteration(Iteration iteration, Document doc) {
        Element iterationElement = doc.createElement("iteration");
        iterationElement.setAttribute("name", iteration.getName());
        iterationElement.setAttribute("description", iteration.getDescription());
        iterationElement.setAttribute("startDate", iteration.getStart().toString());
        iterationElement.setAttribute("endDate", iteration.getEnd().toString());

        Element storiesElement = doc.createElement("stories");
        for (Story story : iteration.getStories()) {
            storiesElement.appendChild(Exporter.writeStory(story, doc));
        }

        iterationElement.appendChild(storiesElement);

        return iterationElement;
    }

    private static void writeProjects(List<Project> projects, Element teamElement, Document doc) {
        Element projectsElement = doc.createElement("projects");

        for (Project proj : projects) {
            Element projElement = Exporter.writeProject(proj, doc);

            projectsElement.appendChild(projElement);
        }

        teamElement.appendChild(projectsElement);
    }

    private static Element writeProject(Project project, Document doc) {
        Element projElement = doc.createElement("project");
        projElement.setAttribute("name", project.getName());
        projElement.setAttribute("description", project.getDescription());

        Element POElement = doc.createElement("productOwner");
        POElement.appendChild(Exporter.writeUser(project.getProductOwner(), doc));
        projElement.appendChild(POElement);

        Element storiesElement = doc.createElement("stories");
        for (Story story : project.getStories()) {
            storiesElement.appendChild(Exporter.writeStory(story, doc));
        }

        projElement.appendChild(storiesElement);

        return  projElement;
    }

    private static Element writeStory(Story story, Document doc) {
        Element storyElement = doc.createElement("story");
        storyElement.setAttribute("id", String.valueOf(story.getId()));
        storyElement.setAttribute("type", String.valueOf(story.getType()));
        storyElement.setAttribute("name", story.getName());
        storyElement.setAttribute("description", story.getDescription());
        storyElement.setAttribute("status", String.valueOf(story.getStatus()));

        if (story.getCompletedOn() != null)
            storyElement.setAttribute("completedOn", story.getCompletedOn().toString());

        if (story.getIterationSetOn() != null)
            storyElement.setAttribute("addedToIterationOn", story.getIterationSetOn().toString());

        storyElement.setAttribute("points", String.valueOf(story.getPoints()));
        storyElement.setAttribute("priority", String.valueOf(story.getPriority()));

        Element tasksElements = doc.createElement("tasks");
        for (Task task : story.getTasks()) {
            tasksElements.appendChild(Exporter.writeTask(task, doc));
        }

        Element testsElements = doc.createElement("tests");
        for (Test test : story.getTests()) {
            testsElements.appendChild(Exporter.writeTest(test, doc));
        }

        storyElement.appendChild(tasksElements);
        storyElement.appendChild(testsElements);

        return  storyElement;
    }

    private static Element writeTask(Task task, Document doc) {
        Element taskElement = doc.createElement("task");
        taskElement.setAttribute("id", String.valueOf(task.getId()));
        taskElement.setAttribute("name", task.getName());
        taskElement.setAttribute("description", task.getDescription());
        taskElement.setAttribute("status", String.valueOf(task.getStatus()));
        taskElement.setAttribute("estimate", String.valueOf(task.getEstimate()));
        taskElement.setAttribute("timeSpent", String.valueOf(task.getTimeSpent()));

        if (task.getOwner() != null) {
            Element ownerElement = doc.createElement("owner");
            ownerElement.appendChild(Exporter.writeUser(task.getOwner(), doc));

            taskElement.appendChild(ownerElement);
        }

        return taskElement;
    }

    private static Element writeTest(Test test, Document doc) {
        Element testElement = doc.createElement("test");
        testElement.setAttribute("id", String.valueOf(test.getId()));
        testElement.setAttribute("name", test.getName());
        testElement.setAttribute("description", test.getDescription());
        testElement.setAttribute("accepted", String.valueOf(test.isAccepted()));

        return testElement;
    }

    private static void writeScrumMaster(User scrumMaster, Element teamElement, Document doc) {
        Element SMElement = doc.createElement("scrumMaster");
        Element userElement = Exporter.writeUser(scrumMaster, doc);

        teamElement.appendChild(SMElement);
        SMElement.appendChild(userElement);
    }

    private static void writeUsers(List<User> users, Element teamElement, Document doc) {
        Element usersElement = doc.createElement("users");

        for (User user : users) {
            Element userElement = Exporter.writeUser(user, doc);

            usersElement.appendChild(userElement);
        }

        teamElement.appendChild(usersElement);
    }

    private static Element writeUser(User user, Document doc) {
        Element userElement = doc.createElement("user");
        userElement.setAttribute("id", String.valueOf(user.getId()));
        userElement.setAttribute("name", user.getName());
        userElement.setAttribute("email", user.getEmail());

        return userElement;
    }
}
