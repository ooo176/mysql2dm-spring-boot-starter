package ooo.github.io.dm.convert.sql;

/**
 * sql处理器
 *
 * @author kaiqin
 */
public interface SqlHandler {

    /**
     * 处理sql
     *
     * @param sql 返回处理后的sql
     * @return 处理后的sql
     */
    String handle(String sql);
}
