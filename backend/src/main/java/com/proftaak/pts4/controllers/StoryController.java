package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.*;
import com.proftaak.pts4.rest.*;
import com.proftaak.pts4.rest.annotations.*;
import com.proftaak.pts4.rest.response.JSONResponse;
import com.proftaak.pts4.rest.response.ResponseFactory;

import java.util.Collection;

/**
 * @author Michon
 */
@Controller
public class StoryController {
    /**
     * Determine the role(s) the logged in user has within the story, if any
     */
    @PreRequest
    public static void determineScopeRoles(RequestData requestData) throws Exception {
        Story story = EbeanEx.find(Story.class, requestData.getParameter("id"));
        StoryController.determineScopeRoles(requestData, story);

        if (requestData.getPayload() != null && requestData.getPayload().containsKey("project")) {
            Project project = EbeanEx.find(Project.class, requestData.getPayload().get("project"));
            ProjectController.determineScopeRoles(requestData, project);
        }
    }

    /**
     * Determine the role(s) the logged in user has within the story, if any
     */
    public static void determineScopeRoles(RequestData requestData, Story story) throws Exception {
        if (story != null) {
            ProjectController.determineScopeRoles(requestData, story.getProject());
        }
    }

    /**
     * GET /story/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET_ONE)
    public static Story getOneHandler(RequestData requestData) throws Exception {
        Story story = EbeanEx.find(Story.class, requestData.getParameter("id"));
        if (story == null) {
            throw HTTPException.ERROR_NOT_FOUND;
        }
        return story;
    }

    /**
     * GET /story
     */
    @RequireAuth
    @Route(method = HTTPMethod.GET)
    public static JSONResponse<Collection<Story>> getAllHandler(RequestData requestData) throws Exception {
        return ResponseFactory.queryToList(requestData, Story.class, Story.queryForUser(requestData.getUser()));
    }

    /**
     * POST /story
     */
    @Field(name = "project", required = true, description = "The id of the project that the new story belongs to", type = Project.class)
    @Field(name = "iteration", description = "The id of the iteration that the new story belongs to", type = Iteration.class)
    @Field(name = "type", description = "The type of the new story", type = Story.Type.class)
    @Field(name = "name", required = true, description = "The name of the new story")
    @Field(name = "description", description = "The description of the new story")
    @Field(name = "status", description = "The status of the new story", type = Story.Status.class)
    @Field(name = "points", description = "The story points of the new story", type = Integer.class)
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.POST)
    public static Story postHandler(RequestData requestData) throws Exception {
        // Determine the story status
        Story.Status status = Story.Status.valueOf(requestData.getPayload().getOrDefault("status", Story.Status.DEFINED.toString()).toString());
        if (status == Story.Status.ACCEPTED) {
            requestData.requireScopeRole(ScopeRole.PRODUCT_OWNER);
        }

        // Get the project
        Project project = EbeanEx.require(EbeanEx.find(Project.class, requestData.getPayload().get("project")));

        // Create the new user story
        Story story = new Story(
            project,
            EbeanEx.find(Iteration.class, requestData.getPayload().get("iteration")),
            Story.Type.valueOf(requestData.getPayload().getOrDefault("type", Story.Type.USER_STORY.toString()).toString()),
            requestData.getPayload().getString("name"),
            requestData.getPayload().getString("description"),
            status,
            0, // Priority
            requestData.getPayload().getInt("points", 0)
        );

        // Check Kanban rules
        KanbanRules kanbanRules = project.getTeam().getKanbanRules();
        if (kanbanRules != null) {
            kanbanRules.enforceRoomFor(story);
        }

        // Save the new story.
        Ebean.save(story);

        // Return the created user story
        return story;
    }

    /**
     * PUT /story/1
     */
    @Field(name = "iteration", description = "The id of the new iteration that the story belongs to", type = Iteration.class)
    @Field(name = "name", description = "The new name of the story")
    @Field(name = "description", description = "The new description of the story")
    @Field(name = "status", description = "The new status of the story", type = Story.Status.class)
    @Field(name = "points", description = "The new story points of the story", type = Integer.class)
    @Field(name = "priority", description = "The new priority of the story", type = Integer.class)
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.PUT)
    public static Story putHandler(RequestData requestData) throws Exception {
        // Get the user story
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getParameter("id")));

        // Change the story
        Payload payload = requestData.getPayload();
        if (payload.containsKey("iteration")) {
            story.setIteration(EbeanEx.find(Iteration.class, requestData.getPayload().get("iteration")));
        }
        if (payload.containsKey("name")) {
            story.setName(payload.getString("name"));
        }
        if (payload.containsKey("description")) {
            story.setDescription(payload.getString("description"));
        }
        if (payload.containsKey("status")) {
            Story.Status status = Story.Status.valueOf(payload.getOrDefault("status", Story.Status.DEFINED.toString()).toString());
            if (story.getStatus() != Story.Status.ACCEPTED && status == Story.Status.ACCEPTED) {
                requestData.requireScopeRole(ScopeRole.PRODUCT_OWNER);
            }
            story.setStatus(status);
        }
        if (payload.containsKey("points")) {
            story.setPoints(payload.getInt("points"));
        }
        if (payload.containsKey("priority")) {
            story.setPriority(payload.getInt("priority"));
        }

        // Check Kanban rules
        KanbanRules kanbanRules = story.getProject().getTeam().getKanbanRules();
        if (kanbanRules != null) {
            kanbanRules.enforceRoomFor(story);
        }

        // Save the changes
        Ebean.save(story);

        // Return the changed user story
        return story;
    }

    /**
     * DELETE /story/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.DELETE)
    public static void deleteHandler(RequestData requestData) throws Exception {
        // Get the user story
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getParameter("id")));

        // Delete the user story
        Ebean.delete(story);
    }

    /**
     * GET /story/1/task
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/story/{id}/task")
    public static JSONResponse<Collection<Task>> getTask(RequestData requestData) throws Exception {
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getParameter("id")));
        Query<Task> query = EbeanEx.queryBelongingTo(Task.class, Story.class, story);
        return ResponseFactory.queryToList(requestData, Task.class, query);
    }

    /**
     * GET /story/1/test
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/story/{id}/test")
    public static JSONResponse<Collection<Test>> getTest(RequestData requestData) throws Exception {
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getParameter("id")));
        Query<Test> query = EbeanEx.queryBelongingTo(Test.class, Story.class, story);
        return ResponseFactory.queryToList(requestData, Test.class, query);
    }
}
