package com.proftaak.pts4.database.tables;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.database.IDatabaseModel;
import com.proftaak.pts4.utils.MailUtils;
import com.proftaak.pts4.utils.PropertiesUtils;

import javax.persistence.*;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by Michon on 25-5-2015.
 */
@Entity
@Table(name = "pending_invitations")
public class PendingInvitation implements IDatabaseModel<Integer> {
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

    public PendingInvitation() {
    }

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

    public static PendingInvitation sendInvite(String email, Team team) {
        try {
            // Check if the email has been invited already
            for (PendingInvitation invitation : team.getPendingInvitations()) {
                if (invitation.getEmail().toLowerCase().equals(email.toLowerCase())) {
                    return null;
                }
            }

            // Create the invite
            PendingInvitation invitation = new PendingInvitation(email, team);
            Ebean.save(invitation);

            // Read the template
            InputStream messageTemplateStream = PendingInvitation.class.getClassLoader().getResourceAsStream("templates/inviteEmailTemplate.txt");
            StringBuilder messageBuilder = new StringBuilder();
            Scanner scanner = new Scanner(messageTemplateStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                messageBuilder.append(line).append("\n");
            }
            scanner.close();

            // Fill the template
            String message = messageBuilder.toString();
            Properties prop = PropertiesUtils.getProperties();
            message = message.replace("{url}", prop.getProperty("general.weburl") + "#/register/" + URLEncoder.encode(email, "UTF-8"));

            // Send the email
            MailUtils.sendMail(email, "Invite to AOYUST", message);

            return invitation;
        } catch (Exception ignored) {
            return null;
        }
    }
}
