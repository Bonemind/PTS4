package com.proftaak.pts4.controllers.auth;

import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.database.User;
import org.restlet.data.Status;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michon on 2-3-2015.
 */
public class LoginController extends BaseController {
    private static final String TOKEN = "super-secret-token";

    public Map<String, Object> postHandler(Map<String, Object> data) throws HTTPException, FileNotFoundException, SQLException {
        // Check the login details.
        Map<String, Object> queryMap = new HashMap<String, Object>();
        queryMap.put("email", data.get("email"));
        User user = User.getDao().queryForFieldValues(queryMap).get(0);
        if (user == null || !user.checkPassword(data.get("password").toString())) {
            throw new HTTPException("Invalid login details", Status.CLIENT_ERROR_UNAUTHORIZED);
        }

        Map<String, Object> output = new HashMap<>();
        output.put("token", TOKEN);
        return output;
    }
}
