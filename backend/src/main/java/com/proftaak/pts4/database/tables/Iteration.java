package com.proftaak.pts4.database.tables;

import com.proftaak.pts4.core.flexjson.ToStringTransformer;
import flexjson.JSON;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michon on 6-4-2015.
 */
@Entity
@Table(name = "iterations")
public class Iteration {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_START = "start";
    public static final String FIELD_END = "end";
    public static final String FIELD_TEAM = "team_id";

    /**
     * The database id of this iteration
     */
    @Id
    @Column(name = FIELD_ID)
    private int id;

    /**
     * The name of this iteration
     */
    @Column(name = FIELD_NAME, nullable = false)
    private String name;

    /**
     * The description of this iteration
     */
    @Column(name = FIELD_DESCRIPTION)
    private String description;

    /**
     * The start of this iteration
     */
    @JSON(transformer = ToStringTransformer.class)
    @Column(name = FIELD_START, nullable = true)
    private LocalDate start;

    /**
     * The end of this iteration
     */
    @JSON(transformer = ToStringTransformer.class)
    @Column(name = FIELD_END, nullable = true)
    private LocalDate end;

    /**
     * The team this iteration belongs to
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_TEAM)
    private Team team;

    /**
     * The user stories of this iteration
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = Story.FIELD_ITERATION)
    private List<Story> stories = new ArrayList<>();

    public Iteration() {
    }

    public Iteration(Team team, LocalDate start, LocalDate end, String name, String description) {
        this.setTeam(team);
        this.setStart(start);
        this.setEnd(end);
        this.setName(name);
        this.setDescription(description);
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
        this.description = description;
    }

    public LocalDate getStart() {
        return this.start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return this.end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public List<Story> getStories() {
        return this.stories;
    }
}
