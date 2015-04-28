package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import com.proftaak.pts4.rest.HTTPException;
import com.proftaak.pts4.rest.HTTPMethod;
import com.proftaak.pts4.rest.RequestData;
import com.proftaak.pts4.rest.annotations.Controller;
import com.proftaak.pts4.rest.annotations.Field;
import com.proftaak.pts4.rest.annotations.RequireAuth;
import com.proftaak.pts4.rest.annotations.Route;
import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * Created by Michon on 2-3-2015
 */
@Controller
public class AuthController {
    /**
     * Login to the application, creating a new token that can be used to authenticate yourself with future requests
     *
     * @return A new token that you can use to authenticate yourself
     */
    @Field(name = "email", required = true, description = "The email address of the user you're trying to authenticate as")
    @Field(name = "password", required = true, description = "The password of the user you're trying to authenticate as")
    @Route(method = HTTPMethod.POST, path = "/auth/login")
    public static Token loginHandler(RequestData requestData) throws Exception {
        // Check the login details
        User user = EbeanEx.find(User.class, User.FIELD_EMAIL, requestData.getPayload().get("email"));
        if (user == null || !user.checkPassword(requestData.getPayload().getString("password"))) {
            throw new HTTPException("Invalid login details", HttpStatus.UNAUTHORIZED_401);
        }

        // Create a new token
        Token token = new Token(user);
        Ebean.save(token);

        // Communicate the token to the client
        return token;
    }

    /**
     * Logout from the application, invalidating your current token so that it cannot be used anymore
     */
    @RequireAuth
    @Route(method = HTTPMethod.POST, path = "/auth/logout")
    public static void logoutHandler(RequestData requestData) throws Exception {
        // Remove the token
        Ebean.delete(requestData.getToken());
    }
}
