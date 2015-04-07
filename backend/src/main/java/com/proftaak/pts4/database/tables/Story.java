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

    public static final String FIELD_ID = "id";
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
    private List<Task> tasks = new ArrayList<>();

    /**
     * The tasks of this story
     */
    @OneToMany(cascade = CascadeType.ALL)
    private List<Task> tests = new ArrayList<>();

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
     * ORM-Lite no-arg constructor
     */
    public Story() {
    }

    public Story(Project project, Iteration iteration, String name, String description, Status status) {
        this(project, iteration, name, description, status, 0);
    }

    public Story(Project project, Iteration iteration, String name, String description, Status status, int priority) {
        this(project, iteration, name, description, status, priority, 0);
    }

    public Story(Project project, Iteration iteration, String name, String description, Status status, int priority, int storyPoints) {
        this.setProject(project);
        this.setIteration(iteration);
        this.setName(name);
        this.setDescription(description);
        this.setStatus(status);
        this.setPriority(priority);
        this.setStoryPoints(storyPoints);
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

    public List<Task> getTests() {
        return this.tests;
    }

}