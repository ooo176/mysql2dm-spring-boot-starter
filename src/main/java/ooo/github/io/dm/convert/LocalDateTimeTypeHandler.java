package ooo.github.io.dm.convert;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.time.LocalDateTime;


/**
 * 达梦数据库兼容LocalDateTime
 * <p>
 * https://blog.csdn.net/imyc7/article/details/118824310
 *
 * @author kaiqin
 */
public class LocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {
    public LocalDateTimeTypeHandler() {
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setTimestamp(i, null);
            return;
        }
        ps.setTimestamp(i, Timestamp.valueOf(parameter));
    }


    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
