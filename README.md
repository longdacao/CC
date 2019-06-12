
## 1. 环境准备
```
fisco-cc存证系统由三家机构组成：深圳通、太保、国泰军安。底层需要三台服务器部署节点与机构应用对接。
节点分配：底层链7个节点，每个机构分配2个节点，剩余一个节点作为公共观察节点。
```
## 2. 获取存证业务源码（三家机构都执行）
```
前提条件：底层链已经按照要求部署完毕。
git clone https://github.com/jishitang/CC.git
执行编译
gradle build
生成dist目录，将dist目录下的lib目录下web3sdk.jar替换成最新
将生成的dist目录名称修改为fiscocc-onbc

```
## 3. 创建存证业务数据库（三家机构都执行）
```
每个机构都必须创建自有的数据库
mysql -u root -p
create database `fcconbc` default character set utf8 collate utf8_general_ci;

登录业务库，给业务库创建业务表
use fcconbc;
source $HOME/fiscocc-onbc/script/db/1.0.0/fiscocc-onbc.sql
（---此处$HOME代表fiscocc-onbc所在的目录路径，下同----）

执行表升级操作
source $HOME/fiscocc-onbc/script/db/2.0.0/fiscocc-onbc.sql

给数据库赋予远程登录访问权限
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' identified by '123456';  
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' identified by '123456'; 
```
## 4. 创建路由地址（只需要在深圳通所在服务器执行，其他两家机构不需要执行）

