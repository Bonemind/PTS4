package com.proftaak.pts4.database.tables;

import org.apache.commons.lang3.StringUtils;

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
     * The project this userstory belongs to
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_PROJECT)
    private Project project;

    /**
     * The iteration this userstory belongs to
     */
    @ManyToOne
    @JoinColumn(name = FIELD_ITERATION)
    private Iteration iteration;

    /**
     * The tasks of this story
     */
    @OneToMany(cascade = CascadeType.ALL)
    private List<Test> tests = new ArrayList<>();

    /**
     * The amount of story points this story has.
     */
    @Column(name = FIELD_STORY_POINTS)
    private int storyPoints;

    /**
     * The priority of this story.
     */
    @Column(name = FIELD_PRIORITY)
    private int priority;

    /**
     * The tasks of this story
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = Task.FIELD_STORY)
    private List<Task> tasks = new ArrayList<>();

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
        this.setStoryPoints(storyPoints);
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
        this.status = status;
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

    public int getStoryPoints() {
        return this.storyPoints;
    }

    public void setStoryPoints(int storyPoints) {
        this.storyPoints = storyPoints;
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