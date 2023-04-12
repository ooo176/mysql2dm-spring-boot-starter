package ooo.github.io.dm.convert.sql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 达梦针对mysql的函数处理
 *
 * @author kaiqin
 */
@Slf4j
@Service
public class FunctionHandler implements SqlHandler {

    /**
     * Mysql 的字符转换为日期 -》 Dm to_date(char,fmt)
     */
    private static final String STR_TO_DATE = "str_to_date";

    /**
     * Mysql 的字符转换为日期 -》 Dm to_date(char,fmt)
     */
    private static final String TO_DATE = "to_date";


    /**
     * Mysql 的字符转换为日期 -》 Dm to_date(char,fmt)
     */
    private static final String STR_TO_DATE_FMT = "%Y-%m-%d";

    /**
     * Mysql 的字符转换为日期 -》 Dm to_date(char,fmt)
     */
    private static final String TO_DATE_FMT = "YYYY-MM-DD";

    /**
     * Mysql 中 DATEDIFF(date1, date2) => Dm DATEDIFF(datepart,date1,date2)
     * Mysql 中计算时间边界 DATEDIFF(date1, date2) => Dm DATEDIFF(datepart,date1,date2)
     * datepart 取值：
     * DD：返回两个日期间隔的天数
     * MONTH：
     * WK：
     * MS：
     */
    private static final String DATEDIFF = "datediff";

    private static final String DATEDIFF_DATEPART = "datediff(DD,";

    @Override
    public String handle(String sql) {
        //将 MYSQL 中 str_to_date() 函数替换为达梦 to_date()，注意时间格式掩码
        if (sql.contains(STR_TO_DATE)) {
            log.info("达梦数据库适配-替换字符转化时间函数str_to_date()");
            sql = sql.replaceAll(STR_TO_DATE_FMT, TO_DATE_FMT).replaceAll(STR_TO_DATE, TO_DATE);
        }

        if (sql.contains(DATEDIFF)) {
            log.info("达梦数据库适配-替换字符转化时间函数DATEDIFF()");
            sql = sql.replaceAll(DATEDIFF + "\\(", DATEDIFF_DATEPART);
        }
        return sql;
    }
}
