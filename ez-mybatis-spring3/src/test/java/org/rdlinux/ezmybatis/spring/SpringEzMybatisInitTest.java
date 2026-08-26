package org.rdlinux.ezmybatis.spring;

import org.apache.ibatis.session.Configuration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.rdlinux.ezmybatis.EzMybatisConfig;
import org.rdlinux.ezmybatis.constant.DbType;
import org.rdlinux.ezmybatis.core.EzMybatisContent;
import org.rdlinux.ezmybatis.core.sqlstruct.table.DbTable;
import org.rdlinux.ezmybatis.core.sqlstruct.table.DynamicTableResolver;
import org.rdlinux.ezmybatis.core.sqlstruct.table.PhysicalTableRoute;
import org.rdlinux.ezmybatis.core.sqlstruct.table.TableRouteResolver;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Spring3 动态物理表路由 Bean 注入测试。
 */
public class SpringEzMybatisInitTest {
    /**
     * 清理测试期间注册的 MyBatis 配置。
     */
    @After
    public void tearDown() {
        EzMybatisContent.destroyAll();
    }

    /**
     * 验证 Spring 容器中的动态路由 Bean 会注入当前 MyBatis 配置。
     */
    @Test
    public void shouldInjectDynamicTableResolverBean() {
        DynamicTableResolver resolver = context -> PhysicalTableRoute.of("tenant_01", "user_2026", null);
        AnnotationConfigApplicationContext applicationContext = createContext("dynamicTableResolver", resolver);
        Configuration configuration = new Configuration();
        EzMybatisConfig config = createConfig(configuration);
        try {
            SpringEzMybatisInit.init(config, applicationContext);

            Assert.assertSame(resolver, EzMybatisContent.getDynamicTableResolver(configuration));
            PhysicalTableRoute route = TableRouteResolver.resolve(configuration, DbTable.of("user"));
            Assert.assertEquals("tenant_01", route.getSchema());
            Assert.assertEquals("user_2026", route.getTableName());
        } finally {
            applicationContext.close();
        }
    }

    /**
     * 验证 Spring 容器中存在多个动态路由 Bean 时初始化会失败。
     */
    @Test
    public void shouldRejectMultipleDynamicTableResolverBeans() {
        DynamicTableResolver firstResolver = context -> null;
        DynamicTableResolver secondResolver = context -> null;
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.registerBean("firstResolver", DynamicTableResolver.class, () -> firstResolver);
        applicationContext.registerBean("secondResolver", DynamicTableResolver.class, () -> secondResolver);
        applicationContext.refresh();
        try {
            try {
                SpringEzMybatisInit.init(createConfig(new Configuration()), applicationContext);
                Assert.fail("Multiple DynamicTableResolver beans should be rejected");
            } catch (IllegalStateException e) {
                Assert.assertTrue(e.getMessage().contains("firstResolver"));
                Assert.assertTrue(e.getMessage().contains("secondResolver"));
            }
        } finally {
            applicationContext.close();
        }
    }

    /**
     * 创建包含动态路由 Bean 的 Spring 应用上下文。
     *
     * @param beanName Bean 名称
     * @param resolver 动态路由器
     * @return 已完成刷新的 Spring 应用上下文
     */
    private AnnotationConfigApplicationContext createContext(String beanName, DynamicTableResolver resolver) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.registerBean(beanName, DynamicTableResolver.class, () -> resolver);
        applicationContext.refresh();
        return applicationContext;
    }

    /**
     * 创建测试用 Ez-MyBatis 配置。
     *
     * @param configuration MyBatis 配置
     * @return Ez-MyBatis 配置
     */
    private EzMybatisConfig createConfig(Configuration configuration) {
        EzMybatisConfig config = new EzMybatisConfig(configuration);
        config.setDbType(DbType.MYSQL);
        return config;
    }
}
