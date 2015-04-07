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
import com.proftaak.pts4.database.tables.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
    }

    /**
     * GET /task
     */
    @RequireAuth
    @Route(method = Route.Method.GET)
    public static Object getAllHandler(RequestData requestData) throws Exception {
        Collection<Task> tasks = new ArrayList<>();
        User user = requestData.getUser();
        for (Team team : user.getTeams()) {
            for (Project project : team.getProjects()) {
                for (Story story : project.getStories()) {
                    tasks.addAll(story.getTasks());
                }
            }
        }
        return tasks;
    }

    /**
     * GET /task/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET_ONE)
    public static Object getOneHandler(RequestData requestData) throws Exception {
        return EbeanEx.require(EbeanEx.find(Task.class, requestData.getParameter("id")));
    }

    /**
     * POST /task
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.POST)
    public static Object postHandler(RequestData requestData) throws Exception {
        // Create the new task
        Task task = new Task(
            EbeanEx.require(EbeanEx.find(Story.class, requestData.getPayload().get("story"))),
            (String) requestData.getPayload().get("name"),
            (String) requestData.getPayload().get("description"),
            Task.Status.valueOf(requestData.getPayload().getOrDefault("status", Task.Status.DEFINED.toString()).toString())
        );
        Ebean.save(task);

        // Return the created task
        return task;
    }

    /**
     * PUT /story/1/task/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.PUT)
    public static Object putHandler(RequestData requestData) throws Exception {
        // Get the user task
        Task task = EbeanEx.require(EbeanEx.find(Task.class, requestData.getParameter("id")));

        // Change the task
        Map<String, Object> payload = requestData.getPayload();
        if (payload.containsKey("name")) {
            task.setName((String) payload.get("name"));
        }
        if (payload.containsKey("description")) {
            task.setDescription((String) payload.get("description"));
        }
        if (payload.containsKey("status")) {
            task.setStatus(Task.Status.valueOf(payload.getOrDefault("status", Task.Status.DEFINED.toString()).toString()));
        }

        // Save the changes
        Ebean.save(task);

        // Return the changed task
        return task;
    }

    /**
     * DELETE /story/1/task/1
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
