package com.proftaak.pts4.importers;

import com.proftaak.pts4.database.tables.*;
import com.versionone.apiclient.Asset;
import com.versionone.apiclient.Query;
import com.versionone.apiclient.Services;
import com.versionone.apiclient.V1Connector;
import com.versionone.apiclient.exceptions.APIException;
import com.versionone.apiclient.exceptions.ConnectionException;
import com.versionone.apiclient.exceptions.OidException;
import com.versionone.apiclient.exceptions.V1Exception;
import com.versionone.apiclient.filters.FilterTerm;
import com.versionone.apiclient.interfaces.IAssetType;
import com.versionone.apiclient.interfaces.IAttributeDefinition;
import com.versionone.apiclient.interfaces.IServices;
import com.versionone.apiclient.services.QueryResult;

import java.net.MalformedURLException;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by stijn on 16/6/2015.
 */
public class VersionOneImporter extends Importer {

    private String baseUrl;
    private static final String APPNAME = "...";
    private static final String VERSION = "1";
    private V1Connector connector;
    private String accesToken;
    private IServices services;



    public VersionOneImporter(String accesToken, String baseUrl) {
        this.accesToken = accesToken;
        this.baseUrl = baseUrl;
    }

    public VersionOneImporter() {
        this.accesToken = "1.2f+cgMtkfPWXzrLy3LrOuTJR9gQ=";
        this.baseUrl = "https://www52.v1host.com/sdf";
    }

    /***
     * creates the connection using the accesToken
     * @throws V1Exception
     * @throws MalformedURLException
     */
    public void initConnection() throws V1Exception, MalformedURLException {
        this.connector = V1Connector
                .withInstanceUrl(this.baseUrl)
                .withUserAgentHeader(APPNAME, VERSION)
                .withAccessToken(this.accesToken)
                .build();

        this.services = new Services(connector);
    }

    /***
     * creates the connection using the given username and password
     * @param username
     * @param password
     * @throws V1Exception
     * @throws MalformedURLException
     */
    public void initConnection(String username, String password) throws V1Exception, MalformedURLException {
        this.connector = V1Connector
                .withInstanceUrl(this.baseUrl)
                .withUserAgentHeader(APPNAME, VERSION)
                .withUsernameAndPassword(username, password)
                .build();

        this.services = new Services(connector);
    }

    /***
     * imports the project that corresponds with the given @nameOfProject
     * @param nameOfProject
     * @param team
     * @param productOwner
     * @return
     * @throws OidException
     * @throws ConnectionException
     * @throws APIException
     */
    public Project importProject(String nameOfProject, Team team, User productOwner) throws OidException, ConnectionException, APIException {
        if (services == null) throw new InvalidParameterException("first init  the connection");

        // setup query
        IAssetType projects = services.getMeta().getAssetType("Scope");
        Query query = new Query(projects);

        // setup attributes that are used in query
        IAttributeDefinition nameOfProjectAtr = projects.getAttributeDefinition("Name");
        IAttributeDefinition identifier = projects.getAttributeDefinition("ID");
        IAttributeDefinition description = projects.getAttributeDefinition("Description");

        // add attributes to query
        query.getSelection().add(nameOfProjectAtr);
        query.getSelection().add(identifier);
        query.getSelection().add(description);

        //setup filter
        FilterTerm filterName = new FilterTerm(nameOfProjectAtr);
        filterName.equal(nameOfProject);
        query.setFilter(filterName);

        // get result from query
        QueryResult queryResult = services.retrieve(query);

        // test if there is a project to be found by the given name
        if (queryResult.getAssets().length == 0) throw new InvalidParameterException("project by that name can't be found");
        Asset projectAsset = queryResult.getAssets()[0];

        // get the projectIdentifier & get the description
        int projectIdentifier = parseInt(projectAsset.getAttribute(identifier).toString());
        String descriptionOfResult = parseString(projectAsset.getAttribute(description).toString());

        // get all the iterations
        HashMap<Integer, Iteration> iterations = importIterations(team);

        // create the project & set relationship
        Project project = new Project(team, productOwner, nameOfProject, descriptionOfResult);
        team.getProjects().add(project);

        // get all stories
        HashMap<Integer, Story> stories = importStories(project, projectIdentifier, iterations);

        // get all tasks, get all defects & get all tests
        importTasks(project, projectIdentifier, stories);
        importDefects(project, projectIdentifier, iterations);
        importTests(project, projectIdentifier, stories);

        List<Story> testStories = project.getStories();

        return project;
    }

