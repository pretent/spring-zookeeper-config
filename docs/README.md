# zkspring 使用说明

## [目录]

* [简述](#简述Overview)
* 目的
* 快速开始
* 其他

### 简述Overview
zkspring是一个可以让zookeeper来管理spring的*.xml配置文件，启动时直接从zookeeper上读取加载配置，并且实时监听zookeeper上配置，一旦配置有所改变，下载最新配置并在不重启web服务的情况下重启spring context 加载应用新配置。

### 目的
设计之处是为了解决多个节点运行同一spring web服务，每次修改配置需要在多个节点上修改的繁琐问题。

### 快速开始
目前支持加载web.xml方式启动spring的两种方式：

* 使用org.springframework.web.context.ContextLoaderListener来加载spring配置启动
* 使用org.springframework.web.servlet.DispatcherServlet来加载spring配置启动(springMVC)

#### 安装使用zookeeper

下载安装原生zookeeper即可[zookeeper](http://zookeeper.apache.org)

#### 下载依赖

下载安装zkspring依赖：[zkspring.jar](../bin/zkspring-0.0.1-SNAPSHOT.jar)
```
<dependency>
	<groupId>org.pretent.config.spring.zk</groupId>
	<artifactId>zkspring</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```

#### 配置项目web.xml

##### ContextLoaderListener方式

```
<context-param>
    <param-name>contextClass</param-name>
    <param-value>org.pretent.config.spring.zk.zkspring.web.context.ZkXmlWebApplicationContext</param-value>
</context-param>
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>zk:/config/applicationContext.xml,zk:/config/app.xml</param-value>
</context-param>
<context-param>
    <param-name>servers</param-name>
    <param-value>127.0.0.1:2181</param-value>
</context-param>
<listener>
    <listener-class>
        org.pretent.config.spring.zk.zkspring.web.listener.ZkContextLoaderListener
    </listener-class>
</listener>
```
此种配置类似于`org.springframework.web.context.ContextLoaderListener`的配置方式，将`ContextLoaderListener`类替换成`org.pretent.config.spring.zk.zkspring.web.listener.ZkContextLoaderListener`,指定`contextClass`参数为：`org.pretent.config.spring.zk.zkspring.web.context.ZkXmlWebApplicationContext`,配置`contextConfigLocation`的值为zk:/*.xml,以zk:/开头表示从zookeeper上读取并加载配置，兼容spring本地配置(classpath:*.xml，将从本地classpath中加载配置),
配置`servers`参数指定zookeeper服务器地址,多个地址间使用,分割(192.168.0.1：2181,192.168.0.2：2181,...)

##### DispatcherServlet方式

```
<servlet>
    <servlet-name>spring</servlet-name>
    <servlet-class>org.pretent.config.spring.zk.zkspring.web.setvlet.ZkDispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>zk:/config/applicationContext.xml,zk:/config/app.xml</param-value>
    </init-param>
    <init-param>
        <param-name>contextClass</param-name>
        <param-value>org.pretent.config.spring.zk.zkspring.web.context.ZkXmlWebApplicationContext</param-value>
    </init-param>
	<init-param>
		<param-name>servers</param-name>
		<param-value>127.0.0.1:2181</param-value>
	</init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>spring</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

此种配置类似于`org.springframework.web.servlet.DispatcherServlet`的配置方式，将`DispatcherServlet`类替换成`org.pretent.config.spring.zk.zkspring.web.listener.ZkContextLoaderListener`,指定`contextClass`参数为：`org.pretent.config.spring.zk.zkspring.web.context.ZkXmlWebApplicationContext`(默认),配置`contextConfigLocation`的值为zk:/*.xml,以zk:/开头表示从zookeeper上读取并加载配置，兼容spring本地配置(classpath:*.xml，将从本地classpath中加载配置)，配置servers参数指定zookeeper服务器地址,多个地址间使用,分割(192.168.0.1：2181,192.168.0.2：2181,...)

### 其他

#### 编译依赖
```
<properties>
	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	<spring.version>3.0.3.RELEASE</spring.version>
	<zkclient.version>0.1</zkclient.version>
	<artifact.scope>compile</artifact.scope>
</properties>

<dependency>
	<groupId>commons-lang</groupId>
	<artifactId>commons-lang</artifactId>
	<version>2.4</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>commons-io</groupId>
	<artifactId>commons-io</artifactId>
	<version>2.2</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>log4j</groupId>
	<artifactId>log4j</artifactId>
	<version>1.2.16</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-core</artifactId>
	<version>${spring.version}</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-context</artifactId>
	<version>${spring.version}</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-web</artifactId>
	<version>${spring.version}</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-webmvc</artifactId>
	<version>${spring.version}</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-tx</artifactId>
	<version>${spring.version}</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-context-support</artifactId>
	<version>${spring.version}</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-test</artifactId>
	<version>${spring.version}</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>org.projectlombok</groupId>
	<artifactId>lombok</artifactId>
	<version>1.16.6</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>org.apache.zookeeper</groupId>
	<artifactId>zookeeper</artifactId>
	<version>3.4.5</version>
	<scope>${artifact.scope}</scope>
</dependency>
<dependency>
	<groupId>com.github.sgroschupf</groupId>
	<artifactId>zkclient</artifactId>
	<version>${zkclient.version}</version>
	<scope>${artifact.scope}</scope>
	<exclusions>
		<exclusion>
			<groupId>log4j</groupId>
			<artifactId>log4j</artifactId>
		</exclusion>
		<exclusion>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
		</exclusion>
		<exclusion>
			<groupId>org.apache.zookeeper</groupId>
			<artifactId>zookeeper</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<!-- servlet api -->
<dependency>
	<groupId>javax.servlet</groupId>
	<artifactId>javax.servlet-api</artifactId>
	<version>3.0.1</version>
	<scope>provided</scope>
</dependency>
```




