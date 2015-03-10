package com.proftaak.pts4;

import com.proftaak.pts4.database.DBUtils;
import com.proftaak.pts4.core.restlet.BaseController;
import org.reflections.Reflections;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.resource.ServerResource;

import java.util.Set;

/**
 * Created by Michon on 2-3-2015.
 */
public class Main extends ServerResource {
    /**
     * The name of the package holding the controllers.
     */
    private static final String CONTROLLER_PACKAGE = "com.proftaak.pts4.controllers";

    public static void main(String[] args) throws Exception {
        // Prepare the database.
        DBUtils.recreateAllTables();
        DBUtils.createTestData();

        // Prepare the component.
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);

        // Perform routing.
        Reflections reflections = new Reflections(CONTROLLER_PACKAGE);
        Set<Class<? extends BaseController>> controllers = reflections.getSubTypesOf(BaseController.class);
        for (Class<? extends BaseController> controller : controllers) {
            // Determine the path of this controller.
            String path = "";
            path += controller.getPackage().getName().substring(CONTROLLER_PACKAGE.length()).replace('.', '/');
            path += "/";
            path += controller.getSimpleName().replace("Controller", "").toLowerCase();

            // Add the controller to the routing.
            component.getDefaultHost().attach(path, controller);
            component.getDefaultHost().attach(path + "/{id}", controller);
        }

        // Start the server.
        component.start();
    }
}
