package com.proftaak.pts4;

import com.proftaak.pts4.database.DBUtils;
import com.proftaak.pts4.rest.SwitchBoard;
import org.glassfish.grizzly.http.server.HttpServer;

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

        // Create the switchboard
        SwitchBoard switchBoard = new SwitchBoard();
        server.getServerConfiguration().addHttpHandler(switchBoard, "/");

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
