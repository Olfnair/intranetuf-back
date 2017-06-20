package mail;

import files.Config;
import files.ConfigFile;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender {
    private static String username;
    private static String password;
    private static final Properties PROPS = new Properties();
    
    private final String subject;
    private final String text;
    private final String recipient;
    
    static {
        ConfigFile configFile = new ConfigFile(Config.PROPERTIES_LOCATION + '/' + "mail.properties");
        try {
            MailSender.configUser(configFile.read("username"), configFile.read("password"));
            MailSender.configServer(configFile.read("host"), configFile.read("port"));
        } catch (IOException ex) {
            Logger.getLogger(MailSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void configUser(String username, String password) {
        MailSender.username = username;
        MailSender.password = password;
    }
    
    public static void configServer(String host, String port) {
        MailSender.PROPS.put("mail.smtp.auth", "true");
        MailSender.PROPS.put("mail.smtp.starttls.enable", "true");
        MailSender.PROPS.put("mail.smtp.host", host); // smtp.gmail.com
        MailSender.PROPS.put("mail.smtp.port", port); // 587
    }
    
    public MailSender(String recipient, String subject, String text) {
        this.subject = subject;
        this.text = text;
        this.recipient = recipient;
    }
    
    public void send() throws MessagingException {
        Session session = Session.getInstance(MailSender.PROPS,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(MailSender.username, MailSender.password);
                    }
                });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(MailSender.username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(this.recipient));
        message.setSubject(this.subject);
        message.setText(this.text);
        
        Transport.send(message);
    }
}