
### 前言  
因为目前 CC 适配 FISCO2.0 的环境安装复杂，参照目前的文档无法进行安装，所以采用克隆版本，等后续开发有相应成熟的指导后，再按照指导进行安装 

### 获取安装包  
附件为三个安装包，分别上传到三个服务器上，然后解压（后续以 ${HOME}/fiscocc-onbc 为解压目录进行说明 ）  
在下面安装时会对各个安装的进行说明    
fiscocc-onbc-gtja.g1.tar.gz  
fiscocc-onbc-tb.g1.tar.gz  
fiscocc-onbc-szt.g1.tar.gz  

### 创建存证业务数据库  
安装 CC 需要三个服务器，分别登陆这三个服务器，然后进行如下操作  
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

PS: 执行 sql 时如果报错可以暂时不用处理  

###  创建路由地址（只需要在深圳通所在服务器,即 fiscocc-onbc-szt.g1.tar.gz 所在服务器执行，其他两家机构不需要执行）  
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
            "ip":"10.107.108.46",
            "channelPort":41001,
            "name":"node0",
            "org":"SZT"
        },
        {
            "ip":"10.107.108.15",
            "channelPort":41001,
            "name":"node1",
            "org":"TB"
        },
        {
            "ip":"10.107.108.17",
            "channelPort":41001,
            "name":"node2",
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
            "nodes":["node0", "node1", "node2"],
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
}

 执行路由地址生成的命令:
 进入$HOME/fiscocc-onbc/script/route/1.0.0
 执行命令：sh route-tool.sh deploy route_deploy.json
 命令执行后返回路由合约hash地址如下示例 （如果命令执行失败，请检查sdk证书和配置的节点IP和端口是否正常）
0xbcd11994c9f13d8eb1fc996bfdf3cb93c491690d
（路由地址需要保存，后面的步骤配置需要用到这个路由地址）
``` 

###  配置机构应用fiscocc-onbc的启动的applicationContext.xml参数（三家机构都要执行）  
```  
进入$HOME/fiscocc-onbc/conf/

拷贝sdk证书：
从机构对应的底层节点下拷贝sdk证书至当前目录

修改机构目录下的applicationContext.xml 
示例如下，主要配置机构所在的底层节点IP和channel port,其他不用修改

<bean id="group1"  class="org.fisco.bcos.channel.handler.ChannelConnections">
<property name="groupId" value="1" />
<property name="connectionsStr">
<list>
	<value>10.107.108.46:41001</value>
</list>
</property>
</bean>


<bean id="group2"  class="org.fisco.bcos.channel.handler.ChannelConnections">
<property name="groupId" value="1" />
<property name="connectionsStr">
<list>
	<value>10.107.108.46:41001</value>
</list>
</property>
</bean>


<bean id="group3"  class="org.fisco.bcos.channel.handler.ChannelConnections">
<property name="groupId" value="1" />
<property name="connectionsStr">
<list>
	<value>10.107.108.46:41001</value>
</list>
</property>
</bean>
```  

###  配置机构应用fiscocc-onbc的启动的application.properties参数（三家机构都要执行）  

```  

1. 拷贝sdk证书
进入$HOME/fiscocc-onbc/conf/
拷贝sdk证书（如果在第5步骤已经执行了，可忽略）

2. 配置 application.properties 参数 
1） 修改数据库名、IP、端口根据实际情况修改；
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://10.107.108.46:3306/fcconbc?useUnicode=true&characterEncoding=utf8
spring.datasource.username=root
# 生成的加密后的密码，密文生成方式请参考README.MD
spring.datasource.password=123456

2） 修改 sdk.routeAddresses 地址
#路由合约地址
sdk.routeAddresses=IDA1EQRG.0xbcd11994c9f13d8eb1fc996bfdf3cb93c491690d,IDALHM0x.0xbcd11994c9f13d8eb1fc996bfdf3cb93c491690d,IDAMxM1y.0xbcd11994c9f13d8eb1fc996bfdf3cb93c491690d

3. 其他参数说明 
sdk.keyStorePath当前机构的签名私钥，私钥生成参考附，目前已经生成，如改动，需要通知多方签名机构
sdk.wb.publickey为当前机构的签名公钥，公钥生成参考附，目前已经生成，如改动，需要通知多方签名机构
sdk.publickeys多方签名机构的公钥，目前已经生成，如改动，需要通知多方签名机构
sdk.topics为消息传递定义的消息通道，请勿改动
sdk.appids数据来源的架构标识，请勿改动
isEnable.whiteIp根据需求，是否开启ip白名单校验
如果要做大数据压测，evidence.maxId=5000000 这个参数可以需要调整
``` 

###  启动存证应用服务(三家机构都要执行）  
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

###  执行存证上链服务测试  
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
