package com.proftaak.pts4.controllers;

import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.core.restlet.annotations.ValidateScopeObject;
import com.proftaak.pts4.database.SprintStatus;
import com.proftaak.pts4.database.tables.Story;
import com.proftaak.pts4.database.tables.Task;
import com.proftaak.pts4.database.tables.User;
import org.restlet.data.Status;

import java.util.Map;

/**
 * @author Michon
 */
@CRUDController(table = Task.class, parent = StoryController.class)
public class TaskController extends BaseController {
    /**
     * Validate a scope object.
     */
    @ValidateScopeObject(Task.class)
    public static boolean processScopeObject(RequestData requestData, Task task) throws Exception {
        Story story = requestData.getScopeObject(Story.class);
        return story.getId() == task.getStory().getId();
    }

    /**
     * GET /story/1/task or /story/1/task/1
     */
    @RequireAuth
    public Object getHandler(RequestData requestData) throws Exception {
        // Get the story.
        Story story = requestData.getScopeObject(Story.class);

        if (requestData.getUrlParams().get("taskId") == null) {
            return Task.getDao().queryBuilder().where().eq(Task.FIELD_STORY, story).query();
        } else {
            return requestData.getScopeObject(Task.class);
        }
    }

    /**
     * POST /story/1/task
     */
    @RequireAuth
    public Object postHandler(RequestData requestData) throws Exception {
        // Get the user story.
        Story story = requestData.getScopeObject(Story.class);

        // Create the new task.
        Task task;
        try {
            SprintStatus status = SprintStatus.valueOf(requestData.getPayload().getOrDefault("status", SprintStatus.DEFINED.toString()).toString());
            if (status == SprintStatus.ACCEPTED) {
                requestData.getUser().getRole().require(User.UserRole.PRODUCT_OWNER);
            }
            task = new Task(
                story,
                (String) requestData.getPayload().get("name"),
                (String) requestData.getPayload().get("description"),
                status
            );
            Task.getDao().create(task);
        } catch (Exception e) {
            e.printStackTrace();
            throw new HTTPException("Invalid request", Status.CLIENT_ERROR_BAD_REQUEST);
        }

        // Return the created task.
        return task;
    }

    /**
     * PUT /story/1/task/1
     */
    @RequireAuth
    public Object putHandler(RequestData requestData) throws Exception {
        // Get the user task.
        Task task = requestData.getScopeObject(Task.class);
        Map<String, Object> payload = requestData.getPayload();

        // Change the task.
        if (payload.containsKey("name")) {
            task.setName((String) payload.get("name"));
        }
        if (payload.containsKey("description")) {
            task.setDescription((String) payload.get("description"));
        }
        if (payload.containsKey("status")) {
            SprintStatus status = SprintStatus.valueOf(payload.getOrDefault("status", SprintStatus.DEFINED.toString()).toString());
            if (task.getStatus() != SprintStatus.ACCEPTED && status == SprintStatus.ACCEPTED) {
                requestData.getUser().getRole().require(User.UserRole.PRODUCT_OWNER);
            }
            task.setStatus(status);
        }

        // Save the changes.
        Task.getDao().update(task);

        // Return the changed task.
        return task;
    }

    /**
     * DELETE /story/1/task/1
     */
    @RequireAuth
    public Object deleteHandler(RequestData requestData) throws Exception {
        // Try to get the task.
        Task task = requestData.getScopeObject(Task.class);

        // Delete the task.
        Task.getDao().delete(task);

        // Return nothing.
        return null;
    }
}
