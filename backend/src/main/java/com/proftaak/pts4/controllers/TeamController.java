package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.rest.HTTPException;
import com.proftaak.pts4.core.rest.RequestData;
import com.proftaak.pts4.core.rest.ScopeRole;
import com.proftaak.pts4.core.rest.annotations.Controller;
import com.proftaak.pts4.core.rest.annotations.PreRequest;
import com.proftaak.pts4.core.rest.annotations.RequireAuth;
import com.proftaak.pts4.core.rest.annotations.Route;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.database.tables.User;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.Map;

/**
 * @author Michon
 */
@Controller
public class TeamController {
    /**
     * For this controller we will want to include the list of tasks in responses
     */
    @PreRequest
    public static void setupSerializer(RequestData requestData) {
        requestData.include("projects");
        requestData.include("scrumMaster");
    }

    /**
     * Determine the role(s) the logged in user has within the team, if any
     */
    @PreRequest
    public static void determineScopeRoles(RequestData requestData) throws Exception {
        Team team = EbeanEx.find(Team.class, requestData.getParameter("id"));
        TeamController.determineScopeRoles(requestData, team);
    }

    /**
     * Determine the role(s) the logged in user has within the team, if any
     */
    public static void determineScopeRoles(RequestData requestData, Team team) {
        User user = requestData.getUser();
        if (user != null && team != null) {
            if (team.getScrumMaster().equals(user)) {
                requestData.addScopeRole(ScopeRole.SCRUM_MASTER);
                requestData.addScopeRole(ScopeRole.SCRUM_MASTER_OR_PRODUCT_OWNER);
            }
            if (team.getUsers().contains(user)) {
                requestData.addScopeRole(ScopeRole.DEVELOPER);
                requestData.addScopeRole(ScopeRole.TEAM_MEMBER);
            }
        }
    }

    /**
     * GET /team
     */
    @RequireAuth
    @Route(method = Route.Method.GET)
    public static Object getAllHandler(RequestData requestData) throws Exception {
        User user = requestData.getUser();
        return user.getTeams();
    }

    /**
     * GET /team/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET_ONE)
    public static Object getHandler(RequestData requestData) throws Exception {
        return Ebean.find(Team.class, requestData.getParameter("id"));
    }

    /**
     * POST /team
     */
    @RequireAuth
    @Route(method = Route.Method.POST)
    public static Object postHandler(RequestData requestData) throws Exception {
        // Create the new team
        Team team = new Team(
            (String) requestData.getPayload().get("name"),
            requestData.getUser()
        );
        Ebean.save(team);

        // Return the created team
        return team;
    }

    /**
     * PUT /team/1
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = Route.Method.PUT)
    public static Object putHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Change the team
        Map<String, Object> payload = requestData.getPayload();
        if (payload.containsKey("name")) {
            team.setName((String) payload.get("name"));
        }

        // Save the changes
        Ebean.save(team);

        // Return the changed team
        return team;
    }

    /**
     * DELETE /team/1
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = Route.Method.DELETE)
    public static Object deleteHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Delete the team
        Ebean.delete(team);

        // Return nothing
        return null;
    }

    /**
     * GET /team/1/project
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET, route = "/team/{id}/project")
    public static Object getProjectHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Return the stories
        return team.getProjects();
    }

    /**
     * GET /team/1/user
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET, route = "/team/{id}/user")
    public static Object getStoryHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Return the users
        return team.getUsers();
    }

    /**
     * POST /team/1/user
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = Route.Method.POST, route = "/team/{id}/user")
    public static Object postMemberHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Get the user
        User user = EbeanEx.require(EbeanEx.find(User.class, User.FIELD_EMAIL, requestData.getPayload().get("email")));

        // Add the user as member of the team.
        if (!team.getUsers().contains(user)) {
            team.getUsers().add(user);
            Ebean.save(team);
        }

        // Return the users
        return team.getUsers();
    }

    /**
     * DELETE /team/1/user/1
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = Route.Method.DELETE, route = "/team/{id}/user/{userId}")
    public static Object deleteMemberHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Get the user
        User user = EbeanEx.require(EbeanEx.find(User.class, requestData.getParameter("userId")));

        // If the user is the scrum master of the team, he cannot be removed
        if (team.getScrumMaster().equals(user)) {
            throw new HTTPException("That user is the SCRUM master and may not be removed.", HttpStatus.CONFLICT_409);
        }

        // Remove the user from the team
        team.getUsers().remove(user);
        Ebean.save(team);

        // Return the users
        return team.getUsers();
    }
}