    /***
     * imports the stories from the given project
     * @param project
     * @param projectIdentifier
     * @param iterations
     * @return
     * @throws ConnectionException
     * @throws APIException
     * @throws OidException
     */
    private HashMap<Integer, Story> importStories(Project project, int projectIdentifier, HashMap<Integer, Iteration> iterations) throws ConnectionException, APIException, OidException {
        // get all available statuses
        HashMap<Integer, Story.Status> statuses = importStoryStatuses();

        // create returnValue
        HashMap<Integer, Story> returnValue = new HashMap<>();

        // setup Query
        IAssetType projects = services.getMeta().getAssetType("Story");
        Query query = new Query(projects);

        // setup attributes that are used in query
        IAttributeDefinition nameOfIterationAtr = projects.getAttributeDefinition("Name");
        IAttributeDefinition projectVO = projects.getAttributeDefinition("Scope");
        IAttributeDefinition affectedByDefects = projects.getAttributeDefinition("AffectedByDefects");
        IAttributeDefinition description = projects.getAttributeDefinition("Description");
        IAttributeDefinition estimate = projects.getAttributeDefinition("Estimate");
        IAttributeDefinition iteration = projects.getAttributeDefinition("Timebox");
        IAttributeDefinition status = projects.getAttributeDefinition("Status");
        IAttributeDefinition identifier = projects.getAttributeDefinition("ID");

        // add attributes to query
        query.getSelection().add(nameOfIterationAtr);
        query.getSelection().add(projectVO);
        query.getSelection().add(affectedByDefects);
        query.getSelection().add(description);
        query.getSelection().add(estimate);
        query.getSelection().add(iteration);
        query.getSelection().add(status);
        query.getSelection().add(identifier);

        // get result from query
        QueryResult queryResult = services.retrieve(query);

        for (Asset storyInResult : queryResult.getAssets()){
            // test if this belongs to the searched project
            if (projectIdentifier != (parseInt(storyInResult.getAttribute(projectVO).toString()))) continue;

            // get the name of the story
            String nameOfStory = parseString(storyInResult.getAttribute(nameOfIterationAtr).toString());

            // get the description of the story
            String descriptionOfResult = parseString(storyInResult.getAttribute(description).toString());

            // get the iteration it belongs to
            Iteration iterationResult = null;
            if (!parseString(storyInResult.getAttribute(iteration).toString()).toLowerCase().equals("null")) {
                iterationResult = iterations.get(parseInt(storyInResult.getAttribute(iteration).toString()));
            }

            // get the current status
            Story.Status storyStatus = null;
            if (!parseString(storyInResult.getAttribute(status).toString()).toLowerCase().equals("null")) {
                System.out.println();
                storyStatus = statuses.get(parseInt(storyInResult.getAttribute(status).toString()));
            } else {
                storyStatus = Story.Status.DEFINED;
            }

            // get the estimate points
            int estimateOfPoints = 0;
            if (!parseString(storyInResult.getAttribute(estimate).toString()).toLowerCase().equals("null")) {
                estimateOfPoints = (int) Math.round(Double.parseDouble(parseString(storyInResult.getAttribute(estimate).toString())));
            }

            //get the identifier
            int identifierOfResult = parseInt(storyInResult.getAttribute(identifier).toString());

            // create story and put into the return value
            Story story = new Story(project, iterationResult, Story.Type.USER_STORY, nameOfStory, descriptionOfResult, storyStatus, 0, estimateOfPoints);
            returnValue.put(identifierOfResult, story);

            // set relationships
            if (iterationResult == null){
                project.getStories().add(story);
            } else {
                iterationResult.getStories().add(story);
            }

            System.out.println(storyInResult.getAttribute(nameOfIterationAtr).toString());
            System.out.println(storyInResult.getAttribute(projectVO).toString());
            System.out.println(storyInResult.getAttribute(affectedByDefects).toString());
            System.out.println(storyInResult.getAttribute(description).toString());
            System.out.println(storyInResult.getAttribute(estimate).toString());
            System.out.println(storyInResult.getAttribute(iteration).toString());
            System.out.println(storyInResult.getAttribute(status).toString());
        }

        return returnValue;
    }

