package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.rest.HTTPException;
import com.proftaak.pts4.core.rest.RequestData;
import com.proftaak.pts4.core.rest.annotations.Controller;
import com.proftaak.pts4.core.rest.annotations.RequireAuth;
import com.proftaak.pts4.core.rest.annotations.Route;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * Created by Michon on 2-3-2015
 */
@Controller
public class AuthController {
    /**
     * POST /auth/login
     */
    @Route(method = Route.Method.POST, route = "/auth/login")
    public static Object loginHandler(RequestData requestData) throws Exception {
        // Check the login details
        User user = EbeanEx.find(User.class, User.FIELD_EMAIL, requestData.getPayload().get("email"));
        if (user == null || !user.checkPassword(requestData.getPayload().get("password").toString())) {
            throw new HTTPException("Invalid login details", HttpStatus.UNAUTHORIZED_401);
        }

        // Create a new token
        Token token = new Token(user);
        Ebean.save(token);

        // Communicate the token to the client
        return token;
    }

    /**
     * POST /auth/logout
     */
    @RequireAuth
    @Route(method = Route.Method.POST, route = "/auth/logout")
    public static Object logoutHandler(RequestData requestData) throws Exception {
        // Remove the token
        Ebean.delete(requestData.getToken());

        // Return nothing
        return null;
    }
}
