package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.*;
import com.proftaak.pts4.rest.*;
import com.proftaak.pts4.rest.annotations.Controller;
import com.proftaak.pts4.rest.annotations.PreRequest;
import com.proftaak.pts4.rest.annotations.RequireAuth;
import com.proftaak.pts4.rest.annotations.Route;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Michon
 */
@Controller
public class TeamController {
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
            // TODO: remove the being a part of a team if you're product owner of one of their projects
            for (Project project : team.getProjects()) {
                if (project.getProductOwner().equals(user)) {
                    requestData.addScopeRole(ScopeRole.TEAM_MEMBER);
                }
            }
        }
    }

    /**
     * GET /team/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET_ONE)
    public static Team getOneHandler(RequestData requestData) throws Exception {
        Team team = EbeanEx.find(Team.class, requestData.getParameter("id"));
        if (team == null) {
            throw HTTPException.ERROR_NOT_FOUND;
        }
        return team;
    }

    /**
     * GET /team
     */
    @RequireAuth
    @Route(method = HTTPMethod.GET)
    public static Collection<Team> getAllHandler(RequestData requestData) throws Exception {
        User user = requestData.getUser();
        // TODO: remove the being a part of a team if you're product owner of one of their projects
        Collection<Team> teams = new HashSet<>();
        teams.addAll(user.getTeams());
        for (Project project : user.getOwnedProjects()) {
            teams.add(project.getTeam());
        }
        return teams;
    }

    /**
     * POST /team
     */
    @RequireAuth
    //@RequireFields(fields = {"name"})
    @Route(method = HTTPMethod.POST)
    public static Team postHandler(RequestData requestData) throws Exception {
        // Create the new team
        Team team = new Team(
            requestData.getPayload().getString("name"),
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
    @Route(method = HTTPMethod.PUT)
    public static Team putHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Change the team
        Payload payload = requestData.getPayload();
        if (payload.containsKey("name")) {
            team.setName(payload.getString("name"));
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
    @Route(method = HTTPMethod.DELETE)
    public static void deleteHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Delete the team
        Ebean.delete(team);
    }

    /**
     * GET /team/1/project
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/team/{id}/project")
    public static Collection<Project> getProjectHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Return the stories
        return team.getProjects();
    }

    /**
     * GET /team/1/user
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/team/{id}/user")
    public static Collection<User> getUserHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Return the users
        return team.getUsers();
    }

    /**
     * POST /team/1/user
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = HTTPMethod.POST, path = "/team/{id}/user")
    public static Collection<User> postMemberHandler(RequestData requestData) throws Exception {
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
    @Route(method = HTTPMethod.DELETE, path = "/team/{id}/user/{userId}")
    public static Collection<User> deleteMemberHandler(RequestData requestData) throws Exception {
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

    /**
     * GET /team/1/story
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/team/{id}/story")
    public static Collection<Story> getStoryHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Get all stories
        Collection<Story> stories = new ArrayList<>();
        for (Project project : team.getProjects()) {
            stories.addAll(project.getStories());
        }
        return stories;
    }

    /**
     * GET /team/1/iteration
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/team/{id}/iteration")
    public static Collection<Iteration> getIterationHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Return the iterations
        return team.getIterations();
    }
}