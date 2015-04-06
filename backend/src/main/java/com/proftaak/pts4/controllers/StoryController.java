package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.rest.HTTPException;
import com.proftaak.pts4.core.rest.RequestData;
import com.proftaak.pts4.core.rest.ScopeRole;
import com.proftaak.pts4.core.rest.annotations.Controller;
import com.proftaak.pts4.core.rest.annotations.PreRequest;
import com.proftaak.pts4.core.rest.annotations.RequireAuth;
import com.proftaak.pts4.core.rest.annotations.Route;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Iteration;
import com.proftaak.pts4.database.tables.Project;
import com.proftaak.pts4.database.tables.Story;

import java.util.Map;

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
     * For this controller we will want to include the list of tasks in responses
     */
    @PreRequest
    public static void setupSerializer(RequestData requestData) {
        requestData.include("tasks");
    }

    /**
     * GET /story
     */
    @RequireAuth
    @Route(method = Route.Method.GET)
    public static Object getAllHandler(RequestData requestData) throws Exception {
        return Ebean.find(Story.class).findList();
    }

    /**
     * GET /story/1
     */
    @RequireAuth
    @Route(method = Route.Method.GET_ONE)
    public static Object getOneHandler(RequestData requestData) throws Exception {
        return EbeanEx.require(EbeanEx.find(Story.class, requestData.getParameter("id")));
    }

    /**
     * POST /story
     */
    @RequireAuth
    @Route(method = Route.Method.POST)
    public static Object postHandler(RequestData requestData) throws Exception {
        // Determine the story status.
        Story.Status status = Story.Status.valueOf(requestData.getPayload().getOrDefault("status", Story.Status.DEFINED.toString()).toString());
        if (status == Story.Status.ACCEPTED) {
            requestData.requireScopeRole(ScopeRole.PRODUCT_OWNER);
        }

        // Determine the story iteration.
        Iteration iteration = null;
        if (requestData.getPayload().containsKey("iteration")) {
            iteration = EbeanEx.require(EbeanEx.find(Iteration.class, requestData.getPayload().get("iteration")));
        }

        // Create the new user story
        Story story = new Story(
            EbeanEx.require(EbeanEx.find(Project.class, requestData.getPayload().get("project"))),
            iteration,
            (String) requestData.getPayload().get("name"),
            (String) requestData.getPayload().get("description"),
            status
        );
        Ebean.save(story);

        // Return the created user story
        return story;
    }

    /**
     * PUT /story/1
     */
    @RequireAuth
    @Route(method = Route.Method.PUT)
    public static Object putHandler(RequestData requestData) throws Exception {
        // Get the user story
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getPayload().get("id")));

        // Change the story
        Map<String, Object> payload = requestData.getPayload();
        if (payload.containsKey("iteration")) {
            story.setIteration(EbeanEx.require(EbeanEx.find(Iteration.class, requestData.getPayload().get("iteration"))));
        }
        if (payload.containsKey("name")) {
            story.setName((String) payload.get("name"));
        }
        if (payload.containsKey("description")) {
            story.setDescription((String) payload.get("description"));
        }
        if (payload.containsKey("status")) {
            Story.Status status = Story.Status.valueOf(payload.getOrDefault("status", Story.Status.DEFINED.toString()).toString());
            if (story.getStatus() != Story.Status.ACCEPTED && status == Story.Status.ACCEPTED) {
                requestData.requireScopeRole(ScopeRole.PRODUCT_OWNER);
            }
            story.setStatus(status);
        }

        // Save the changes
        Ebean.save(story);

        // Return the changed user story
        return story;
    }

    /**
     * DELETE /story/1
     */
    @RequireAuth
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

        // Return the stories
        return story.getTasks();
    }
}
