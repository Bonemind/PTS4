package com.proftaak.pts4;

import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.database.DBUtils;
import org.reflections.Reflections;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.resource.ServerResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Michon on 2-3-2015
 */
public class Main extends ServerResource {
    /**
     * The name of the package holding the controllers
     */
    private static final String CONTROLLER_PACKAGE = "com.proftaak.pts4.controllers";

    private static final int PORT = 8182;

    public static void main(String[] args) throws Exception {
        // Prepare the database
        DBUtils.init();

        // Prepare the component
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, PORT);

        // Perform routing
        Reflections reflections = new Reflections(CONTROLLER_PACKAGE);
        Collection<Class<? extends BaseController>> controllers = reflections.getSubTypesOf(BaseController.class);
        for (Class<? extends BaseController> controller : controllers) {
            // Do the routing
            String path = "";
            CRUDController crudController = controller.getAnnotation(CRUDController.class);
            if (crudController != null) {
                List<CRUDController> controllerList = new ArrayList<>();
                controllerList.add(crudController);
                while (crudController != null) {
                    if (crudController.parent() != null && crudController.parent().getAnnotation(CRUDController.class) != null) {
                        crudController = crudController.parent().getAnnotation(CRUDController.class);
                        controllerList.add(crudController);
                    } else {
                        break;
                    }
                }
                path += "/";
                while (controllerList.size() > 1) {
                    crudController = controllerList.remove(controllerList.size() - 1);
                    String name = crudController.name().isEmpty() ? crudController.table().getSimpleName().toLowerCase() : crudController.name();
                    path += name;
                    path += "/";
                    path += "{" + name + "Id}";
                    path += "/";
                }
                String name = controllerList.get(0).name().isEmpty() ? controllerList.get(0).table().getSimpleName().toLowerCase() : controllerList.get(0).name();
                path += name;

                component.getDefaultHost().attach(path, controller);
                component.getDefaultHost().attach(path + "/{" + name + "Id}", controller);
            } else {
                path += controller.getPackage().getName().substring(CONTROLLER_PACKAGE.length()).replace('.', '/');
                path += "/";
                path += controller.getSimpleName().replace("Controller", "").toLowerCase();
                component.getDefaultHost().attach(path, controller);
                component.getDefaultHost().attach(path + "/{id}", controller);
            }

            // Add the controller to the routing
            component.getDefaultHost().attach(path, controller);
            component.getDefaultHost().attach(path + "/{id}", controller);
        }

        // Start the server
        component.start();
    }
}
