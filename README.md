# operate-log-spring-boot-starter

#### 介绍

Operate-Log使用starter依赖，实现web接口日志输入到数据库；以低代码侵入为优势，只需简单配置yml文件即可插拔使用。

#### 安装教程

直接引用依赖

```xml
<dependency>
    <groupId>io.github.flypiggy-stack</groupId>
    <artifactId>operate-log-spring-boot-starter</artifactId>
    <version>1.1.0</version>
</dependency>
```

1. 直接引用maven中央仓库依赖
2. 直接down下代码，mvn deploy到私有nesux或者阿里镜像仓库（一定要放到仓库，不然maven依赖无法传递）

#### 使用说明

1. 需要配置mysql或者maria数据库
2. 配置yaml文件
    ```yaml
    spring:
      operate-log:
        table-name: web_log
        enable: true
        api-package-path:
          - com.xxx.xxx.xxx
    ```

**具体属性解释** <br>
spring.operate-log.enable：启用，默认false <br>
spring.operate-log.table-name：表名，用于存储操作日志的表；默认web_log <br>
spring.operate-log.api-package-path：需要扫描的包及其下面所有包；默认空，必填项 <br>
spring.operate-log.class-info-value：'classInfo' 字段引用 '@Api' 注解中的值； 当 'tags' 时，仅采用第一个参数；默认TAGS <br>
spring.operate-log.exclude.api：需要排除的，不要扫描的接口；以k-v形式；默认空，非必填项 <br>
spring.operate-log.exclude.http-method：需要排除的，http请求方式；默认空，非必填项 <br>
**操作人**
可直接使用LogOperatorContext.ser("操作人信息");
