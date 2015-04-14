package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.rest.HTTPException;
import com.proftaak.pts4.rest.Payload;
import com.proftaak.pts4.rest.RequestData;
import com.proftaak.pts4.rest.ScopeRole;
import com.proftaak.pts4.rest.annotations.*;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.*;

import java.util.Collection;
import java.util.HashSet;

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
    @Route(method = Route.Method.GET_ONE)
    public static Object getOneHandler(RequestData requestData) throws Exception {
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
    @Route(method = Route.Method.GET)
    public static Object getAllHandler(RequestData requestData) throws Exception {
        Collection<Story> stories = new HashSet<>();
        User user = requestData.getUser();
        for (Team team : user.getTeams()) {
            for (Project project : team.getProjects()) {
                stories.addAll(project.getStories());
            }
        }
        for (Project project : user.getOwnedProjects()) {
            stories.addAll(project.getStories());
        }
        return stories;
    }

    /**
     * POST /story
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @RequireFields(fields = {"project", "name"})
    @Route(method = Route.Method.POST)
    public static Object postHandler(RequestData requestData) throws Exception {
        // Determine the story status
        Story.Status status = Story.Status.valueOf(requestData.getPayload().getOrDefault("status", Story.Status.DEFINED.toString()).toString());
        if (status == Story.Status.ACCEPTED) {
            requestData.requireScopeRole(ScopeRole.PRODUCT_OWNER);
        }

        // Create the new user story
        Story story = new Story(
            EbeanEx.require(EbeanEx.find(Project.class, requestData.getPayload().get("project"))),
            EbeanEx.find(Iteration.class, requestData.getPayload().get("iteration")),
            Story.Type.valueOf(requestData.getPayload().getOrDefault("type", Story.Type.USER_STORY.toString()).toString()),
            requestData.getPayload().getString("name"),
            requestData.getPayload().getString("description"),
            status,
            0, // Priority
            requestData.getPayload().getInt("points", 0)
        );
        Ebean.save(story);

        // Return the created user story
        return story;
    }

    /**
     * PUT /story/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.PUT)
    public static Object putHandler(RequestData requestData) throws Exception {
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
            int priority = payload.getInt("priority");
            if (story.getPriority() != priority) {
                requestData.requireScopeRole(ScopeRole.PRODUCT_OWNER);
            }
            story.setPriority(priority);
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
    @Route(method = Route.Method.DELETE)
    public static Object deleteHandler(RequestData requestData) throws Exception {
        // Get the user story
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getParameter("id")));

        // Delete the user story
        Ebean.delete(story);

        // Return nothing
        return null;
    }

    /**
     * GET /story/1/task
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET, route = "/story/{id}/task")
    public static Object getTask(RequestData requestData) throws Exception {
        // Get the story
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getParameter("id")));

        // Return the tasks
        return story.getTasks();
    }

    /**
     * GET /story/1/test
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET, route = "/story/{id}/test")
    public static Object getTest(RequestData requestData) throws Exception {
        // Get the story
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getParameter("id")));

        // Return the tests
        return story.getTests();
    }
}
