package com.proftaak.pts4.database;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.proftaak.pts4.database.tables.*;
import com.proftaak.pts4.utils.PropertiesUtils;
import com.proftaak.pts4.utils.ReflectionUtils;
import javassist.NotFoundException;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        User u1 = new User("test", "Test Person", "test");
        User u2 = new User("dev", "Developer name", "dev");
        User u3 = new User("po", "Our Product owner", "po");
        Ebean.save(u1);
        Ebean.save(u2);
        Ebean.save(u3);

        Token tk1 = new Token(u1, "test");
        Token tk2 = new Token(u2, "dev");
        Token tk3 = new Token(u3, "po");
        Ebean.save(tk1);
        Ebean.save(tk2);
        Ebean.save(tk3);

        Team tm = new Team("A-team", u1);
        tm.getUsers().add(u2);
        Ebean.save(tm);

        Iteration it = new Iteration(tm, LocalDate.now().minusWeeks(1), LocalDate.now().plusWeeks(1), "Sprint 1", null);
        Ebean.save(it);

        Project p = new Project(tm, u3, "PTS4", "Proftaak S4");
        Ebean.save(p);

        Story us1 = new Story(p, null, Story.Type.DEFECT, "Foo", null, Story.Status.DEFINED, 0, 3);
        Story us2 = new Story(p, it, Story.Type.USER_STORY, "Lorem", "Lorem Ipsum Dolor Sit Amet", Story.Status.IN_PROGRESS, 1, 4);
        Story us3 = new Story(p, it, Story.Type.USER_STORY, "Bar", null, Story.Status.DONE, 0, 3);
        Story us4 = new Story(p, it, Story.Type.USER_STORY, "Foo Bar", null, Story.Status.DONE, 1, 4);
        Story us5 = new Story(p, it, Story.Type.USER_STORY, "Stuff", null, Story.Status.DONE, 1, 6);
        try {
            ReflectionUtils.setFieldValue(Story.class, "completedOn", us3, LocalDateTime.now().minusDays(6).minusHours(4));
            ReflectionUtils.setFieldValue(Story.class, "completedOn", us4, LocalDateTime.now().minusDays(4).minusHours(1));
            ReflectionUtils.setFieldValue(Story.class, "completedOn", us5, LocalDateTime.now().minusDays(1).minusHours(5));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        Ebean.save(us1);
        Ebean.save(us2);
        Ebean.save(us3);
        Ebean.save(us4);
        Ebean.save(us5);

        Task t11 = new Task(us1, null, "Frontend", null, 2, Task.Status.DEFINED);
        Task t12 = new Task(us1, u1, "Backend", "Do backend stuff", 3.5, Task.Status.IN_PROGRESS);
        Task t21 = new Task(us2, null, "Frontend", null, 1, Task.Status.DEFINED);
        Task t22 = new Task(us2, null, "Backend", null, 1, Task.Status.DONE);
        Ebean.save(t11);
        Ebean.save(t12);
        Ebean.save(t21);
        Ebean.save(t22);

        Test test1 = new Test(us1, "us1Test", "test some stuff");
        Test test2 = new Test(us1, "us1Test1", "test some stuff");
        test2.setAccepted(true);
        Test test3 = new Test(us2, "us2Test", "test some stuff");
        Ebean.save(test1);
        Ebean.save(test2);
        Ebean.save(test3);
    }
}