    /***
     * imports the stories from the given project
     * @param project
     * @param projectIdentifier
     * @param iterations
     * @return
     * @throws ConnectionException
     * @throws APIException
     * @throws OidException
     */
    private void importDefects(Project project, int projectIdentifier, HashMap<Integer, Iteration> iterations) throws ConnectionException, APIException, OidException {
        // get all available statuses
        HashMap<Integer, Story.Status> statuses = importStoryStatuses();

        // setup Query
        IAssetType defects = services.getMeta().getAssetType("Defect");
        Query query = new Query(defects);

        // setup attributes that are used in query
        IAttributeDefinition nameOfDefectAtr = defects.getAttributeDefinition("Name");
        IAttributeDefinition projectVO = defects.getAttributeDefinition("Scope");
        IAttributeDefinition description = defects.getAttributeDefinition("Description");
        IAttributeDefinition estimate = defects.getAttributeDefinition("Estimate");
        IAttributeDefinition iteration = defects.getAttributeDefinition("Timebox");
        IAttributeDefinition status = defects.getAttributeDefinition("Status");
        IAttributeDefinition identifier = defects.getAttributeDefinition("ID");

        // add attributes to query
        query.getSelection().add(nameOfDefectAtr);
        query.getSelection().add(projectVO);
        query.getSelection().add(description);
        query.getSelection().add(estimate);
        query.getSelection().add(iteration);
        query.getSelection().add(status);
        query.getSelection().add(identifier);

        // get result from query
        QueryResult queryResult = services.retrieve(query);

        for (Asset defectInResult : queryResult.getAssets()){
            // test if this belongs to the searched project
            if (projectIdentifier != (parseInt(defectInResult.getAttribute(projectVO).toString()))) continue;

            // get the name of the defect
            String nameOfDefect = parseString(defectInResult.getAttribute(nameOfDefectAtr).toString());

            // get the description of the defect
            String descriptionOfResult = parseString(defectInResult.getAttribute(description).toString());

            // get the iteration it belongs to
            Iteration iterationResult = null;
            if (!parseString(defectInResult.getAttribute(iteration).toString()).toLowerCase().equals("null")) {
                iterationResult = iterations.get(parseInt(defectInResult.getAttribute(iteration).toString()));
            }

            // get the current status
            Story.Status storyStatus = null;
            if (!parseString(defectInResult.getAttribute(status).toString()).toLowerCase().equals("null")) {
                storyStatus = statuses.get(parseInt(defectInResult.getAttribute(status).toString()));
            } else {
                storyStatus = Story.Status.DEFINED;
            }

            // get the estimate points
            int estimateOfPoints = 0;
            if (!parseString(defectInResult.getAttribute(estimate).toString()).toLowerCase().equals("null")) {
                estimateOfPoints = (int) Math.round(Double.parseDouble(parseString(defectInResult.getAttribute(estimate).toString())));
            }

            // create story and put into the return value
            Story defect = new Story(project, iterationResult, Story.Type.DEFECT, nameOfDefect, descriptionOfResult, storyStatus, 0, estimateOfPoints);

            // set relationships
            if (iterationResult == null){
                project.getStories().add(defect);
            } else{
                iterationResult.getStories().add(defect);
            }

            System.out.println(defectInResult.getAttribute(nameOfDefectAtr).toString());
            System.out.println(defectInResult.getAttribute(projectVO).toString());
            System.out.println(defectInResult.getAttribute(description).toString());
            System.out.println(defectInResult.getAttribute(estimate).toString());
            System.out.println(defectInResult.getAttribute(iteration).toString());
            System.out.println(defectInResult.getAttribute(status).toString());
        }
    }

