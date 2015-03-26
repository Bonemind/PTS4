package com.proftaak.pts4.database.tables;

import flexjson.JSON;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michon
 */
@Entity
@Table(name = "users")
public class User implements Serializable {
    public static final String FIELD_ID = "id";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_PASSWORD = "password";
    public static final String TABLE_JOIN_TEAM = Team.TABLE_JOIN_USER;

    /**
     * The database id of this user
     */
    @Id
    @Column(name = FIELD_ID)
    private int id;

    /**
     * The email address of this user
     */
    @Column(name = FIELD_EMAIL, nullable = false, unique = true)
    private String email;

    /**
     * The password of this user
     */
    @JSON(include = false)
    @Column(name = FIELD_PASSWORD, nullable = false)
    private String password;

    /**
     * The tokens of this user
     */
    @OneToMany(cascade = CascadeType.ALL)
    private List<Token> tokens = new ArrayList<>();

    /**
     * The teams to which this user belongs
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = TABLE_JOIN_TEAM)
    private List<Team> teams = new ArrayList<>();

    /**
     * The teams of which this user is the scrum master
     */
    @OneToMany
    @JoinColumn(name = Team.FIELD_SCRUM_MASTER)
    private List<Team> ownedTeams = new ArrayList<>();

    /**
     * The projects of which this user is the product owner
     */
    @OneToMany
    @JoinColumn(name = Project.FIELD_PRODUCT_OWNER)
    private List<Project> ownedProjects = new ArrayList<>();

    /**
     * ORM-Lite no-arg constructor
     */
    public User() {
    }

    /**
     * Create a new user
     */
    public User(String email, String password) {
        this.setEmail(email);
        this.setPassword(password);
    }

    public int getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
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
     * @return this.True if they are the same, false otherwise
     */
    public boolean checkPassword(String pass) {
        return BCrypt.checkpw(pass, this.password);
    }

    public List<Token> getTokens() {
        return this.tokens;
    }

    public List<Team> getTeams() {
        return this.teams;
    }

    public List<Team> getOwnedTeams() {
        return this.ownedTeams;
    }

    public List<Project> getOwnedProjects() {
        return this.ownedProjects;
    }
}