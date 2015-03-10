package com.proftaak.pts4.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import com.proftaak.pts4.core.PropertiesUtils;
import com.proftaak.pts4.database.tables.Story;
import com.proftaak.pts4.database.tables.Task;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import org.reflections.Reflections;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtils {
    /**
     * The name of the package holding the tables.
     */
    private static final String TABLES_PACKAGE = "com.proftaak.pts4.database.tables";

    /**
     * The connection source.
     */
    private static JdbcPooledConnectionSource connSource;

    /**
     * Creates an open pooled JDBC connection source
     *
     * @return a pooled connection source
     * @throws java.sql.SQLException Thrown if anything went wrong while connecting to the database
     */
    public static ConnectionSource getConnectionSource() throws SQLException, FileNotFoundException {
        if (connSource == null || !connSource.isOpen()) {
            Properties p = PropertiesUtils.getProperties();
            connSource = new JdbcPooledConnectionSource(p.getProperty("mysql.url"), p.getProperty("mysql.username"), p.getProperty("mysql.password"));
        }
        return connSource;
    }

    /**
     * Recreates all tables, dropping any existing ones in the process
     *
     * @throws java.sql.SQLException  Thrown when connecting to the database fails
     * @throws ClassNotFoundException Thrown when we try to load a class that somehow doesn't exist
     * @throws java.io.IOException
     */
    @SuppressWarnings("unchecked")
    public static void recreateAllTables() throws SQLException, ClassNotFoundException, IOException {
        ConnectionSource connSource = DBUtils.getConnectionSource();
        Reflections r = new Reflections(TABLES_PACKAGE);
        for (Class<?> tableClass : r.getTypesAnnotatedWith(DatabaseTable.class)) {
            TableUtils.dropTable(connSource, tableClass, true);
            TableUtils.createTable(connSource, tableClass);
        }
    }

    /**
     * Writes some test data to the database
     *
     * @throws java.sql.SQLException
     */
    public static void createTestData() throws SQLException, FileNotFoundException {
        Dao<User, Integer> userDao = User.getDao();
        Dao<Token, String> tokenDao = Token.getDao();
        Dao<Story, Integer> storyDao = Story.getDao();
        Dao<Task, Integer> taskDao = Task.getDao();

        User u = new User("test", "test");
        userDao.create(u);

        Token t = new Token(u, "test");
        tokenDao.create(t);

        Story us1 = new Story("Foo");
        storyDao.create(us1);
        Story us2 = new Story("Lorem", "Lorem Ipsum Dolor Sit Amet", SprintStatus.IN_PROGRESS);
        storyDao.create(us2);

        Task t11 = new Task(us1, "Frontend");
        taskDao.create(t11);
        Task t12 = new Task(us1, "Backend", "Do backend stuff");
        taskDao.create(t12);
        Task t21 = new Task(us2, "Frontend");
        taskDao.create(t21);
        Task t22 = new Task(us2, "Backend", null, SprintStatus.ACCEPTED);
        taskDao.create(t22);
    }
}