    /***
     * imports the stories from the given project
     * @param project
     * @param projectIdentifier
     * @param stories
     * @return
     * @throws ConnectionException
     * @throws APIException
     * @throws OidException
     */
    private HashMap<Integer, Task> importTasks(Project project, int projectIdentifier, HashMap<Integer, Story> stories) throws ConnectionException, APIException, OidException {
        // get all available statuses
        HashMap<Integer, Task.Status> statuses = importTaskStatuses();

        // create returnValue
        HashMap<Integer, Task> returnValue = new HashMap<>();

        // setup Query
        IAssetType tasks = services.getMeta().getAssetType("Task");
        Query query = new Query(tasks);

        // setup attributes that are used in query
        IAttributeDefinition nameOfTaskAtr = tasks.getAttributeDefinition("Name");
        IAttributeDefinition projectVO = tasks.getAttributeDefinition("Scope");
        IAttributeDefinition description = tasks.getAttributeDefinition("Description");
        IAttributeDefinition parent = tasks.getAttributeDefinition("Parent");
        IAttributeDefinition estimate = tasks.getAttributeDefinition("Estimate");
        IAttributeDefinition iteration = tasks.getAttributeDefinition("Timebox");
        IAttributeDefinition status = tasks.getAttributeDefinition("Status");
        IAttributeDefinition identifier = tasks.getAttributeDefinition("ID");

        // add attributes to query
        query.getSelection().add(nameOfTaskAtr);
        query.getSelection().add(projectVO);
        query.getSelection().add(description);
        query.getSelection().add(estimate);
        query.getSelection().add(iteration);
        query.getSelection().add(status);
        query.getSelection().add(identifier);
        query.getSelection().add(parent);

        // get result from query
        QueryResult queryResult = services.retrieve(query);

        for (Asset taskInResult : queryResult.getAssets()){
            if (projectIdentifier != (parseInt(taskInResult.getAttribute(projectVO).toString()))) continue;

            // parse name & description
            String nameOfTask = parseString(taskInResult.getAttribute(nameOfTaskAtr).toString());
            String descriptionOfResult = parseString(taskInResult.getAttribute(description).toString());

            // parse the status
            Task.Status taskStatus = null;
            if (!parseString(taskInResult.getAttribute(status).toString()).toLowerCase().equals("null")) {
                taskStatus = statuses.get(parseInt(taskInResult.getAttribute(status).toString()));
            }

            // parse the estimateOfPoints
            int estimateOfPoints = 0;
            if (!parseString(taskInResult.getAttribute(estimate).toString()).toLowerCase().equals("null")) {
                estimateOfPoints = (int) Math.round(Double.parseDouble(parseString(taskInResult.getAttribute(estimate).toString())));
            }

            // parse the identifier
            int identifierOfResult = parseInt(taskInResult.getAttribute(identifier).toString());

            // parse the parent
            int parentIdentifier = parseInt(taskInResult.getAttribute(parent).toString());
            Story story = stories.get(parentIdentifier);

            // set relationships
            Task task = new Task(story, new User(), nameOfTask, descriptionOfResult, (double)estimateOfPoints,  taskStatus);
            story.getTasks().add(task);

            // add to returnValue
            returnValue.put(identifierOfResult, task);

            System.out.println(task.getName() + " Parent: " + task.getStory().toString());
        }

        return returnValue;
    }

