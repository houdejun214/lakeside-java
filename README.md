lakeside-java
=============

A java program library, it aim to help us with some simple coding tips. You can consider it as equal with
the apache commons libraries except that I added some custome implementation that we need in practical
development environment.

Much of the code are extracted from my present work, I hope this library code can help you to get things better.

Here I would like to thank my guys that help to contribute it.


1. lakeside-core

    core library which include some general java utils.

2. lakeside-data

    java data operation libarary which contain many database access interface, as Mysql, Mongo, Redis, Sqlite...
    we split into some child projects for each source.
	* lakeside-data-sql
	* lakeside-data-mongo
	* lakeside-data-redis
	* lakeside-data-sqlite
	* lakeside-data-ssh
	* lakeside-data-berkeleydb

3. lakeside-web

    A java web project library , it provide some custom class that will be used to resolve some special java-web project problem, especial integrate with spring framework.

How to use this project
-----------------------
* You can fork or clone the project and use it in your project directly.
* Use our maven repository in your maven project.

```
<repository>
  <id>dj.mvn.repo</id>
  <url>https://raw.githubusercontent.com/houdejun214/mvn-repo/master/</url>
  <!-- use snapshot version -->
  <snapshots>
     <enabled>true</enabled>
     <updatePolicy>always</updatePolicy>
   </snapshots>
</repository>

<dependency>
  <groupId>com.lakeside</groupId>
  <artifactId>lakeside-core</artifactId>
  <version>1.1.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>com.lakeside</groupId>
  <artifactId>lakeside-data-sql</artifactId>
  <version>1.1.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>com.lakeside</groupId>
  <artifactId>lakeside-web</artifactId>
  <version>1.1.0-SNAPSHOT</version>
</dependency>
```
