package com.proftaak.pts4.utils;

import com.sun.mail.smtp.SMTPTransport;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * Created by Michon on 25-5-2015.
 */
public class MailUtils {
    /**
     * Send an email
     *
     * @param email The email address to send the email to
     * @param subject The subject line of the email
     * @param message The body of the email
     */
    public static void sendMail(String email, String subject, String message) throws Exception {
        Properties props = PropertiesUtils.getProperties();

        // Create a session
        Session session = Session.getDefaultInstance(new Properties(), null);

        // Create the message
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(props.getProperty("mail.from")));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        msg.setSubject(subject);
        msg.setContent(message, "text/html; charset=utf-8");
        msg.setSentDate(new Date());

        // Send the message
        SMTPTransport t = (SMTPTransport)session.getTransport("smtp");
        t.connect(props.getProperty("mail.host"), Integer.parseInt(props.getProperty("mail.port")), props.getProperty("mail.user"), props.getProperty("mail.pass"));
        t.sendMessage(msg, msg.getAllRecipients());
        t.close();
    }
}
