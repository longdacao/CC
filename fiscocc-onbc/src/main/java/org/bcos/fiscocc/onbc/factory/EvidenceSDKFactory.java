package org.bcos.fiscocc.onbc.factory;

import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.ECPrivateKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.fisco.bcos.channel.client.Service;
import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.evidence.sdkImpl.EvidenceFaceCallbackImpl;
import org.bcos.evidence.sdkImpl.EvidenceFaceImpl;
import org.bcos.fiscocc.onbc.dto.ConfigInfoDTO;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * 初始化SDK
 */
public class EvidenceSDKFactory {
	private Logger logger = LoggerFactory.getLogger(EvidenceSDKFactory.class);

	private String keyStorePath;
	private String keyStorePassword;

	public String getKeyStorePath() {
		return keyStorePath;
	}

	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public EvidenceFace getEvidenceFace() {
		try {
			//********************签名私钥加载
			KeyStore ks = KeyStore.getInstance("JKS");
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource keystoreResource = resolver.getResource(getKeyStorePath());
			ks.load(keystoreResource.getInputStream(), getKeyStorePassword().toCharArray());
			Key key = ks.getKey("ec", getKeyStorePassword().toCharArray());

			//********************运行基础通信的Service类
			ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
			//applicationContext.xml配置文件seviceid
	        String[] appidBeanIDs = context.getBeanNamesForType(Service.class);
			String[] appidArr = this.configInfoDTO.getAppids().split(",");
	        String[] addressArr = this.configInfoDTO.getRouteAddresses().split(",");
	        String[] topicArr = this.configInfoDTO.getTopics().split(",");
	        String[] publicKeyArr = this.configInfoDTO.getPublickeys().split(",");
	        String wbPublickey = this.configInfoDTO.getWbPublickey();
	        
	       
			Map<String, String> publicKeyAppIdMap = new HashMap<String, String>();
			for(String publicKey : publicKeyArr) {
				publicKeyAppIdMap.put(publicKey.split("\\.")[1], publicKey.split("\\.")[0]);
	        }
			//查询需要发送签名的appId
			String localAppid = publicKeyAppIdMap.get(wbPublickey);
			
	        
	        /*if (appidBeanIDs.length != appidArr.length || addressArr.length != appidArr.length) {
	            throw new Exception("service bean's length not match appids/route's length");
	        }*/
	        
	        Map<String, String> topicMap = new HashMap<String, String>(); 
	        for(String topic : topicArr) {
	        	topicMap.put(topic.split("\\.")[0], topic.split("\\.")[1]);
	        }
	        logger.info("##########printTopic: {}", topicMap);
	        
	        EvidenceFace evidenceFace = new EvidenceFaceImpl();
	        for (int i = 0; i < appidBeanIDs.length; i++) {
	        	String beanID = appidBeanIDs[i];
	        	logger.info("run beanID:{}", beanID);
	            Service routeService = (Service)context.getBean(beanID);
              int groupId = routeService.getGroupId();
              logger.info("#### group id: {}", groupId);

	            //这里把建立连接的时间改为10分钟
	            routeService.setConnectSeconds(600);
	        	
	        	ChannelEthereumService channelEthereumService = new ChannelEthereumService();
	        	channelEthereumService.setChannelService(routeService);
	        	channelEthereumService.setTimeout(10000);
				//启动回调函数，接受消息后，执行签名
				EvidenceFaceCallbackImpl callback = new EvidenceFaceCallbackImpl();
				callback.setSDK(evidenceFace);
				evidenceFace.setPushCallback(callback);

	        	for (int j = 0; j < appidArr.length; j++) {
	        		for (int k = 0; k < addressArr.length; k++) {
	        			if (beanID.split("_")[1].equals(appidArr[j]) && appidArr[j].equals(addressArr[k].split("\\.")[0])) {
	        				logger.info("run appid:{} address:{}, topic: {}", appidArr[j], addressArr[k].split("\\.")[1], Arrays.asList(topicMap.get(localAppid)).toString());
	        				//通过appid初始化路由表相关的操作，增加接受消息功能
	        				evidenceFace.initAppidRoute(appidArr[j], routeService, Web3j.build(channelEthereumService, groupId), (ECPrivateKey) key, addressArr[k].split("\\.")[1], Arrays.asList(topicMap.get(localAppid)));
	        				//evidenceFace.initAppidRoute(appidArr[j], routeService, Web3j.build(channelEthereumService), (ECPrivateKey) key, addressArr[k].split("\\.")[1], null);//最后两个参数：路由链的路由地址，topic(客户端不用设置)
	        				//按照appid加载私钥
	        				evidenceFace.loadPrivateKey(appidArr[j], (ECPrivateKey) key);
	        				//按照appid run SDK
	        				evidenceFace.run(appidArr[j], groupId);
	        			}
		        	}
	        	}
	        }
	        logger.info("appids:{}, routeAddress:{}. server start to listen topic:{}", appidArr, addressArr, topicMap);
	        
			return evidenceFace;
		} catch (Exception e) {
			logger.error("初始化sdk错误", e);
		}
		return null;
	}
    @Autowired
    private ConfigInfoDTO configInfoDTO;
}
