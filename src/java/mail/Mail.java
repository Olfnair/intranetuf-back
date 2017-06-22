package mail;

import config.ApplicationConfig;
import config.ConfigFile;
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

public class Mail {
    private static String username;
    private static String password;
    private static final Properties PROPS = new Properties();
    
    private final String subject;
    private final String text;
    private final String recipient;
    
    static {
        ConfigFile configFile = new ConfigFile(ApplicationConfig.PROPERTIES_LOCATION + '/' + "mail.properties");
        try {
            Mail.configUser(configFile.read("username"), configFile.read("password"));
            Mail.configServer(configFile);
        } catch (IOException ex) {
            Logger.getLogger(Mail.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void configUser(String username, String password) {
        Mail.username = username;
        Mail.password = password;
    }
    
    public static void configServer(ConfigFile config) throws IOException {
        Mail.PROPS.put("mail.transport.protocol", "smtp");
        Mail.PROPS.put("mail.smtp.auth", config.read("auth"));
        if(config.read("starttls").equals("true")) {
            Mail.PROPS.put("mail.smtp.starttls.enable", "true");
        }
        else if(config.read("ssl").equals("true")) {
            Mail.PROPS.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
            Mail.PROPS.put("mail.smtp.socketFactory.fallback", "false");
        }
        Mail.PROPS.put("mail.smtp.host", config.read("host")); // smtp.gmail.com
        Mail.PROPS.put("mail.smtp.port", config.read("port")); // 587
        Mail.PROPS.put("mail.smtp.user", config.read("user"));
        Mail.PROPS.put("mail.smtp.password", config.read("password"));
    }
    
    public Mail(String recipient, String subject, String text) {
        this.subject = subject;
        this.text = text;
        this.recipient = recipient;
    }
    
    public void send() throws MessagingException {
        Session session = Session.getInstance(Mail.PROPS,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(Mail.username, Mail.password);
                    }
                });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(Mail.username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(this.recipient));
        message.setSubject(this.subject);
        message.setText(this.text);
        
        Transport.send(message);
    }
}