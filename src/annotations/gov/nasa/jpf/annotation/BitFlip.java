package gov.nasa.jpf.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Pu Yi <lukeyi@pku.edu.cn>
 *
 * An annotation used to specify fields/parameters to perform bit flipping exploration
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE })
public @interface BitFlip {
  int value() default 1;
}
