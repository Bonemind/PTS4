package com.proftaak.pts4.database.tables;

import flexjson.JSON;

import javax.persistence.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michon
 */
@Entity
@Table(name = "teams")
public class Team {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SCRUM_MASTER = "scrum_master";
    public static final String FIELD_USERS = "users";
    public static final String TABLE_JOIN_USER = "teamusers";

    /**
     * The database id of this team
     */
    @Id
    @Column(name = FIELD_ID)
    private int id;

    /**
     * The name of this team
     */
    @Column(name = FIELD_NAME, nullable = false)
    private String name;

    /**
     * The SCRUM master of this team
     */
    @JSON(include = false)
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_SCRUM_MASTER)
    private User scrumMaster;

    /**
     * The users of this team
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = TABLE_JOIN_USER)
    private List<User> users = new ArrayList<>();

    /**
     * ORM-Lite no-arg constructor
     */
    public Team() {
    }

    public Team(String name, User scrumMaster) throws FileNotFoundException {
        this.setName(name);
        this.setScrumMaster(scrumMaster);
        this.users.add(scrumMaster);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getScrumMaster() {
        return scrumMaster;
    }

    public void setScrumMaster(User scrumMaster) {
        this.scrumMaster = scrumMaster;
    }

    public List<User> getUsers() {
        return users;
    }
}