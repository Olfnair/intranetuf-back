/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.exceptions;

/**
 *
 * @author Florian
 */
public class DateException extends Exception {
    public DateException() {
        super();
    }
    
    public DateException(String message) {
        super(message);
    }  
}
