package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Iteration;
import com.proftaak.pts4.database.tables.Story;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.database.tables.User;
import com.proftaak.pts4.rest.*;
import com.proftaak.pts4.rest.annotations.Controller;
import com.proftaak.pts4.rest.annotations.PreRequest;
import com.proftaak.pts4.rest.annotations.RequireAuth;
import com.proftaak.pts4.rest.annotations.Route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Michon
 */
@Controller
public class IterationController {
    /**
     * Determine the role(s) the logged in user has within the iteration, if any
     */
    @PreRequest
    public static void determineScopeRoles(RequestData requestData) throws Exception {
        Iteration iteration = EbeanEx.find(Iteration.class, requestData.getParameter("id"));
        if (iteration != null) {
            TeamController.determineScopeRoles(requestData, iteration.getTeam());
            return;
        }

        if (requestData.getPayload() != null && requestData.getPayload().containsKey("team")) {
            Team team = EbeanEx.find(Team.class, requestData.getPayload().get("team"));
            if (team != null) {
                TeamController.determineScopeRoles(requestData, team);
            }
        }
    }

    /**
     * GET /iteration/1
     */
    @RequireAuth
    @Route(method = HTTPMethod.GET_ONE)
    public static Iteration getOneHandler(RequestData requestData) throws Exception {
        Iteration iteration = EbeanEx.find(Iteration.class, requestData.getParameter("id"));
        if (iteration == null) {
            throw HTTPException.ERROR_NOT_FOUND;
        }
        return iteration;
    }

    /**
     * GET /iteration
     */
    @RequireAuth
    @Route(method = HTTPMethod.GET)
    public static Collection<Iteration> getAllHandler(RequestData requestData) throws Exception {
        Collection<Iteration> iterations = new ArrayList<>();
        User user = requestData.getUser();
        for (Team team : user.getTeams()) {
            iterations.addAll(team.getIterations());
        }
        return iterations;
    }

    /**
     * POST /iteration
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    //@RequireFields(fields = {"team", "name"})
    @Route(method = HTTPMethod.POST)
    public static Iteration postHandler(RequestData requestData) throws Exception {
        // Create the new iteration
        Iteration iteration = new Iteration(
            EbeanEx.require(EbeanEx.find(Team.class, requestData.getPayload().get("team"))),
            LocalDate.parse(requestData.getPayload().getString("start")),
            LocalDate.parse(requestData.getPayload().getString("end")),
            requestData.getPayload().getString("name"),
            requestData.getPayload().getString("description")
        );
        Ebean.save(iteration);

        // Return the created iteration
        return iteration;
    }

    /**
     * PUT /iteration/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.PUT)
    public static Iteration putHandler(RequestData requestData) throws Exception {
        // Get the iteration
        Iteration iteration = EbeanEx.require(EbeanEx.find(Iteration.class, requestData.getParameter("id")));

        // Change the iteration
        Payload payload = requestData.getPayload();
        if (payload.containsKey("start")) {
            iteration.setStart(LocalDate.parse(payload.getString("start")));
        }
        if (payload.containsKey("end")) {
            iteration.setStart(LocalDate.parse(payload.getString("end")));
        }
        if (payload.containsKey("name")) {
            iteration.setName(payload.getString("name"));
        }
        if (payload.containsKey("description")) {
            iteration.setDescription(payload.getString("description"));
        }

        // Save the changes
        Ebean.save(iteration);

        // Return the changed iteration
        return iteration;
    }

    /**
     * DELETE /iteration/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.DELETE)
    public static void deleteHandler(RequestData requestData) throws Exception {
        // Get the iteration
        Iteration iteration = EbeanEx.require(EbeanEx.find(Iteration.class, requestData.getParameter("id")));

        // Delete the iteration
        Ebean.delete(iteration);
    }

    /**
     * GET /iteration/1/story
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/iteration/{id}/story")
    public static Collection<Story> getStoryHandler(RequestData requestData) throws Exception {
        // Get the iteration
        Iteration iteration = EbeanEx.require(EbeanEx.find(Iteration.class, requestData.getParameter("id")));

        // Return the stories
        return iteration.getStories();
    }
}