package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.core.restlet.RequestData;
import com.proftaak.pts4.core.restlet.ScopeRole;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.ProcessScopeObject;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.database.tables.Story;
import com.proftaak.pts4.database.tables.Task;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.database.tables.User;
import org.json.HTTP;
import org.restlet.data.Status;

import java.util.Map;

/**
 * @author Michon
 */
@CRUDController(table = User.class, parent = TeamController.class, name = "member")
public class TeamMemberController extends BaseController {
    /**
     * Validate whether the user is a member of the team
     */
    @ProcessScopeObject(User.class)
    public static void validateUserInTeam(RequestData requestData, User user) throws Exception {
        Team team = requestData.getScopeObject(Team.class);
        if (!team.getUsers().contains(user)) {
            throw HTTPException.ERROR_OBJECT_NOT_FOUND;
        }
    }

    /**
     * POST /team/1/member
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    public Object postHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = requestData.getScopeObject(Team.class);

        // Get the user
        User user = Ebean.find(User.class, requestData.getPayload().get("id"));
        if (user == null) {
            throw HTTPException.ERROR_OBJECT_NOT_FOUND;
        }

        // Add the user as member of the team.
        if (!team.getUsers().contains(user)) {
            team.getUsers().add(user);
            Ebean.save(team);
        }

        // Return nothing
        return null;
    }

    /**
     * DELETE /team/1/member/1
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    public Object deleteHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = requestData.getScopeObject(Team.class);

        // Get the user
        System.out.println(requestData.getUrlParams());
        User user = requestData.getScopeObject(User.class);

        // If the user is the scrum master of the team, he cannot be removed
        if (team.getScrumMaster().equals(user)) {
            throw new HTTPException("That user is the SCRUM master and may not be removed.", Status.CLIENT_ERROR_CONFLICT);
        }

        // Remove the user from the team
        team.getUsers().remove(user);
        Ebean.save(team);

        // Return nothing
        return null;
    }
}
