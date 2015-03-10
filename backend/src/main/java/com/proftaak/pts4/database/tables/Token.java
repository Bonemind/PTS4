package com.proftaak.pts4.database.tables;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.proftaak.pts4.database.DBUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

/**
 * @author Michon
 */
@DatabaseTable(tableName = "tokens")
public class Token {

    /**
     * The time a token stays valid, in milliseconds.
     */
    public static final int TIME_TO_LIVE = 1000 * 60 * 60 * 24;

    /**
     * The token
     */
    @DatabaseField(id = true)
    private String token;

	/**
	 * The user
	 */
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	private User user;

	/**
	 * Date of creation of the token, used for TTL
	 */
	@DatabaseField(dataType = DataType.DATE)
	private Date creationDate;

	/**
	 * ORM-Lite no-arg constructor
	 */
	public Token() {
	}

    /**
     * Create a new token.
     * @param user The user to whom the token belongs
     */
    public Token(User user) {
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.creationDate = new Date();
    }
    public Token(User user, String token) {
        this(user);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    /**
     * Check whether the token is still valid
     * @return True if the token is still valid, False otherwise
     */
    public boolean isValid() {
        long tokenEndTimestamp = this.creationDate.getTime() + Token.TIME_TO_LIVE;
        long currentTimestamp = new Date().getTime();
        return currentTimestamp < tokenEndTimestamp;
    }

    /**
     * Get the DAO for this table
     * @return The DAO for this table
     */
    public static Dao<Token, String> getDao() throws FileNotFoundException, SQLException {
        return DaoManager.createDao(DBUtils.getConnectionSource(), Token.class);
    }
}