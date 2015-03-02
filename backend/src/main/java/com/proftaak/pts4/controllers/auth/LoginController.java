package com.proftaak.pts4.controllers.auth;

import com.proftaak.pts4.core.BaseController;
import org.restlet.data.Status;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Michon on 2-3-2015.
 */
public class LoginController extends BaseController {
    private static final String TOKEN = "super-secret-token";
    private static final String EMAIL = "test";
    private static final String PASSWORD = "test";

    public Object postHandler(Object data) throws IOException {
        // Check the login details.
        Map<String, String> loginData = (Map<String, String>) data;
        if (!loginData.get("email").equals(EMAIL) || !loginData.get("password").equals(PASSWORD)) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return "Invalid login details";
        }

        Map<String, String> output = new HashMap<>();
        output.put("token", TOKEN);
        return output;
    }
}
