package info.flexmojos.compatibilitykit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)
public @interface FlexCompatibility
{

    String minVersion() default "";

    String maxVersion() default ""; 

}
