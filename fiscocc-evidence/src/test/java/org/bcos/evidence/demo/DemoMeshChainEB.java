package org.bcos.evidence.demo;

import org.fisco.bcos.channel.client.Service;
import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.evidence.sdkImpl.EvidenceFaceImpl;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suyuhui on 18/1/18.
 */
public class DemoMeshChainEB {

    public static void server(String appids, String routeAddress, String topics) throws Exception {

        EvidenceFace evidenceFace = new EvidenceFaceImpl();
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = DemoMeshChainEB.class.getClassLoader().getResourceAsStream("ectest_eb.jks");
        ks.load(ksInputStream, "Ebkj@2017".toCharArray());
        Key key = ks.getKey("ec", "Ebkj@2017".toCharArray());

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service routeService = context.getBean(Service.class);
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(routeService);

        List<String> topicList = new ArrayList<>();
        for(String topic : topics.split(",")) {
        	topicList.add(topic);
        }

        evidenceFace.initRoute(routeService, Web3j.build(channelEthereumService), (ECPrivateKey) key, routeAddress, topicList);
        for(String appid : appids.split(",")) {
            evidenceFace.loadPrivateKey(appid, (ECPrivateKey) key);
            evidenceFace.run(appid, 1);
            CallbackImpl callback = new CallbackImpl();
            callback.setSDK(evidenceFace);
            evidenceFace.setPushCallback(callback);
        }

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
    }
}
