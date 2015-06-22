package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.proftaak.pts4.database.EbeanEx;
import com.proftaak.pts4.database.tables.Project;
import com.proftaak.pts4.database.tables.Story;
import com.proftaak.pts4.database.tables.Team;
import com.proftaak.pts4.database.tables.User;
import com.proftaak.pts4.rest.*;
import com.proftaak.pts4.rest.annotations.*;
import com.proftaak.pts4.rest.response.JSONResponse;
import com.proftaak.pts4.rest.response.ResponseFactory;

import java.util.Collection;

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
    public static JSONResponse<Collection<Project>> getAllHandler(RequestData requestData) throws Exception {
        return ResponseFactory.queryToList(requestData, Project.class, Project.queryForUser(requestData.getUser()));
    }

    /**
     * POST /project
     */
    @Field(name = "team", required = true, description = "The id of the team the new project belongs to", type = Team.class)
    @Field(name = "productOwner", required = true, description = "The email of the user that will be the product owner of the new project", type = User.class)
    @Field(name = "name", required = true, description = "The name of the new project")
    @Field(name = "description", description = "The description of the new project")
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    @Route(method = HTTPMethod.POST)
    public static Project postHandler(RequestData requestData) throws Exception {
        // Create the new project
        Project project = new Project(
                EbeanEx.require(EbeanEx.find(Team.class, requestData.getPayload().get("team"))),
                EbeanEx.require(EbeanEx.find(User.class, User.FIELD_NAME, requestData.getPayload().get("productOwner"))),
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
    @Field(name = "productOwner", description = "The email of the new user that will be the product owner of the project", type = User.class)
    @Field(name = "name", description = "The new name of the project")
    @Field(name = "description", description = "The new description of the project")
    @RequireAuth(role = ScopeRole.SCRUM_MASTER_OR_PRODUCT_OWNER)
    @Route(method = HTTPMethod.PUT)
    public static Project putHandler(RequestData requestData) throws Exception {
        // Get the project
        Project project = EbeanEx.require(EbeanEx.find(Project.class, requestData.getParameter("id")));

        // Change the project
        Payload payload = requestData.getPayload();
        if (payload.containsKey("productOwner")) {
            project.setProductOwner(EbeanEx.find(User.class, User.FIELD_NAME, requestData.getPayload().get("productOwner")));
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
    public static JSONResponse<Collection<Story>> getStoryHandler(RequestData requestData) throws Exception {
        Project project = EbeanEx.require(EbeanEx.find(Project.class, requestData.getParameter("id")));
        Query<Story> query = EbeanEx.queryBelongingTo(Story.class, Project.class, project);
        return ResponseFactory.queryToList(requestData, Story.class, query);
    }
}
