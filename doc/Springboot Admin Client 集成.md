# Springboot Admin Client

| 版本 | 更新内容 | 更新人 |  更新时间  |
| :--: | :------: | :----: | :--------: |
| V1.0 | 初始版本 | 陈伟成 | 2022-09-29 |

springboot admin 提供了应用信息监控，包括应用CPU、JVM、线程、应用状态、依赖组件、环境配置文件、类、日志、REST接口、缓存等信息的监控。**目前只在开发、测试环境下开启**

## 客户端集成

客户端集成需要完成以下工作：

1. pom添加依赖
   单体启动请在\*-web-run-single加入以下依赖，微服务启动请在\*-web-run-rpc加入以下依赖

   ```xml
   <dependency>
       <groupId>de.codecentric</groupId>
       <artifactId>spring-boot-admin-starter-client</artifactId>
       <exclusions>
           <!--与auditlog-starter-web-run-rpc冲突-->
           <exclusion>
               <groupId>org.hdrhistogram</groupId>
               <artifactId>HdrHistogram</artifactId>
           </exclusion>
       </exclusions>
   </dependency>
   ```

   如果应用依赖了`auditlog`审计日志，可能会产生`HdrHistogram`依赖版本冲突，需要在`spring-boot-admin-starter-client`移除该依赖。

   ```xml
   <exclusions>
     <!--与auditlog-starter-web-run-rpc冲突-->
     <exclusion>
       <groupId>org.hdrhistogram</groupId>
       <artifactId>HdrHistogram</artifactId>
     </exclusion>
   </exclusions>
   ```

2. 增加配置文件`application-config-sba.yml`
   `application-config-sba.yml`配置文件如下：

   ```yaml
   spring:
     boot:
       admin:
         client:
           enabled: ${SBA_CLIENT_ENABLE:false}
           instance:
             serviceUrl: ${SBA_CLIENT:http://127.0.0.1:8080}
           url: ${SBA_SERVER:http://127.0.0.1:8080}
   management:
     endpoints:
       web:
         exposure:
           include: '*'
     endpoint:
       health:
         show-details: always
     health:
       rabbit:
         enabled: false
       elasticsearch:
         enabled: false
   ```

   - SBA_CLIENT_ENABLE：是否启用`springboot admin client`
   - SBA_CLIENT：`springboot admin client`服务地址，配置`rainbond`**前端应用地址**。例如网架图应用（开发环境）配置为`https://adssx-test-gzdevops3.tsintergy.com/grid/`
   - SBA_SERVER：`springboot admin server`服务地址，请参考[spring boot admin 服务端地址](##Springboot Admin Server)

   完成`application-config-sba.yml`文件配置后，需要在`application-include-web-run-*.myl`文件中`include`该`yml`文件。

3. 配置权限拦截
   如果应用集成了`securityng`,则需要**增加**对以下路径的拦截的忽略，`application-config-tsie-usercenter.yml`文件中配置如下：

   ```yaml
   tsie:
     securityng:
       ignoredUrls:
         - /api/actuator/**
         - /actuator/**
         - /api/instances/**
   ```

4. 配置应用名称（可选）
   `springboot admin server web`页面展示注册的应用名称使用的是`spring.application.name`这个环境变量配置的名称。因此为了区分各个注册的应用需要为相应的应用配置`spring.application.name`，如网架图应用为`grid-app`。命名规则为【省份简称】-【应用简称】-【app|service】，例如广东火电应用为：`gd-fire-app`。**不可用中文，因为`dubbo`服务注册`meta`元数据中包含的应用名称也是取`spring.application.name`这个环境变量的值，而在此次不可用中文**

5. 配置日志等级
   为防止大量`springboot admin`监控日志输出，需要对`*_log4j2.xml`配置文件增加以下配置： 

   ```xml
   <Logger name="org.springframework.core.log.CompositeLog" level="ERROR"/>
   ```

   增加一个`<Logger>`将`org.springframework.core.log.CompositeLog`日志等级调整为`ERROR`



## Springboot Admin Server

`springboot admin`服务端地址如下：

| 环境 |                   地址                    |
| :--: | :---------------------------------------: |
| 开发 | http://sbaserver.gzdevops.tsintergy.com/  |
| 测试 | http://sbaserver.gzdevops3.tsintergy.com/ |



## Springboot admin Server Web

`springboot admin`服务端`web`地址如下：

| 环境 |                        地址                         |
| :--: | :-------------------------------------------------: |
| 开发 | http://sbaserver.gzdevops.tsintergy.com/index.html  |
| 测试 | http://sbaserver.gzdevops3.tsintergy.com/index.html |

