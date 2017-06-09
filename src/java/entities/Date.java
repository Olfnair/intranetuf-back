/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author Florian
 */
@Entity
public class Date implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int min;
    private int sec;
    
    public Date() {
    }
    
    public Date(int year, int month, int day) throws DateException {
        this.year = year;
        this.month = month;
        this.day = day;
        if(! valid()) throw new DateException();
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
        if(! valid()) throw new DateException();
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) throws DateException {
        this.month = month;
        if(! valid()) throw new DateException();
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) throws DateException {
        this.day = day;
        if(! valid()) throw new DateException();
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) throws DateException {
        this.hour = hour;
        if(! valid()) throw new DateException();
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) throws DateException {
        this.min = min;
        if(! valid()) throw new DateException();
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) throws DateException {
        this.sec = sec;
        if(! valid()) throw new DateException();
    }
    
    private boolean valid() {
        if(this.month > 12 || this.day > 31 || this.hour > 23 || this.min > 59 || this.sec > 59) {
            return false;
        }
        else if(month == 2) {
            if(bissex() && day > 29)
                return false;
            else if(day > 28)
                return false;
        }
        return true;
    }
    
    /*
     *  Depuis l'ajustement du calendrier grégorien, l'année sera bissextile (elle aura 366 jours)1 :
     *  1. si l'année est divisible par 4 et non divisible par 100, ou
     *  2. si l'année est divisible par 400.
     */
    public boolean bissex() {
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
