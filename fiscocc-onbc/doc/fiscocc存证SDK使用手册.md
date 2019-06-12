# fiscocc存证SDK使用手册
## 1. 软件环境
JDK1.8+或以上

## 2. SDK API
```
重要数据结构:
public class EvidenceData {
    private List<String> signatures;//证据签名列表
    private String message;//证据hash
    private String evidenceInfo;//证据说明信息
    private List<String> publicKeys;//证据生效需要的公钥列表
}

```

```
接口文件：
package cn.webank.evidence.sdk;
import cn.webank.channel.client.Service;
import cn.webank.web3j.abi.datatypes.Address;
import cn.webank.web3j.abi.datatypes.Utf8String;
import cn.webank.web3j.protocol.Web3j;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.util.concurrent.ExecutionException;

public interface EvidenceFace {
    /**
     * 计算特定数据的hash值
     *
     */
    public String sha3(byte[] input);

   /**
     * 本接口会连接存证的json和相关文件的hash算出存证的hash
     *
     */
    public String allSha3(String json, List<String> hashs);

    /**
     *加载私钥
     *
     * @param ecPrivateKey
     */
    public void loadPrivateKey(ECPrivateKey ecPrivateKey);

/**
 	* 按照appid加载私钥
 	* @param appid
 	* @param ecPrivateKey
 	*/
	public void loadPrivateKey(String appid, ECPrivateKey ecPrivateKey);

    /**
     *获取公钥
     *
     */
    public String getPublickey();

/**
 	* 按appid获取公钥
 	* @param appid
 	* @return
 	*/
	public String getPublickey(String appid);

    /**
     *设置基础通信的Service类
     */
    public void setService(Service service);

/**
	*获取基础通信的Service类
	*/
	public Service getService();

	
/**
	* 按照appid和seq获取Service
	* @param appid
	* @param seq
	*/
	public Service getService(String appid, BigInteger seq);

    /**
     *设置用于访问区块链接口的Web3J类
     */
    public void setWeb3j(Web3j web3j);

    /**
     *run SDK
     */
    void run();

	/**
	* 按照appid run SDK
	* @param appid,org
	*/
	void run(String appid, String org);

/**
	* 初始化路由表相关的操作
	* @param service 路由链的service类
	* @param web3j 路由链的web3j类
	* @param ecPrivateKey 路由链的ECPrivateKey类
	* @param address 路由合约地址
	* @param topics 关注的topic列表，仅对于server端有效；客户端可设置为null
	*/
	void initRoute(Service service, Web3j web3j, ECPrivateKey ecPrivateKey, String address, List<String> topics);

    /**
     *对特定数据进行签名
     *返回一个签名
     */
    public String signMessage(String evidence);

/**
	* 按照appid对特定数据进行签名
	* @param appid
	* @param evidenceHash
 	*/
	public String signMessage(String appid, String evidenceHash);

    /**
     *验证特定数据的签名
     *返回一个公钥
     */
    public String verifySignedMessage(String evidenceHash, String signatureData) throws SignatureException;

/**
	* 按照appid验证特定数据的签名,返回一个公钥
	* @param appid
	* @param evidenceHash
	* @param signatureData
	* @throws SignatureException
	*/
	public String verifySignedMessage(String appid, String evidenceHash, String signatureData) throws SignatureException;

    /**
     *根据区块链hash值，获取证据数据
     *
     */
    public EvidenceData getMessagebyHash(String address)throws InterruptedException, ExecutionException;

/**
	* 按照appid,存证唯一序列号seq,区块链hash值，获取证据数据
	* @param appid
	* @param seq
	* @param address
	* @throws InterruptedException
	* @throws ExecutionException
	*/
	public EvidenceData getMessagebyHash(String appid, BigInteger seq, String address)throws InterruptedException, ExecutionException;

    /**
     *将特定证据数据的签名发送至区块链
     *
     * @param address 证据地址
     * @param evidenceHash 证据信息
     * @param signatureData 证据签名
     * @return void
     */
    public void sendSignatureToBlockChain(String address, String evidenceHash, String signatureData)throws InterruptedException, ExecutionException;

/**
	* 按照appid，存证唯一序列号和存证地址将特定证据数据的签名发送至区块链
	* @param appid
	* @param seq
	* @param address
	* @param evidenceHash
	* @param signatureData
	* @throws InterruptedException
	* @throws ExecutionException
	* @throws SignatureException
	*/
	public boolean sendSignatureToBlockChain(String appid, BigInteger seq, String address, String evidenceHash, String signatureData)throws InterruptedException, ExecutionException, SignatureException;

    /**
     *设置异步回调
     */
    public void setPushCallback(Callback callback);

    /**
     *新建一个存证
     */
    public Address newEvidence(Utf8String evi, Utf8String info, String signatureDataString, Address addr)throws InterruptedException, ExecutionException;

/**
	* 按照appid，存证唯一序列号seq新建一个存证
	* @param appid
	* @param seq
	* @param evi
	* @param info
	* @param id
	* @param signatureDataString
	* @throws InterruptedException
	* @throws ExecutionException
	* @throws SignatureException
	*/
	public Address newEvidence(String appid, BigInteger seq, Utf8String evi, Utf8String info, Utf8String id, String signatureDataString)throws InterruptedException, ExecutionException, SignatureException;

    /**
     *验证证据是否收集全了签名
     */
    public boolean verifyEvidence(EvidenceData data)throws SignatureException;

/**
	* 按照appid来验证证据是否收集全了签名
	* @param appid
	* @param data
	* @throws SignatureException
	*/
	public boolean verifyEvidence(String appid, EvidenceData data)throws SignatureException;

}

```
- 参数说明：

