package com.proftaak.pts4.database.tables;

import com.proftaak.pts4.utils.flexjson.ToPKTransformer;
import com.proftaak.pts4.database.DatabaseModel;
import flexjson.JSON;

import javax.persistence.*;

/**
 * Created by stijn on 7/4/2015.
 */
@Entity
@Table(name = "Tests")
public class Test implements DatabaseModel {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_ACCEPTED = "accepted";
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
    @Column(name = FIELD_ACCEPTED, nullable = false)
    private boolean accepted = false;

    /**
     * The user story of this task
     */
    @JSON(transformer = ToPKTransformer.class)
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_STORY)
    private Story story;

    public Test() {
    }

    public Test(Story story, String name, String description) {
        this.name = name;
        this.description = description;
        this.story = story;
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

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public Story getStory() {
        return this.story;
    }

    public void setStory(Story story) {
        this.story = story;
    }
}
