package com.proftaak.pts4.controllers;

import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.database.tables.Story;
import com.proftaak.pts4.database.tables.User;
import org.restlet.data.Status;

import java.util.Map;

/**
 * @author Michon
 */
@CRUDController(table = Story.class)
public class StoryController extends BaseController {
    /**
     * GET /story or /story/1
     */
    @RequireAuth
    public Object getHandler(RequestData requestData) throws Exception {
        if (requestData.getUrlParams().get("storyId") == null) {
            return Story.getDao().queryForAll();
        } else {
            return requestData.getScopeObject(Story.class);
        }
    }

    /**
     * POST /story
     */
    @RequireAuth
    public Object postHandler(RequestData requestData) throws Exception {
        // Create the new user story.
        Story story;
        try {
            Story.Status status = Story.Status.valueOf(requestData.getPayload().getOrDefault("status", Story.Status.DEFINED.toString()).toString());
            if (status == Story.Status.ACCEPTED) {
                requestData.getUser().getRole().require(User.UserRole.PRODUCT_OWNER);
            }
            story = new Story(
                    (String) requestData.getPayload().get("name"),
                    (String) requestData.getPayload().get("description"),
                    status
            );
            Story.getDao().create(story);
        } catch (Exception e) {
            e.printStackTrace();
            throw new HTTPException("Invalid request", Status.CLIENT_ERROR_BAD_REQUEST);
        }

        // Return the created user story.
        return story;
    }

    /**
     * PUT /story/1
     */
    @RequireAuth
    public Object putHandler(RequestData requestData) throws Exception {
        // Try to get the user story.
        Story story = requestData.getScopeObject(Story.class);
        Map<String, Object> payload = requestData.getPayload();

        // Change the story.
        if (payload.containsKey("name")) {
            story.setName((String) payload.get("name"));
        }
        if (payload.containsKey("description")) {
            story.setDescription((String) payload.get("description"));
        }
        if (payload.containsKey("status")) {
            Story.Status status = Story.Status.valueOf(payload.getOrDefault("status", Story.Status.DEFINED.toString()).toString());
            if (story.getStatus() != Story.Status.ACCEPTED && status == Story.Status.ACCEPTED) {
                requestData.getUser().getRole().require(User.UserRole.PRODUCT_OWNER);
            }
            story.setStatus(status);
        }

        // Save the changes.
        Story.getDao().update(story);

        // Return the changed user story.
        return story;
    }

    /**
     * DELETE /story/1
     */
    @RequireAuth
    public Object deleteHandler(RequestData requestData) throws Exception {
        // Try to get the user story.
        Story story = requestData.getScopeObject(Story.class);

        // Delete the user story.
        Story.getDao().delete(story);

        // Return nothing.
        return null;
    }
}
