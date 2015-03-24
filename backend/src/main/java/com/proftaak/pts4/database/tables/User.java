package com.proftaak.pts4.database.tables;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.proftaak.pts4.core.gson.GsonExclude;
import com.proftaak.pts4.core.restlet.HTTPException;
import com.proftaak.pts4.database.DBTable;
import com.proftaak.pts4.database.DBUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author Michon
 */
@DatabaseTable(tableName = "users")
public class User extends DBTable {

    public static final String FIELD_ID = "id";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_PASSWORD = "password";
    public static final String FIELD_ROLE = "role";

    public static enum UserRole {
        DEVELOPER,
        PRODUCT_OWNER;

        public void require(UserRole... roles) throws HTTPException {
            if (!Arrays.asList(roles).contains(this)) {
                throw HTTPException.ERROR_FORBIDDEN;
            }
        }
    }

    /**
     * The database id of this user
     */
    @DatabaseField(generatedId = true, columnName = FIELD_ID)
    private int id;

    /**
     * The email address of this user
     */
    @DatabaseField(canBeNull = false, unique = true, columnName = FIELD_EMAIL)
    private String email;

    /**
     * The password of this user
     */
    @GsonExclude
    @DatabaseField(canBeNull = false, columnName = FIELD_PASSWORD)
    private String password;

    /**
     * The role of this user
     */
    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = FIELD_ROLE)
    private UserRole role;

    /**
     * The tokens this user has
     */
    @GsonExclude
    @ForeignCollectionField(eager = false)
    private ForeignCollection<Token> tokens;

    /**
     * ORM-Lite no-arg constructor
     */
    public User() {
    }

    /**
     * Create a new user.
     */
    public User(String email, String password, UserRole role) {
        this.setEmail(email);
        this.setPassword(password);
        this.setRole(role);
    }

    public int getId() {
        return this.id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets a new password for the user. Hashed and salts it in the process
     *
     * @param newPassword The new password
     */
    public void setPassword(String newPassword) {
        String salt = BCrypt.gensalt();
        this.password = BCrypt.hashpw(newPassword, salt);
    }

    /**
     * Compares the plaintext password passed to it with the hashed stored version
     *
     * @param pass The plaintext password
     * @return True if they are the same, false otherwise
     */
    public boolean checkPassword(String pass) {
        return BCrypt.checkpw(pass, this.password);
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.getId());
    }

    /**
     * Get the DAO for this table
     *
     * @return The DAO for this table
     */
    public static Dao<User, Integer> getDao() throws FileNotFoundException, SQLException {
        return DaoManager.createDao(DBUtils.getConnectionSource(), User.class);
    }
}