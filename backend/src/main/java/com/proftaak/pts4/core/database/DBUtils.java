package com.proftaak.pts4.core.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import com.proftaak.pts4.core.Config;
import com.proftaak.pts4.database.User;
import org.reflections.Reflections;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtils {
    /**
     * The name of the package holding the tables.
     */
    private static final String TABLES_PACKAGE = "com.proftaak.pts4.database";

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
            Properties p = Config.getProperties();
			connSource = new JdbcPooledConnectionSource(p.getProperty("mysql.url"), p.getProperty("mysql.username"), p.getProperty("mysql.password"));
		}
		return connSource;
	}

	/**
	 * Recreates all tables, dropping any existing ones in the process
     *
	 * @throws java.sql.SQLException  Thrown when connecting to the database fails
	 * @throws ClassNotFoundException  Thrown when we try to load a class that somehow doesn't exist
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
	 * @throws java.sql.SQLException
	 */
	public static void createTestData() throws SQLException, FileNotFoundException {
		Dao<User, Integer> userDao = User.getDao();

		User u = new User("test", "test");
		userDao.create(u);
	}
}
