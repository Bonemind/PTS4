package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.*;
import com.proftaak.pts4.rest.*;
import com.proftaak.pts4.rest.annotations.*;
import com.proftaak.pts4.rest.response.JSONResponse;
import com.proftaak.pts4.rest.response.ResponseFactory;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.*;

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
    public static JSONResponse<Collection<Team>> getAllHandler(RequestData requestData) throws Exception {
        return ResponseFactory.queryToList(requestData, Team.class, Team.queryForUser(requestData.getUser()));
    }

    /**
     * POST /team
     */
    @Field(name = "name", required = true, description = "The name of the new team")
    @Field(name = "effortTrackingEnabled", description = "Whether effort tracking is enabled for the new team")
    @Field(name = "kanbanRules", description = "The kanban rules for the new team", type = KanbanRules.class)
    @RequireAuth
    @Route(method = HTTPMethod.POST)
    public static Team postHandler(RequestData requestData) throws Exception {
        // Create the new team
        Team team = new Team(
            requestData.getPayload().getString("name"),
            requestData.getUser()
        );
        team.setEffortTrackingEnabled(requestData.getPayload().getBoolean("effortTrackingEnabled"));
        team.setKanbanRules(requestData.getPayload().getEmbeddable(KanbanRules.class, "kanbanRules"));
        Ebean.save(team);

        // Return the created team
        return team;
    }

    /**
     * PUT /team/1
     */
    @Field(name = "name", description = "The new name of the team")
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
        if (payload.containsKey("effortTrackingEnabled")) {
            team.setEffortTrackingEnabled(payload.getBoolean("effortTrackingEnabled"));
        }
        if (payload.containsKey("kanbanRules")) {
            team.setKanbanRules(payload.getEmbeddable(KanbanRules.class, "kanbanRules"));
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
    public static JSONResponse<Collection<Project>> getProjectHandler(RequestData requestData) throws Exception {
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));
        Query<Project> query = EbeanEx.queryBelongingTo(Project.class, Team.class, team);
        return ResponseFactory.queryToList(requestData, Project.class, query);
    }

    /**
     * GET /team/1/user
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/team/{id}/user")
    public static JSONResponse<Collection<User>> getUserHandler(RequestData requestData) throws Exception {
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));
        Query<User> query = Ebean.createQuery(User.class);
        query.where().eq("teams." + Team.FIELD_ID, team);
        return ResponseFactory.queryToList(requestData, User.class, query);
    }

    /**
     * POST /team/1/user
     */
    @Field(name = "user", required = true, description = "The name or email address of the new team member")
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = HTTPMethod.POST, path = "/team/{id}/user")
    public static void postMemberHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));

        // Get the user
        String identifier = requestData.getPayload().getString("user");
        User user = User.findByNameOrEmail(identifier);
        if (user == null && identifier != null && identifier.contains("@")) {
            PendingInvitation.sendInvite(identifier, team);
        }

        // Add the user as member of the team.
        if (user != null && !team.getUsers().contains(user)) {
            team.getUsers().add(user);
            Ebean.save(team);
        }
    }

    /**
     * DELETE /team/1/user/1
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = HTTPMethod.DELETE, path = "/team/{id}/user/{userId}")
    public static void deleteMemberHandler(RequestData requestData) throws Exception {
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
    }

    /**
     * GET /team/1/story
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/team/{id}/story")
    public static JSONResponse<Collection<Story>> getStoryHandler(RequestData requestData) throws Exception {
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));
        Query<Project> projectQuery = EbeanEx.queryBelongingTo(Project.class, Team.class, team);
        Query<Story> query = EbeanEx.queryBelongingTo(Story.class, Project.class, projectQuery);
        return ResponseFactory.queryToList(requestData, Story.class, query);
    }

    /**
     * GET /team/1/iteration
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/team/{id}/iteration")
    public static JSONResponse<Collection<Iteration>> getIterationHandler(RequestData requestData) throws Exception {
        Team team = EbeanEx.require(EbeanEx.find(Team.class, requestData.getParameter("id")));
        Query<Iteration> query = EbeanEx.queryBelongingTo(Iteration.class, Team.class, team);
        return ResponseFactory.queryToList(requestData, Iteration.class, query);
    }
}
