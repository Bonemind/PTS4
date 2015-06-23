package com.proftaak.pts4.database.tables;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Query;
import com.proftaak.pts4.database.IDatabaseModel;
import com.proftaak.pts4.json.ToPKTransformer;
import flexjson.JSON;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michon
 */
@Entity
@Table(name = "projects")
public class Project implements IDatabaseModel<Integer> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_TEAM = "team_id";
    public static final String FIELD_PRODUCT_OWNER = "product_owner_id";

    /**
     * The database id of this project
     */
    @Id
    @Column(name = FIELD_ID)
    private int id;

    /**
     * The name of this project
     */
    @Column(name = FIELD_NAME, nullable = false)
    private String name;

    /**
     * The description of this project
     */
    @Column(name = FIELD_DESCRIPTION)
    private String description;

    /**
     * The team this project belongs to
     */
    @JSON(transformer = ToPKTransformer.class)
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_TEAM)
    private Team team;

    /**
     * The product owner of this project
     */
    @JSON(transformer = ToPKTransformer.class)
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_PRODUCT_OWNER)
    private User productOwner;

    /**
     * The user stories of this project
     */
    @JSON(include = false)
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = Story.FIELD_PROJECT)
    private List<Story> stories = new ArrayList<>();

    /**
     * ORM-Lite no-arg constructor
     */
    public Project() {
    }

    public Project(Team team, User productOwner, String name, String description) {
        this.setTeam(team);
        this.setProductOwner(productOwner);
        this.setName(name);
        this.setDescription(description);
    }

    @Override
    public Integer getPK() {
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

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public List<Story> getStories() {
        return this.stories;
    }

    public User getProductOwner() {
        return this.productOwner;
    }

    public void setProductOwner(User productOwner) {
        this.productOwner = productOwner;
    }

    /**
     * Build a query to get all projects to which a given user has access
     */
    public static Query<Project> queryForUser(User user) {
        Query<Project> query = Ebean.createQuery(Project.class);
        query.where().or(
                Expr.eq(Project.FIELD_PRODUCT_OWNER, user.getId()),
                Expr.in(Project.FIELD_TEAM, Team.queryForUser(user).select(Team.FIELD_ID))
        );
        return query;
    }
}