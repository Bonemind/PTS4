package com.proftaak.pts4.database;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.proftaak.pts4.core.PropertiesUtils;
import com.proftaak.pts4.database.tables.*;
import javassist.NotFoundException;

import java.io.FileNotFoundException;
import java.util.Properties;

public class DBUtils {
    /**
     * The name of the package holding the tables
     */
    private static final String TABLE_PACKAGE = "com.proftaak.pts4.database.tables";

    public static void init() throws FileNotFoundException, NotFoundException {
        initEbeanServer();
        createTestData();
    }

    /**
     * Initialize the ebean server
     */
    public static void initEbeanServer() throws FileNotFoundException {
        ServerConfig config = new ServerConfig();
        config.setName("main");

        // Read config
        Properties p = PropertiesUtils.getProperties();
        DataSourceConfig dbConfig = new DataSourceConfig();
        dbConfig.setDriver(p.getProperty("database.driver"));
        dbConfig.setUrl(p.getProperty("database.url"));
        dbConfig.setUsername(p.getProperty("database.username"));
        dbConfig.setPassword(p.getProperty("database.password"));
        config.setDataSourceConfig(dbConfig);

        // Set DDL options..
        config.setDdlGenerate(true);
        config.setDdlRun(true);

        // Register the server as the default server
        config.setDefaultServer(true);
        config.setRegister(true);

        // Create the instance
        EbeanServerFactory.create(config);
    }

    /**
     * Writes some test data to the database
     *
     * @throws java.sql.SQLException
     */
    public static void createTestData() throws FileNotFoundException {
        User u1 = new User("test", "test");
        Ebean.save(u1);
        User u2 = new User("dev", "dev");
        Ebean.save(u2);
        User u3 = new User("po", "po");
        Ebean.save(u3);

        Token tk1 = new Token(u1, "test");
        Ebean.save(tk1);
        Token tk2 = new Token(u2, "dev");
        Ebean.save(tk2);
        Token tk3 = new Token(u3, "po");
        Ebean.save(tk3);

        Team tm = new Team("A-team", u1);
        tm.getUsers().add(u2);
        Ebean.save(tm);

        Story us1 = new Story("Foo");
        Ebean.save(us1);
        Story us2 = new Story("Lorem", "Lorem Ipsum Dolor Sit Amet", Story.Status.IN_PROGRESS);
        Ebean.save(us2);

        Task t11 = new Task(us1, "Frontend");
        Ebean.save(t11);
        Task t12 = new Task(us1, "Backend", "Do backend stuff");
        Ebean.save(t12);
        Task t21 = new Task(us2, "Frontend");
        Ebean.save(t21);
        Task t22 = new Task(us2, "Backend", null, Task.Status.DONE);
        Ebean.save(t22);
    }
}
