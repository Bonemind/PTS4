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
    @ManyToOne
    @JoinColumn(name = FIELD_PROJECT)
    private Project project;

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

    public Story(Project project, String name) {
        this(project, name, null);
    }

    public Story(Project project, String name, String description) {
        this(project, name, description, Status.DEFINED);
    }

    public Story(Project project, String name, String description, Status status) {
        this.setProject(project);
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

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Task> getTasks() {
        return this.tasks;
    }
}