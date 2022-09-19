package org.flypiggy.operate.log.spring.boot.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import static org.flypiggy.operate.log.spring.boot.starter.properties.ClassInfoEnum.TAGS;

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
    private ClassInfoEnum classInfoValue = TAGS;

    @NestedConfigurationProperty
    private Exclude exclude;

    /**
     * The package that needs to intercept the operation log and all its following package API interfaces.
     */
    private String[] apiPackagePath;

    /**
     * Select a data source type to store operation logs.
     */
    private DatasourceEnum storeType = DatasourceEnum.JDBC;

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
     * Whether to print the warning log during execution.
     */
    private Boolean waningLog = false;
}
