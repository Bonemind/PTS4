package com.proftaak.pts4.database.tables;

import com.proftaak.pts4.core.flexjson.ToPKTransformer;
import com.proftaak.pts4.database.DatabaseModel;
import flexjson.JSON;

import javax.persistence.*;

/**
 * Created by stijn on 7/4/2015.
 */
@Entity
@Table(name = "Tests")
public class Test implements DatabaseModel {
    public enum Status{
        /**
         * Acceptance test is defined
         */
        DEFINED,

        /**
         * Acceptance test has been completed
         */
        ACCEPTED
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
    @JSON(transformer = ToPKTransformer.class)
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_STORY)
    private Story story;

    public Test(Story story, String name, String description,Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.story = story;
    }

    public Test() {
    }

    @Override
    public Object getPK() {
        return this.getId();
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