|函数名|用途|
|:----:  |:-----: |
|sha3|计算特定数据的hash值，使用sha3-256算法|
|allSha3|连接存证的json和相关文件的hash算出存证的hash|
|loadPrivateKey|加载私钥|
|getPublicKey|获取公钥|
|setService|设置基础通信的Service类|
|setWeb3j|设置用于访问区块链接口的Web3J类|
|run|启动sdk|
|signMessage|对特定数据进行签名，返回一个签名|
|verifySignedMessage|验证特定数据的签名，返回公钥|
|getMessagebyHash|根据区块链hash值，获取证据数据，返回一个EvidenceData|
|sendSignatureToBlockChain|将特定证据数据的签名发送至区块链|
|setPushCallback|设置异步回调，用来接收证据|
|verifyEvidence|验证证据，返回true or false|

## 3. 上链流程详解
### 3.1. 生成私钥
在Shell命令行下，使用Java的keytool生成私钥，示例：
keytool -genkeypair -alias ec -keyalg EC -keysize 256 -sigalg SHA256withECDSA  -validity 365 -storetype JKS -keystore ectest.jks -storepass 123456

该流程生成的密钥，供后续流程使用，只需使用一次。
各方各自生成私钥，并妥善保管。

### 3.2. SDK 构造
构造区块链存证SDK，需要如下：
- 签名私钥加载
```
String appid = "";//预先分配
KeyStore ks = KeyStore.getInstance("JKS");
InputStream ksInputStream = Demo.class.getClassLoader().getResourceAsStream("ectest.jks");
ks.load(ksInputStream, "123456".toCharArray());//123456为keystore密码
Key key = ks.getKey("ec", "123456".toCharArray());//123456为私钥密码
evidenceFace.loadPrivateKey(appid, (ECPrivateKey) key);

```

- 基础通信的Service类

区块链SDK一经初始化，整个应用的生命周期均可用，建议使用Spring管理或使用单例模式

```
ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
Service routeService = context.getBean(Service.class);
ChannelEthereumService channelEthereumService = new ChannelEthereumService();
channelEthereumService.setChannelService(routeService);
evidenceFace.initRoute(routeService, Web3j.build(channelEthereumService), (ECPrivateKey) key, routeAddress, null);//routeAddress为路由合约地址
evidenceFace.run(appid);

```
Service使用的配置文件如下
```
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
   <bean id="channelService" class="cn.webank.channel.client.Service">
     <property name="threadPool">
               <bean class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
                       <property name="corePoolSize" value="50" />
                       <property name="maxPoolSize" value="100" />
                       <property name="queueCapacity" value="500" />
                       <property name="keepAliveSeconds" value="60" />
                       <property name="rejectedExecutionHandler">
                               <bean class="java.util.concurrent.ThreadPoolExecutor.AbortPolicy" />
                       </property>
               </bean>
       </property>

      <property name="orgID" value="WB" /><!-- 配置本机构名称 -->
      <property name="allChannelConnections">
         <map>
            <entry key="WB">
               <bean class="cn.webank.channel.handler.ChannelConnections">
                  <property name="connectionsStr">
                     <list>
                        <value>nodeWB@119.29.57.154:30330</value><!-- 格式：节点名@IP地址:端口，节点名可以为任意名称 -->
                     </list>
                  </property>
               </bean>
            </entry>
         </map>
      </property>
   </bean>
   <bean id="signersAddressConf" class="cn.webank.evidence.utils.SignersAddressConf">
      <property name="evidenceSignersDataAddress" value="0x0f74e2529e561b2749034a3d235da8cf04ab8bea" />
   </bean>
</beans>

```

