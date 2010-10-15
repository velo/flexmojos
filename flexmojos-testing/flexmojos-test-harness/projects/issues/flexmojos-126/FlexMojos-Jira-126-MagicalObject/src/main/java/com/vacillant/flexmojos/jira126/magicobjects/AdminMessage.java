package com.vacillant.flexmojos.jira126.magicobjects;

import com.vacillant.flexmojos.jira126.annotations.MagicObject;
import com.vacillant.flexmojos.jira126.annotations.MagicObject.MagicType;

@MagicObject(MagicType.REROUTABLE)
public class AdminMessage {
    private String message;
    
    public AdminMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
