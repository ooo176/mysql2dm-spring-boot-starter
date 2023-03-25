package ooo.github.io.dm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * 达梦自动装配类
 *
 * @author qinkai
 */
@Configuration
@ConditionalOnExpression("#{'${db.type}'.equals('dm') or '${db.type}'.equals('dm')}")
@ComponentScan("ooo.github.io")
public class DmAutoConfiguration {


}

