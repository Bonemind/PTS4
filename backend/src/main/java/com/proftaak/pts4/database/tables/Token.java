package com.proftaak.pts4.database.tables;

import com.proftaak.pts4.database.DatabaseModel;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * @author Michon
 */
@Entity
@Table(name = "tokens")
public class Token implements DatabaseModel<String> {
    public static final String FIELD_TOKEN = "token";
    public static final String FIELD_USER = "user_id";
    public static final String FIELD_DATE_CREATION = "date_creation";

    /**
     * The time a token stays valid, in milliseconds
     */
    public static final int TIME_TO_LIVE = 1000 * 60 * 60 * 24;

    /**
     * The token
     */
    @Id
    @Column(name = FIELD_TOKEN)
    private String token;

    /**
     * The user
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = FIELD_USER)
    private User user;

    /**
     * Date of creation of the token, used for TTL
     */
    @Column(name = FIELD_DATE_CREATION, nullable = false)
    private Date dateCreation;

    /**
     * ORM-Lite no-arg constructor
     */
    public Token() {
    }

    /**
     * Create a new token
     *
     * @param user The user to whom the token belongs
     */
    public Token(User user) {
        this.token = UUID.randomUUID().toString();
        this.user = user;
        this.dateCreation = new Date();
    }

    public Token(User user, String token) {
        this(user);
        this.token = token;
    }

    @Override
    public String getPK() {
        return this.getToken();
    }

    public String getToken() {
        return this.token;
    }

    public User getUser() {
        return this.user;
    }

    public Date getDateCreation() {
        return this.dateCreation;
    }

    /**
     * Check whether the token is still valid
     *
     * @return this.True if the token is still valid, False otherwise
     */
    public boolean isValid() {
        long tokenEndTimestamp = this.dateCreation.getTime() + Token.TIME_TO_LIVE;
        long currentTimestamp = new Date().getTime();
        return currentTimestamp < tokenEndTimestamp;
    }
}