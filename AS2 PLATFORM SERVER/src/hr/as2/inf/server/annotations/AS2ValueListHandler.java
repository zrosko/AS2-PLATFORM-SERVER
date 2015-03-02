package hr.as2.inf.server.annotations;

import hr.as2.inf.common.annotations.copyright.AS2Author;
import hr.as2.inf.common.annotations.copyright.AS2Copyright;
import hr.as2.inf.common.annotations.copyright.AS2Version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Implements design pattern http://www.oracle.com/technetwork/java/valuelisthandler-142464.html
 * to handle result set in parts while sending them to the client.
 * @author zdravko
 *
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
@AS2Copyright
@AS2Author
@AS2Version
public @interface AS2ValueListHandler {

}
