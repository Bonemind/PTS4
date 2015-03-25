package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.core.restlet.RequestData;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import org.restlet.data.Status;

/**
 * Created by Michon on 2-3-2015
 */
public class AuthLogoutController extends BaseController {
    @RequireAuth
    public Object postHandler(RequestData requestData) throws Exception {
        // Remove the token
        Ebean.delete(requestData.getToken());

        // Return nothing
        return null;
    }
}
