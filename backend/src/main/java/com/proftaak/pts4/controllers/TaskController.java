package com.proftaak.pts4.controllers;

import com.proftaak.pts4.core.annotations.RequireAuth;
import com.proftaak.pts4.core.annotations.Route;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.database.SprintStatus;
import com.proftaak.pts4.database.tables.Story;
import com.proftaak.pts4.database.tables.Task;
import org.restlet.data.Status;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michon
 */
@Route("/story/{storyId}/task")
public class TaskController extends BaseController {
    protected static Task getTask(String storyId, String id) throws HTTPException, FileNotFoundException, SQLException {
        int taskId = Integer.parseInt(id);
        Task task = Task.getDao().queryForId(taskId);
        if (task == null || !String.valueOf(task.getStory().getId()).equals(storyId)) {
            throw new HTTPException("That task does not exist", Status.CLIENT_ERROR_NOT_FOUND);
        }
        return task;
    }

    /**
     * GET /story/1/task or /story/1/task/1
     */
    @Override
    @RequireAuth
    public Object getHandler(Map<String, Object> urlParams) throws Exception {
        Story story = StoryController.getStory(urlParams.get("storyId").toString());
        if (urlParams.get("id") == null) {
            return Task.getDao().queryBuilder().where().eq(Task.FIELD_STORY, story).query();
        } else {
            return this.getTask(urlParams.get("storyId").toString(), urlParams.get("id").toString());
        }
    }

    /**
     * POST /story/1/task
     */
    @Override
    @RequireAuth
    public Object postHandler(Map<String, Object> data, Map<String, Object> urlParams) throws Exception {
        // Get the user story.
        Story story = StoryController.getStory(urlParams.get("storyId").toString());

        // Create the new task.
        Task task;
        try {
            task = new Task(
                    story,
                    (String) data.get("name"),
                    (String) data.get("description"),
                    SprintStatus.valueOf(data.getOrDefault("status", SprintStatus.DEFINED.toString()).toString())
            );
            Task.getDao().create(task);
        } catch (Exception e) {
            e.printStackTrace();
            throw new HTTPException("Invalid request", Status.CLIENT_ERROR_BAD_REQUEST);
        }

        // Return the created user story.
        Map<String, Object> fakeParams = new HashMap<>();
        fakeParams.put("storyId", story.getId());
        fakeParams.put("id", task.getId());
        return getHandler(fakeParams);
    }

    /**
     * PUT /story/1/task/1
     */
    @Override
    @RequireAuth
    public Object putHandler(Map<String, Object> data, Map<String, Object> urlParams) throws Exception {
        // Get the user story/task.
        Task task = this.getTask(urlParams.get("storyId").toString(), urlParams.get("id").toString());

        // Change the task.
        if (data.containsKey("name")) {
            task.setName((String) data.get("name"));
        }
        if (data.containsKey("description")) {
            task.setDescription((String) data.get("description"));
        }
        if (data.containsKey("status")) {
            task.setStatus(SprintStatus.valueOf(data.getOrDefault("status", SprintStatus.DEFINED.toString()).toString()));
        }

        // Save the changes.
        Task.getDao().update(task);

        // Return the changed user story.
        Map<String, Object> fakeParams = new HashMap<>();
        fakeParams.put("storyId", task.getStory().getId());
        fakeParams.put("id", task.getId());
        return getHandler(fakeParams);
    }

    /**
     * DELETE /story/1/task/1
     */
    @Override
    @RequireAuth
    public Object deleteHandler(Map<String, Object> urlParams) throws Exception {
        // Try to get the user story.
        Task task = this.getTask(urlParams.get("storyId").toString(), urlParams.get("id").toString());

        // Delete the user story.
        Task.getDao().delete(task);

        // Return nothing.
        return null;
    }
}