```
拷贝sdk证书：
进入$HOME/fiscocc-onbc/script/route/1.0.0/conf-route/
从深圳通对应的底层节点拷贝sdk证书至当前目录。

配置节点连接：
进入$HOME/fiscocc-onbc/script/route/1.0.0/conf-route/
修改applicationContext.xml ，配置深圳通的底层节点IP和channel port端口，如下示例：
 <list>
 <value>119.29.91.75:8310</value>
 </list>
 
配置路由规则：
进入$HOME/fiscocc-onbc/script/route/1.0.0
修改route_deploy.json文件，ip、channelPort需要修改，如下示例 深圳通SZT、太保TB、国泰君安GTJA
（如果要做大数据压测，则需要将seqMax设置大些，如下 "seqMax":100000000,）

{
    "nodes":[
        {
            "ip":"10.107.108.47",
            "channelPort":8310,
            "name":"node0",
            "org":"SZT"
        },
        {
            "ip":"10.107.108.47",
            "channelPort":8311,
            "name":"node1",
            "org":"SZT"
        },
        {
            "ip":"10.107.108.28",
            "channelPort":8310,
            "name":"node2",
            "org":"TB"
        },
        {
            "ip":"10.107.108.28",
            "channelPort":8311,
            "name":"node3",
            "org":"TB"
        },
        {
            "ip":"10.107.108.35",
            "channelPort":8310,
            "name":"node4",
            "org":"GTJA"
        },
        {
            "ip":"10.107.108.35",
            "channelPort":8311,
            "name":"node5",
            "org":"GTJA"
        },
    ],
    "orgs":[
        {
            "name":"SZT"
        },
        {
            "name":"TB"
        },
        {
            "name":"GTJA"
        }
    ],
    "groups":[
        {"groupId": 1}
        ],
    "sets":[
        {
            "name":"set0",
            "nodes":["node0", "node1", "node2", "node3", "node4", "node5"],
            "publicAddress":["SZT.0xa9c6b6c8a3105ce8589d0efa2fcaa1549f8175de", "TB.0x1c597dcfff4cdab6fa72cb7ef0e9785f6609098d", "GTJA.0x541ca7fbd5eaedb21d12808c00f0b90a4ed9d4bd"]
        }
    ],
    "rules":[
        {
            "appid":"IDA1EQRG",
            "name":"IDA1EQRG-1",
            "seqMin":1,
            "seqMax":100000000,
            "orgs":["SZT","TB","GTJA"],
            "sets":["set0"]
        },
        {
            "appid":"IDALHM0x",
            "name":"IDALHM0x-1",
            "seqMin":1,
            "seqMax":100000000,
            "orgs":["SZT","TB","GTJA"],
            "sets":["set0"]
        },
        {
            "appid":"IDAMxM1y",
            "name":"IDAMxM1y-1",
            "seqMin":1,
            "seqMax":100000000,
            "orgs":["SZT","TB","GTJA"],
            "sets":["set0"]
        }
    ]

 执行路由地址生成的命令:
 进入$HOME/fiscocc-onbc/script/route/1.0.0
 执行命令：sh route-tool.sh deploy route_deploy.json
 命令执行后返回路由合约hash地址如下示例 （如果命令执行失败，请检查sdk证书和配置的节点IP和端口是否正常）
0xbcd11994c9f13d8eb1fc996bfdf3cb93c491690d
（路由地址需要保存，后面的步骤配置需要用到这个路由地址）
```
## 5. 配置机构应用fiscocc-onbc的启动的applicationContext.xml参数（三家机构都要执行）
```
进入$HOME/fiscocc-onbc/conf/

拷贝sdk证书：
从机构对应的底层节点下拷贝sdk证书至当前目录

修改机构目录下的applicationContext.xml（当太保与国泰君安搭建环境的时候，需要把env-prd-tb、env-prd-gtja下配置好的文件替换当前conf目录下的文件；） 示例如下，主要配置机构所在的底层节点IP和channel port。

<?xml version="1.0" encoding="UTF-8" ?>

<beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
           xmlns:tx="http://www.springframework.org/schema/tx" xmlns:aop="http://www.springframework.org/schema/aop"
           xmlns:context="http://www.springframework.org/schema/context"
           xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-2.5.xsd  
         http://www.springframework.org/schema/tx   
    http://www.springframework.org/schema/tx/spring-tx-2.5.xsd  
         http://www.springframework.org/schema/aop   
    http://www.springframework.org/schema/aop/spring-aop-2.5.xsd">


        <bean id="encryptType" class="org.fisco.bcos.web3j.crypto.EncryptType">
                <constructor-arg value="0"/> <!-- 0:standard 1:guomi -->
        </bean>

        <bean id="channelService_IDA1EQRG" class="org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig">
                <property name="allChannelConnections">
                        <list>
                                <bean id="group1"  class="org.fisco.bcos.channel.handler.ChannelConnections">
                                        <property name="groupId" value="1" />
                                        <property name="connectionsStr">
                                                <list>
                                                        <value>10.107.108.47:8310</value>
                                                        <value>10.107.108.47:8311</value>
                                                </list>
                                        </property>
                                </bean>
                        </list>
                </property>
        </bean>
  
  <bean id="channelService_IDALHM0x" class="org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig">
                <property name="allChannelConnections">
                        <list>
                                <bean id="group2"  class="org.fisco.bcos.channel.handler.ChannelConnections">
                                        <property name="groupId" value="1" />
                                        <property name="connectionsStr">
                                                <list>
                                                        <value>10.107.108.47:8310</value>
                                                        <value>10.107.108.47:8311</value>
                                                </list>
                                        </property>
                                </bean>
                        </list>
                </property>
</bean>


        <bean id="channelService_IDAMxM1y" class="org.fisco.bcos.channel.handler.GroupChannelConnectionsConfig">
                <property name="allChannelConnections">
                        <list>
                                <bean id="group3"  class="org.fisco.bcos.channel.handler.ChannelConnections">
                                        <property name="groupId" value="1" />
                                        <property name="connectionsStr">
                                                <list>
                                                        <value>10.107.108.47:8310</value>
                                                        <value>10.107.108.47:8311</value>
                                                </list>
                                        </property>
                                </bean>
                        </list>
                </property>
</bean>

<bean id="pool" class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
            <property name="corePoolSize" value="1000" />
            <property name="maxPoolSize" value="2000" />
            <property name="queueCapacity" value="4000" />
            <property name="keepAliveSeconds" value="60" />
            <property name="rejectedExecutionHandler">
                    <bean class="java.util.concurrent.ThreadPoolExecutor.AbortPolicy" />
            </property>
</bean>

        <bean id="service_IDA1EQRG" class="org.fisco.bcos.channel.client.Service" depends-on="channelService_IDA1EQRG">
    <property name="threadPool" ref="pool" />
                <property name="groupId" value="1" />
                <property name="orgID" value="SZT" />
                <property name="allChannelConnections" ref="channelService_IDA1EQRG"></property>
        </bean>
  
  <bean id="service_IDALHM0x" class="org.fisco.bcos.channel.client.Service" depends-on="channelService_IDALHM0x">
    <property name="threadPool" ref="pool" />
                <property name="groupId" value="1" />
                <property name="orgID" value="SZT" />
                <property name="allChannelConnections" ref="channelService_IDALHM0x"></property>
        </bean>
  
  <bean id="service_IDAMxM1y" class="org.fisco.bcos.channel.client.Service" depends-on="channelService_IDAMxM1y">
    <property name="threadPool" ref="pool" />
                <property name="groupId" value="1" />
                <property name="orgID" value="SZT" />
                <property name="allChannelConnections" ref="channelService_IDAMxM1y"></property>
        </bean>



</beans>



```
## 6. 配置机构应用fiscocc-onbc的启动的application.properties参数（三家机构都要执行）
```
进入$HOME/fiscocc-onbc/conf/

拷贝sdk证书（如果在第5步骤已经执行了，可忽略）

配置application.properties参数：

application.properties参数配置文件需要修改处如下：

数据库名、IP、端口根据实际情况修改；
数据库密码，建议不要用明文，密文采用druid的加密方式，密文生成参考附录；
==sdk.routeAddresses配置需要使用第4步骤生成的路由地址
sdk.keyStorePath当前机构的签名私钥，私钥生成参考附，目前已经生成，如改动，需要通知多方签名机构
sdk.wb.publickey为当前机构的签名公钥，公钥生成参考附，目前已经生成，如改动，需要通知多方签名机构
sdk.publickeys多方签名机构的公钥，目前已经生成，如改动，需要通知多方签名机构
sdk.topics为消息传递定义的消息通道，请勿改动
sdk.appids数据来源的架构标识，请勿改动
isEnable.whiteIp根据需求，是否开启ip白名单校验
如果要做大数据压测，evidence.maxId=5000000 这个参数可以需要调整

示例如下：
########################################## DB Info ##########################################
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://xxx:3306/fcconbc?useUnicode=true&characterEncoding=utf8
spring.datasource.username=xxx
# 生成的加密后的密码，密文生成方式请参考README.MD
spring.datasource.password=xxx
# 生成的公钥
public-key=MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIo3/8D4BRQ+zJafo98BNa9XS28XNfBki4sHOSiOuE1ybpUdSplDVt4oiHmGSjkb8fLEp1fZxmiRl8mWgD2AmjUCAwEAAQ==
# 配置 connection-properties，启用加密，配置公钥
spring.datasource.druid.connection-properties=config.decrypt=true;config.decrypt.key=${public-key}
# 启用ConfigFilter
spring.datasource.druid.filter.config.enabled=true
# 初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时
spring.datasource.druid.initial-size=5
# 最小连接池数量
spring.datasource.druid.min-idle=5
# 最大连接池数量
spring.datasource.druid.max-active=200
# 获取连接时最大等待时间，单位毫秒。配置了maxWait之后， 缺省启用公平锁，并发效率会有所下降， 如果需要可以通过配置useUnfairLock属性为true使用非公平锁。
spring.datasource.druid.max-wait=10000
# 有两个含义： 1) Destroy线程会检测连接的间隔时间 2) testWhileIdle的判断依据，详细看testWhileIdle属性的说明
spring.datasource.druid.time-between-eviction-runs-millis=10000
spring.datasource.druid.min-evictable-idle-time-millis=300000
# 用来检测连接是否有效的sql，要求是一个查询语句。 如果validationQuery为null，testOnBorrow、testOnReturn、 testWhileIdle都不会其作用。
spring.datasource.druid.validation-query=SELECT 'x'
spring.datasource.druid.validation-query-timeout=20
# 建议配置为true，不影响性能，并且保证安全性。 申请连接的时候检测，如果空闲时间大于 timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
spring.datasource.druid.test-while-idle=true
# 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
spring.datasource.druid.test-on-borrow=true
# 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
spring.datasource.druid.test-on-return=false
# 是否缓存preparedStatement，也就是PSCache。 PSCache对支持游标的数据库性能提升巨大，比如说oracle。 在mysql5.5以下的版本中没有PSCache功能，建议关闭掉。作者在5.5版本中使用PSCache，通过监控界面发现PSCache有缓存命中率记录， 该应该是支持PSCache。
spring.datasource.druid.pool-prepared-statements=true
# 要启用PSCache，必须配置大于0，当大于0时， poolPreparedStatements自动触发修改为true。 在Druid中，不会存在Oracle下PSCache占用内存过多的问题， 可以把这个数值配置大一些，比如说100
spring.datasource.druid.max-pool-prepared-statement-per-connection-size=50
########################################## mybatis Info #####################################
mybatis.mapper-locations=classpath:mapper/*.xml
########################################## log Info ##########################################
logging.config=classpath:log4j2.xml
########################################## Thread Info #######################################
my.corePoolSize=500
my.maxPoolSize=1000
my.queueCapacity=500
my.keepAlive=60
channel.corePoolSize=500
channel.maxPoolSize=1000
channel.queueCapacity=500
channel.keepAlive=60
########################################## sdk Info ##########################################
# sdk私钥文件
sdk.keyStorePath=classpath:szt.jks
# sdk私钥密码
sdk.keyStorePassword=123456
# sdk公钥
sdk.wb.publickey=0xa9c6b6c8a3105ce8589d0efa2fcaa1549f8175de
# 路由合约地址
sdk.routeAddresses=IDA1EQRG.0x318894e68900869568f3ef7738fb6cfff6a11810,IDALHM0x.0x318894e68900869568f3ef7738fb6cfff6a11810,IDAMxM1y.0x318894e68900869568f3ef7738fb6cfff6a11810
# 通信topics，topic依托于业务场景，多个以逗号分割
sdk.topics=IDA1EQRG.topic1000010001001,IDALHM0x.topic1000010001002,IDAMxM1y.topic1000010001003
# 通知业务相关机构公钥publickeys及topic，多个以逗号分割
#sdk.publickeys=IDA1EQRG.0x1c597dcfff4cdab6fa72cb7ef0e9785f6609098d#topic1000010001001,IDALHM0x.0x1c597dcfff4cdab6fa72cb7ef0e9785f6609098d#topic1000010001001
# APPID和publickeys
sdk.publickeys=IDA1EQRG.0xa9c6b6c8a3105ce8589d0efa2fcaa1549f8175de,IDALHM0x.0x1c597dcfff4cdab6fa72cb7ef0e9785f6609098d,IDAMxM1y.0x541ca7fbd5eaedb21d12808c00f0b90a4ed9d4bd
# 业务场景appids，多个以逗号分割
sdk.appids=IDA1EQRG,IDALHM0x,IDAMxM1y
# 多链开关（false-旧版本；true-新版本）
sdk.switch=true
########################################## other Info ##########################################
# 访问端口号设置
server.port=24451
# 请求上链次数
chain.count.limit=5
# 请求签名通知次数
nodify.count.limit=10
# 数据库查询条数
select.count.limit=20
# 定时检查线程不退出，查找超过10分钟的记录
select.chainTime=600
# 每次sleep 30毫秒，控制异步速度
select.internalSleepTime=30
# 定时任务启动间隔
schedule.time=0 0/1 * * * ?
# 是否开启IP白名单，true是开启，false是不开启
isEnable.whiteIp=true
# 证据最大条数，当超过最大值时，需要扩容链
evidence.maxId=5000000


```

