package com.proftaak.pts4.database.tables;

import com.proftaak.pts4.core.flexjson.ToPKTransformer;
import com.proftaak.pts4.core.flexjson.ToStringTransformer;
import com.proftaak.pts4.database.DatabaseModel;
import flexjson.JSON;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michon
 */
@Entity
@Table(name = "stories")
public class Story implements DatabaseModel {
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

    public enum Type {
        /**
         * An user story
         *
         * This is functionality for the end user
         * It is usually described as "As a _ I can _ so that I can _"
         */
        USER_STORY,

        /**
         * A defect
         *
         * This is something that is broken that needs fixing
         */
        DEFECT,

        /**
         * Stuff
         *
         * This is everything that needs to be done that is not either functionality for the end user or a bugfix
         */
        STUFF;
    }

    public static final String FIELD_ID = "id";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_COMPLETED_ON = "completed_on";
    public static final String FIELD_PROJECT = "project_id";
    public static final String FIELD_ITERATION = "iteration_id";
    public static final String FIELD_STORY_POINTS = "story_points";
    public static final String FIELD_PRIORITY = "priority";

    /**
     * The database id of this userstory
     */
    @Id
    @Column(name = FIELD_ID)
    private int id;

    /**
     * The type of this userstory
     */
    @Column(name = FIELD_TYPE)
    @Enumerated(EnumType.STRING)
    private Type type;

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
     * When the userstory was completed
     */
    @JSON(transformer = ToStringTransformer.class)
    @Column(name = FIELD_COMPLETED_ON)
    private LocalDateTime completedOn;

    /**
     * The project this userstory belongs to
     */
    @JSON(transformer = ToPKTransformer.class)
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_PROJECT)
    private Project project;

    /**
     * The iteration this userstory belongs to
     */
    @JSON(transformer = ToPKTransformer.class)
    @ManyToOne
    @JoinColumn(name = FIELD_ITERATION)
    private Iteration iteration;

    /**
     * The amount of story points this story has.
     */
    @Column(name = FIELD_STORY_POINTS)
    private int points;

    /**
     * The priority of this story.
     */
    @Column(name = FIELD_PRIORITY)
    private int priority;

    /**
     * The tasks of this story
     */
    @JSON(include = false)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = Task.FIELD_STORY)
    private List<Task> tasks = new ArrayList<>();

    /**
     * The tests of this story
     */
    @JSON(include = false)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = Test.FIELD_STORY)
    private List<Test> tests = new ArrayList<>();

    /**
     * ORM-Lite no-arg constructor
     */
    public Story() {
    }

    public Story(Project project, Iteration iteration, Type type, String name, String description, Status status, int priority, int storyPoints) {
        this.setProject(project);
        this.setIteration(iteration);
        this.type = type;
        this.setName(name);
        this.setDescription(description);
        this.setStatus(status);
        this.setPriority(priority);
        this.setPoints(storyPoints);
    }

    @Override
    public Object getPK() {
        return this.getId();
    }

    public int getId() {
        return this.id;
    }

    public Type getType() {
        return this.type;
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
        // Update the completedOn field
        switch (status) {
            case DEFINED:
            case IN_PROGRESS:
                this.completedOn = null;
                break;

            case DONE:
            case ACCEPTED:
                if (this.completedOn == null) {
                    this.completedOn = LocalDateTime.now();
                }
                break;
        }

        // Store the new status
        this.status = status;
    }

    public LocalDateTime getCompletedOn() {
        return completedOn;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Iteration getIteration() {
        return this.iteration;
    }

    public void setIteration(Iteration iteration) {
        this.iteration = iteration;
    }

    public List<Task> getTasks() {
        return this.tasks;
    }

    public int getPoints() {
        return this.points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<Test> getTests() {
        return this.tests;
    }
}