- 用于通信加密和校验的ca.crt和client.keystore

无需特别关注，只要将ca.crt和client.keystore这两个文件放到classpath下就可以了

### 3.3. 注册证据上链回调
使用SDK的setPushCallback方法，设置一个异步回调类，当区块链上有新的证据hash时，该回调会被调用，传入参数为：业务appid，存证唯一序列号seq，证据hash的交易ID
```
CallbackImpl callback = new CallbackImpl();//实现一个自己的Callback，并Override其中的onPush方法
evidenceFace.setPushCallback(callback);
callback.setSDK(evidenceFace);

public class CallbackImpl implements Callback {
@Override
	public void onPush(String appid, BigInteger seq, String address) {

}
}

```

### 3.4. 根据交易ID获取证据信息
使用getMessageByHash方法，从区块链获取证据数据，传入参数为业务appid,存证唯一序列号，交易ID，返回证据数据。
```
String signatureData = evidenceFace.signMessage(appid, evidenceData.getMessage());
```

### 3.5. 核对证据并对证据签名
使用signMessage方法，对证据数据进行签名
```
String signatureData = evidenceFace.signMessage(appid, evidenceData.getMessage());
```

### 3.6. 将签名数据上链
对证据签名后，使用sendSignatureToBlockChain，将已签名的数据上链
```
evidenceFace.sendSignatureToBlockChain(appid, seq, address, evidenceData.getMessage(), signatureData);
```

## 4. 取证流程详解
取证流程共用上链流程的evidenceFace
### 4.1. 根据交易ID获取证据信息
使用getMessageByHash方法，从区块链获取证据数据，业务appid,存证唯一序列号,传入参数为交易ID，返回证据数据。
```
EvidenceData evidenceData =  evidenceFace.getMessagebyHash(appid, seq, address);
```
### 4.2 校验证据是否生效
```
boolean verifyFlag = evidenceFace.verifyEvidence(appid, evidenceData);
```

## 5.	AMOP通讯

链上信使协议AMOP（Advance Messages Onchain Protocal）系统旨在为联盟链提供一个安全高效的消息信道，联盟链中的各个机构，只要部署了区块链节点，无论是共识节点还是观察节点，均可使用AMOP进行通讯，AMOP有如下优势：  
- 实时：AMOP消息不依赖区块链交易和共识，消息在节点间实时传输，延时在毫秒级。  
- 可靠：AMOP消息传输时，自动寻找区块链网络中所有可行的链路进行通讯，只要收发双方至少有一个链路可用，消息就保证可达。  
- 高效：AMOP消息结构简洁、处理逻辑高效，仅需少量cpu占用，能充分利用网络带宽。  
- 安全：AMOP的所有通讯链路使用SSL加密，加密算法可配置。
- 易用：使用AMOP时，无需在SDK做任何额外配置。

### 5.1 存证SDK使用
AMOP的消息收发基于topic（主题）机制，服务端（广仲）首先设置一个topic，客户端（微众）往该topic发送消息，服务端即可收到。
AMOP支持在同一个区块链网络中有多个topic收发消息，topic支持任意数量的服务端和客户端，当有多个服务端关注同一个topic时，该topic的消息将随机下发到其中一个可用的服务端。

