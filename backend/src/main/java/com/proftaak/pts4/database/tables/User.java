package com.proftaak.pts4.database.tables;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.proftaak.pts4.database.DatabaseModel;
import com.proftaak.pts4.database.EbeanEx;
import flexjson.JSON;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michon
 */
@Entity
@Table(name = "users")
public class User implements DatabaseModel<Integer> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_NAME = "name";
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
    @JSON(include = false)
    @Column(name = FIELD_EMAIL, nullable = false, unique = true)
    private String email;

    /**
     * The name of this user
     */
    @Column(name = FIELD_NAME, nullable = false, unique = true)
    private String name;

    /**
     * The password of this user
     */
    @JSON(include = false)
    @Column(name = FIELD_PASSWORD, nullable = false)
    private String password;

    /**
     * The tokens of this user
     */
    @JSON(include = false)
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
    @JSON(include = false)
    @OneToMany
    @JoinColumn(name = Team.FIELD_SCRUM_MASTER)
    private List<Team> ownedTeams = new ArrayList<>();

    /**
     * The projects of which this user is the product owner
     */
    @JSON(include = false)
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
    public User(String email, String name, String password) {
        this.setName(name);
        this.setEmail(email);
        this.setPassword(password);
    }

    @Override
    public Integer getPK() {
        return this.getId();
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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


    /**
     * Find an user by either it's name or it's email address
     *
     * @param identifier The name/email address
     */
    public static User findByNameOrEmail(String identifier) {
        return EbeanEx.find(Ebean.find(User.class)
            .where()
            .or(
                Expr.ieq(User.FIELD_EMAIL, identifier),
                Expr.ieq(User.FIELD_NAME, identifier)
            ).query()
        );
    }

}