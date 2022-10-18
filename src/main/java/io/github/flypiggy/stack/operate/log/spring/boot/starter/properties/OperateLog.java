package io.github.flypiggy.stack.operate.log.spring.boot.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("spring.operate-log")
public class OperateLog {

    /**
     * Whether to enable operate-log.
     */
    private Boolean enable = false;

    /**
     * The 'classInfo' field references the value in the '@Api' annotation; when 'tags', only the first parameter is used.
     */
    private ClassInfoEnum classInfoValue = ClassInfoEnum.TAGS;

    @NestedConfigurationProperty
    private Exclude exclude;

    /**
     * The package that needs to intercept the operation log and all its following package API interfaces.
     */
    private String[] apiPackagePath;

    /**
     * Select a data source type to store operation logs.
     */
    private DatasourceEnum storeType;

    /**
     * elasticsearch related configuration.
     */
    @NestedConfigurationProperty
    private Elasticsearch elasticsearch = new Elasticsearch();

    /**
     * jdbc related configuration.
     */
    @NestedConfigurationProperty
    private Jdbc jdbc = new Jdbc();

    /**
     * mongodb related configuration.
     */
    @NestedConfigurationProperty
    private Mongodb mongodb = new Mongodb();

    /**
     * The level of printing exception logs.
     * You need to specify the exception thrown. If it is empty, all exceptions are thrown.
     * e.g. You need throw NullPointerException, configure NullPointerException.
     */
    private PrintLogLevelEnum printLogLevel = PrintLogLevelEnum.NONE;

    /**
     * You need to specify the exception thrown. If it is empty, all exceptions are thrown.
     * e.g. You need throw NullPointerException, configure NullPointerException.
     */
    private String[] thrownExceptionName;

    /**
     * Whether to use the swagger annotation is not turned on by default.
     */
    private Boolean useSwaggerAnnotation = false;
}
