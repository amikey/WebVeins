# WebVeins

[![Join the chat at https://gitter.im/WebVeins/Lobby](https://badges.gitter.im/WebVeins/Lobby.svg)](https://gitter.im/WebVeins/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/xiongbeer/WebVeins.svg?branch=master)](https://travis-ci.org/xiongbeer/WebVeins)
[![LICENCE](https://img.shields.io/badge/licence-MIT-blue.svg)](https://raw.githubusercontent.com/xiongbeer/WebVeins/master/LICENSE)
[![Docs](https://img.shields.io/badge/docs-latest-blue.svg)](https://xiongbeer.gitbooks.io/webveinsguide/content/)
## 分布式爬虫调度器

### 简介
WebVeins是一个将单机爬虫快速整合为分布式爬虫的框架，它并不关心爬虫具体的业务逻辑，与爬虫是松耦合的  

### Manual
更新中...   
[文档](https://xiongbeer.gitbooks.io/webveinsguide/content/)

### Shell
提供了获取运行信息的脚本，源码中也提供了对应的API  

    Usage:
        webveins [COMMAND] [OPTIONS]

    COMMAND:
        -v                                  print the version
        -h                                  this help message
        -l [workers|tasks|filters]          list the status
        -r [manager|worker]                 run service
        -s [manager|worker]                 stop service
        -c [manager|worker]                 check if the local service is running    

### 网页管理 preview
基于  [tomcat](https://github.com/apache/tomcat) 和 [AdminLTE](https://github.com/almasaeed2010/AdminLTE) 实现了一个简易的网页可视化监控的demo，现在只有最基础的功能，在后续的版本随着API的完善会成为一个完善的集群监控和控制系统  

![webveins-web](data/images/webveins-web.png)

提前试用
[WebVeins-visualization
](https://github.com/xiongbeer/WebVeins-visualization)

### TODOLIST
* [ ] 完整的监控与控制API
* [ ] 支持多个active manager，能在任务分类的情况下进行工作
* [ ] 支持多个filter，新增部分filter类型
* [ ] python API支持
* [ ] Go API支持
* [ ] 完善可视化项目
* [ ] 加强负载均衡与性能优化


### 依赖
- ZooKeeper >= 3.4.9
- Hadoop >= 2.7.3
- jdk 1.8
- linux内核系统

### 安装
1. clone本项目
```
$ git clone https://github.com/xiongbeer/WebVeins.git
```

2. 将clone的根目录添加到环境变量
```
# cat WEBVEINS_HOME=*YOUR_CLONE_DIR* >> /etc/profile
```

3. 配置$WEBVEINS_HOME/conf下的core.xml文件，里面有详细的注释

4. 启动zookeeper服务与hdfs文件系统，启动$WEBVEINS_HOME/bin下的wvformat脚本，初始化zookeeper和hdfs的目录树
```
$ $WEBVEINS_HOME/bin/wvformat -n
```

5. 启动manager服务[如果有多个，第一台机器启动的manager为active状态，后续启动的为standby状态]
```
$ $WEBVEINS_HOME/bin/webveins -r manager
```

6. 启动worker服务[爬虫节点需要启动，需要shell功能也需要开启此服务]
```
$ $WEBVEINS_HOME/bin/webveins -r worker
```

### Quick Start

爬虫继承 *com.xiongbeer.webveins.service.Action* 这个抽象类

```
public class Crawler extends Action{
        /* 在爬取过程中需要把新的Url保存下来，结束后上传 */
        private static Set<String> newUrls = new new ConcurrentSet<String>();
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
