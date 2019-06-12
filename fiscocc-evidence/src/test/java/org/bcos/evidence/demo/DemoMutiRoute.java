package org.bcos.evidence.demo;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.List;

import org.fisco.bcos.channel.client.Service;
import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.evidence.sdkImpl.EvidenceFaceImpl;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by suyuhui on 18/3/29.
 */
public class DemoMutiRoute {
	static Logger logger = LoggerFactory.getLogger(DemoMutiRoute.class);
	
    public static void server(String appids, String routeAddress, String topics) throws Exception {

        EvidenceFace evidenceFace = new EvidenceFaceImpl();
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = DemoMeshChain.class.getClassLoader().getResourceAsStream("tb.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());

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

        /*if (args.length < 3) {
            System.out.println("please input args:appids, routeAddress, topics");
            System.exit(0);
        }

        String appids = args[0];
        String routeAddress = args[1];
        String topics = args[2];
        server(appids, routeAddress, topics);*/
//        server("IDA1EQRG,IDALHM0x", "IDA1EQRG.0x39e951cae12235677d58905771430def526de05d,IDALHM0x.0x39c79f14c6969de7b382c7b4ee7142849b12f593", "topic1000010001001");
        server("IDA1EQRG,IDALHM0x", "IDA1EQRG.0xa7959b4d1df012c2fa156b918bbc666940722e7a,IDALHM0x.0xa7959b4d1df012c2fa156b918bbc666940722e7a", "topic1000010001001");
    }
}
