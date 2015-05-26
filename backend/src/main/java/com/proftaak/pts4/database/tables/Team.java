package com.proftaak.pts4.database.tables;

import com.proftaak.pts4.database.DatabaseModel;
import com.proftaak.pts4.json.ToPKTransformer;
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
public class Team implements DatabaseModel<Integer> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_SCRUM_MASTER = "scrum_master";
    public static final String FIELD_EFFORT_TRACKING_ENABLED = "effort_tracking";
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
    @JSON(transformer = ToPKTransformer.class)
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_SCRUM_MASTER)
    private User scrumMaster;

    /**
     * The Kanban rules for this team
     */
    @Embedded
    private KanbanRules kanbanRules;

    /**
     * Whether effort tracking is enabled for this team
     */
    @Column(name = FIELD_EFFORT_TRACKING_ENABLED, nullable = false)
    private boolean effortTrackingEnabled = true;

    /**
     * The users of this team
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = TABLE_JOIN_USER)
    private List<User> users = new ArrayList<>();

    /**
     * The projects of this team
     */
    @JSON(include = false)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = Project.FIELD_TEAM)
    private List<Project> projects = new ArrayList<>();

    /**
     * The iterations of this team
     */
    @JSON(include = false)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = Iteration.FIELD_TEAM)
    private List<Iteration> iterations = new ArrayList<>();

    /**
     * The pending invitations for this team
     */
    @JSON(include = false)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = PendingInvitation.FIELD_TEAM)
    private List<PendingInvitation> pendingInvitations = new ArrayList<>();

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

    @Override
    public Integer getPK() {
        return this.getId();
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getScrumMaster() {
        return this.scrumMaster;
    }

    public void setScrumMaster(User scrumMaster) {
        this.scrumMaster = scrumMaster;
    }

    public KanbanRules getKanbanRules() {
        return this.kanbanRules;
    }

    public void setKanbanRules(KanbanRules kanbanRules) {
        this.kanbanRules = kanbanRules;
    }

    public boolean isEffortTrackingEnabled() {
        return this.effortTrackingEnabled;
    }

    public void setEffortTrackingEnabled(boolean effortTrackingEnabled) {
        this.effortTrackingEnabled = effortTrackingEnabled;
    }

    public List<User> getUsers() {
        return this.users;
    }

    public List<Project> getProjects() {
        return this.projects;
    }

    public List<Iteration> getIterations() {
        return this.iterations;
    }

    public List<PendingInvitation> getPendingInvitations() {
        return this.pendingInvitations;
    }
}