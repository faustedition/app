package de.faustedition.graph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface GraphDatabaseTransactional {
	Class<? extends Throwable>[] successfulExceptions() default {};
}
