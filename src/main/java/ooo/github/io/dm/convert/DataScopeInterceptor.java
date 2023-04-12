package ooo.github.io.dm.convert;


import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import lombok.extern.slf4j.Slf4j;
import ooo.github.io.dm.convert.sql.SqlHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

/**
 * @author fengwang26
 * @version 1.0
 * mybatis-plus 全局拦截器
 */
@ConditionalOnProperty(prefix = "iflytek.dm.interceptor", name = "enable", havingValue = "true", matchIfMissing = true)
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

    /**
     * 如果是identity，需要转为大写并加引号 todo
     */
    private static final String IDENTITY = "identity";
    private static final String SLASH_IDENTITY_SLASH = "\"identity\"";


    @Autowired
    private List<SqlHandler> sqlHandlers;


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
        } else {
            log.info("无法判断Mybatis执行类：{}", handler.getClass().getName());
        }

        return invocation.proceed();
    }

    /**
     * 处理返回结果集
     *
     * @param result 入参
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
        for (SqlHandler sqlHandler : sqlHandlers) {
            transSql = sqlHandler.handle(transSql);
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
