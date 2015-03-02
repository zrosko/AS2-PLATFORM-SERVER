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
 * Pattern: Server invocation interceptor.
 * To be used by invokers.
 * @author zrosko
 * @Copyright 2014
 */
@Target({ElementType.TYPE,ElementType.PACKAGE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AS2Copyright
@AS2Author
@AS2Version
public @interface AS2Interceptor {
	public static enum Type {BEFORE, AFTER}
	Type type() default Type.BEFORE;
}