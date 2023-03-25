package ooo.github.io.dm.datasource;

import lombok.Data;

/**
 * db配置对象
 *
 * @author kaiqin
 */
@Data
public class DbConfigDTO {

    private String ip;

    private String port;

    private String driverClassName;

    private String database;

    private String username;

    private String password;

    private String url;

}
