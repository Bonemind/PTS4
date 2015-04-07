package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.rest.RequestData;
import com.proftaak.pts4.core.rest.ScopeRole;
import com.proftaak.pts4.core.rest.annotations.Controller;
import com.proftaak.pts4.core.rest.annotations.PreRequest;
import com.proftaak.pts4.core.rest.annotations.RequireAuth;
import com.proftaak.pts4.core.rest.annotations.Route;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Iteration;
import com.proftaak.pts4.database.tables.Project;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.database.tables.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
     * GET /iteration
     */
    @RequireAuth
    @Route(method = Route.Method.GET)
    public static Object getAllHandler(RequestData requestData) throws Exception {
        Collection<Iteration> iterations = new ArrayList<>();
        User user = requestData.getUser();
        for (Team team : user.getTeams()) {
            iterations.addAll(team.getIterations());
        }
        return iterations;
    }

    /**
     * GET /iteration/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET_ONE)
    public static Object getOneHandler(RequestData requestData) throws Exception {
        return EbeanEx.require(EbeanEx.find(Iteration.class, requestData.getParameter("id")));
    }

    /**
     * POST /iteration
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.POST)
    public static Object postHandler(RequestData requestData) throws Exception {
        // Create the new iteration
        Iteration iteration = new Iteration(
            EbeanEx.require(EbeanEx.find(Team.class, requestData.getPayload().get("team"))),
            LocalDate.parse((String) requestData.getPayload().get("start")),
            LocalDate.parse((String) requestData.getPayload().get("end")),
            (String) requestData.getPayload().get("name"),
            (String) requestData.getPayload().get("description")
        );
        Ebean.save(iteration);

        // Return the created iteration
        return iteration;
    }

    /**
     * PUT /iteration/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.PUT)
    public static Object putHandler(RequestData requestData) throws Exception {
        // Get the iteration
        Iteration iteration = EbeanEx.require(EbeanEx.find(Iteration.class, requestData.getParameter("id")));

        // Change the iteration
        Map<String, Object> payload = requestData.getPayload();
        if (payload.containsKey("start")) {
            iteration.setStart(LocalDate.parse((String) payload.get("start")));
        }
        if (payload.containsKey("end")) {
            iteration.setStart(LocalDate.parse((String) payload.get("end")));
        }
        if (payload.containsKey("name")) {
            iteration.setName((String) payload.get("name"));
        }
        if (payload.containsKey("description")) {
            iteration.setDescription((String) payload.get("description"));
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
    @Route(method = Route.Method.DELETE)
    public static Object deleteHandler(RequestData requestData) throws Exception {
        // Get the iteration
        Iteration iteration = EbeanEx.require(EbeanEx.find(Iteration.class, requestData.getParameter("id")));

        // Delete the iteration
        Ebean.delete(iteration);

        // Return nothing
        return null;
    }

    /**
     * GET /iteration/1/story
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET, route = "/iteration/{id}/story")
    public static Object getStoryHandler(RequestData requestData) throws Exception {
        // Get the iteration
        Iteration iteration = EbeanEx.require(EbeanEx.find(Iteration.class, requestData.getParameter("id")));

        // Configure the serializer.
        requestData.exclude("iteration");

        // Return the stories
        return iteration.getStories();
    }
}
