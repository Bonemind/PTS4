package com.proftaak.pts4.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.proftaak.pts4.core.database.DBUtils;
import com.proftaak.pts4.core.gson.GsonExclude;
import org.mindrot.jbcrypt.BCrypt;

import java.io.FileNotFoundException;
import java.sql.SQLException;

/**
 * @author Michon
 */
@DatabaseTable(tableName = "users")
public class User {
	/**
	 * The database id of this user
	 */
	@DatabaseField(generatedId = true)
	private int id;

    /**
     * The email address of this user
     */
    @DatabaseField(canBeNull = false, unique = true)
    private String email;

	/**
	 * The password of this user
	 */
	@GsonExclude
	@DatabaseField(canBeNull = false)
	private String password;

	/**
	 * ORM-Lite no-arg constructor
	 */
	public User() {
	}

    /**
     * Create a new user.
     */
    public User(String email, String password) {
        this.setEmail(email);
        this.setPassword(password);
    }

    /**
     * Gets the database id of this user
     * @return The database id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Returns this user's email
     * @return The email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets this user's email
     * @param email The email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

	/**
	 * Sets a new password for the user. Hashed and salts it in the process
	 * @param newPassword The new password
	 */
	public void setPassword(String newPassword) {
		String salt = BCrypt.gensalt();
		this.password = BCrypt.hashpw(newPassword, salt);
	}

	/**
	 * Compares the plaintext password passed to it with the hashed stored version
	 * @param pass The plaintext password
	 * @return True if they are the same, false otherwise
	 */
	public boolean checkPassword(String pass) {
        return BCrypt.checkpw(pass, this.password);
	}

    /**
     * Get the DAO for this table
     * @return The DAO for this table
     */
    public static Dao<User, Integer> getDao() throws FileNotFoundException, SQLException {
        return DaoManager.createDao(DBUtils.getConnectionSource(), User.class);
    }
}