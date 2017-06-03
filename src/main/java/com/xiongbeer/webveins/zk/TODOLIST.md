* [ ] 新增一项检查Task: HDFS中的waitingtasks中的tasks必须保持与Znode中非finished状态下的tasks一致
* [x] 在启动manager服务的时候要检查active_manager的ip，若与本机相同则说明有过断开马上重启或者中间有失连
* [ ] 允许多个manager（学习联邦HDFS），既能每个管理不同大类的任务，也能解决主从单master性能瓶颈问题
* [ ] 尽量增加异步方法....
* [x] 任务黑名单机制
* [ ] filter白名单机制
* [ ] filter cache file引入hdfs的HA思想
* [x] 用curator替代原生API
* [ ] 加入权限管理
* [ ] manage有些步骤是独立的，可以拆分出来并行化