package annotationProcessor;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Sublayout {
    String[] args() default {};

    int length() default -1;

    int repeated() default -1;
}
