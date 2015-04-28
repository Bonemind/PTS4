package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.*;
import com.proftaak.pts4.rest.*;
import com.proftaak.pts4.rest.annotations.*;

import java.util.Collection;
import java.util.HashSet;

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
    @Route(method = HTTPMethod.GET_ONE)
    public static Test getOneHandler(RequestData requestData) throws Exception {
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
    @Route(method = HTTPMethod.GET)
    public static Test[] getAllHandler(RequestData requestData) throws Exception {
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
    @Field(name = "name", required = true, description = "The name of the new test")
    @Field(name = "description", description = "The description of the new test")
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    //@RequireFields(fields = {"story", "name"})
    @Route(method = HTTPMethod.POST)
    public static Test postHandler(RequestData requestData) throws Exception {
        Story story = EbeanEx.require(EbeanEx.find(Story.class, requestData.getPayload().get("story")));

        // Create the new test
        Test test = new Test(
            story,
            requestData.getPayload().getString("name"),
            requestData.getPayload().getString("description")
        );
        Ebean.save(test);

        // Return the created test
        return test;
    }

    /**
     * PUT /test/1
     */
    @Field(name = "name", required = true, description = "The new name of the test")
    @Field(name = "description", description = "The new description of the test")
    @Field(name = "accepted", description = "The new accepted state of the test")
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.PUT)
    public static Test putHandler(RequestData requestData) throws Exception {
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
        if (payload.containsKey("accepted")) {
            test.setAccepted(payload.getBoolean("accepted"));
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
    @Route(method = HTTPMethod.DELETE)
    public static void deleteHandler(RequestData requestData) throws Exception {
        // Get the test
        Test test = EbeanEx.require(EbeanEx.find(Test.class, requestData.getParameter("id")));

        // Delete the test
        Ebean.delete(test);
    }
}
