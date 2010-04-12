package org.sonatype.flexmojos;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class SimpleEntity {

    @Column
    private String name;
    @Column
    private Double value;
    
    public SimpleEntity() {
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
}
