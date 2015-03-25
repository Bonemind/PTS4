package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.core.restlet.RequestData;
import com.proftaak.pts4.core.restlet.ScopeRole;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.PreRequest;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.database.tables.Story;

import java.util.Map;

/**
 * @author Michon
 */
@CRUDController(table = Story.class)
public class StoryController extends BaseController {
    /**
     * For this controller we will want to include the list of tasks in responses
     */
    @PreRequest
    public static void setupSerializer(RequestData requestData) {
        requestData.getSerializer().include("tasks");
    }

    /**
     * GET /story or /story/1
     */
    @RequireAuth
    public Object getHandler(RequestData requestData) throws Exception {
        if (requestData.getUrlParams().get("storyId") == null) {
            return Ebean.find(Story.class).findList();
        } else {
            return requestData.getScopeObject(Story.class);
        }
    }

    /**
     * POST /story
     */
    @RequireAuth
    public Object postHandler(RequestData requestData) throws Exception {
        // Create the new user story
        Story story;
        try {
            Story.Status status = Story.Status.valueOf(requestData.getPayload().getOrDefault("status", Story.Status.DEFINED.toString()).toString());
            if (status == Story.Status.ACCEPTED) {
                requestData.requireScopeRole(ScopeRole.PRODUCT_OWNER);
            }
            story = new Story(
                    (String) requestData.getPayload().get("name"),
                    (String) requestData.getPayload().get("description"),
                    status
            );
            Ebean.save(story);
        } catch (Exception e) {
            e.printStackTrace();
            throw HTTPException.ERROR_BAD_REQUEST;
        }

        // Return the created user story
        return story;
    }

    /**
     * PUT /story/1
     */
    @RequireAuth
    public Object putHandler(RequestData requestData) throws Exception {
        // Try to get the user story
        Story story = requestData.getScopeObject(Story.class);
        Map<String, Object> payload = requestData.getPayload();

        // Change the story
        if (payload.containsKey("name")) {
            story.setName((String) payload.get("name"));
        }
        if (payload.containsKey("description")) {
            story.setDescription((String) payload.get("description"));
        }
        if (payload.containsKey("status")) {
            Story.Status status = Story.Status.valueOf(payload.getOrDefault("status", Story.Status.DEFINED.toString()).toString());
            if (story.getStatus() != Story.Status.ACCEPTED && status == Story.Status.ACCEPTED) {
                requestData.requireScopeRole(ScopeRole.PRODUCT_OWNER);
            }
            story.setStatus(status);
        }

        // Save the changes
        Ebean.save(story);

        // Return the changed user story
        return story;
    }

    /**
     * DELETE /story/1
     */
    @RequireAuth
    public Object deleteHandler(RequestData requestData) throws Exception {
        // Try to get the user story
        Story story = requestData.getScopeObject(Story.class);

        // Delete the user story
        Ebean.delete(story);

        // Return nothing
        return null;
    }
}
