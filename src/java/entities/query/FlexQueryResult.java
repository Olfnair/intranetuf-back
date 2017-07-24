/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.query;

import entities.*;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 *
 * @author Florian
 * @param <T>
 */
@XmlRootElement
@XmlSeeAlso({
    Credentials.class,
    File.class,
    Log.class,
    Project.class,
    ProjectRight.class,
    User.class,
    Version.class,
    WorkflowCheck.class
})
public class FlexQueryResult<T> {
    private Long totalCount = -1L;
    private List<T> list;
    
    public FlexQueryResult() {
    }
    
    public FlexQueryResult(List<T> list) {
        this.list = list;
    }
    
    public FlexQueryResult(List<T> list, Long totalCount) {
        this.list = list;
        this.totalCount = totalCount;
    }

    public Long getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
    
    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }  
}
