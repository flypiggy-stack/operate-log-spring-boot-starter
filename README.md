# operate-log-spring-boot-starter

#### 介绍

Operate-Log使用starter依赖，实现web接口日志输入到数据库；以低代码侵入为优势，只需简单配置yml文件即可插拔使用。

#### 安装教程

直接引用依赖

```xml
<dependency>
    <groupId>io.github.flypiggy-stack</groupId>
   <artifactId>operate-log-spring-boot-starter</artifactId>
   <version>1.2.0</version>
</dependency>
```

1. 直接引用maven中央仓库依赖
2. 直接down下代码，mvn deploy到私有nesux或者阿里镜像仓库（一定要放到仓库，不然maven依赖无法传递）

#### 使用说明

##### 一、最简启用

1. 配置yml
    ```yaml
    spring:
      operate-log:
        enable: true
        api-package-path:
          - com.xxx.xxx.xxx
    ```

##### 二、ES启用

1. 配置yml
    ```yaml
    spring:
      operate-log:
        enable: true
        api-package-path:
          - com.xxx.xxx.xxx
        store-type: elasticsearch
        elasticsearch:
          nodes: ["ip:port","ip:port"] #es集群节点
          account: (按需填写)
          password: (按需填写)
          index:
            name: web_log #索引名
            type: final_unchanged #有[final_unchanged\date_suffix]两种类型索引；final_unchanged是固定索引；date_suffix是索引名加上时间尾缀，具体时间尾缀由suffix类型确定
            suffix: year #有[year\month\day]类型选择，year为记录产生的年为尾缀，以此为例，索引为web_log_2022
    ```
2. 可预见问题 <br>
   es作为存储对象，需要以入es相关依赖
   ```xml
    <dependency>
        <groupId>co.elastic.clients</groupId>
        <artifactId>elasticsearch-java</artifactId>
        <version>7.17.4</version>
    </dependency>
    <dependency>
        <groupId>jakarta.json</groupId>
        <artifactId>jakarta.json-api</artifactId>
        <version>2.0.1</version>
    </dependency>
    <dependency>
        <groupId>io.github.flypiggy-stack</groupId>
        <artifactId>operate-log-spring-boot-starter</artifactId>
        <version>1.1.1</version>
    </dependency>     
   ```

#### yaml配置属性解释

spring.operate-log.enable：启用，默认false <br>
spring.operate-log.table-name：表名，用于存储操作日志的表；默认web_log <br>
spring.operate-log.api-package-path：需要扫描的包及其下面所有包；默认空，必填项 <br>
spring.operate-log.class-info-value：'classInfo' 字段引用 '@Api' 注解中的值； 当 'tags' 时，仅采用第一个参数；默认TAGS <br>
spring.operate-log.exclude.api：需要排除的，不要扫描的接口；以k-v形式；默认空，非必填项 <br>
spring.operate-log.exclude.http-method：需要排除的，http请求方式；默认空，非必填项 <br>
spring.operate-log.store-type：[jdbc\elasticsearch]两种存储方式，需要注意的是选择相应的存储方式需要引用相应的依赖，默认jdbc<br>

以下配置只有选择elasticsearch才能生效 <br>
spring.operate-log.elasticsearch.nodes：es集群；默认'localhost:9200'<br>
spring.operate-log.elasticsearch.account：es账号；默认空，非必填项 <br>
spring.operate-log.elasticsearch.password：es密码；默认空，非必填项 <br>
spring.operate-log.elasticsearch.index.name：索引名；默认web_log <br>
spring.operate-log.elasticsearch.index.type：[final_unchanged\date_suffix]
两种类型索引；final_unchanged是固定索引；date_suffix是索引名加上时间尾缀，具体时间尾缀由suffix类型确定；默认final_unchanged <br>
spring.operate-log.elasticsearch.index.suffix：[year\month\day]
类型选择，year为记录产生的年为尾缀，以此为例，索引为web_log_2022；默认year，只有当选择date_suffix此配置才生效 <br>

以下配置只有选择jdbc才能生效 <br>
spring.operate-log.jdbc.tableName：表名，用于存储操作日志的表；默认web_log <br>
<br>
**操作人**
可直接使用LogOperatorContext.set("操作人信息");

#### 未来新增特性

1. 增加mongoDB作为存储对象
2. 增加注解，直接使用注解来拦截接口
