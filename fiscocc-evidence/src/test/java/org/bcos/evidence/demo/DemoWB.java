package org.bcos.evidence.demo;

import org.fisco.bcos.channel.client.Service;
import org.bcos.evidence.sdk.EvidenceData;
import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.evidence.sdkImpl.EvidenceFaceImpl;
import org.bcos.evidence.utils.SignersAddressConf;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.ECPrivateKey;

/**
 * Created by mingzhenliu on 2017/8/8.
 */
public class DemoWB {
    public static void main(String[]args)throws Exception{

        //new 一个证据上链sdk的实现类
        EvidenceFace evidenceFace = new EvidenceFaceImpl();

        //读取keystore里的EC证书私钥，用来初始化SDK的实现类
        //证书可以用java工具keytool生成
        // keytool -genkeypair -alias ec -keyalg EC -keysize 256 -sigalg SHA256withECDSA  -validity 365 -storetype JKS -keystore ectest.jks -storepass 123456
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = Demo.class.getClassLoader().getResourceAsStream("ectest.jks");
        ks.load(ksInputStream, "123456".toCharArray());
        Key key = ks.getKey("ec", "123456".toCharArray());
        evidenceFace.loadPrivateKey((ECPrivateKey) key);

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        evidenceFace.setService(service);
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setTimeout(2000);
        channelEthereumService.setChannelService(service);
        evidenceFace.setWeb3j(Web3j.build(channelEthereumService));
        SignersAddressConf signersAddressConf = context.getBean(SignersAddressConf.class);
        CallbackImpl callback = new CallbackImpl();
        evidenceFace.setPushCallback(callback);
        callback.setSDK(evidenceFace);

        evidenceFace.createSignersDataInstance(signersAddressConf.getEvidenceSignersDataAddress());

        //启动sdk线程
        evidenceFace.run();

        String hashValue = evidenceFace.sha3("publicAddressConf".getBytes());
        System.out.println("--------- hashValue "+ hashValue);
        System.out.println("--------- getPublickey "+ evidenceFace.getPublickey());
        String signatureData = evidenceFace.signMessage(hashValue);
        System.out.println("--------- verifySignedMessage "+   evidenceFace.verifySignedMessage("bea4eceef72c098bc3704e33fef967d28c645594a680ff84c478fed8b9d0e745","1b3ecef3eec4c994d685a3cb773e6af2654f788326ff5644790cc773629b96641745fe27fcb121a99e1dcf10c730e5a7f05aef8caeb776d82322705a15ccb21109"));

         Address addrss = evidenceFace.newEvidence(hashValue,
               "hello","hello",
              signatureData);
        System.out.println("--------- Message addrss "+ addrss.toString());

        EvidenceData evi = evidenceFace.getMessagebyHash("0xa199a1dd9d0747ee167fd8faa11263e57b85f0d6");
        //EvidenceData evi = evidenceFace.getMessagebyHash(addrss.toString());
        System.out.println("--------- Message "+ evi.getEvidenceHash());
        for (String str:evi.getSignatures()
                ) {
            System.out.println("--------- Message Signature "+ str);
            System.out.println("--------- Message Signature addr "+ evidenceFace.verifySignedMessage(hashValue,str));
        }

    }
}
