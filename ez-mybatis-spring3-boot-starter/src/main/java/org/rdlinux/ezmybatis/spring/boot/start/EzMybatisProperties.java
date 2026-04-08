package org.rdlinux.ezmybatis.spring.boot.start;

import org.rdlinux.ezmybatis.constant.DbType;
import org.rdlinux.ezmybatis.constant.MapRetKeyCasePolicy;
import org.rdlinux.ezmybatis.constant.NameCasePolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

//@ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
@ConfigurationProperties(prefix = EzMybatisProperties.EZ_MYBATIS_PREFIX)
public class EzMybatisProperties {
    public static final String EZ_MYBATIS_PREFIX = "ez-mybatis";
    /**
     * 数据库类型
     */
    private DbType dbType;
    /**
     * 转义关键词
     */
    private boolean escapeKeyword = true;
    /**
     * 查询结果使用map接收的key转换策略
     */
    private MapRetKeyCasePolicy mapRetKeyCasePolicy;
    /**
     * 表名构建后二次转换策略
     */
    private NameCasePolicy tableNameCasePolicy = NameCasePolicy.ORIGINAL;
    /**
     * 列名构建后二次转换策略
     */
    private NameCasePolicy columnNameCasePolicy = NameCasePolicy.ORIGINAL;
    /**
     * 启用oracle offset fetch分页
     */
    private Boolean enableOracleOffsetFetchPage = false;

    public DbType getDbType() {
        return this.dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public boolean isEscapeKeyword() {
        return this.escapeKeyword;
    }

    public void setEscapeKeyword(boolean escapeKeyword) {
        this.escapeKeyword = escapeKeyword;
    }

    public MapRetKeyCasePolicy getMapRetKeyCasePolicy() {
        return this.mapRetKeyCasePolicy;
    }

    public void setMapRetKeyCasePolicy(MapRetKeyCasePolicy mapRetKeyCasePolicy) {
        this.mapRetKeyCasePolicy = mapRetKeyCasePolicy;
    }

    public NameCasePolicy getTableNameCasePolicy() {
        return this.tableNameCasePolicy;
    }

    public void setTableNameCasePolicy(NameCasePolicy tableNameCasePolicy) {
        this.tableNameCasePolicy = tableNameCasePolicy;
    }

    public NameCasePolicy getColumnNameCasePolicy() {
        return this.columnNameCasePolicy;
    }

    public void setColumnNameCasePolicy(NameCasePolicy columnNameCasePolicy) {
        this.columnNameCasePolicy = columnNameCasePolicy;
    }

    public Boolean getEnableOracleOffsetFetchPage() {
        return this.enableOracleOffsetFetchPage;
    }

    public void setEnableOracleOffsetFetchPage(Boolean enableOracleOffsetFetchPage) {
        this.enableOracleOffsetFetchPage = enableOracleOffsetFetchPage;
    }
}
