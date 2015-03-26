package com.proftaak.pts4.controllers;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.core.restlet.RequestData;
import com.proftaak.pts4.core.restlet.ScopeRole;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.PreRequest;
import com.proftaak.pts4.core.restlet.annotations.ProcessScopeObject;
import com.proftaak.pts4.core.restlet.annotations.RequireAuth;
import com.proftaak.pts4.database.tables.*;
import com.proftaak.pts4.database.tables.Project;

import java.util.Map;

/**
 * @author Michon
 */
@CRUDController(table = Project.class, parent = TeamController.class)
public class ProjectController extends BaseController {
    /**
     * For this controller we will want to include the list of tasks in responses
     */
    @PreRequest
    public static void setupSerializer(RequestData requestData) {
        requestData.getSerializer().include("stories");
    }

    /**
     * Determine the role(s) the logged in user has within the team, if any
     */
    @PreRequest
    public static void determineProjectRole(RequestData requestData) throws Exception {
        User user = requestData.getUser();
        Project project = requestData.getScopeObject(Project.class, false);
        if (user != null && project != null) {
            if (project.getProductOwner().equals(user)) {
                requestData.addScopeRole(ScopeRole.PRODUCT_OWNER);
                requestData.addScopeRole(ScopeRole.TEAM_MEMBER);
            }
        }
    }

    /**
     * Validate whether the project and team that are in scope belong together
     */
    @ProcessScopeObject(Project.class)
    public static void validateProjectInTeam(RequestData requestData, Project project) throws Exception {
        Team team = requestData.getScopeObject(Team.class);
        if (!team.equals(project.getTeam())) {
            throw HTTPException.ERROR_OBJECT_NOT_FOUND;
        }
    }
    /**
     * GET /project or /project/1
     */
    @RequireAuth
    public Object getHandler(RequestData requestData) throws Exception {
        if (requestData.getUrlParams().get("projectId") == null) {
            return Ebean.find(Project.class).findList();
        } else {
            return requestData.getScopeObject(Project.class);
        }
    }

    /**
     * POST /project
     */
    @RequireAuth(role = ScopeRole.SCRUM_MASTER)
    public Object postHandler(RequestData requestData) throws Exception {
        // Create the new project
        Project project;
        try {
            project = new Project(
                requestData.getScopeObject(Team.class),
                Ebean.find(User.class, requestData.getPayload().get("productOwner")),
                (String) requestData.getPayload().get("name"),
                (String) requestData.getPayload().get("description")
            );
            Ebean.save(project);
        } catch (Exception e) {
            e.printStackTrace();
            throw HTTPException.ERROR_BAD_REQUEST;
        }

        // Return the created project
        return project;
    }

    /**
     * PUT /project/1
     */
    @RequireAuth(role = ScopeRole.PRODUCT_OWNER)
    public Object putHandler(RequestData requestData) throws Exception {
        // Get the project
        Project project = requestData.getScopeObject(Project.class);
        Map<String, Object> payload = requestData.getPayload();

        // Change the project
        if (payload.containsKey("productOwner")) {
            project.setProductOwner(Ebean.find(User.class, requestData.getPayload().get("productOwner")));
        }
        if (payload.containsKey("name")) {
            project.setName((String) payload.get("name"));
        }
        if (payload.containsKey("description")) {
            project.setDescription((String) payload.get("description"));
        }

        // Save the changes
        Ebean.save(project);

        // Return the changed project
        return project;
    }

    /**
     * DELETE /project/1
     */
    @RequireAuth(role = ScopeRole.PRODUCT_OWNER)
    public Object deleteHandler(RequestData requestData) throws Exception {
        // Get the project
        Project project = requestData.getScopeObject(Project.class);

        // Delete the project
        Ebean.delete(project);

        // Return nothing
        return null;
    }
}
