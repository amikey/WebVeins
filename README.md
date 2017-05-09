# WebVeins
[![Build Status](https://travis-ci.org/xiongbeer/WebVeins.svg?branch=master)](https://travis-ci.org/xiongbeer/WebVeins)
[![LICENCE](https://img.shields.io/badge/licence-MIT-blue.svg)](https://raw.githubusercontent.com/xiongbeer/WebVeins/master/LICENSE.txt)
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

Managers下注册着分发员active_manager，还可以提供standby_manager:在active_manager挂掉的清况下，接管它的工作，让系统不会阻塞  
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

### 快速上手
依赖
- Hadoop 2.7.3
- ZooKeeper 3.4.10
- Jdk 1.8
- linux内核系统

简单的单机测试：  
1. clone本项目
2. 将本项目的根目录添加到环境变量中
```
$ cat > 'WEBVEINS_HOME=$YOUR_CLONE_PATH'
```
3. 调整配置文件 *$WEBVEINS_HOME/conf/core.xml*
4. 启动zookeeper服务
5. 启动hdfs服务
6. 启动Manager服务
```
$ $WEBVEINS_HOME/wvManager.sh
```
7. 启动Server服务
```
$ $WEBVEINS_HOME/wvServer.sh
```
8. 编写自己的爬虫端

    爬虫继承 *com.xiongbeer.webveins.service.Action* 这个抽象类
    ```
    public class Crawler extends Action{
        /* 在爬取过程中需要把新的Url保存下来，结束后上传 */
        private static Set<String> newUrls = new HashSet<String>();
        /*
            返回true代表任务成功，false则为放弃该任务
            每当Server端成功领取到任务就会执行run方法
        */
        @Override
        public boolean run(String urlFilePath) {
            try {
                /* 读取任务Urls */
                List<String> urlsList = new UrlFileLoader().readFileByLine(urlFilePath);

                //do something

                /* 上传新的urls */
                Bootstrap.upLoadNewUrls(newUrls);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return false;
        }
        ...
        public static void main(String[] args){
            Crawler crawler = new Crawler();
            /* Bootstrap为一个引导类，需要把要使用的爬虫实例对象传给它 */
            Bootstrap bootstrap = new Bootstrap(crawler);
            bootstrap.runClient();
            try {
                /* 短暂的等待，等待Client与Server建立长连接 */
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                /*
                    连接建立，告诉Server爬虫已经准备好啦！
                    当Server收到这个消息的时候就会开始领取任务了
                 */
                bootstrap.ready();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    ```
    具体的实例可以参考源代码中example包下的结合webmagic的例子
