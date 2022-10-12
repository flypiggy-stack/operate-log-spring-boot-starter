# operate-log-spring-boot-starter

#### 介绍

Operate-Log使用starter依赖，实现web接口日志输出到多种存储对象；以低代码侵入为优势，只需简单配置yml文件即可插拔使用。

#### 安装教程

直接引用依赖

```xml
<dependency>
   <groupId>io.github.flypiggy-stack</groupId>
   <artifactId>operate-log-spring-boot-starter</artifactId>
   <version>1.2.2</version>
</dependency>
```

1. 直接引用maven中央仓库依赖
2. 直接down下代码，mvn deploy到私有nesux或者阿里镜像仓库（一定要放到仓库，不然maven依赖无法传递）

#### 使用说明

##### 一、JDBC连接方式的数据库

1. 引入依赖
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-jdbc</artifactId>
   </dependency>
   ```

2. 配置yml
   提示：jdbc连接信息使用spring-boot的配置
    ```yaml
    spring:
      operate-log:
        enable: true  #启用
        store-type: jdbc  #jdbc连接方式
        api-package-path:
          - com.xxx.xxx.xxx #需要拦截的包
        jdbc:
          table-name: log_table #日志输出表名
    ```

##### 二、启用ES

1. 引入依赖
   ```xml
    <dependency>
        <groupId>co.elastic.clients</groupId>
        <artifactId>elasticsearch-java</artifactId>
        <version>7.17.4</version>
    </dependency>
   ```
   ```xml
   <dependency>
       <groupId>jakarta.json</groupId>
       <artifactId>jakarta.json-api</artifactId>
       <version>2.0.1</version>
   </dependency>  
   ```
   **es的依赖版本需要与es实例兼容，避免未知异常出现；目前支持的es版本7.15及以上**

2. 配置yml
    ```yaml
    spring:
      operate-log:
        enable: true
        api-package-path:
          - com.xxx.xxx.xxx
        store-type: elasticsearch
        elasticsearch:
          nodes: ["ip:port","ip:port"]  #es集群节点
          username: (按需填写)
          password: (按需填写)
          index:
            name: web_log #索引名
            type: final_unchanged #有[final_unchanged\date_suffix]两种类型索引；final_unchanged是固定索引；date_suffix是索引名加上时间尾缀，具体时间尾缀由suffix类型确定
            suffix: year #有[year\month\day]类型选择，year为记录产生的年为尾缀，以此为例，索引为web_log_2022
    ```

##### 三、启用mongodb

1. 引入依赖
   ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>
   ```

2. 配置yml
   提示：mongodb连接信息使用spring-boot的配置
    ```yaml
    spring:
      operate-log:
        enable: true
        api-package-path:
          - com.xxx.xxx.xxx
        store-type: mongodb
        mongodb:
          collection-name: web_log #集合名
    ```

#### yaml其他配置说明

```yaml
spring:
   operate-log:
      class-info-value: tags #'classInfo' 字段引用 '@Api' 注解中的值； 当 'tags' 时，仅采用第一个参数；默认TAGS
      exclude: #排除拦截的配置
         api:
            put:
               - /xx/xx/xx #put请求方式的，此接口不需要拦截；支持*匹配
         http-method: delete,head,post #delete\head\post请求方式不需要拦截，数组形式
      thrown-exception-name: ['NullPointerException'] #为空时，抛出所有异常；若指定异常，则只抛出指定的异常
```

#### 操作人

可直接使用LogOperatorContext.set("操作人信息");

#### 未来新增特性

1. 增加对swagger的弱依赖