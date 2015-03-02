package hr.as2.inf.server.annotations.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AS2FacadeMethodSecured {

	public String methodName();

	public int arguments() default 0;

	public Class<? extends Exception> expected() default java.lang.Exception.class;

	public String Author();

}