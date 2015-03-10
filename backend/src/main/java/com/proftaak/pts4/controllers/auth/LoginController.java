package com.proftaak.pts4.controllers.auth;

import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import org.restlet.data.Status;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michon on 2-3-2015.
 */
public class LoginController extends BaseController {
    public Object postHandler(Map<String, Object> data) throws HTTPException, FileNotFoundException, SQLException {
        // Check the login details.
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("email", data.get("email"));
        User user = User.getDao().queryForFieldValues(queryMap).get(0);
        if (user == null || !user.checkPassword(data.get("password").toString())) {
            throw new HTTPException("Invalid login details", Status.CLIENT_ERROR_UNAUTHORIZED);
        }

        // Create a new token.
        Token token = new Token(user);
        Token.getDao().create(token);

        // Communicate the token to the client.
        Map<String, Object> output = new HashMap<>();
        output.put("token", token.getToken());
        return output;
    }
}