    /***
     * imports the Iterations from the given Iteration
     * this method only works if there is one project available
     * @param team
     * @return
     * @throws ConnectionException
     * @throws APIException
     * @throws OidException
     */
    private HashMap<Integer, Iteration> importIterations(Team team) throws ConnectionException, APIException, OidException {
        // create returnValue
        HashMap<Integer, Iteration> returnValue = new HashMap<>();

        // setup Query
        IAssetType projects = services.getMeta().getAssetType("Timebox");
        Query query = new Query(projects);

        // setup attributes that are used in query
        IAttributeDefinition nameOfIterationAtr = projects.getAttributeDefinition("Name");
        IAttributeDefinition beginDate = projects.getAttributeDefinition("BeginDate");
        IAttributeDefinition endDate = projects.getAttributeDefinition("EndDate");
        IAttributeDefinition description = projects.getAttributeDefinition("Description");
        IAttributeDefinition id = projects.getAttributeDefinition("ID");
        //IAttributeDefinition scope = projects.getAttributeDefinition("SecurityScope");

        // add attributes to query
        query.getSelection().add(nameOfIterationAtr);
        query.getSelection().add(beginDate);
        query.getSelection().add(endDate);
        query.getSelection().add(description);
        query.getSelection().add(id);
        //query.getSelection().add(scope);

        // get result from query
        QueryResult queryResult = services.retrieve(query);

        for (Asset iteration : queryResult.getAssets()){
            // needed to test for more then one project
            //if (projectIdentifier != (parseInt(iteration.getAttribute(scope).toString()))) continue;

            LocalDate startDate = parseDate(iteration.getAttribute(beginDate).toString());
            LocalDate endingDate = parseDate(iteration.getAttribute(beginDate).toString());
            String name = iteration.getAttribute(nameOfIterationAtr).toString();
            String iterationDescription = iteration.getAttribute(nameOfIterationAtr).toString();
            String unparsedIdentifier = iteration.getAttribute(id).toString().split(":")[1];
            int requestedId = Integer.parseInt(unparsedIdentifier.substring(0, unparsedIdentifier.length() - 1));

            // create Iteration and put into returnValue
            Iteration iterationForReturnValue = new Iteration(team, startDate, endingDate, name, iterationDescription);
            returnValue.put(requestedId, iterationForReturnValue);

            // set relationships
            team.getIterations().add(iterationForReturnValue);
        }

        return returnValue;
    }

    /***
     * imports all the tests from the given project
     * @param project
     * @param stories
     */
    private void importTests(Project project, int projectIdentifier, HashMap<Integer, Story> stories) throws ConnectionException, APIException, OidException {
        // setup Query
        IAssetType tests = services.getMeta().getAssetType("Test");
        Query query = new Query(tests);

        // setup attributes that are used in query
        IAttributeDefinition nameOfTestAtr = tests.getAttributeDefinition("Name");
        IAttributeDefinition storyAtr = tests.getAttributeDefinition("Super");
        IAttributeDefinition description = tests.getAttributeDefinition("Description");
        IAttributeDefinition parent = tests.getAttributeDefinition("Parent");
        IAttributeDefinition estimate = tests.getAttributeDefinition("Estimate");
        IAttributeDefinition projectVO = tests.getAttributeDefinition("Scope");
        IAttributeDefinition status = tests.getAttributeDefinition("Status");
        IAttributeDefinition identifier = tests.getAttributeDefinition("ID");

        // add attributes to query
        query.getSelection().add(nameOfTestAtr);
        query.getSelection().add(projectVO);
        query.getSelection().add(description);
        query.getSelection().add(estimate);
        query.getSelection().add(status);
        query.getSelection().add(identifier);
        query.getSelection().add(parent);

        // get result from query
        QueryResult queryResult = services.retrieve(query);

        for (Asset testInResult : queryResult.getAssets()){
            if (projectIdentifier != (parseInt(testInResult.getAttribute(projectVO).toString()))) continue;

            // parse name & description
            String nameOfTest = parseString(testInResult.getAttribute(nameOfTestAtr).toString());
            String descriptionOfResult = parseString(testInResult.getAttribute(description).toString());


            // parse the estimateOfPoints
            int estimateOfPoints = 0;
            if (!parseString(testInResult.getAttribute(estimate).toString()).toLowerCase().equals("null")) {
                estimateOfPoints = (int) Math.round(Double.parseDouble(parseString(testInResult.getAttribute(estimate).toString())));
            }

            // parse the identifier
            int identifierOfResult = parseInt(testInResult.getAttribute(identifier).toString());

            // parse the parent
            int parentIdentifier = parseInt(testInResult.getAttribute(parent).toString());
            Story story = stories.get(parentIdentifier);

            // create test
            Test test = new Test(story, nameOfTest, descriptionOfResult);

            // set relationships
            story.getTests().add(test);
        }
    }

