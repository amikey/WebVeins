# WebVeins
[![Build Status](https://travis-ci.org/xiongbeer/WebVeins.svg?branch=master)](https://travis-ci.org/xiongbeer/WebVeins)
## 稳定的分布式爬虫框架
## 开发中...
### TODO-LIST
- Spider
    * [x] 普通队列
    * [x] url去重:布隆过滤器
        * [x] 内存布隆过滤器
        * [x] 持久化布隆过滤器
        * [x] 压缩持久化布隆过滤器
        * [x] nio mappedbuffer
        * [x] 提供友好的用户接口
    * [ ] 优先级队列
    * [ ] multThread
    * [ ] 增量爬取
- Http
    * [x] HttpClientGenerator
    * [ ] Http状态处理
    * [ ] Request封装
    * [ ] proxy
- Page && Site
    * [x] charset封装
    * [x] userAgent封装
    * [x] targetRequestUrl封装
    * [ ] headers封装
    * [ ] cookies封装
    - Html
        * [x] rawTest
        * [ ] links
        * [ ] images
        * [ ] pdf
        * [ ] docx
        * [ ] cssSelector
        * [ ] jsonSelector
        * [ ] xpath
        * [ ] regex
- pipeline
    * [ ] console
    * [ ] files
    * [ ] singleFile
- recover
    * [ ] checkPoint
    * [ ] edits
    * [ ] fsimage
    * [ ] hdfsSetting
- scheduler
    * [ ] on plan
- zookeeper
    * [x] Manager,Tasks,Workers基础设置
    * [x] Manager选举及管理
    * [x] Tasks队列构建
    * [x] Tasls监控与管理
    * [ ] Workers监控
    * [x] 节点抖动
    * [x] 羊群效应
    * [ ] 增强权限
- save
    * [ ] 本地持久化
    * [ ] HDFS持久化
- loger
    * [x] slf4j
