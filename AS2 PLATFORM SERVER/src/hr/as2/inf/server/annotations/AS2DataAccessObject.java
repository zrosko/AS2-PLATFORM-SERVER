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

@Target({ElementType.TYPE,ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@AS2Copyright
@AS2Author
@AS2Version
public @interface AS2DataAccessObject {
	//Nested enumerated types
	public static enum Type {JDBC, AS400, JMS, CICS}
	//Annotation members.
	Type type() default Type.JDBC;
}