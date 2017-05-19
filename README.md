# WebVeins

[![Join the chat at https://gitter.im/WebVeins/Lobby](https://badges.gitter.im/WebVeins/Lobby.svg)](https://gitter.im/WebVeins/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/xiongbeer/WebVeins.svg?branch=master)](https://travis-ci.org/xiongbeer/WebVeins)
[![LICENCE](https://img.shields.io/badge/licence-MIT-blue.svg)](https://raw.githubusercontent.com/xiongbeer/WebVeins/master/LICENSE)
[![Docs](https://img.shields.io/badge/docs-latest-blue.svg)](https://xiongbeer.gitbooks.io/webveinsguide/content/)
## 分布式爬虫调度器
## 开发中，请勿下载使用...
### Manual
[文档](https://xiongbeer.gitbooks.io/webveinsguide/content/)
### 简介
WebVeins是一个将单机爬虫快速整合为分布式爬虫的框架   
工作原理可以做一个简单的抽象：
将独立的爬虫比作工人，他们的工作是围绕Url展开的，这里就将其比作任务表  
工人从公共的仓库去领取任务表，处理的过程中会得到新的任务，将他们收集起来，
等到任务完成的时候上交给仓库  
为了不重复执行任务，还需要一个管理任务表的人员，比作分发员，他将仓库中反馈回来的新的任务表进行过滤，去除那些不要的，然后切成固定大小的片发送到仓库，等待工人去领取。
![abstract](data/images/Verilog.png)  
当然，这个比喻非常笼统，还有三个重要的点没有提到：工作节点的管理、任务管理和故障处理。  
下面也做一个简单的介绍：  
上图的仓库实际上是对ZooKeeper和HDFS这两个框架的抽象  
利用ZooKeeper来实现对工作节点和任务的发放  
这里将Znode分为了3类

     /
     ├── wvManagers
     ├── wvWorkers
     └── wvTasks

Managers下注册着分发员active_manager，还可以提供standby_manager：在active_manager挂掉的清况下，接管它的工作，让系统不会阻塞  
Manager除了负责过滤和分发新的任务，也负责维护任务表单，监控Workers的状态  
Workers不依赖于Manager，他们只关心有没有新的任务可以领取，但是当manager死亡以后不能发布新的任务时，他们就会阻塞，他们在启动的时候会在 wvWorkers 下注册自己，一旦死亡，Manager可以立刻知道，然后将他执行的任务重新分配给其他状态正常的任务节点  
关于Tasks，一个新的Task诞生的时候便会在 wvTasks 下永久注册自己，只有确保完成以后才会被删除，Task的状态分为3种:
1. WAITING 等待Worker领取
2. RUNNING 已经被领取，任务执行中
3. FINISHED 任务完成，等待回收

利用HDFS来传送Task文件和Manager的一些缓存文件，目录树为

    /
    └── webveins
        ├── bloom
        └── tasks
            ├── finishedtasks
            ├── newurls
            └── waitingtasks

bloom下存储着bloom过滤器的缓存文件  
tasks下则是用于共享的url文件，其文件名就是任务名  

那么故障处理是怎么实现的呢？
分布式系统肯定会遇到单点故障，甚至集群崩溃，为了不丢失上次的状态，主要实现的方法是实现关键步骤操作的原子性  
单点故障这里可以分为
- Manager死亡
- Worker死亡

Manager死亡并不会影响其他的节点，重启它或者提前设置standby_manager接管它的任务就可以了  
Worker死亡带来的主要问题是会占据Task不释放，这里Manager会定期检查，将死亡Worker领取的任务重置为无主状态  

集群崩溃可以分为
- HDFS崩溃
- ZooKeeper崩溃
- 断电等事故

这样的事故相当严重了，任何一种发生集群肯定都不能正常工作了，但是可以恢复到崩溃前的状态  
有关url文件持久化的操作都是原子性的，也就是说
- 出现在 wvTasks 下的任务一定已经持久化到HDFS
- 没有出现在 wvTasks 下的任务一定没有持久化到HDFS，而且他的状态一定不会是完成，所以重启后不会被漏掉

虽然原理很简单，但是可以做到不丢失url文件  
用户在开发的时候，如果也遵循 任务完全完成才将业务数据持久化数据库，不完成则一定不持久化到数据库的原则就可以防止重复存储上次残留的少部分数据。

那么这样设计有何优势呢？
- 扩展很方便，搭建和配置好集群后只要关注单机爬虫本身，写好了连入集群领取任务工作就可以了
- 在保证ZooKeeper集群稳定的清况，可以支持非常多的爬虫一起工作，具体是多少本人没有条件去测试，但结合设计从理论上来说，支持上万的机器是没有问题的：网上有一部分框架使用的Znode的数量级甚至达到了百万级，而且在本框架中对Znode设置Watch的数量极少，也不通过Znode传送数据，ZooKeeper集群本身也可以通过扩展observer来提高性能，框架本身也实现了负载均衡以降低ZooKeeper集群的负载，所以性能瓶颈应该是很高的
