# FISCOCC存证v2

变更说明：存证增加扩展字段

## 1.介绍
fiscocc存证属于fiscocc项目一部分，主要提供存证、取证功能；

## 2.业务场景
目前深证通、太保、国泰君安对文件hash【文件hash是指：将文件转换成hash值】进行多方签名，然后将hash值、多方签名上链；
存证服务部署在深证通，该服务需要提供数据库，且需要连接区块链节点；如果存证服务部署在内网连接区块链节点，存证服务需要连接区块链节点前置；如果存证服务部署在外网区，可以直接连接区块链节点，裁剪区块链节点前置；

## 3.上下文关系
![avatar](https://note.youdao.com/yws/api/personal/file/A845F0DB64464E38A4ACB2ED14C3B5B5?method=download&shareKey=325a13e8bd985e5288fdd2140fd2fa01)


## 4.包含子系统
实现存证功能需如下子系统：
- a.业务存证区块链节点(fiscocc-ebc)：存证区块链节点；
- b.存证区块链节点前置服务(fiscocc-ebcf)：根据部署区域的不同，可裁剪该子系统；
- c.存证上链服务(fiscocc-onbc)：可以通过前置服务连接区块链节点，也可以直连区块链节点，根据部署区域的不同处理；该服务需要数据库；
- d.存证SDK(fiscocc-evidencesdk)：sdk嵌入在fiscocc-onbc中，sdk提供：证据上链、证据加签、获取证据信息，详情见《存证SDK使用手册》；
- f.存证智能合约(fiscocc-ec)：使用solidity开发，部署在区块链节点上，开发完成后嵌入到SDK中，通过web3j生成java代码，与sdk可以直接调用合约方法；
- g.存证多链路由智能合约(fiscocc-ec)：为了区块链节点的可扩展性，存证数据上链采用多链架构，详情见《多链使用手册》

## 5.软件版本
- 区块链：FISCO-BCOS V1.3.0
- 公网web3sdk：V1.1.0
- 操作系统：CentOS (7.2 64位)
- 语言：java[1.8+]
- 数据库：mysql[5.6+]

## 6.环境搭建

#### 深证通
##### 6.1. 搭建存证区块链节点

- 搭建区块链节点，参考官方开源FISCO-BCOS v1.3.0版本，准备端口（单个节点，每加一个节点，端口递增）："rpcport":"8548","p2pport":"30306",“channelPort":"8824"

##### 6.2. 搭建存证区块链节点前置(fiscocc-ebcf)
```sh
拉取代码，进入fiscocc-ebcf目录，执行gradle build命令，
生成dist文件夹，生成如下目录：
---apps 项目jar包
---conf 配置文件
---lib  引用的jar包
---serverStatus.sh  查询进程脚本
---start.sh 启动脚本
---stop.sh  停止脚本
---server.env 配置jdk的路径

生成的dist文件夹为发布的物料包，
更改dist文件夹名为fiscocc-ebcf，放在/data/app/目录下

配置文件conf/applicationContext.xml：8824对应区块链节点的channelPort端口；
18824是前置监听的端口，如果上链服务通过前置访问区块链节点，则上链服务的配置文件配置该端口。
<bean id="proxyServer" class="org.bcos.channel.proxy.Server">
	<property name="threadPool">
		<bean class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
			<property name="corePoolSize" value="500" />
			<property name="maxPoolSize" value="1000" />
			<property name="queueCapacity" value="500" />
			<property name="keepAliveSeconds" value="60" />
			<property name="rejectedExecutionHandler">
				<bean class="java.util.concurrent.ThreadPoolExecutor.AbortPolicy" />
			</property>
		</bean>
	</property>
	<property name="remoteConnections">
			<bean class="org.bcos.channel.handler.ChannelConnections">
				<property name="connectionsStr">
					<list>
						<value>nodeWB@10.107.105.225:8824</value><!-- 格式：节点名@IP地址:端口，节点名可以为任意名称 -->
						<value>nodeWB@10.107.105.193:8824</value><!-- 格式：节点名@IP地址:端口，节点名可以为任意名称 -->
					</list>
				</property>
			</bean>
		</property>
		
		<property name="localConnections">
			<bean class="org.bcos.channel.handler.ChannelConnections">
			</bean>
		</property>
		<!-- 区块链前置监听端口配置，区块链SDK连接用 -->
		<property name="bindPort" value="18824"/>
</bean>

```
- 配置完成后，执行start.sh
- 查看log是否正常打印: tail -f /data/app/logs/fiscocc-ebcf/fiscocc-ebcf.log
- 启动打印启动失败：有可能是start.sh脚本执行时分配的内存过大，vim start.sh

> 修改前：JAVA_OPTS+=" -server -Xmx2048m -Xms2048m -XX:NewSize=512m -XX:MaxNewSize=512m -XX:PermSize=128m -XX:MaxPermSize=128m"

> 修改后：JAVA_OPTS+=" -server -Xmx1024m -Xms1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=128m -XX:MaxPermSize=128m"

- 启动后，发现没有产生日志文件，请确定/data/app/logs目录的用户权限与执行start.sh的权限一致；

- 启动日志如果发现：Received fatal alert: certificate_unknown错误，表示证书有问题；
需要重新覆盖fiscocc-ebcf/conf/路径下的ca.crt, client.keystore文件，详情见“问题”章节；
fiscocc-ebcf前置服务搭建完成；

- 执行命令报错：“start.sh: line 2: $'\r': command not found
”，需要执行格式转换命令命令：dos2unix serverStatus.sh; dos2unix start.sh; dos2unix stop.sh

##### 6.3. 搭建存证上链服务(fiscocc-onbc)

```
1. 搭建mysql，创建数据库，数据库名：fcconbc（可以根据实际情况自定义）

2. 拉取代码，进入fiscocc-onbc目录，执行gradle build命令，
生成如下目录：
---apps 项目jar包
---conf 配置文件
---script 数据库脚本与多链路由脚本
---lib  引用的jar包
---serverStatus.sh  查询进程脚本
---start.sh 启动脚本
---stop.sh  停止脚本
---server.env 配置jdk的路径

生成的dist文件夹为发布的物料包，
更改dist文件夹名为fiscocc-onbc，放在/data/app/目录下

3. 在数据库中执行数据库脚本：/fiscocc-onbc/script/db/1.0.0/fiscocc-onbc.sql

```


###### 6.3.1. 获取路由地址1（太保与国泰君安请勿执行该步骤）
执行多链路由，获取路由地址，路由工具位置fiscocc-onbc/script/route/1.0.0/conf-route/applicationContext.xml配置路由链，以下ip地址对应区块链节点或者fiscocc-ebcf服务ip，端口对应区块链节点的channelPort端口或者fiscocc-ebcf服务监听端口;请注意，这里路由链复用存证;
```sh
fiscocc-onbc/script/route/1.0.0/conf-route/ca.crt与client.keystore需要
从fisco-bcos的物料包复制过来替换；
fiscocc-onbc/script/route/1.0.0/conf-route/applicationContext.xml配置文件

<bean id="channelService" class="org.bcos.channel.client.Service">
	<property name="orgID" value="SZT" />
	<property name="threadPool">
		<bean class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
			<property name="corePoolSize" value="1000" />
			<property name="maxPoolSize" value="2000" />
			<property name="queueCapacity" value="500" />
			<property name="keepAliveSeconds" value="60" />
			<property name="rejectedExecutionHandler">
				<bean class="java.util.concurrent.ThreadPoolExecutor.AbortPolicy" />
			</property>
		</bean>
	</property>
	<property name="allChannelConnections">
		<map>
			<entry key="SZT">
				<bean class="org.bcos.channel.handler.ChannelConnections">
					<property name="connectionsStr">
						<list>
							<value>nodeSZT@10.107.105.225:18824</value><!-- 前置 -->
							<!-- <value>node_szt_1@10.107.105.225:8824</value> -->
						</list>
					</property>
				</bean>
			</entry>
		</map>
	</property>
</bean>
```
script/route/1.0.0/route_deploy.json配置文件修改，配置路由规则
```
以下内容：ip、channelPort需要修改
{
    "nodes":[
        {
            "ip":"10.107.105.225",
            "channelPort":8824,
            "name":"node0",
            "org":"SZT"
        },
        {
            "ip":"10.107.105.193",
            "channelPort":8824,
            "name":"node1",
            "org":"SZT"
        },
        {
            "ip":"10.107.105.225",
            "channelPort":8825,
            "name":"node2",
            "org":"TB"
        },
        {
            "ip":"10.107.105.193",
            "channelPort":8825,
            "name":"node3",
            "org":"TB"
        },
        {
            "ip":"10.107.105.225",
            "channelPort":8826,
            "name":"node4",
            "org":"GTJA"
        },
        {
            "ip":"10.107.105.193",
            "channelPort":8826,
            "name":"node5",
            "org":"GTJA"
        }
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
            "seqMax":50000,
            "orgs":["SZT","TB","GTJA"],
            "sets":["set0"]
        },
        {
            "appid":"IDALHM0x",
            "name":"IDALHM0x-1",
            "seqMin":1,
            "seqMax":50000,
            "orgs":["SZT","TB","GTJA"],
            "sets":["set0"]
        },
        {
            "appid":"IDAMxM1y",
            "name":"IDAMxM1y-1",
            "seqMin":1,
            "seqMax":50000,
            "orgs":["SZT","TB","GTJA"],
            "sets":["set0"]
        }
    ]
}

```

######  6.3.2. 获取路由地址2（太保与国泰君安请勿执行该步骤）
配置完后，进入/data/app/fiscocc-onbc/script/route/1.0.0路径，执行命令：sh route-tool.sh deploy route_deploy.json 返回合约地址，具体参考《多链使用手册》，多链路由启动完成；

######  6.3.3. 启动fiscocc-onbc上链服务 配置applicationContext.xml
进入路径/data/app/fiscocc-onbc/conf/，配置fiscocc-onbc子系统applicationContext.xml，当太保与国泰君安搭建环境的时候，需要把env-prd-tb、env-prd-gtja下配置好的文件替换当前conf目录下的文件；
- 注意事项：
- a. bean的id=“service_IDA1EQRG”中“IDA1EQRG”代表数据来源深证通的机构标识APPID，如果目前数据源有3家机构的话，需要初始化3个bean，如下配置；
- b. 以下ip为区块链节点ip或者fiscocc-ebcf服务ip，端口为区块链节点的channelPort端口或者fiscocc-ebcf服务的监听端口
```sh
fiscocc-onbc/conf/ca.crt与client.keystore需要
从fisco-bcos的物料包复制过来替换；

fiscocc-onbc子系统applicationContext.xml配置文件
<bean id="service_IDA1EQRG" class="org.bcos.channel.client.Service">
		<property name="orgID" value="SZT" />
		<property name="threadPool">
			<bean class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
				<property name="corePoolSize" value="500" />
				<property name="maxPoolSize" value="1000" />
				<property name="queueCapacity" value="500" />
				<property name="keepAliveSeconds" value="60" />
				<property name="rejectedExecutionHandler">
					<bean class="java.util.concurrent.ThreadPoolExecutor.AbortPolicy" />
				</property>
			</bean>
		</property>
		<property name="allChannelConnections">
			<map>
				<entry key="SZT">
					<bean class="org.bcos.channel.handler.ChannelConnections">
						<property name="connectionsStr">
							<list>
								<value>nodeSZT@10.107.105.225:18824</value>
								<!-- <value>nodeSZT@10.107.105.225:8824</value>
								<value>nodeSZT@10.107.105.193:8824</value> -->
							</list>
						</property>
					</bean>
				</entry>
			</map>
		</property>
	</bean>
	
	<bean id="service_IDALHM0x" class="org.bcos.channel.client.Service">
		<property name="orgID" value="SZT" />
		<property name="threadPool">
			<bean class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
				<property name="corePoolSize" value="500" />
				<property name="maxPoolSize" value="1000" />
				<property name="queueCapacity" value="500" />
				<property name="keepAliveSeconds" value="60" />
				<property name="rejectedExecutionHandler">
					<bean class="java.util.concurrent.ThreadPoolExecutor.AbortPolicy" />
				</property>
			</bean>
		</property>
		<property name="allChannelConnections">
			<map>
				<entry key="SZT">
					<bean class="org.bcos.channel.handler.ChannelConnections">
						<property name="connectionsStr">
							<list>
								<value>nodeSZT@10.107.105.225:18824</value>
								<!-- <value>nodeSZT@10.107.105.225:8824</value>
								<value>nodeSZT@10.107.105.193:8824</value> -->
							</list>
						</property>
					</bean>
				</entry>
			</map>
		</property>
	</bean>
	
	<bean id="service_IDAMxM1y" class="org.bcos.channel.client.Service">
		<property name="orgID" value="SZT" />
		<property name="threadPool">
			<bean class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
				<property name="corePoolSize" value="500" />
				<property name="maxPoolSize" value="1000" />
				<property name="queueCapacity" value="500" />
				<property name="keepAliveSeconds" value="60" />
				<property name="rejectedExecutionHandler">
					<bean class="java.util.concurrent.ThreadPoolExecutor.AbortPolicy" />
				</property>
			</bean>
		</property>
		<property name="allChannelConnections">
			<map>
				<entry key="SZT">
					<bean class="org.bcos.channel.handler.ChannelConnections">
						<property name="connectionsStr">
							<list>
								<value>nodeSZT@10.107.105.225:18824</value>
								<!-- <value>nodeSZT@10.107.105.225:8824</value>
								<value>nodeSZT@10.107.105.193:8824</value> -->
							</list>
						</property>
					</bean>
				</entry>
			</map>
		</property>
	</bean>
```
######  6.3.4. 启动fiscocc-onbc上链服务 配置application.properties
application.properties配置文件需要修改处如下：
- 数据库名、IP、端口根据实际情况修改；
- 数据库密码，建议不要用明文，密文采用druid的加密方式，密文生成参考附录；
- ==sdk.routeAddresses配置需要使用6.3.3. 获取的路由地址==
- sdk.keyStorePath当前机构的签名私钥，私钥生成参考附，目前已经生成，如改动，需要通知多方签名机构
- sdk.wb.publickey为当前机构的签名公钥，公钥生成参考附，目前已经生成，如改动，需要通知多方签名机构
- sdk.publickeys多方签名机构的公钥，目前已经生成，如改动，需要通知多方签名机构
- sdk.topics为消息传递定义的消息通道，请勿改动
- sdk.appids数据来源的架构标识，请勿改动
- isEnable.whiteIp根据需求，是否开启ip白名单校验
```sh
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
nodify.count.limit=5
# 数据库查询条数
select.count.limit=200
# 定时检查线程不退出，查找超过10分钟的记录
select.chainTime=600
# 每次sleep 30毫秒，控制异步速度
select.internalSleepTime=30
# 定时任务启动间隔
schedule.time=0 0/1 * * * ?
# 是否开启IP白名单，true是开启，false是不开启
isEnable.whiteIp=true
# 证据最大条数，当超过最大值时，需要扩容链
evidence.maxId=100000
```
- 配置完成后，执行start.sh
- 查看log是否正常打印: tail -f /data/app/logs/fiscocc-onbc/fiscocc-onbc.log
- 启动打印启动失败：有可能是start.sh脚本执行时分配的内存过大，vim start.sh

> 修改前：JAVA_OPTS+=" -server -Xmx2048m -Xms2048m -XX:NewSize=512m -XX:MaxNewSize=512m -XX:PermSize=128m -XX:MaxPermSize=128m"

> 修改后：JAVA_OPTS+=" -server -Xmx1024m -Xms1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:PermSize=128m -XX:MaxPermSize=128m"

- 启动后，发现没有产生日志文件，请确定/data/app/logs目录的用户权限与执行start.sh的权限一致；

- 启动日志如果发现：Received fatal alert: certificate_unknown错误，表示证书有问题；
需要重新覆盖fiscocc-onbc/conf/路径下的ca.crt, client.keystore文件，详情见“问题”章节；

- 执行命令报错：“start.sh: line 2: $'\r': command not found
”，需要执行格式转换命令命令：dos2unix serverStatus.sh; dos2unix start.sh; dos2unix stop.sh

#### 太保
同上，注意：请勿执行多链路由步骤，需要搭建自己的数据库；

#### 国泰君安
同上，注意：请勿执行多链路由步骤，需要搭建自己的数据库；

## 7. 存证上链服务接口
##### 7.1. setEvidence接口说明
- 请求URL: https://xxx:24451/fiscocc-onbc/evidence/setevidence
- 请求方法:POST
- 请求头:Content-Type: application/json
- 请求参数

|参数|说明|类型|长度|是否必填|
|:----|:-----|:-----|------|------|
|appId|机构标识|String|32|必填|
|hash|Hash值|String|32|必填|
|userInfo|用户信息|结构体||必填|
|userName|用户信息-用户名|String|32|必填|
|identificationType|用户信息-证件类型:0:身份证 1:护照 2:组织机构代码证|String|32|必填|
|identificationNo|用户信息-证件号|String|32|必填|
|exData|扩展字段|String|1024|必填|


- 返回参数


|参数|说明|类型|
|:----|:-----|:-----|
|errorMsg|结果的返回码：0:成功，非0失败|字符串|
|errorMsg|返回结果描述|字符串|
|data|证据信息|结构体|
|evidenceId|业务回执|字符串|



##### 返回头
```sh
请求：
{
	"appId":"IDA1EQRG",
	"hash":"0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
    "exData":"exData",
	"userInfo":{
		"userName":"张三70",
		"identificationType":"0",
		"identificationNo":"360121198601027231"
	}
}

响应：
{
    "errorCode": 0,
    "errorMsg": "成功！",
    "data": {
        "evidenceId": "70"
    }
}

```


#### 7.2. 获取证据地址getevidence接口(查询当前机构证据地址)
- 请求URL: https://xxx:24451/fiscocc-onbc/evidence/getevidence
- 请求方法:POST
- 请求头:Content-Type: application/json
- 请求参数：

|参数|说明|类型|长度|是否必填|
|:----|:-----|:-----|-----|-----|
|appId|机构标识|String|32|必填|
|evidenceId|证据回执|string|32|必填|


- 返回参数


|参数|说明|类型|
|:----|:-----|:-----|
|errorCode|结果的返回码：0:成功，非0失败|字符串|
|errorMsg|返回结果描述|字符串|
|data|证据信息|结构体|
|evidenceAddress|证据地址|字符串|
例子：
```sh
请求：
{
	"appId":"IDA1EQRG",
	"evidenceId":"42"
}

响应：
{
    "msg": "成功！",
    "code": 0,
    "data": {
        "evidenceAddress":"oxxxxx"
    }
}

```



#### 7.3. 取证verifyEvidence接口
- 请求URL: https://xxx:24451/fiscocc-onbc/evidence/verifyEvidence
- 请求方法:POST
- 请求头:Content-Type: application/json
- 请求参数：

|参数|说明|类型|长度|是否必填|
|:----|:-----|:-----|-----|-----|
|appId|机构标识|String|32|必填|
|evidenceId|存证回执|string|32|必填|
|evidenceAddress|证据地址|String|128|必填|


- 返回参数


|参数|说明|类型|
|:----|:-----|:-----|
|errorCode|结果的返回码：0:成功，非0失败|字符串|
|errorMsg|返回结果描述|字符串|
|data|证据信息|结构体|
|signatures|已签名字符串|字符串数组|
|isSigned|是否签名完成：true完成，false未完成|字符串|
|unSignedInfo|未签名公钥与机构|键值对|
|evidenceHash|文件hash|字符串|
|evidenceID|证据ID|字符串|
|evidenceInfo|证据信息|字符串|
|publicKeys|需要签名的公钥|字符串数组|

例子：
```sh
请求：
{
	"appId":"IDA1EQRG",
	"evidenceId":"66",
	"evidenceAddress":"0xca5755be15d81da269037db26cee0de64df5826f"
}

响应：
{
    "msg": "成功！",
    "code": 0,
    "data": {
        "isSigned": "false",
        "unSignedInfo": {
            "0xa9c6b6c8a3105ce8589d0efa2fcaa1549f8175de": "IDA1EQRG"
        },
        "evidenceData": {
            "signatures": [
                "1c6569a877b5bb7712c5de77ce881f29588243e7f46e2686ed16376e31bab243910dac8239876ed9afdaa4f1d89accad02cf52d1eb6e23288c724aaf8589d99d87",
                "1b5c8e91f383b8d7ab1fb28db4263cd093069163bd0f31a78f3820cd29a61f261a72d584698095952516eef59bb19b5adaac9623d6c48d2781252489e2a859fd8a"
            ],
            "evidenceHash": "0x75455c4ccddf95dcf49b87bb64f21df257997987",
            "evidenceID": "66",
            "evidenceInfo": "66",
            "exData":"exData",
            "publicKeys": [
                "0x541ca7fbd5eaedb21d12808c00f0b90a4ed9d4bd",
                "0xa9c6b6c8a3105ce8589d0efa2fcaa1549f8175de",
                "0x1c597dcfff4cdab6fa72cb7ef0e9785f6609098d"
            ]
        }
    }
}
```

## 8. IP白名单
- 通过拦截器实现IP白名单功能
- IP配置在t_white_ip表中
- application.properties文件中isEnable.whiteIp=false可以关闭IP白名单过滤

## 9. 定时任务
- 证据上链定时任务
- 证据签名定时任务
- 以上两个定时任务通过一个配置控制，目前每分钟轮询一次，每10分钟执行一次上链，最多上链10次


## 注意事项
- 现在业务场景，各家机构通过各自上链服务证据上链，且需要互签；

## 问题
- Received fatal alert: certificate_unknown错误
> 该问题的产生主要是fiscocc-onbc、fiscc-ebcf/conf/路径下ca.crt、client.keystore有问题，需要重新生成；搭建区块链节点的方式是通过物料包的方，请先熟悉物料包搭建流程；之后进行如下步骤：
> 1. 进入物料包目录：dependencies/rlp_dir/node_rlp_0/ca/sdk；
> 2. 复制ca.crt client.keystore文件至fiscocc-onbc、fiscc-ebcf/conf目录下；
> 3. 请注意，每个机构的物料包不同，每个机构的ca.crt client.keystore文件不一样，各自机构处理各自机构的文件；

## 附录
### 数据库密钥生成方式

- 命令：java -cp [druid的jar包] com.alibaba.druid.filter.config.ConfigTools [数据库密码]

```sh
- 例子：java -cp druid-1.1.10.jar com.alibaba.druid.filter.config.ConfigTools root
privateKey:MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAijf/wPgFFD7Mlp+j3wE1r1dLbxc18GSLiwc5KI64TXJulR1KmUNW3iiIeYZKORvx8sSnV9nGaJGXyZaAPYCaNQIDAQABAkA6C15KKV3orJ66OnxU8GsdIWm6U2MBexfm4LeuQpE/ZEgq8YQiKALML7LdSK4ZSLrUBfwUOo5S37Y0LI78+ebZAiEA4tohIIbkVNWRv3O8BNhNk6FQsFyJE7KN2PjQKCpVbUsCIQCb+nGdlStpgNYLmD1RzjRVAm/tSZLKXHB9gBqRqArmfwIgMfrLB6aQkdxH8z1ldE/Pr7H/3AtXLB7Pv7j564+AKMcCIGMAQi7wKF7NrI4tcfZDeInggyRMR4Rzyd6Oec6rp0eHAiEAnuA7Mbf2mIX2ynhJfVzyJuAk6yoOu+S5lhny0Ds+mPU=
publicKey:MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIo3/8D4BRQ+zJafo98BNa9XS28XNfBki4sHOSiOuE1ybpUdSplDVt4oiHmGSjkb8fLEp1fZxmiRl8mWgD2AmjUCAwEAAQ==
password:a9hJkkVn8CELCIepavJKVNXv0wxhXDvmDZWVvzsKsuYY3IMXdh7rAb4j2UTEdlmBFRoM3e1T+eUny3HzNGkhtA==
```

- a.通过以上命令会生成:privateKey(私钥)、publicKey(公钥)、password(加密密码);
- b.生成之后，修改application.propertites配置文件属性：spring.datasource.password=password, public-key=publicKey;

### 生成client.keystore
```sh
FISCO-BCOS中client.keystore 的生成方法
1、client.keystore是用做web3sdk的SSL证书。
2、在web3sdk V1.0.0中，client.keystore里面有两个证书，一个client证书，一个ca证书。client.keystore的密码必须为“123456”,keystore中有私钥的client证书的密码也必须为“123456”。
client.keystore生成方式:
（1）keytool -import -trustcacerts -alias ca -file ca.crt -keystore client.keystore
（2）openssl pkcs12 -export -name client -in server.crt -inkey server.key -out keystore.p12
（3）keytool -importkeystore -destkeystore client.keystore -srckeystore keystore.p12 -srcstoretype pkcs12 -alias client
（4）Attention！ Password must be ”123456”
```

### 生成签名公私钥

##### 私钥是由linux系统下java JDK/bin中的keytool工具生成的（生成命令如下）:
```sh
参数说明：
-genkey：创建证书
-alias：证书的别名。在一个证书库文件中，别名是唯一用来区分多个证书的标识符
-keyalg：密钥的算法，非对称加密的话就是RSA
-keystore：证书库文件保存的位置和文件名。如果路径写错的话，会出现报错信息。如果在路径下，证书库文件不存在，那么就会创建一个
-keysize：密钥长度，一般都是1024
-validity：证书的有效期，单位是天。比如36500的话，就是100年
-genkeypair:生成一对非对称密钥
注意：
1.密钥库的密码至少必须6个字符，可以是纯数字或者字母或者数字和字母的组合等等
2."名字与姓氏"应该是输入域名，而不是我们的个人姓名，其他的可以不填

例子：keytool -genkeypair -alias ec -keyalg EC -keysize 256 -sigalg SHA256withECDSA -validity 3650 -storetype JKS -keystore user.jks -storepass 123456
```
##### 公钥生成
```sh
进入部署目录：
windows环境输入命令：(如果window环境运行异常，请使用工具类方法：org.bcos.fiscocc.onbc.util.SignPulibcKeyUtil)
java -cp 'conf/;apps/*;lib/*;' getPublicKey keyStoreFileName keyStorePassword keyPassword 
例子：java -cp 'conf/;apps/*;lib/*;' org.bcos.fiscocc.onbc.util.SignPulibcKeyUtil getPublicKey user.jks 123456 123456

linux环境输入命令：
java -cp 'conf/:apps/*:lib/*' getPublicKey keyStoreFileName keyStorePassword keyPassword 
例子：java -cp 'conf/:apps/*:lib/*' org.bcos.fiscocc.onbc.util.SignPulibcKeyUtil getPublicKey user.jks 123456 123456
```

##### 文件转换成hash
```
参考web3sdk org.bcos.web3j.crypto.Hash

进入部署目录：
windows环境输入命令：(如果window环境运行异常，请使用工具类方法：org.bcos.fiscocc.onbc.util.FileHash)
java -cp 'conf/;apps/*;lib/*;' file
例子：java -cp 'conf/;apps/*;lib/*;' org.bcos.fiscocc.onbc.util.FileHash szt.jks

linux环境输入命令：
java -cp 'conf/:apps/*:lib/*' file
例子：java -cp 'conf/:apps/*:lib/*' org.bcos.fiscocc.onbc.util.FileHash szt.jks
