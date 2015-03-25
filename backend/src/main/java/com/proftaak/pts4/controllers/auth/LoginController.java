package com.proftaak.pts4.controllers.auth;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.core.restlet.RequestData;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import org.restlet.data.Status;

/**
 * Created by Michon on 2-3-2015
 */
public class LoginController extends BaseController {
    @Override
    public Object postHandler(RequestData requestData) throws Exception {
        // Check the login details
        User user = Ebean.find(User.class).where().eq(User.FIELD_EMAIL, requestData.getPayload().get("email")).findUnique();
        if (user == null || !user.checkPassword(requestData.getPayload().get("password").toString())) {
            throw new HTTPException("Invalid login details", Status.CLIENT_ERROR_UNAUTHORIZED);
        }

        // Create a new token
        Token token = new Token(user);
        Ebean.save(token);

        // Communicate the token to the client
        return token;
    }
}
