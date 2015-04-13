package com.proftaak.pts4.database.tables;

import com.proftaak.pts4.core.flexjson.ToPKTransformer;
import com.proftaak.pts4.database.DatabaseModel;
import flexjson.JSON;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michon
 */
@Entity
@Table(name = "tasks")
public class Task implements DatabaseModel {
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
    public static final String FIELD_ESTIMATE = "estimate";
    public static final String FIELD_STORY = "story_id";
    public static final String FIELD_OWNER = "owner";

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
     * The time estimate of this task
     */
    @Column(name = FIELD_ESTIMATE, nullable = false)
    private double estimate = 0;

    /**
     * The user story of this task
     */
    @JSON(transformer = ToPKTransformer.class)
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_STORY)
    private Story story;

    /**
     * The user who owns this task
     */
    @JSON(transformer = ToPKTransformer.class)
    @ManyToOne(optional = true)
    @JoinColumn(name = FIELD_OWNER)
    private User owner;

    /**
     * The progress of this task
     */
    @JSON(include = false)
    @OneToMany
    @JoinColumn(name = TaskProgress.FIELD_TASK)
    private List<TaskProgress> progress = new ArrayList<>();

    /**
     * ORM-Lite no-arg constructor
     */
    public Task() {
    }

    public Task(Story story, User owner, String name, String description, double estimate, Status status) {
        this.story = story;
        this.setName(name);
        this.setDescription(description);
        this.setEstimate(estimate);
        this.setStatus(status);
        this.setOwner(owner);
    }

    @Override
    public Object getPK() {
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

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.trimToNull(description);
    }

    public double getEstimate() {
        return this.estimate;
    }

    public void setEstimate(double estimate) {
        this.estimate = estimate;
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

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getOwner() {
        return this.owner;
    }

    public List<TaskProgress> getProgress() {
        return this.progress;
    }

    /**
     * Get the todo.
     *
     * This is effort - combined task progress.
     * This will never be lower than 0.
     */
    public double getTodo() {
        double done = 0;
        for (TaskProgress progress : this.getProgress()) {
            done += progress.getEffort();
        }
        return Math.max(estimate - done, 0);
    }

    /**
     * Set the todo.
     *
     * This will set effort to combined task progress + new todo.
     */
    public void setTodo(double todo) {
        double done = 0;
        for (TaskProgress progress : this.getProgress()) {
            done += progress.getEffort();
        }
        this.setEstimate(done + todo);
    }
}