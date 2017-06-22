/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package mail;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

/**
 *
 * @author Florian
 */
public class SendMailThread extends Thread {
    private final Mail mail;
    
    public SendMailThread(Mail mail) {
        super();
        this.mail = mail;
    }
    
    @Override
    public void run() {
        try {
            mail.send();
        } catch (MessagingException ex) {
            Logger.getLogger(SendMailThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
