# WebVeins
[![Build Status](https://travis-ci.org/xiongbeer/WebVeins.svg?branch=master)](https://travis-ci.org/xiongbeer/WebVeins)
[![LICENCE](https://img.shields.io/badge/licence-MIT-blue.svg)](https://raw.githubusercontent.com/xiongbeer/WebVeins/master/LICENSE.txt)
[![Docs](https://img.shields.io/badge/docs-latest-blue.svg)](https://xiongbeer.gitbooks.io/webveinsguide/content/)
## 分布式爬虫调度器
## 开发中...
### Manual
[文档](https://xiongbeer.gitbooks.io/webveinsguide/content/)
### TODO-LIST
- Filter
    * [x] url去重:布隆过滤器
        * [x] 内存布隆过滤器
        * [x] 持久化布隆过滤器
        * [x] 压缩持久化布隆过滤器
        * [x] nio mappedbuffer
        * [x] 提供友好的用户接口
        * [ ] 优化缓存
- recover
    * [x] checkPoint
    * [ ] edits
    * [ ] fsimage
    * [x] hdfsSetting
- zookeeper
    * [x] Manager,Tasks,Workers基础设置
    * [x] Manager选举及管理
    * [x] Tasks队列构建
    * [x] Tasls监控与管理
    * [x] Workers监控
    * [x] 节点抖动
    * [x] 羊群效应
    * [x] 检查失效worker与task
    * [ ] 增强权限
- save
    * [x] 本地持久化
    * [x] HDFS持久化
- loger
    * [x] slf4j
