package com.proftaak.pts4.database.tables;

import flexjson.JSON;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;

/**
 * @author Michon
 */
@Entity
@Table(name = "tasks")
public class Task {
    public enum Status {
        /**
         * Task is defined
         */
        DEFINED,

        /**
         * Task has been started
         */
        IN_PROGRESS,

        /**
         * Task has been completed, is now waiting for verification
         */
        DONE
    }

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_STORY = "story_id";

    /**
     * The database id of this task
     */
    @Id
    @Column(name = FIELD_ID)
    private int id;

    /**
     * The name of this task
     */
    @Column(name = FIELD_NAME, nullable = false)
    private String name;

    /**
     * The description of this task
     */
    @Column(name = FIELD_DESCRIPTION)
    private String description;

    /**
     * The status of this task
     */
    @Column(name = FIELD_STATUS, nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * The user story of this task
     */
    @JSON(include = false)
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_STORY)
    private Story story;

    /**
     * ORM-Lite no-arg constructor
     */
    public Task() {
    }

    public Task(Story story, String name) {
        this(story, name, null);
    }

    public Task(Story story, String name, String description) {
        this(story, name, description, Status.DEFINED);
    }

    public Task(Story story, String name, String description, Status status) {
        this.story = story;
        this.setName(name);
        this.setDescription(description);
        this.setStatus(status);
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

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.trimToNull(description);
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Story getStory() {
        return this.story;
    }
}