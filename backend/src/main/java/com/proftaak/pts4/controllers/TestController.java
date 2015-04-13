package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.rest.HTTPException;
import com.proftaak.pts4.core.rest.Payload;
import com.proftaak.pts4.core.rest.RequestData;
import com.proftaak.pts4.core.rest.ScopeRole;
import com.proftaak.pts4.core.rest.annotations.Controller;
import com.proftaak.pts4.core.rest.annotations.PreRequest;
import com.proftaak.pts4.core.rest.annotations.RequireAuth;
import com.proftaak.pts4.core.rest.annotations.Route;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.*;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Created by stijn on 7/4/2015.
 */
@Controller
public class TestController {
    /**
     * Determine the role(s) the logged in user has within the test, if any
     */
    @PreRequest
    public static void determineScopeRoles(RequestData requestData) throws Exception {
        Test test = EbeanEx.find(Test.class, requestData.getParameter("id"));
        if (test != null) {
            StoryController.determineScopeRoles(requestData, test.getStory());
        }

        if (requestData.getPayload() != null && requestData.getPayload().containsKey("story")) {
            Story story = EbeanEx.find(Story.class, requestData.getPayload().get("story"));
            StoryController.determineScopeRoles(requestData, story);
        }
    }

    /**
     * GET /test/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET_ONE)
    public static Object getOneHandler(RequestData requestData) throws Exception {
        Test test = EbeanEx.find(Test.class, requestData.getParameter("id"));
        if (test == null) {
            throw HTTPException.ERROR_NOT_FOUND;
        }
        return test;
    }

    /**
     * GET /test
     */
    @RequireAuth
    @Route(method = Route.Method.GET)
    public static Object getAllHandler(RequestData requestData) throws Exception {
        Collection<Test> tests = new HashSet<>();
        User user = requestData.getUser();
        for (Team team : user.getTeams()) {
            for (Project project : team.getProjects()) {
                for (Story story : project.getStories()) {
                    tests.addAll(story.getTests());
                }
            }
        }
        for (Project project : user.getOwnedProjects()) {
            for (Story story : project.getStories()) {
                tests.addAll(story.getTests());
            }
        }
        return tests;
    }

    /**
     * POST /test
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.POST)
    public static Object postHandler(RequestData requestData) throws Exception {
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getPayload().get("story")));

        // Create the new test
        Test test = new Test(
                story,
                requestData.getPayload().getString("name"),
                requestData.getPayload().getString("description"),
                Test.Status.valueOf(requestData.getPayload().getOrDefault("status", Test.Status.DEFINED.toString()).toString())
        );
        Ebean.save(test);

        // Return the created test
        return test;
    }

    /**
     * PUT /test/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.PUT)
    public static Object putHandler(RequestData requestData) throws Exception {
        // Get the test
        Test test = EbeanEx.require(EbeanEx.find(Test.class, requestData.getParameter("id")));

        // Change the test
        Payload payload = requestData.getPayload();
        if (payload.containsKey("name")) {
            test.setName(payload.getString("name"));
        }
        if (payload.containsKey("description")) {
            test.setDescription(payload.getString("description"));
        }
        if (payload.containsKey("status")) {
            test.setStatus(Test.Status.valueOf(payload.getOrDefault("status", Test.Status.DEFINED.toString()).toString()));
        }

        // Save the changes
        Ebean.save(test);

        // Return the changed test
        return test;
    }

    /**
     * DELETE /test/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.DELETE)
    public static Object deleteHandler(RequestData requestData) throws Exception {
        // Get the test
        Test test = EbeanEx.require(EbeanEx.find(Test.class, requestData.getParameter("id")));

        // Delete the test
        Ebean.delete(test);

        // Return nothing
        return null;
    }
}