    /***
     * imports all the story statuses available and match them to the native Story enum
     * @return
     * @throws ConnectionException
     * @throws APIException
     * @throws OidException
     */
    private HashMap<Integer, Story.Status> importStoryStatuses() throws ConnectionException, APIException, OidException {
        // create returnValue
        HashMap<Integer, Story.Status> returnValue = new HashMap<>();

        // setup Query
        IAssetType projects = services.getMeta().getAssetType("StoryStatus");
        Query query = new Query(projects);

        // setup attributes that are used in query
        IAttributeDefinition nameOfIterationAtr = projects.getAttributeDefinition("Name");
        IAttributeDefinition identifier = projects.getAttributeDefinition("ID");

        // add attributes to query
        query.getSelection().add(nameOfIterationAtr);
        query.getSelection().add(identifier);

        // get result from query
        QueryResult queryResult = services.retrieve(query);

        System.out.println("statuses--");

        for (Asset iteration : queryResult.getAssets()){
            String name = parseString(iteration.getAttribute(nameOfIterationAtr).toString());
            System.out.println("here:" + iteration.getAttribute(identifier).toString());
            int indentifierOfResult = parseInt(iteration.getAttribute(identifier).toString());

            switch (name){
                case "Accepted":
                    returnValue.put(indentifierOfResult, Story.Status.ACCEPTED);
                    break;
                case "Future":
                    returnValue.put(indentifierOfResult, Story.Status.DEFINED);
                    break;
                case "Done":
                    returnValue.put(indentifierOfResult, Story.Status.DONE);
                    break;
                case "In Progress":
                    returnValue.put(indentifierOfResult, Story.Status.IN_PROGRESS);
                    break;
            }
        }

        return returnValue;
    }

    /***
     * imports all the task statuses available and match them to the native Story enum
     * @return
     * @throws ConnectionException
     * @throws APIException
     * @throws OidException
     */
    private HashMap<Integer, Task.Status> importTaskStatuses() throws ConnectionException, APIException, OidException {
        // create returnValue
        HashMap<Integer, Task.Status> returnValue = new HashMap<>();

        // setup Query
        IAssetType projects = services.getMeta().getAssetType("TaskStatus");
        Query query = new Query(projects);

        // setup attributes that are used in query
        IAttributeDefinition nameOfIterationAtr = projects.getAttributeDefinition("Name");
        IAttributeDefinition identifier = projects.getAttributeDefinition("ID");

        // add attributes to query
        query.getSelection().add(nameOfIterationAtr);
        query.getSelection().add(identifier);

        // get result from query
        QueryResult queryResult = services.retrieve(query);

        System.out.println("statuses--");

        for (Asset iteration : queryResult.getAssets()){
            String name = parseString(iteration.getAttribute(nameOfIterationAtr).toString());
            System.out.println("here:" + iteration.getAttribute(identifier).toString());
            int indentifierOfResult = parseInt(iteration.getAttribute(identifier).toString());

            switch (name){
                case "":
                    returnValue.put(indentifierOfResult, Task.Status.DEFINED);
                    break;
                case "Done":
                    returnValue.put(indentifierOfResult, Task.Status.DONE);
                    break;
                case "In Progress":
                    returnValue.put(indentifierOfResult, Task.Status.IN_PROGRESS);
                    break;
            }
        }

        return returnValue;
    }

    /***
     * parses the date format given by versionOne
     * @param rawDate
     * @return
     */
    private LocalDate parseDate(String rawDate){
        LocalDate returnValue = null;

        String parsedString = rawDate.substring(7, rawDate.length()-1);
        parsedString = parsedString.substring(0, 10) + parsedString.substring(parsedString.length()-5, parsedString.length());

        returnValue = LocalDate.parse(parsedString, DateTimeFormatter.ofPattern("E MMM dd yyyy"));

        return returnValue;
    }

    /***
     * parses the given string and returns it value
     * @param input
     * @return
     */
    private String parseString(String input){
        return input.substring(7, input.length()-1);
    }

    /***
     * parses the given string and returns it value in a Integer
     * @param input
     * @return
     */
    private int parseInt(String input){
        return Integer.valueOf(parseString(input).split(":")[1]);
    }
}
