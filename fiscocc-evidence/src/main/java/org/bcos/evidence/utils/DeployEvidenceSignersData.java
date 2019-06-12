package org.bcos.evidence.utils;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.fisco.bcos.channel.client.Service;
import org.bcos.evidence.contract.EvidenceSignersData;
import org.bcos.evidence.sdkImpl.BCConstant;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.DynamicArray;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DeployEvidenceSignersData {
    static Logger logger = LoggerFactory.getLogger(DeployEvidenceSignersData.class);

    public static void main(String[] args) throws Exception {
        System.out.println("部署DeployEvidenceSignersData");

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContextInitSigners.xml");
        Service service = context.getBean(Service.class);
        service.run();
        PublicAddressConf conf = context.getBean(PublicAddressConf.class);
        Thread.sleep(3000);

        System.out.println("开始部署");
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        Web3j web3 = Web3j.build(channelEthereumService);
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream =  DeployEvidenceSignersData.class.getClassLoader().getResourceAsStream("szt.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());
        ECKeyPair keyPair = ECKeyPair.create(((ECPrivateKey) key).getS());

        System.out.println("===================================================================");
        Credentials credentials = Credentials.create(keyPair);
        ConcurrentHashMap<String, String> addressConf = conf.getAllPublicAddress();
        List<String> evidenceSigners = new ArrayList<String>(addressConf.values());
        try {
            EvidenceSignersData evidenceSignersData = EvidenceSignersData.deploy(web3, credentials, BCConstant.gasPrice, BCConstant.gasLimit,evidenceSigners).sendAsync().get();
            System.out.println("DeployEvidenceSignersData getContractAddress " + evidenceSignersData.getContractAddress());
            List signers = evidenceSignersData.getSigners().send();
            for (int i = 0; i < signers.size(); i++) {
                System.out.println("DeployEvidenceSignersData array[" + i + "] = " + signers.get(i));
            }
        } catch (Exception e) {
            System.out.println("error " + e);
        }
    }
}
