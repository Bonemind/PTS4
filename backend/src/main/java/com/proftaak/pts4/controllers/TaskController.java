package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.server.lib.util.Str;
import com.proftaak.pts4.core.rest.HTTPException;
import com.proftaak.pts4.core.rest.Payload;
import com.proftaak.pts4.core.rest.RequestData;
import com.proftaak.pts4.core.rest.ScopeRole;
import com.proftaak.pts4.core.rest.annotations.*;
import com.proftaak.pts4.database.EbeanEx;
import org.glassfish.grizzly.http.util.HttpStatus;
import com.proftaak.pts4.database.tables.*;

import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashSet;

/**
 * @author Michon
 */
@Controller
public class TaskController {
    /**
     * Determine the role(s) the logged in user has within the task, if any
     */
    @PreRequest
    public static void determineScopeRoles(RequestData requestData) throws Exception {
        Task task = EbeanEx.find(Task.class, requestData.getParameter("id"));
        if (task != null) {
            StoryController.determineScopeRoles(requestData, task.getStory());
        }

        if (requestData.getPayload() != null && requestData.getPayload().containsKey("story")) {
            Story story = EbeanEx.find(Story.class, requestData.getPayload().get("story"));
            StoryController.determineScopeRoles(requestData, story);
        }
    }

    /**
     * GET /task/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET_ONE)
    public static Object getOneHandler(RequestData requestData) throws Exception {
        Task task = EbeanEx.find(Task.class, requestData.getParameter("id"));
        if (task == null) {
            throw HTTPException.ERROR_NOT_FOUND;
        }
        return task;
    }

    /**
     * GET /task
     */
    @RequireAuth
    @Route(method = Route.Method.GET)
    public static Object getAllHandler(RequestData requestData) throws Exception {
        Collection<Task> tasks = new HashSet<>();
        User user = requestData.getUser();
        for (Team team : user.getTeams()) {
            for (Project project : team.getProjects()) {
                for (Story story : project.getStories()) {
                    tasks.addAll(story.getTasks());
                }
            }
        }
        for (Project project : user.getOwnedProjects()) {
            for (Story story : project.getStories()) {
                tasks.addAll(story.getTasks());
            }
        }
        return tasks;
    }

    /**
     * POST /task
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @RequireFields(fields = {"story", "name"})
    @Route(method = Route.Method.POST)
    public static Object postHandler(RequestData requestData) throws Exception {
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getPayload().get("story")));

        // Get the new owner
        User assignedOwner = EbeanEx.find(User.class, requestData.getPayload().get("owner"));
        if (assignedOwner != null && !story.getProject().getTeam().getUsers().contains(assignedOwner)) {
            throw new HTTPException("User not part of team", HttpStatus.BAD_REQUEST_400);
        }

        // Create the new task
        Task task = new Task(
            story,
            assignedOwner,
            requestData.getPayload().getString("name"),
            requestData.getPayload().getString("description"),
            requestData.getPayload().getDouble("estimate", 0.0),
            Task.Status.valueOf(requestData.getPayload().getOrDefault("status", Task.Status.DEFINED.toString()).toString())
        );
        Ebean.save(task);

        // Return the created task
        return task;
    }

    /**
     * PUT /task/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.PUT)
    public static Object putHandler(RequestData requestData) throws Exception {
        // Get the user task
        Task task = EbeanEx.require(EbeanEx.find(Task.class, requestData.getParameter("id")));

        // Change the task
        Payload payload = requestData.getPayload();
        if (payload.containsKey("name")) {
            task.setName(payload.getString("name"));
        }
        if (payload.containsKey("description")) {
            task.setDescription(payload.getString("description"));
        }
        if (payload.containsKey("estimate")) {
            task.setEstimate(payload.getDouble("estimate"));
        }
        if (payload.containsKey("todo")) {
            task.setTodo(payload.getDouble("todo"));
        }
        if (payload.containsKey("status")) {
            task.setStatus(Task.Status.valueOf(payload.getOrDefault("status", Task.Status.DEFINED.toString()).toString()));
        }
        if (payload.containsKey("owner")) {
            User assignedOwner = EbeanEx.require(EbeanEx.find(User.class, requestData.getPayload().get("owner")));

            if (assignedOwner != null && !task.getStory().getProject().getTeam().getUsers().contains(assignedOwner)) {
                throw new HTTPException("User not part of team", HttpStatus.BAD_REQUEST_400);
            }

            task.setOwner(assignedOwner);
        }

        // Save the changes
        Ebean.save(task);

        // Return the changed task
        return task;
    }

    /**
     * DELETE /task/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.DELETE)
    public static Object deleteHandler(RequestData requestData) throws Exception {
        // Get the task
        Task task = EbeanEx.require(EbeanEx.find(Task.class, requestData.getParameter("id")));

        // Delete the task
        Ebean.delete(task);

        // Return nothing
        return null;
    }
}
