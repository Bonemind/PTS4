package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.rest.HTTPException;
import com.proftaak.pts4.core.rest.Payload;
import com.proftaak.pts4.core.rest.RequestData;
import com.proftaak.pts4.core.rest.ScopeRole;
import com.proftaak.pts4.core.rest.annotations.Controller;
import com.proftaak.pts4.core.rest.annotations.PreRequest;
import com.proftaak.pts4.core.rest.annotations.RequireAuth;
import com.proftaak.pts4.core.rest.annotations.Route;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Project;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.database.tables.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashSet;

/**
 * @author Michon
 */
@Controller
public class ProjectController {
    /**
     * Determine the role(s) the logged in user has within the project, if any
     */
    @PreRequest
    public static void determineScopeRoles(RequestData requestData) throws Exception {
        Project project = EbeanEx.find(Project.class, requestData.getParameter("id"));
        ProjectController.determineScopeRoles(requestData, project);

        if (requestData.getPayload() != null && requestData.getPayload().containsKey("team")) {
            Team team = EbeanEx.find(Team.class, requestData.getPayload().get("team"));
            TeamController.determineScopeRoles(requestData, team);
        }
    }

    /**
     * Determine the role(s) the logged in user has within the project, if any
     */
    public static void determineScopeRoles(RequestData requestData, Project project) {
        User user = requestData.getUser();
        if (user != null && project != null) {
            if (project.getProductOwner().equals(user)) {
                requestData.addScopeRole(ScopeRole.PRODUCT_OWNER);
                requestData.addScopeRole(ScopeRole.SCRUM_MASTER_OR_PRODUCT_OWNER);
                requestData.addScopeRole(ScopeRole.TEAM_MEMBER);
            }
            TeamController.determineScopeRoles(requestData, project.getTeam());
        }
    }

    /**
     * GET /project/1
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET_ONE)
    public static Object getOneHandler(RequestData requestData) throws Exception {
        Project project = EbeanEx.find(Project.class, requestData.getParameter("id"));
        if (project == null) {
            throw HTTPException.ERROR_NOT_FOUND;
        }
        return project;
    }

    /**
     * GET /project
     */
    @RequireAuth
    @Route(method = Route.Method.GET)
    public static Object getAllHandler(RequestData requestData) throws Exception {
        Collection<Project> projects = new HashSet<>();
        User user = requestData.getUser();
        for (Team team : user.getTeams()) {
            projects.addAll(team.getProjects());
        }
        projects.addAll(user.getOwnedProjects());
        return projects;
    }

    /**
     * POST /project
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = Route.Method.POST)
    public static Object postHandler(RequestData requestData) throws Exception {
        // Create the new project
        Project project = new Project(
            EbeanEx.require(EbeanEx.find(Team.class, requestData.getPayload().get("team"))),
            EbeanEx.require(EbeanEx.find(User.class, User.FIELD_EMAIL, requestData.getPayload().get("productOwner"))),
            requestData.getPayload().getString("name"),
            requestData.getPayload().getString("description")
        );
        Ebean.save(project);

        // Return the created project
        return project;
    }

    /**
     * PUT /project/1
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER_OR_PRODUCT_OWNER)
    @Route(method = Route.Method.PUT)
    public static Object putHandler(RequestData requestData) throws Exception {
        // Get the project
        Project project = EbeanEx.require(EbeanEx.find(Project.class, requestData.getParameter("id")));

        // Change the project
        Payload payload = requestData.getPayload();
        if (payload.containsKey("productOwner")) {
            project.setProductOwner(EbeanEx.find(User.class, User.FIELD_EMAIL, requestData.getPayload().get("productOwner")));
        }
        if (payload.containsKey("name")) {
            project.setName(payload.getString("name"));
        }
        if (payload.containsKey("description")) {
            project.setDescription(payload.getString("description"));
        }

        // Save the changes
        Ebean.save(project);

        // Return the changed project
        return project;
    }

    /**
     * DELETE /project/1
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER_OR_PRODUCT_OWNER)
    @Route(method = Route.Method.DELETE)
    public static Object deleteHandler(RequestData requestData) throws Exception {
        // Get the project
        Project project = EbeanEx.require(EbeanEx.find(Project.class, requestData.getParameter("id")));

        // Delete the project
        Ebean.delete(project);

        // Return nothing
        return null;
    }

    /**
     * GET /project/1/story
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = Route.Method.GET, route = "/project/{id}/story")
    public static Object getStoryHandler(RequestData requestData) throws Exception {
        // Get the project
        Project project = EbeanEx.require(EbeanEx.find(Project.class, requestData.getParameter("id")));

        // Configure the serializer.
        requestData.exclude("project");

        // Return the stories
        return project.getStories();
    }
}
