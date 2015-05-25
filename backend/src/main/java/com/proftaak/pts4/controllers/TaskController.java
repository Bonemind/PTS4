package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.*;
import com.proftaak.pts4.rest.*;
import com.proftaak.pts4.rest.annotations.*;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.Collection;
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
    @Route(method = HTTPMethod.GET_ONE)
    public static Task getOneHandler(RequestData requestData) throws Exception {
        Task task = EbeanEx.find(Task.class, requestData.getParameter("id"));
        if (task == null) {
            throw HTTPException.ERROR_NOT_FOUND;
        }
        return task;
    }

    /**
     * GET /task/
     */
    @RequireAuth
    @Route(method = HTTPMethod.GET)
    public static Collection<Task> getAllHandler(RequestData requestData) throws Exception {
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
    @Field(name = "story", required = true, description = "The id of the story that the new task belongs to", type = Story.class)
    @Field(name = "owner", description = "The id of the user that is the owner of the new task", type = User.class)
    @Field(name = "name", required = true, description = "The name of the new task")
    @Field(name = "description", description = "The description of the new task")
    @Field(name = "estimate", description = "The time estimate (in hours) of the new task", type = Double.class)
    @Field(name = "status", description = "The status of the new task", type = Task.Status.class)
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.POST)
    public static Task postHandler(RequestData requestData) throws Exception {
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
    @Field(name = "owner", description = "The id of the user that is the new owner of the task", type = User.class)
    @Field(name = "name", description = "The new name of the task")
    @Field(name = "description", description = "The new description of the task")
    @Field(name = "estimate", description = "The new time estimate (in hours) of the task", type = Double.class)
    @Field(name = "todo", description = "The new time todo (in hours) of the task", type = Double.class)
    @Field(name = "status", description = "The new status of the task", type = Task.Status.class)
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.PUT)
    public static Task putHandler(RequestData requestData) throws Exception {
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
            User assignedOwner = EbeanEx.find(User.class, requestData.getPayload().get("owner"));

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
    @Route(method = HTTPMethod.DELETE)
    public static void deleteHandler(RequestData requestData) throws Exception {
        // Get the task
        Task task = EbeanEx.require(EbeanEx.find(Task.class, requestData.getParameter("id")));

        // Delete the task
        Ebean.delete(task);
    }

    /**
     * POST /task/1/effort
     */
    @Field(name = "effort", required = true, description = "The time spent working on this task", type = Double.class)
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.POST, path = "/task/{id}/effort")
    public static Task postEffortHandler(RequestData requestData) throws Exception {
        // Get the task
        Task task = EbeanEx.require(EbeanEx.find(Task.class, requestData.getParameter("id")));

        // Track the effort
        task.setTimeSpent(task.getTimeSpent() + requestData.getPayload().getDouble("effort"));

        // Save the task
        Ebean.save(task);

        return task;
    }
}
