package com.proftaak.pts4;

import com.proftaak.pts4.core.rest.Router;
import com.proftaak.pts4.database.DBUtils;
import org.glassfish.grizzly.http.server.*;

import java.net.InetSocketAddress;

/**
 * Created by Michon on 2-3-2015
 */
public class Main {

    private static final int PORT = 8182;

    public static void main(String[] args) throws Exception {
        // Prepare the database
        DBUtils.init();

        // Create the server
        HttpServer server = HttpServer.createSimpleServer("/", Main.PORT);

        // Create the router.
        Router router = new Router();
        server.getServerConfiguration().addHttpHandler(router, "/");

        // Run the server
        try {
            server.start();
            System.out.println("Press any key to stop the server...");
            System.in.read();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
