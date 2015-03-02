package hr.as2.inf.server.annotations;

import hr.as2.inf.common.annotations.copyright.AS2Author;
import hr.as2.inf.common.annotations.copyright.AS2Copyright;
import hr.as2.inf.common.annotations.copyright.AS2Version;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Pattern: Layer Supertype
 * To be inherited by all server side Facade classes.
 * @author zrosko
 * @Copyright 2014
 */
@Target({ElementType.TYPE,ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AS2Copyright
@AS2Author
@AS2Version
public @interface AS2FacadeServer {
	public static enum Type {PASSTHROUGH, CACHEABLE, LOOPBACK}
	public static enum Singleton {YES, NO}
	Type type() default Type.PASSTHROUGH;
	Singleton  singleton() default Singleton.YES;
}