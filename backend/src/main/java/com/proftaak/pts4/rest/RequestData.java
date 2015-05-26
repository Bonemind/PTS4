package com.proftaak.pts4.rest;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import com.proftaak.pts4.json.JSONSerializerFactory;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.glassfish.grizzly.http.server.Request;
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
     * The serializer that will be used to serialize the return data
     */
    private JSONSerializer serializer;

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

    public JSONSerializer getSerializer() {
        return this.serializer;
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
     * @param matcher The matcher for the current route
     * @return The RequestData object for this request
     */
    protected static RequestData buildRequest(Request request, Matcher matcher) throws HTTPException {
        RequestData data = new RequestData();

        // Create a serializer
        data.serializer = JSONSerializerFactory.createSerializer();

        // Store the request
        data.request = request;

        // Store the matcher
        data.matcher = matcher;

        // Get the payload, if any
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        JSONDeserializer<Map> deserializer = new JSONDeserializer<>();
        try {
            if (reader.ready()) {
                data.payload = new Payload((HashMap<String, Object>) deserializer.deserialize(reader.readLine()));
            }
        } catch (Exception e) {
            throw new HTTPException("Malformed payload", HttpStatus.BAD_REQUEST_400);
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
