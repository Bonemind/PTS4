package com.proftaak.pts4.controllers.auth;

import com.proftaak.pts4.core.BaseController;
import com.proftaak.pts4.core.HTTPException;
import org.restlet.data.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michon on 2-3-2015.
 */
public class LoginController extends BaseController {
    private static final String TOKEN = "super-secret-token";
    private static final String EMAIL = "test";
    private static final String PASSWORD = "test";

    public Map<String, Object> postHandler(Map<String, Object> data) throws HTTPException {
        // Check the login details.
        if (!data.get("email").equals(EMAIL) || !data.get("password").equals(PASSWORD)) {
            throw new HTTPException("Invalid login details", Status.CLIENT_ERROR_UNAUTHORIZED);
        }

        Map<String, Object> output = new HashMap<>();
        output.put("token", TOKEN);
        return output;
    }
}
