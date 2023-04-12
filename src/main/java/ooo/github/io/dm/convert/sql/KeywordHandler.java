package ooo.github.io.dm.convert.sql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 关键字处理，达梦的关键字目前已知的有desc,type
 * 需要在业务侧加上@TableField(value = "`desc`")
 *
 * <p>
 * select  id,code,parent_code,NAME,"DESC","TYPE",sort,table_name,table_desc,create_by,create_time,update_by,update_time  from tb_aaa
 * 对于达梦数据库来说，关键字需要双引号包裹起来，并且需要转为大写（mysql只需要用``包裹即可）
 *
 * @author kaiqin
 */
@Service
@Slf4j
public class KeywordHandler implements SqlHandler {

    private static final String ANTI_QUOTATION_MARKS = "`";

    private static final String TYPE_LOWER_CASE = "\"type\"";

    private static final String DESC_LOWER_CASE = "\"desc\"";

    private static final String DESC_UPPER_CASE = "\"DESC\"";

    private static final String TYPE_UPPER_CASE = "\"TYPE\"";

    @Override
    public String handle(String sql) {
        if (sql == null) {
            return null;
        }

        if (sql.contains(ANTI_QUOTATION_MARKS)) {
            log.info("达梦数据库适配-去除反引号[`]！");
            sql = sql.replaceAll(ANTI_QUOTATION_MARKS, "\"");
            String[] sqlSplit = sql.split("");
            StringBuilder finalSql = new StringBuilder();
            boolean start = false;
            log.info("达梦数据库关键字适配-关键字转写");
            for (String each : sqlSplit) {
                if (Objects.equals(each, "\"")) {
                    start = !start;
                }
                if (start) {
                    finalSql.append(each.toUpperCase());
                } else {
                    finalSql.append(each);
                }
            }
            return finalSql.toString();
        }
        return sql;
    }
}