## 7. 启动存证应用服务(三家机构都要执行）
```
进入$HOME/fiscocc-onbc
执行start.sh
查看log是否正常打印: tail -f $HOME/logs/fiscocc-onbc/fiscocc-onbc.log
启动打印启动失败：有可能是start.sh脚本执行时分配的内存过大，vim start.sh


修改前：JAVA_OPTS+=" -server -Xmx2048m -Xms2048m -XX:NewSize=512m -XX:MaxNewSize=512m -XX:PermSize=128m -XX:MaxPermSize=128m"


修改后：JAVA_OPTS+=" -server -Xmx1024m -Xms1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=128m -XX:MaxPermSize=128m"

启动后，发现没有产生日志文件，请确定$HOME/logs目录的用户权限与执行start.sh的权限一致；


启动日志如果发现：Received fatal alert: certificate_unknown错误，表示证书有问题；
需要重新覆盖fiscocc-onbc/conf/路径下的ca.crt, client.keystore文件，详情见“问题”章节；


执行命令报错：“start.sh: line 2: $'\r': command not found
”，需要执行格式转换命令命令：dos2unix serverStatus.sh; dos2unix start.sh; dos2unix stop.sh
```

## 8. 执行存证上链服务测试
```
存证上链，可以从三家机构分别发起：命令如下：

深圳通侧发起
curl -l -H "Content-type: application/json" -X POST -d '{"appId":"IDA1EQRG","evidenceIdNum":"100000"}' http://10.107.108.47:24451/fiscocc-onbc/press/sendBatchOnbc
太保侧发起
curl -l -H "Content-type: application/json" -X POST -d '{"appId":"IDALHM0x","evidenceIdNum":"100000"}' http://10.107.108.28:24451/fiscocc-onbc/press/sendBatchOnbc
国泰君安侧发起
curl -l -H "Content-type: application/json" -X POST -d '{"appId":"IDAMxM1y","evidenceIdNum":"100000"}' http://10.107.108.35:24451/fiscocc-onbc/press/sendBatchOnbc

检查上链服务是否正常：
查询三家机构所在的数据库fcconbc下表字段是否签名成功
select count(*) from t_evidence_status_info where sign_flag=1; 表示签名成功
select count(*) from t_evidence_status_info where sign_flag=11; 签名失败超过重试次数
默认情况下签名会重试10次，因此如果存在sign_flag=11时，就说明有上链失败的情况。

```
