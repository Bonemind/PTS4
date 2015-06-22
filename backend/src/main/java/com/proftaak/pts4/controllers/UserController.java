package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.PendingInvitation;
import com.proftaak.pts4.database.tables.User;
import com.proftaak.pts4.rest.HTTPException;
import com.proftaak.pts4.rest.HTTPMethod;
import com.proftaak.pts4.rest.Payload;
import com.proftaak.pts4.rest.RequestData;
import com.proftaak.pts4.rest.annotations.Controller;
import com.proftaak.pts4.rest.annotations.Field;
import com.proftaak.pts4.rest.annotations.RequireAuth;
import com.proftaak.pts4.rest.annotations.Route;
import com.proftaak.pts4.rest.response.JSONResponse;
import com.proftaak.pts4.rest.response.ResponseFactory;
import org.glassfish.grizzly.http.util.HttpStatus;

import javax.persistence.PersistenceException;
import java.util.Collection;

/**
 * @author Michon
 */
@Controller
public class UserController {
    /**
     * GET /user/1
     */
    @RequireAuth
    @Route(method = HTTPMethod.GET_ONE)
    public static User getOneHandler(RequestData requestData) throws Exception {
        // Get the user
        User user = EbeanEx.find(User.class, requestData.getParameter("id"));

        // Return the user
        return user;
    }

    /**
     * GET /user
     * Returns a sorted list of usernames instead of users
     */
    @RequireAuth
    @Route(method = HTTPMethod.GET)
    public static JSONResponse<Collection<String>> getAllHandler(RequestData requestData) throws Exception {
        return ResponseFactory.queryToList(requestData, User.class, Ebean.createQuery(User.class), User::getName);
    }

    /**
     * POST /user
     */
    @Field(name = "email", required = true, description = "The email address of the new user")
    @Field(name = "name", required = true, description = "The name of the new user")
    @Field(name = "password", required = true, description = "The password of the new user")
    @Route(method = HTTPMethod.POST)
    public static User postHandler(RequestData requestData) throws Exception {
        // Create the new user
        User user = new User(
                requestData.getPayload().get("email").toString(),
                requestData.getPayload().get("name").toString(),
                requestData.getPayload().get("password").toString()
        );
        try {
            Ebean.save(user);
        } catch (PersistenceException e) {
            throw new HTTPException("That email address or name is already in use", HttpStatus.CONFLICT_409);
        }

        // Check if there are any pending invitations for this email address
        Collection<PendingInvitation> pendingInvitations = Ebean.find(PendingInvitation.class)
                .where()
                .eq(PendingInvitation.FIELD_EMAIL, requestData.getPayload().get("email").toString())
                .findList();
        for (PendingInvitation invitation : pendingInvitations) {
            user.getTeams().add(invitation.getTeam());
            Ebean.delete(invitation);
        }
        Ebean.save(user);

        // Return the created user
        return user;
    }

    /**
     * PUT /user/1
     */
    @Field(name = "password", description = "The new password of the user")
    @RequireAuth
    @Route(method = HTTPMethod.PUT)
    public static User putHandler(RequestData requestData) throws Exception {
        // Get the user
        User user = EbeanEx.require(EbeanEx.find(User.class, requestData.getParameter("id")));

        // Check whether this matches the currently logged in user
        if (!user.equals(requestData.getUser())) {
            throw HTTPException.ERROR_FORBIDDEN;
        }

        // Change the user
        Payload payload = requestData.getPayload();
        if (payload.containsKey("password")) {
            user.setPassword(payload.getString("password"));
        }

        // Save the changes
        Ebean.save(user);

        // Return the changed user
        return user;
    }

    /**
     * DELETE /user/1
     */
    @RequireAuth
    @Route(method = HTTPMethod.DELETE)
    public static void deleteHandler(RequestData requestData) throws Exception {
        // Get the user
        User user = EbeanEx.require(EbeanEx.find(User.class, requestData.getParameter("id")));

        // Check whether this matches the currently logged in user
        if (!user.equals(requestData.getUser())) {
            throw HTTPException.ERROR_FORBIDDEN;
        }

        // If the user is scrum master of any teams, refuse to delete him
        if (user.getOwnedTeams().size() > 0) {
            throw new HTTPException("This user cannot be removed because he is a SCRUM master of one or more teams", HttpStatus.CONFLICT_409);
        }

        // If the user is still product owner of any projects, refuse to delete him.
        if (user.getOwnedProjects().size() > 0) {
            throw new HTTPException("This user cannot be removed because he is a product owner of one or more projects", HttpStatus.CONFLICT_409);
        }

        // Delete the user
        Ebean.delete(user);
    }
}
