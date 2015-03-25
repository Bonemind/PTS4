package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.core.restlet.RequestData;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.core.restlet.annotations.ProcessScopeObject;
import com.proftaak.pts4.database.tables.User;
import org.restlet.data.Status;

import java.util.Map;

/**
 * @author Michon
 */
@CRUDController(table = User.class)
public class UserController extends BaseController {
    /**
     * Validate whether the logged in user is the same user as the user in scope, to prevent editing other users
     */
    @ProcessScopeObject(User.class)
    public static boolean validateUserSelf(RequestData requestData, User user) throws Exception {
        if (!requestData.getUser().equals(user)) {
            throw HTTPException.ERROR_FORBIDDEN;
        }
        return true;
    }

    /**
     * POST /user
     */
    public Object postHandler(RequestData requestData) throws Exception {
        // Create the new user
        User user;

        // Check for email conflicts
        String email = requestData.getPayload().get("email").toString();
        if (Ebean.find(User.class).where().eq(User.FIELD_EMAIL, email).findRowCount() > 0) {
            throw new HTTPException("That email address is already in use", Status.CLIENT_ERROR_CONFLICT);
        }

        // Create the new user
        user = new User(email, requestData.getPayload().get("password").toString());
        Ebean.save(user);

        // Return the created user
        return user;
    }

    /**
     * PUT /user/1
     */
    @RequireAuth
    public Object putHandler(RequestData requestData) throws Exception {
        // Get the user
        User user = requestData.getScopeObject(User.class);
        Map<String, Object> payload = requestData.getPayload();

        // Change the user
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
    public Object deleteHandler(RequestData requestData) throws Exception {
        // Try to get the user
        User user = requestData.getScopeObject(User.class);

        // Delete the user
        Ebean.delete(user);

        // Return nothing
        return null;
    }
}
