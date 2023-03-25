package ooo.github.io.dm.datasource;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author kaiqin
 */
@Data
@Component
@ConfigurationProperties(DbConfig.PREFIX)
public class DbConfig {

    public static final String PREFIX = "ooo";

    private DbConfigDTO mysql;

    private DbConfigDTO dm8;

}
