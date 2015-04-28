package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Project;
import com.proftaak.pts4.database.tables.Story;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.database.tables.User;
import com.proftaak.pts4.rest.*;
import com.proftaak.pts4.rest.annotations.Controller;
import com.proftaak.pts4.rest.annotations.PreRequest;
import com.proftaak.pts4.rest.annotations.RequireAuth;
import com.proftaak.pts4.rest.annotations.Route;

import java.util.Collection;
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
    @Route(method = HTTPMethod.GET_ONE)
    public static Project getOneHandler(RequestData requestData) throws Exception {
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
    @Route(method = HTTPMethod.GET)
    public static Collection<Project> getAllHandler(RequestData requestData) throws Exception {
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
    //@RequireFields(fields = {"team", "productOwner", "name"})
    @Route(method = HTTPMethod.POST)
    public static Project postHandler(RequestData requestData) throws Exception {
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
    @Route(method = HTTPMethod.PUT)
    public static Project putHandler(RequestData requestData) throws Exception {
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
    @Route(method = HTTPMethod.DELETE)
    public static void deleteHandler(RequestData requestData) throws Exception {
        // Get the project
        Project project = EbeanEx.require(EbeanEx.find(Project.class, requestData.getParameter("id")));

        // Delete the project
        Ebean.delete(project);
    }

    /**
     * GET /project/1/story
     */
    @RequireAuth(role = ScopeRole.TEAM_MEMBER)
    @Route(method = HTTPMethod.GET, path = "/project/{id}/story")
    public static Collection<Story> getStoryHandler(RequestData requestData) throws Exception {
        // Get the project
        Project project = EbeanEx.require(EbeanEx.find(Project.class, requestData.getParameter("id")));

        // Return the stories
        return project.getStories();
    }
}
