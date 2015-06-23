package com.proftaak.pts4.rest;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import flexjson.JSONDeserializer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.util.ContentType;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author Michon
 */
public class RequestData {
    /**
     * The matcher of the url match
     */
    private Matcher matcher;

    /**
     * The payload that has been sent from the client
     */
    private Payload payload;

    /**
     * The currently logged in user, if any
     */
    private User user;

    /**
     * The currently used token, if any
     */
    private Token token;

    /**
     * The request
     */
    private Request request;

    /**
     * The roles the current user has within the current scope.
     */
    private Collection<ScopeRole> roles = new HashSet<>();

    public Payload getPayload() {
        return this.payload;
    }

    public User getUser() {
        return this.user;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public Token getToken() {
        return this.token;
    }

    public Request getRequest() {
        return this.request;
    }

    public void addScopeRole(ScopeRole role) {
        this.roles.add(role);
    }

    public void requireScopeRole(ScopeRole role) throws HTTPException {
        if (!this.roles.contains(role)) {
            throw HTTPException.ERROR_FORBIDDEN;
        }
    }

    public String getParameter(String name) {
        try {
            return this.matcher.group(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Build a RequestData object for the current request
     *
     * @param request The request
     * @param payload The payload
     * @param matcher The matcher for the current route
     * @return The RequestData object for this request
     */
    protected static RequestData buildRequest(Request request, Payload payload, Matcher matcher) throws HTTPException {
        RequestData data = new RequestData();

        // Store the request
        data.request = request;

        // Store the matcher
        data.matcher = matcher;

        // Store the payload
        data.payload = payload;
        if (data.payload == null) {
            data.payload = new Payload(new HashMap<>());
        }

        // Get the token/user, if any
        String tokenString = request.getHeader("X-TOKEN");
        Token token = tokenString == null ? null : Ebean.find(Token.class, tokenString);
        if (token != null && token.isValid()) {
            data.token = token;
            data.user = token.getUser();
            data.addScopeRole(ScopeRole.USER);
        }

        return data;
    }
}
