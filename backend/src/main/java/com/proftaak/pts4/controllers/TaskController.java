package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.rest.HTTPException;
import com.proftaak.pts4.core.rest.RequestData;
import com.proftaak.pts4.core.rest.annotations.Controller;
import com.proftaak.pts4.core.rest.annotations.PreRequest;
import com.proftaak.pts4.core.rest.annotations.RequireAuth;
import com.proftaak.pts4.core.rest.annotations.Route;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Story;
import com.proftaak.pts4.database.tables.Task;
import com.proftaak.pts4.database.tables.User;
import org.glassfish.grizzly.http.util.HttpStatus;

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
        return Ebean.find(Task.class).findList();
    }

    /**
     * GET /task/1
     */
    @RequireAuth
    @Route(method = Route.Method.GET_ONE)
    public static Object getOneHandler(RequestData requestData) throws Exception {
        return EbeanEx.require(EbeanEx.find(Task.class, requestData.getParameter("id")));
    }

    /**
     * POST /task
     */
    @RequireAuth
    @Route(method = Route.Method.POST)
    public static Object postHandler(RequestData requestData) throws Exception {
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getPayload().get("story")));
        User assignedOwner = EbeanEx.find(User.class, requestData.getPayload().get("owner"));

        if(!story.getProject().getTeam().getUsers().contains(assignedOwner)) {
            throw new HTTPException("User not part of team", HttpStatus.BAD_REQUEST_400);
        }

        // Create the new task
        Task task = new Task(
            story,
            (String) requestData.getPayload().get("name"),
            (String) requestData.getPayload().get("description"),
            Task.Status.valueOf(requestData.getPayload().getOrDefault("status", Task.Status.DEFINED.toString()).toString()),
            assignedOwner
        );
        Ebean.save(task);

        // Return the created task
        return task;
    }

    /**
     * PUT /story/1/task/1
     */
    @RequireAuth
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
        if(payload.containsKey("owner")) {
            User assignedOwner = EbeanEx.require(EbeanEx.find(User.class, requestData.getPayload().get("owner")));

            if(!task.getStory().getProject().getTeam().getUsers().contains(assignedOwner)) {
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
     * DELETE /story/1/task/1
     */
    @RequireAuth
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
