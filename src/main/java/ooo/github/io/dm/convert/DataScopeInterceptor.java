package ooo.github.io.dm.convert;


import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

/**
 * @author kaiqin
 */
@ConditionalOnProperty(prefix = "ooo.dm.interceptor", name = "enable", havingValue = "true", matchIfMissing = true)
@Intercepts({
        //SQL语句处理器
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}), //预备工作
        //返回集处理器 - 处理查询结果集;
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class}), //处理返回结果
        //参数处理器
        //@Signature(type = ParameterHandler.class, method = "getParameterObject", args = {PreparedStatement.class}), //处理输出参数
        //执行器
        //@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
})
@Component
@Slf4j
public class DataScopeInterceptor extends AbstractSqlParserHandler implements Interceptor {


    private static final String ANTI_QUOTATION_MARKS = "`";

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

    /**
     * 如果是identity，需要转为大写并加引号 todo
     */
    private static final String IDENTITY = "identity";
    private static final String SLASH_IDENTITY_SLASH = "\"identity\"";

    private static final String DATEDIFF_DATEPART = "datediff(DD,";
    private static final String SCHEMA_TABLES = "information_schema.tables";
    private static final String SCHEMA_COLUMNS = "information_schema.columns";


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //目前只拦截达梦数据库SQL语法
        //获取代理对象
        Object handler = PluginUtils.realTarget(invocation.getTarget());
        if (handler instanceof StatementHandler) {
            MetaObject metaObject = SystemMetaObject.forObject(handler);
            this.sqlParser(metaObject);
            //处理SQL
            BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
            String sql = boundSql.getSql();
            String transSql = this.modifySql(sql);
            metaObject.setValue("delegate.boundSql.sql", transSql);
        } else if (handler instanceof ResultSetHandler) {
            //获取返回结果集
            Object result = invocation.proceed();
            Object o = this.formatResultMap(result);
            return this.formatResultMap(o);
        }


        //else if(handler instanceof ParameterHandler){
        //    ParameterHandler parameterHandler = (ParameterHandler) handler;
        //    PreparedStatement ps = (PreparedStatement) invocation.getArgs()[0];
        //
        //    // 反射获取 BoundSql 对象，此对象包含生成的sql和sql的参数map映射
        //    Field boundSqlField = parameterHandler.getClass().getDeclaredField("boundSql");
        //    boundSqlField.setAccessible(true);
        //    BoundSql boundSql = (BoundSql) boundSqlField.get(parameterHandler);
        //
        //    List<String> paramNames = new ArrayList<>();
        //}
        else {
            log.info("无法判断Mybatis执行类：{}", handler.getClass().getName());
        }

        return invocation.proceed();
    }

    /**
     * 处理返回结果集
     *
     * @param result
     */
    private Object formatResultMap(Object result) {
        if (ObjectUtils.isEmpty(result)) {
            return result;
        }
        if (result instanceof ArrayList) {
            ArrayList resultList = (ArrayList) result;
            List<Object> transResultList = new ArrayList<>(resultList.size());
            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i) instanceof Map) {
                    Map<Object, Object> resultMap = (Map<Object, Object>) resultList.get(i);
                    //构建储存结果集Map
                    Map<Object, Object> transResultMap = new HashMap<>(resultMap.size());
                    resultMap.forEach((k, v) -> {
                        if (k instanceof String) {
                            transResultMap.put(((String) k).toLowerCase(), v);
                        }
                    });
                    transResultList.add(transResultMap);
                } else {
                    transResultList.add(resultList.get(i));
                }
            }
            return transResultList;
        }
        return result;
    }

    /**
     * 格式化SQL
     *
     * @param sql 待处理的SQL
     */
    private String modifySql(String sql) {
        String transSql = sql.toLowerCase();
        //判断Sql中是否含有
        if (transSql.contains(ANTI_QUOTATION_MARKS)) {
            log.info("达梦数据库适配-去除反引号[`]！");
            transSql = transSql.replaceAll(ANTI_QUOTATION_MARKS, "\"");

        }
        //将 MYSQL 中 str_to_date() 函数替换为达梦 to_date()，注意时间格式掩码
        if (transSql.contains(STR_TO_DATE)) {
            log.info("达梦数据库适配-替换字符转化时间函数str_to_date()");
            transSql = transSql.replaceAll(STR_TO_DATE_FMT, TO_DATE_FMT).replaceAll(STR_TO_DATE, TO_DATE);
        }

        // Datediff
        if (transSql.contains(DATEDIFF)) {
            log.info("达梦数据库适配-替换字符转化时间函数DATEDIFF()");
            transSql = transSql.replaceAll(DATEDIFF + "\\(", DATEDIFF_DATEPART);
        }

        return transSql;

    }


    /**
     * 生成拦截对象的代理
     * ParameterHandler,ResultSetHandler,StatementHandler,Executor的代理类
     *
     * @param target 目标对象
     * @return 代理对象
     */
    @Override
    public Object plugin(Object target) {
        //把拦截器对象封装成Plugin代理对象
        return Plugin.wrap(target, this);
    }

    /**
     * mybatis配置的属性
     *
     * @param properties mybatis配置的属性
     */
    @Override
    public void setProperties(Properties properties) {

    }


}
