package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.core.restlet.RequestData;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.core.restlet.annotations.ProcessScopeObject;
import com.proftaak.pts4.database.tables.Story;
import com.proftaak.pts4.database.tables.Task;

import java.util.Map;

/**
 * @author Michon
 */
@CRUDController(table = Task.class, parent = StoryController.class)
public class TaskController extends BaseController {
    /**
     * Validate whether the task and story that are in scope belong together
     */
    @ProcessScopeObject(Task.class)
    public static void validateTaskInStory(RequestData requestData, Task task) throws Exception {
        Story story = requestData.getScopeObject(Story.class);
        if (!story.equals(task.getStory())) {
            throw HTTPException.ERROR_OBJECT_NOT_FOUND;
        }
    }

    /**
     * GET /story/1/task or /story/1/task/1
     */
    @RequireAuth
    public Object getHandler(RequestData requestData) throws Exception {
        if (requestData.getUrlParams().get("taskId") == null) {
            Story story = requestData.getScopeObject(Story.class);
            return Ebean.find(Task.class).where().eq(Task.FIELD_STORY, story.getId()).query().findList();
        } else {
            return requestData.getScopeObject(Task.class);
        }
    }

    /**
     * POST /story/1/task
     */
    @RequireAuth
    public Object postHandler(RequestData requestData) throws Exception {
        // Get the user story
        Story story = requestData.getScopeObject(Story.class);

        // Create the new task
        Task task;
        try {
            task = new Task(
                    story,
                    (String) requestData.getPayload().get("name"),
                    (String) requestData.getPayload().get("description"),
                    Task.Status.valueOf(requestData.getPayload().getOrDefault("status", Task.Status.DEFINED.toString()).toString())
            );
            Ebean.save(task);
        } catch (Exception e) {
            e.printStackTrace();
            throw HTTPException.ERROR_BAD_REQUEST;
        }

        // Return the created task
        return task;
    }

    /**
     * PUT /story/1/task/1
     */
    @RequireAuth
    public Object putHandler(RequestData requestData) throws Exception {
        // Get the user task
        Task task = requestData.getScopeObject(Task.class);
        Map<String, Object> payload = requestData.getPayload();

        // Change the task
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
    @RequireAuth
    public Object deleteHandler(RequestData requestData) throws Exception {
        // Try to get the task
        Task task = requestData.getScopeObject(Task.class);

        // Delete the task
        Ebean.delete(task);

        // Return nothing
        return null;
    }
}
