/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florian
 */
public class MailConfig extends Config {
    private final String username;
    private final String password;
    private final String email;
    private final Properties props = new Properties();
    
    public MailConfig(String filepath) {
        super(filepath);
        String usernameTmp = null;
        String passwordTmp = null;
        String emailTmp = null;
        try {
            ConfigFile config = getConfigFile();
            usernameTmp = config.read("username");
            passwordTmp = config.read("password");
            emailTmp = config.read("user");
            configServer();
        } catch (IOException ex) {
            Logger.getLogger(MailConfig.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            username = usernameTmp;
            password = passwordTmp;
            email = emailTmp;
        }
    }
    
    private void configServer(
            String host, String port, String user, String password, String auth, String tls, String ssl
    ) {
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", auth);
        if(tls.equals("true")) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        else if(ssl.equals("true")) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
            props.put("mail.smtp.socketFactory.fallback", "false");
        }
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.user", user);
        props.put("mail.smtp.password", password);
    }
    
    private void configServer() throws IOException {
        ConfigFile config = getConfigFile();
        configServer(config.read("host"), config.read("port"), config.read("user"),
                config.read("password"), config.read("auth"), config.read("starttls"), config.read("ssl"));
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Properties getProps() {
        return props;
    }   
}
