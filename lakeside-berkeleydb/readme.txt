这是一个Key-Value DB. 该DB的实现思路是基于Apache Thrift封装了Berkely DB JE,以提供
Client/Server类型的数据访问。

具体实现是使用了开源项目Hajo  https://sites.google.com/a/lifeandit.com/hajo/，我们
这里是将Hajo的实现保留过来，并进行扩展。


Check the Apache Thrift maven configuration setting. https://github.com/dtrott/maven-thrift-plugin

Hajo 0.2beta
================================================================================

Hajo is a simple RPC interface built for Berkeley DB JE using Apache Thrift.
The basic idea for Hajo was born in a project of mine, where we had to build
a service on top of Berkeley DB JE so that multiple threads could write/delete
data from the database. Back then we solved using a Spring based service hosted
in Tomcat.

After I learnt Thrift, I felt that it would be good to have a simple Thrift
interface and it would be much more easy to use and maintain. Hence Hajo was
born.

The name Hajo comes from a place in Assam, India. It is a place rich in
archelogical history and my father likes that place a lot. More details can
be found here - http://en.wikipedia.org/wiki/Hajo.

Hajo is a Thrift based RPC server. The interface file is included in the
distribution. Unzip the package, and add the Hajo jar file to your class path.
There is a sample test config file included, use that and a port of your choice
to start the server. The command to be used is

java com.hajo.Main <config file> <port> <simple|multi>

A simple webserver is started on port 1122 that can be used to check the
Berkeley DB stats. This page is simple HTML with no auto refresh etc.

The client code for Thrift interface is part of the Hajo jar. All the Hajo
specific code is in a call HajoService. For details on how to use Thrift API,
please see the Thrift site (http://incubator.apache.org/thrift/) or the test
code in the included source.