/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package entities;

import entities.exception.DateException;
import java.io.Serializable;
import java.util.Calendar;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian
 */
@Entity
@XmlRootElement
public class Date implements Serializable {
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int year = 0;
    private int month = 0;
    private int day = 0;
    private int hour = 0;
    private int min = 0;
    private int sec = 0;
    
    public static Date now() {
        Calendar now = Calendar.getInstance();
        Date date = new Date();
        date.year = now.get(Calendar.YEAR);
        date.month = now.get(Calendar.MONTH) + 1;
        date.day = now.get(Calendar.DAY_OF_MONTH);
        date.hour = now.get(Calendar.HOUR_OF_DAY);
        date.min = now.get(Calendar.MINUTE);
        date.sec = now.get(Calendar.SECOND);
        return date;
    }
    
    public Date() {
    }
    
    public Date(int year, int month, int day) throws DateException {
        this.year = year;
        this.month = month;
        this.day = day;
        if(! validDate()) throw new DateException();
    }
    
    public Date(int year, int month, int day, int hour, int min, int sec) throws DateException {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.month = month;
        this.sec = sec;
        if(! valid()) throw new DateException();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) throws DateException {
        this.year = year;
        if(this.month == 0) this.month = 1;
        if(this.day == 0) this.day = 1;
        if(! validDate()) throw new DateException();
    }
    
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) throws DateException {
        this.month = month;
        if(this.day == 0) this.day = 1;
        if(! validDate()) throw new DateException();
    }
    
    public int getDay() {
        return day;
    }
    
    public void setDay(int day) throws DateException {
        this.day = day;
        if(this.month == 0) this.month = 1;
        if(! validDate()) throw new DateException();
    }
    
    public int getHour() {
        return hour;
    }
    
    public void setHour(int hour) throws DateException {
        this.hour = hour;
        if(! validHour()) throw new DateException();
    }
    
    public int getMin() {
        return min;
    }
    
    public void setMin(int min) throws DateException {
        this.min = min;
        if(! validHour()) throw new DateException();
    }
    
    public int getSec() {
        return sec;
    }
    
    public void setSec(int sec) throws DateException {
        this.sec = sec;
        if(! validHour()) throw new DateException();
    }
    
    private boolean validDate() {
        if(this.month == 0 || this.day == 0) {
            return false;
        }
        if (this.month > 12 || this.day > 31
         || this.month <  1 || this.day <  1) {
            return false;
        }
        if (this.month == 2) {
            return this.isBissex() ? this.day <= 29 : this.day <= 28;
        }
        return this.day < 31 || ((this.month - 1) % 7) % 2 == 0;
    }
    
    private boolean validHour() {
        return this.hour < 24 && this.min < 60 && this.sec < 60
            && this.hour >= 0 && this.min >= 0 && this.sec >= 0;
    }
    
    private boolean valid() {
        return this.validDate() && this.validHour();
    }
    
    public boolean isValidDate() {
        return this.validDate();
    }
    
    public boolean isValidHour() {
        return this.validHour();
    }
    
    public boolean isValid() {
        return this.valid();
    }
    
    /*
    *  Depuis l'ajustement du calendrier grégorien, l'année sera bissextile (elle aura 366 jours)1 :
    *  1. si l'année est divisible par 4 et non divisible par 100, ou
    *  2. si l'année est divisible par 400.
    */
    public boolean isBissex() {
        return year % 400 == 0 || year % 4 == 0 && year % 100 != 0;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Date)) {
            return false;
        }
        Date other = (Date) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "entities.Date[ id=" + id + " ]";
    }
    
}
