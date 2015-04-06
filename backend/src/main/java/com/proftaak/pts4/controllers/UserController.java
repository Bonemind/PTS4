package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.rest.HTTPException;
import com.proftaak.pts4.core.rest.RequestData;
import com.proftaak.pts4.core.rest.annotations.Controller;
import com.proftaak.pts4.core.rest.annotations.RequireAuth;
import com.proftaak.pts4.core.rest.annotations.Route;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.User;
import org.glassfish.grizzly.http.util.HttpStatus;

import javax.persistence.PersistenceException;
import java.util.Map;

/**
 * @author Michon
 */
@Controller
public class UserController {
    /**
     * POST /user
     */
    @Route(method = Route.Method.POST)
    public static Object postHandler(RequestData requestData) throws Exception {
        // Create the new user
        User user = new User(
            requestData.getPayload().get("email").toString(),
            requestData.getPayload().get("password").toString()
        );
        try {
            Ebean.save(user);
        } catch (PersistenceException e) {
            throw new HTTPException("That email address is already in use", HttpStatus.CONFLICT_409);
        }

        // Return the created user
        return user;
    }

    /**
     * PUT /user/1
     */
    @RequireAuth
    @Route(method = Route.Method.PUT)
    public static Object putHandler(RequestData requestData) throws Exception {
        // Get the user
        User user = EbeanEx.require(EbeanEx.find(User.class, requestData.getParameter("id")));

        // Change the user
        Map<String, Object> payload = requestData.getPayload();
        if (payload.containsKey("password")) {
            user.setPassword((String) payload.get("password"));
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
    @Route(method = Route.Method.DELETE)
    public static Object deleteHandler(RequestData requestData) throws Exception {
        // Get the user
        User user = EbeanEx.require(EbeanEx.find(User.class, requestData.getParameter("id")));

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

        // Return nothing
        return null;
    }
}
