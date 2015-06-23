package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Project;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.export.Exporter;
import com.proftaak.pts4.importers.RallyImporter;
import com.proftaak.pts4.importers.VersionOneImporter;
import com.proftaak.pts4.rest.HTTPMethod;
import com.proftaak.pts4.rest.Payload;
import com.proftaak.pts4.rest.RequestData;
import com.proftaak.pts4.rest.ScopeRole;
import com.proftaak.pts4.rest.annotations.*;
import com.proftaak.pts4.rest.response.RawResponse;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Michon
 */
@Controller
public class ImportExportController {
    /**
     * Determine the role(s) the logged in user has within the team, if any
     */
    @PreRequest
    public static void determineScopeRoles(RequestData requestData) throws Exception {
        Team team = EbeanEx.find(Team.class, requestData.getParameter("id"));
        TeamController.determineScopeRoles(requestData, team);
    }

    /**
     * GET /team/1/export
     */
    @RequireAuth
    @Route(method = HTTPMethod.GET_ONE, path = "/team/{id}/export")
    public static RawResponse exportHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.find(Team.class, requestData.getParameter("id"));

        // Exported the team and return the string result
        return new RawResponse(Exporter.export(team), "application/xml");
    }

    /**
     * POST /team/1/import/rally
     */
    @Field(name = "iterations", required = true, description = "The iterations XML file")
    @Field(name = "stories", required = true, description = "The stories XML file")
    @Field(name = "tasks", required = true, description = "The tasks XML file")
    @Field(name = "defects", required = true, description = "The defects XML file")
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = HTTPMethod.POST, path = "/team/{id}/import/rally")
    public static void importRallyHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.find(Team.class, requestData.getParameter("id"));

        // Read the files
        Collection<InputStream> inputStreams = new ArrayList<>();
        Payload payload = requestData.getPayload();
        inputStreams.add(payload.getFileContent("iterations"));
        inputStreams.add(payload.getFileContent("stories"));
        inputStreams.add(payload.getFileContent("tasks"));
        inputStreams.add(payload.getFileContent("defects"));

        // Perform the import
        RallyImporter.importRally(inputStreams, team);
    }

    /**
     * POST /team/1/import/versionone
     */
    @Field(name = "url", required = true, description = "The versionone base URL")
    @Field(name = "token", required = true, description = "The access token")
    @Field(name = "project", required = true, description = "The project name")
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = HTTPMethod.POST, path = "/team/{id}/import/versionone")
    public static void importVersioOneHandler(RequestData requestData) throws Exception {
        // Get the team
        Team team = EbeanEx.find(Team.class, requestData.getParameter("id"));

        // Perform the import
        Payload payload = requestData.getPayload();
        VersionOneImporter versionOneImporter = new VersionOneImporter(payload.getString("token"), payload.getString("url"));
        versionOneImporter.initConnection();
        Project project = versionOneImporter.importProject(payload.getString("project"), team, team.getScrumMaster());
        Ebean.beginTransaction();
        Ebean.save(team.getIterations());
        Ebean.update(team);
        Ebean.save(project);
        Ebean.commitTransaction();
    }
}
