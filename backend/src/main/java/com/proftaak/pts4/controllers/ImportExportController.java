package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.PendingInvitation;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.database.tables.User;
import com.proftaak.pts4.export.Exporter;
import com.proftaak.pts4.rest.HTTPException;
import com.proftaak.pts4.rest.HTTPMethod;
import com.proftaak.pts4.rest.Payload;
import com.proftaak.pts4.rest.RequestData;
import com.proftaak.pts4.rest.annotations.Controller;
import com.proftaak.pts4.rest.annotations.Field;
import com.proftaak.pts4.rest.annotations.RequireAuth;
import com.proftaak.pts4.rest.annotations.Route;
import com.proftaak.pts4.rest.response.RawResponse;
import org.glassfish.grizzly.http.util.HttpStatus;

import javax.persistence.PersistenceException;
import java.util.Collection;
import java.util.TreeSet;

/**
 * @author Michon
 */
@Controller
public class ImportExportController {

    /**
     * GET /team/1/export
     */
    @RequireAuth
    @Route(method = HTTPMethod.GET_ONE, path = "/team/{id}/export")
    public static RawResponse getOneHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.find(Team.class, requestData.getParameter("id"));

        // Exported the team and return the string result
        return new RawResponse(Exporter.export(team), "application/xml");
    }
}
