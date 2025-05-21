package org.rdlinux.ezmybatis.spring.boot.start;

import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.rdlinux.ezmybatis.EzMybatisConfig;
import org.rdlinux.ezmybatis.constant.TableNamePattern;
import org.rdlinux.ezmybatis.core.EzMybatisContent;
import org.rdlinux.ezmybatis.core.dao.EzDao;
import org.rdlinux.ezmybatis.core.dao.JdbcInsertDao;
import org.rdlinux.ezmybatis.core.dao.JdbcUpdateDao;
import org.rdlinux.ezmybatis.core.mapper.EzMapper;
import org.rdlinux.ezmybatis.spring.EzMybatisMapperScannerConfigurer;
import org.rdlinux.ezmybatis.spring.SpringEzMybatisInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Import(EzMybatisAutoConfiguration.EzMapperRegistrar.class)
@Configuration
@ConditionalOnClass({EzMapper.class, EzMybatisProperties.class})
@EnableConfigurationProperties(EzMybatisProperties.class)
@AutoConfigureBefore({MybatisAutoConfiguration.class})
public class EzMybatisAutoConfiguration implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(EzMybatisAutoConfiguration.class);
    private ApplicationContext applicationContext;
    @Resource
    private EzMybatisProperties ezMybatisProperties;

    @Bean
    public JdbcInsertDao jdbcInsertDao() {
        SqlSessionTemplate sqlSessionTemplate = this.applicationContext.getBean("sqlSessionTemplate",
                SqlSessionTemplate.class);
        return new JdbcInsertDao(sqlSessionTemplate);
    }

    @Bean
    public JdbcUpdateDao jdbcUpdateDao() {
        SqlSessionTemplate sqlSessionTemplate = this.applicationContext.getBean("sqlSessionTemplate",
                SqlSessionTemplate.class);
        return new JdbcUpdateDao(sqlSessionTemplate);
    }

    @Bean
    public EzDao ezDao() {
        EzMapper ezMapper = this.applicationContext.getBean(EzMapper.class);
        return new EzDao(ezMapper);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ConfigurationCustomizer ezConfigurationCustomizer() {
        return configuration -> {
            EzMybatisConfig ezMybatisConfig = new EzMybatisConfig(configuration);
            ezMybatisConfig.setEscapeKeyword(this.ezMybatisProperties.isEscapeKeyword());
            if (this.ezMybatisProperties.getMapRetKeyPattern() != null) {
                ezMybatisConfig.setMapRetKeyPattern(this.ezMybatisProperties.getMapRetKeyPattern());
            }
            ezMybatisConfig.setTableNamePattern(TableNamePattern.ORIGINAL);
            if (this.ezMybatisProperties.getTableNamePattern() != null) {
                ezMybatisConfig.setTableNamePattern(this.ezMybatisProperties.getTableNamePattern());
            }
            if (this.ezMybatisProperties.getEnableOracleOffsetFetchPage() != null) {
                ezMybatisConfig.setEnableOracleOffsetFetchPage(this.ezMybatisProperties.getEnableOracleOffsetFetchPage());
            }
            SpringEzMybatisInit.init(ezMybatisConfig, EzMybatisAutoConfiguration.this.applicationContext);
            if (this.ezMybatisProperties.getDbType() != null) {
                EzMybatisContent.setDbType(configuration, this.ezMybatisProperties.getDbType());
            }
        };
    }

    public static class EzMapperRegistrar implements BeanFactoryAware, EnvironmentAware, ImportBeanDefinitionRegistrar {
        private BeanFactory beanFactory;
        private Environment environment;

        @Override
        public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
                                            @NonNull BeanDefinitionRegistry registry) {

            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                EzMybatisAutoConfiguration.log.debug("Could not determine auto-configuration package," +
                        " automatic mapper scanning disabled.");
                return;
            }

            EzMybatisAutoConfiguration.log.debug("Searching for mappers annotated with @Mapper");

            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
            packages.add(EzMapper.class.getPackage().getName());

            if (EzMybatisAutoConfiguration.log.isDebugEnabled()) {
                packages.forEach(pkg -> EzMybatisAutoConfiguration.log
                        .debug("Using auto-configuration base package '{}'", pkg));
            }
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
                    EzMybatisMapperScannerConfigurer.class);
            builder.addPropertyValue("processPropertyPlaceHolders", true);
            builder.addPropertyValue("annotationClass", Mapper.class);
            builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
            BeanWrapper beanWrapper = new BeanWrapperImpl(MapperScannerConfigurer.class);
            Set<String> propertyNames = Stream.of(beanWrapper.getPropertyDescriptors()).map(PropertyDescriptor::getName)
                    .collect(Collectors.toSet());
            if (propertyNames.contains("lazyInitialization")) {
                // Need to mybatis-spring 2.0.2+
                builder.addPropertyValue("lazyInitialization", "${mybatis.lazy-initialization:false}");
            }
            if (propertyNames.contains("defaultScope")) {
                // Need to mybatis-spring 2.0.6+
                builder.addPropertyValue("defaultScope", "${mybatis.mapper-default-scope:}");
            }

            // for spring-native
            boolean injectSqlSession = this.environment.getProperty("mybatis.inject-sql-session-on-mapper-scan",
                    Boolean.class, Boolean.TRUE);
            if (injectSqlSession && this.beanFactory instanceof ListableBeanFactory) {
                ListableBeanFactory listableBeanFactory = (ListableBeanFactory) this.beanFactory;
                Optional<String> sqlSessionTemplateBeanName = Optional
                        .ofNullable(this.getBeanNameForType(SqlSessionTemplate.class, listableBeanFactory));
                Optional<String> sqlSessionFactoryBeanName = Optional
                        .ofNullable(this.getBeanNameForType(SqlSessionFactory.class, listableBeanFactory));
                if (sqlSessionTemplateBeanName.isPresent() || !sqlSessionFactoryBeanName.isPresent()) {
                    builder.addPropertyValue("sqlSessionTemplateBeanName",
                            sqlSessionTemplateBeanName.orElse("sqlSessionTemplate"));
                } else {
                    builder.addPropertyValue("sqlSessionFactoryBeanName", sqlSessionFactoryBeanName.get());
                }
            }
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

            registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition());
        }

        @Override
        public void setBeanFactory(@NonNull BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setEnvironment(@NonNull Environment environment) {
            this.environment = environment;
        }

        private String getBeanNameForType(Class<?> type, ListableBeanFactory factory) {
            String[] beanNames = factory.getBeanNamesForType(type);
            return beanNames.length > 0 ? beanNames[0] : null;
        }
    }
}
