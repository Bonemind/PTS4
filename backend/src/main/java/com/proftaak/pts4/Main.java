package com.proftaak.pts4;

import com.proftaak.pts4.core.restlet.BaseController;
import com.proftaak.pts4.database.DBUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.resource.ServerResource;

import java.util.Collection;
import java.util.List;

/**
 * Created by Michon on 2-3-2015
 */
public class Main extends ServerResource {

    private static final int PORT = 8182;

    public static void main(String[] args) throws Exception {
        // Prepare the database
        DBUtils.init();

        // Prepare the component
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, PORT);

        // Perform routing
        Reflections reflections = new Reflections(BaseController.CONTROLLER_PACKAGE);
        Collection<Class<? extends BaseController>> controllers = reflections.getSubTypesOf(BaseController.class);
        for (Class<? extends BaseController> controller : controllers) {
            // Do the routing
            List<String> path = BaseController.getRoutes(controller);
            component.getDefaultHost().attach("/" + StringUtils.join(path, '/'), controller);
            path.remove(path.size() - 1);
            component.getDefaultHost().attach("/" + StringUtils.join(path, '/'), controller);
        }

        // Start the server
        component.start();
    }
}