服务端代码案例：
```
EvidenceFace evidenceFace = new EvidenceFaceImpl();
KeyStore ks = KeyStore.getInstance("JKS");
InputStream ksInputStream = Demo.class.getClassLoader().getResourceAsStream("ectest.jks");
ks.load(ksInputStream, "123456".toCharArray());
Key key = ks.getKey("ec", "123456".toCharArray());

ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
Service routeService = context.getBean(Service.class);
ChannelEthereumService channelEthereumService = new ChannelEthereumService();
channelEthereumService.setChannelService(routeService);

List<String> topics = new ArrayList<>();
topics.add(topic);

evidenceFace.initRoute(routeService, Web3j.build(channelEthereumService), (ECPrivateKey) key, routeAddress, topics);
evidenceFace.run(appid, org);
evidenceFace.loadPrivateKey(appid, (ECPrivateKey) key);
CallbackImpl callback = new CallbackImpl();
callback.setSDK(evidenceFace);
evidenceFace.setPushCallback(callback);

```

服务端的CallbackImpl类案例:
```
public class CallbackImpl implements Callback {
    @Override
public void onPush(String appid, BigInteger seq, String address) {
    try {
        //用证据地址获取证据
        EvidenceData evidenceData =  evidenceFace.getMessagebyHash(appid, seq, address);
        //证据签名
        String signatureData = evidenceFace.signMessage(appid, evidenceData.getEvidenceHash());
        //发送证据上链
        evidenceFace.sendSignatureToBlockChain(appid, seq,  address,evidenceData.getEvidenceHash(),signatureData);
    }
    catch (Exception e) {
        logger.error("onPush exception", e);
    }
}


  public void setSDK(EvidenceFace evidenceFace) {
    this.evidenceFace = evidenceFace;
  }

  EvidenceFace evidenceFace;
}

```

客户端代码案例
```
public EvidenceFace getEvidenceFace() {
	try {
		KeyStore ks = KeyStore.getInstance("JKS");
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource keystoreResource = resolver.getResource(getKeyStorePath());
		ks.load(keystoreResource.getInputStream(), getKeyStorePassword().toCharArray());
		Key key = ks.getKey("ec", getKeyStorePassword().toCharArray());

		ChannelEthereumService channelEthereumService = new ChannelEthereumService();
		channelEthereumService.setChannelService(service);
		channelEthereumService.setTimeout(10000);
		
		EvidenceFace evidenceFace = new EvidenceFaceImpl();
           evidenceFace.initRoute(service, Web3j.build(channelEthereumService), (ECPrivateKey) key, signersAddressConf.getEvidenceSignersDataAddress(), null);//最后两个参数：路由链的路由地址，topic
           for(String appid : this.configInfoDTO.getAppids().split(",")) {
               evidenceFace.loadPrivateKey(appid, (ECPrivateKey) key);
               evidenceFace.run(appid, this.configInfoDTO.getOrg());
           }

		return evidenceFace;
	} catch (Exception e) {
		logger.error("初始化sdk错误", e);
	}
	return null;
}

发送通知：
public void sendNotify(String appid, BigInteger seq, String address
) throws JsonProcessingException {
  logger.info("----------------发送通知----------------");
  Service service = evidenceSDK.getService(appid, seq);
NotifyRequest notifyRequest = new NotifyRequest();
notifyRequest.setEvidenceAddress(address);
notifyRequest.setAppid(appid);
notifyRequest.setSeq(seq);

ChannelRequest channelRequest = new ChannelRequest();
channelRequest.setFromOrg("WB");
channelRequest.setMessageID(service.newSeq());
channelRequest.setContent(mapper.writeValueAsString(notifyRequest));

String[] topics = configInfoDTO.getTopics().split(",");
logger.info("通知sendNotify topics:{} ", configInfoDTO.getTopics());
for (int i = 0; i < topics.length; i++) {
    channelRequest.setToTopic(topics[i]);
    channelRequest.setMessageID(service.newSeq());
    service.asyncSendChannelMessage2(channelRequest, new ChannelResponseCallback2() {
        @Override
        public void onResponseMessage(ChannelResponse response) {
            if(response.getErrorCode().equals(0)) {
                logger.info("seq:{} 通知成功", response.getMessageID());
            }
            else {
                logger.error("seq:{} 通知失败:{} {}", response.getMessageID(), response.getErrorCode(), response.getErrorMessage());
            }
        }
    });
}

}

```

