package hr.as2.inf.server.annotations;

import hr.as2.inf.common.annotations.copyright.AS2Author;
import hr.as2.inf.common.annotations.copyright.AS2Copyright;
import hr.as2.inf.common.annotations.copyright.AS2Version;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Business object can be:
 * PASSTHROUGH - a simple no state object to
 * pass data from client to persistence layer only
 * CACHEABLE - an object that keep cached values in memory
 * and has a mechanism to reset the cache. 
 * LOOPBACK - an object that do not pass control to the 
 * persistence layer at all.
 */
@Target({ ElementType.TYPE, ElementType.PACKAGE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@AS2Copyright
@AS2Author
@AS2Version
public @interface AS2BusinessObject {
	// Nested enumerated types
	public static enum Type {
		PASSTHROUGH, CACHEABLE, LOOPBACK
	}

	public static enum Singleton {
		YES, NO
	}

	// Annotation members.
	Type type() default Type.PASSTHROUGH;

	Singleton singleton() default Singleton.YES;
}