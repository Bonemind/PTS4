package com.proftaak.pts4.database.tables;

import flexjson.JSON;

import javax.persistence.*;

/**
 * Created by stijn on 7/4/2015.
 */
@Entity
@Table(name = "Tests")
public class Test {
    public enum Status{
        /**
         * Acceptance test is defined
         */
        DEFINED,

        /**
         * Acceptance test has been completed
         */
        DONE
    }

    public static final String FIELD_ID = "id";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_STORY = "story_id";

    /**
     * The database id of this Test
     */
    @Id
    @Column(name = FIELD_ID)
    private int id;

    /**
     * The name of this Test
     */
    @Column(name = FIELD_NAME, nullable = false)
    private String name;

    /**
     * The description of this Test
     */
    @Column(name = FIELD_DESCRIPTION)
    private String description;

    /**
     * The status of this Test
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

    public Test(Story story, String name, String description,Status status, int id) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.story = story;
    }

    public Test() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
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
        this.description = description;
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

    public void setStory(Story story) {
        this.story = story;
    }
}
