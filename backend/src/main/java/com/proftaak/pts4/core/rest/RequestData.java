package com.proftaak.pts4.core.rest;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.glassfish.grizzly.http.server.Request;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;

/**
 * @author Michon
 */
public class RequestData {
    /**
     * The matcher of the url match
     */
    protected Matcher matcher;

    /**
     * The payload that has been sent from the client
     */
    protected Map<String, Object> payload;

    /**
     * The currently logged in user, if any
     */
    protected User user;

    /**
     * The currently used token, if any
     */
    protected Token token;

    /**
     * The roles the current user has within the current scope.
     */
    private Collection<ScopeRole> roles = new TreeSet<>();

    /**
     * The JSONSerializer that will be used to serialize the returned object
     */
    protected JSONSerializer jsonSerializer;

    /**
     * Constructs a new RequestData.
     */
    protected RequestData() {
        this.jsonSerializer = new JSONSerializer();
        this.jsonSerializer.exclude("class");
    }

    public Map<String, Object> getPayload() {
        return this.payload;
    }

    public User getUser() {
        return this.user;
    }

    public Token getToken() {
        return this.token;
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
     * Include a field in the response
     *
     * @param field The field to include
     */
    public void include(String field) {
        this.jsonSerializer.include(field);
    }

    /**
     * Exclude a field in the response
     *
     * @param field The field to exclude
     */
    public void exclude(String field) {
        this.jsonSerializer.exclude(field);
    }

    /**
     * Build a RequestData object for the current request
     *
     * @param request The request
     * @param matcher The matcher for the current route
     * @return The RequestData object for this request
     */
    protected static RequestData buildRequest(Request request, Matcher matcher) throws FileNotFoundException {
        RequestData data = new RequestData();

        // Store the matcher
        data.matcher = matcher;

        // Get the payload, if any
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        JSONDeserializer<Map> deserializer = new JSONDeserializer<>();
        try {
            if (reader.ready()) {
                data.payload = deserializer.deserialize(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
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
