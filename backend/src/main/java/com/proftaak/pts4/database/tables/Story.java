package com.proftaak.pts4.database.tables;

import flexjson.JSON;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michon
 */
@Entity
@Table(name = "stories")
public class Story {
    public enum Status {
        /**
         * Story is defined
         */
        DEFINED,

        /**
         * Story has been started
         */
        IN_PROGRESS,

        /**
         * Story has been completed, is now waiting for verification
         */
        DONE,

        /**
         * Story has been accepted
         */
        ACCEPTED
    }

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_STATUS = "status";

    /**
     * The database id of this userstory
     */
    @Id
    @Column(name = FIELD_ID)
    private int id;

    /**
     * The name of this userstory
     */
    @Column(name = FIELD_NAME, nullable = false)
    private String name;

    /**
     * The description of this userstory
     */
    @Column(name = FIELD_DESCRIPTION)
    private String description;

    /**
     * The status of this userstory
     */
    @Enumerated(EnumType.STRING)
    @Column(name = FIELD_STATUS, nullable = false)
    private Status status;

    /**
     * The tasks of this story
     */
    @OneToMany(cascade = CascadeType.ALL)
    private List<Task> tasks = new ArrayList<>();

    /**
     * ORM-Lite no-arg constructor
     */
    public Story() {
    }

    public Story(String name) {
        this(name, null);
    }

    public Story(String name, String description) {
        this(name, description, Status.DEFINED);
    }

    public Story(String name, String description, Status status) {
        this.setName(name);
        this.setDescription(description);
        this.setStatus(status);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Task> getTasks() {
        return tasks;
    }
}