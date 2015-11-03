# zkspring 使用说明

## 简述
zkspring是一个可以让zookeeper来管理spring的*.xml配置文件，

## 目的

## 快速开始
目前支持加载web.xml方式启动spring的两种方式：
	* 使用org.springframework.web.context.ContextLoaderListener来加载spring配置启动
	* 使用org.springframework.web.servlet.DispatcherServlet来加载spring配置启动（springMVC）

### 安装使用zookeeper

下载安装原生zookeeper即可[zookeeper](http://zookeeper.apache.org)

### 下载依赖

下载安装zkspring依赖：
```
<dependency>
	<groupId>org.pretent.config.spring.zk</groupId>
	<artifactId>zkspring</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```






