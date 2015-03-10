package com.proftaak.pts4.controllers;

import com.proftaak.pts4.core.annotations.RequireAuth;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.database.SprintStatus;
import com.proftaak.pts4.database.tables.Story;
import org.restlet.data.Status;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michon
 */
public class StoryController extends BaseController {
    protected static Story getStory(String id) throws HTTPException, FileNotFoundException, SQLException {
        int storyId = Integer.parseInt(id);
        Story story = Story.getDao().queryForId(storyId);
        if (story == null) {
            throw new HTTPException("That story does not exist", Status.CLIENT_ERROR_NOT_FOUND);
        }
        return story;
    }

    /**
     * GET /story or /story/1
     */
    @Override
    @RequireAuth
    public Object getHandler(Map<String, Object> urlParams) throws Exception {
        if (urlParams.get("id") == null) {
            return Story.getDao().queryForAll();
        } else {
            return this.getStory(urlParams.get("id").toString());
        }
    }

    /**
     * POST /story
     */
    @Override
    @RequireAuth
    public Object postHandler(Map<String, Object> data, Map<String, Object> urlParams) throws Exception {
        // Create the new user story.
        Story story;
        try {
            story = new Story(
                    (String) data.get("name"),
                    (String) data.get("description"),
                    SprintStatus.valueOf(data.getOrDefault("status", SprintStatus.DEFINED.toString()).toString())
            );
            Story.getDao().create(story);
        } catch (Exception e) {
            e.printStackTrace();
            throw new HTTPException("Invalid request", Status.CLIENT_ERROR_BAD_REQUEST);
        }

        // Return the created user story.
        Map<String, Object> fakeParams = new HashMap<>();
        fakeParams.put("id", story.getId());
        return getHandler(fakeParams);
    }

    /**
     * PUT /story/1
     */
    @Override
    @RequireAuth
    public Object putHandler(Map<String, Object> data, Map<String, Object> urlParams) throws Exception {
        // Try to get the user story.
        Story story = this.getStory(urlParams.get("id").toString());

        // Change the story.
        if (data.containsKey("name")) {
            story.setName((String) data.get("name"));
        }
        if (data.containsKey("description")) {
            story.setDescription((String) data.get("description"));
        }
        if (data.containsKey("status")) {
            story.setStatus(SprintStatus.valueOf(data.getOrDefault("status", SprintStatus.DEFINED.toString()).toString()));
        }

        // Save the changes.
        Story.getDao().update(story);

        // Return the changed user story.
        Map<String, Object> fakeParams = new HashMap<>();
        fakeParams.put("id", story.getId());
        return getHandler(fakeParams);
    }

    /**
     * DELETE /story/1
     */
    @Override
    @RequireAuth
    public Object deleteHandler(Map<String, Object> urlParams) throws Exception {
        // Try to get the user story.
        Story story = this.getStory(urlParams.get("id").toString());

        // Delete the user story.
        Story.getDao().delete(story);

        // Return nothing.
        return null;
    }
}
