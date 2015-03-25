package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.core.restlet.RequestData;
import com.proftaak.pts4.core.restlet.ScopeRole;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.PreRequest;
import com.proftaak.pts4.core.restlet.annotations.ProcessScopeObject;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.database.tables.User;

import java.util.Map;

/**
 * @author Michon
 */
@CRUDController(table = Team.class)
public class TeamController extends BaseController {
    @PreRequest
    public static void determineTeamRole(RequestData requestData) throws Exception {
        User user = requestData.getUser();
        Team team = requestData.getScopeObject(Team.class, false);
        if (user != null && team != null) {
            if (team.getScrumMaster().equals(user)) {
                requestData.addScopeRole(ScopeRole.SCRUM_MASTER);
            }
            if (team.getUsers().contains(user)) {
                requestData.addScopeRole(ScopeRole.DEVELOPER);
            }
        }
    }

    /**
     * GET /team or /team/1
     */
    @RequireAuth
    public Object getHandler(RequestData requestData) throws Exception {
        if (requestData.getUrlParams().get("teamId") == null) {
            return Ebean.find(Team.class).findList();
        } else {
            System.out.println(requestData.getScopeRoles());
            return requestData.getScopeObject(Team.class);
        }
    }

    /**
     * POST /team
     */
    @RequireAuth
    public Object postHandler(RequestData requestData) throws Exception {
        // Create the new user team
        Team team;
        try {
            team = new Team((String) requestData.getPayload().get("name"), requestData.getUser());
            Ebean.save(team);
        } catch (Exception e) {
            e.printStackTrace();
            throw HTTPException.ERROR_BAD_REQUEST;
        }

        // Return the created user team
        return team;
    }

    /**
     * PUT /team/1
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    public Object putHandler(RequestData requestData) throws Exception {
        // Try to get the user team
        Team team = requestData.getScopeObject(Team.class);
        Map<String, Object> payload = requestData.getPayload();

        // Change the team
        if (payload.containsKey("name")) {
            team.setName((String) payload.get("name"));
        }

        // Save the changes
        Ebean.save(team);

        // Return the changed user team
        return team;
    }

    /**
     * DELETE /team/1
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    public Object deleteHandler(RequestData requestData) throws Exception {
        // Try to get the user team
        Team team = requestData.getScopeObject(Team.class);

        // Delete the user team
        Ebean.delete(team);

        // Return nothing
        return null;
    }
}
