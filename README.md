# Niubi Job是什么
niubi-job是一个具备高可用特性的专门针对定时任务的任务调度框架.

# Niubi Job的特点
它是一个专门针对定时任务所设计的分布式任务调度框架,但它有以下特点.
 * 动态发布任务，通过web控制台上传任务jar包即可发布任务，发布新任务时，正在运行的任务不受任何影响。
 * 可靠性较高的灾备机制，采用成熟的分布式系统解决方案zookeeper处理节点间的协作。
 * 智能负载均衡，拥有理论上无限的伸缩能力（仅master-slave模式支持）
 * 有较为详细的任务执行日志，保存在logs文件夹当中。
 * 完美支持spring的运行环境。
 * 支持多种模式，例如单机模式(单机模式也可以用来测试)、伪分布式以及真正的分布式。
 * 简单易用，降低门槛。

# 如何下载
##### 当前稳定版本 : 0.9.5
 * [niubi-job-cluster.zip](http://www.zuoxiaolong.com/download/niubi-job-cluster.zip "niubi-job-cluster.zip")   [Windows版本]
 * [niubi-job-cluster.tar.gz](http://www.zuoxiaolong.com/download/niubi-job-cluster.tar.gz "niubi-job-cluster.tar.gz")   [Unix/Mac版本]
 * [niubi-job-console.war](http://www.zuoxiaolong.com/download/niubi-job-console.war "niubi-job-console.war")   [web控制台war包]

##### 编译最新版本
自己编译一样简单，你只需安装好git和maven，然后执行以下命令即可。
```
git clone git@github.com:xiaolongzuo/niubi-job.git
cd niubi-job
mvn clean package
```

##### web控制台默认的用户名密码
 * username : admin
 * password : 123456

# 文档
 * 安装就是这么简单，详见[安装文档](http://www.cnblogs.com/zuoxiaolong/p/niubi-job-1.html "http://www.cnblogs.com/zuoxiaolong/p/niubi-job-1.html")
 * 开发任务就是这么简单，详见[开发文档](http://www.cnblogs.com/zuoxiaolong/p/niubi-job-2.html "http://www.cnblogs.com/zuoxiaolong/p/niubi-job-2.html")
 * 如果你想了解更多，可以参考[框架设计原理简介](http://www.cnblogs.com/zuoxiaolong/p/niubi-job-3.html "http://www.cnblogs.com/zuoxiaolong/p/niubi-job-3.html")
 * 全新的0.9.4.2介绍，包含了全套[Console控制台教程](http://www.cnblogs.com/zuoxiaolong/p/niubi-job-4.html "http://www.cnblogs.com/zuoxiaolong/p/niubi-job-4.html")
 * 如果你遇见了问题,请查看[FAQ文档](http://www.cnblogs.com/zuoxiaolong/p/niubi-job-5.html "http://www.cnblogs.com/zuoxiaolong/p/niubi-job-5.html")

# 开源协议(License)
Niubi Job框架基于开源协议Apache License 2.0。(The Niubi Job Framework is released under version 2.0 of the Apache License.)

# 变更历史
##### 0.9.5
 * 修改所有数据库字段的命名,避免与各类数据库的关键字冲突.
 * 添加重要模块的单元测试,保证项目的稳定性.

##### 0.9.4.2
 * 修复集群节点缺少guava的BUG.

##### 0.9.4.1
 * 将cluster的依赖瘦身

##### 0.9.4
 * 所有任务公用一个调度器,节省大量线程资源.
 * 优化类加载器

##### 0.9.3
 * 优化console界面
 * 修复一些BUG
