package com.proftaak.pts4.controllers;

import com.proftaak.pts4.core.annotations.RequireAuth;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.database.SprintStatus;
import com.proftaak.pts4.database.tables.UserStory;
import org.restlet.data.Status;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michon
 */
public class UserStoryController extends BaseController {
    private UserStory getUserStory(String id) throws HTTPException, FileNotFoundException, SQLException {
        int storyId = Integer.parseInt(id);
        UserStory story = UserStory.getDao().queryForId(storyId);
        if (story == null) {
            throw new HTTPException("That story does not exist", Status.CLIENT_ERROR_NOT_FOUND);
        }
        return story;
    }

    /**
     * GET /userstory
     */
    @Override
    @RequireAuth
    public Map<String, Object> getHandler() throws Exception {
        // Get the list of user stories.
        Map<String, Object> output = new HashMap<>();
        List<UserStory> stories = UserStory.getDao().queryForAll();
        output.put("stories", stories);
        return output;
    }

    /**
     * GET /userstory/1
     */
    @Override
    @RequireAuth
    public Map<String, Object> getHandler(String urlParam) throws Exception {
        // Build the output.
        Map<String, Object> output = new HashMap<>();
        output.put("story", this.getUserStory(urlParam));
        return output;
    }

    /**
     * POST /userstory
     */
    @Override
    @RequireAuth
    public Map<String, Object> postHandler(Map<String, Object> data) throws Exception {
        // Create the new user story.
        UserStory story;
        try {
            story = new UserStory(
                    (String) data.get("name"),
                    (String) data.get("description"),
                    SprintStatus.valueOf(data.getOrDefault("status", SprintStatus.DEFINED.toString()).toString())
            );
            UserStory.getDao().create(story);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new HTTPException("Invalid request", Status.CLIENT_ERROR_BAD_REQUEST);
        }

        // Return the created user story.
        return getHandler(String.valueOf(story.getId()));
    }

    /**
     * PUT /userstory/1
     */
    @Override
    @RequireAuth
    public Map<String, Object> putHandler(Map<String, Object> data, String urlParam) throws Exception {
        // Try to get the user story.
        UserStory story = this.getUserStory(urlParam);

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
        UserStory.getDao().update(story);

        // Return the changed user story.
        return getHandler(String.valueOf(story.getId()));
    }

    /**
     * DELETE /userstory/1
     */
    @Override
    @RequireAuth
    public Map<String, Object> deleteHandler(String urlParam) throws Exception {
        // Try to get the user story.
        UserStory story = this.getUserStory(urlParam);

        // Delete the user story.
        UserStory.getDao().delete(story);

        // Return nothing.
        return null;
    }
}
