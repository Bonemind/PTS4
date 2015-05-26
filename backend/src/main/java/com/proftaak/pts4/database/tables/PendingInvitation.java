package com.proftaak.pts4.database.tables;

import com.proftaak.pts4.database.DatabaseModel;

import javax.persistence.*;

/**
 * Created by Michon on 25-5-2015.
 */
@Entity
@Table(name = "pending_invitations")
public class PendingInvitation implements DatabaseModel<Integer> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_TEAM = "team";

    /**
     * The database id of this invitation
     */
    @Id
    @Column(name = FIELD_ID)
    private int id;

    /**
     * The invited email address
     */
    @Column(name = FIELD_EMAIL, nullable = false)
    private String email;

    /**
     * The team the email address was invited to
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_TEAM)
    private Team team;

    public PendingInvitation() {}

    public PendingInvitation(String email, Team team) {
        this.email = email;
        this.team = team;
    }

    public Integer getPK() {
        return this.getId();
    }

    public int getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public Team getTeam() {
        return this.team;
    }
}
