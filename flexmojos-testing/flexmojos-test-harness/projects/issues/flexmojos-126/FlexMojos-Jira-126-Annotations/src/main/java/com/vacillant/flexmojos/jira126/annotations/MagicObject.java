package com.vacillant.flexmojos.jira126.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface MagicObject {
    public enum MagicType { NO_MAGIC, REROUTABLE, TRANSIENT }
    
    MagicType value() default MagicType.NO_MAGIC;
}
