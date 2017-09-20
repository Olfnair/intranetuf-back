package mail;

import config.ApplicationConfig;
import config.MailConfig;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail {
    private final String subject;
    private final String text;
    private final String recipient;
    private final MailConfig config;
    
    public Mail(MailConfig config, String recipient, String subject, String text) {
        this.config = config;
        this.subject = subject;
        this.text = text;
        this.recipient = recipient;
    }
    
    public Mail(String recipient, String subject, String text) {
        this(ApplicationConfig.MAIL_CONFIG, recipient, subject, text);
    }
    
    public void send() throws MessagingException {
        Session session = Session.getInstance(config.getProps(),
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.getUsername(), config.getPassword());
                    }
                });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(config.getEmail()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(this.recipient));
        message.setSentDate(new Date());
        message.setSubject(this.subject);
        message.setText(this.text);
        
        Transport.send(message);
    }
}