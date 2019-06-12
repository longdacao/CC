package org.bcos.evidence.demo;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.fisco.bcos.channel.client.Service;
import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.evidence.sdkImpl.EvidenceFaceImpl;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;

/**
 * Created by suyuhui on 18/3/29.
 */
public class DemoMutiRouteEB {
	static Logger logger = LoggerFactory.getLogger(DemoMutiRouteEB.class);
	
    public static void server(String appids, String routeAddress, String topics) throws Exception {

        EvidenceFace evidenceFace = new EvidenceFaceImpl();
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = DemoMeshChain.class.getClassLoader().getResourceAsStream("ectest.jks");
        ks.load(ksInputStream, "Ebkj@2017".toCharArray());
        Key key = ks.getKey("ec", "Ebkj@2017".toCharArray());

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        String[] appidBeanNames = context.getBeanNamesForType(Service.class);
        String[] appidArr = appids.split(",");
        String[] topicArr = topics.split(",");
        String[] addressArr = routeAddress.split(",");

        if (appidBeanNames.length != appidArr.length || addressArr.length != appidArr.length) {
            throw new Exception("service bean length not match appids/route address/topics length");
        }
        
        List<String> topicList = new ArrayList<>();
        for(String topic : topicArr) {
        	topicList.add(topic);
        }
        
        for (int i = 0; i < appidBeanNames.length; i++) {
        	String beanName = appidBeanNames[i];
        	logger.info("run beanName:{}", beanName);
            Service routeService = (Service)context.getBean(beanName);
        	
        	ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        	channelEthereumService.setChannelService(routeService);
        	channelEthereumService.setTimeout(10000);
        	for (int j = 0; j < appidArr.length; j++) {
        		for (int k = 0; k < addressArr.length; k++) {
        			if (beanName.split("_")[1].equals(appidArr[j]) && appidArr[j].equals(addressArr[k].split("\\.")[0])) {
        				logger.info("run appid:{} address:{}", appidArr[j], addressArr[k].split("\\.")[1]);
        				evidenceFace.initAppidRoute(appidArr[j], routeService, Web3j.build(channelEthereumService), (ECPrivateKey) key, addressArr[k].split("\\.")[1], topicList);
        				evidenceFace.loadPrivateKey(appidArr[j], (ECPrivateKey) key);
        				evidenceFace.run(appidArr[j], 1);
        			}
	        	}
        	}
        }

        CallbackImpl callback = new CallbackImpl();
        callback.setSDK(evidenceFace);
        evidenceFace.setPushCallback(callback);

        System.out.printf("appids:%s, routeAddress:%s. server start to listen topic:%s\n", appids, routeAddress, topics);
    }

    public static void main(String[]args)throws Exception{

        if (args.length < 3) {
            System.out.println("please input args:appids, routeAddress, topics");
            System.exit(0);
        }

        String appids = args[0];
        String routeAddress = args[1];
        String topics = args[2];
        server(appids, routeAddress, topics);
//        server("IDA9NiQu,IDA3hAXa", "IDA9NiQu.0xeea4ca21beb8c81a7d90757d6d2372a5f3b64345,IDA3hAXa.0xeea4ca21beb8c81a7d90757d6d2372a5f3b64345", "topic1000010002001");
    }
}
