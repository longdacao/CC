package org.bcos.evidence.demo;

import org.fisco.bcos.channel.client.Service;
import org.bcos.evidence.contract.EvidenceSignersData;
import org.bcos.evidence.sdk.EvidenceData;
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


public class Demo {
    public static void main(String[]args)throws Exception{

        //new 一个证据上链sdk的实现类
        EvidenceFace evidenceFace = new EvidenceFaceImpl();

        //读取keystore里的EC证书私钥，用来初始化SDK的实现类
        //证书可以用java工具keytool生成
        // keytool -genkeypair -alias ec -keyalg EC -keysize 256 -sigalg SHA256withECDSA  -validity 365 -storetype JKS -keystore ectest.jks -storepass 123456
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = Demo.class.getClassLoader().getResourceAsStream("ectestgz.jks");
        ks.load(ksInputStream, "gzyijian@123".toCharArray());
        Key key = ks.getKey("ec", "gzyijian@123".toCharArray());
        evidenceFace.loadPrivateKey((ECPrivateKey) key);

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        Service service = context.getBean(Service.class);
        evidenceFace.setService(service);
        ChannelEthereumService channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        evidenceFace.setWeb3j(Web3j.build(channelEthereumService));
        CallbackImpl callback = new CallbackImpl();
        evidenceFace.setPushCallback(callback);
        callback.setSDK(evidenceFace);

        //启动sdk线程
        evidenceFace.run();
        String hashValue = evidenceFace.sha3(new String("publicAddressConf").getBytes());
        System.out.println("--------- hashValue "+ hashValue);
        System.out.println("--------- getPublickey "+ evidenceFace.getPublickey());
        String signatureData = evidenceFace.signMessage(hashValue);
        System.out.println("--------- signatureData "+ signatureData);


        EvidenceData evi = evidenceFace.getMessagebyHash("0x41c67d6535b0c08ecd55f118aa7cf78041d7e3fd");
        System.out.println("--------- Message "+ evi.getEvidenceHash());
        for (String str:evi.getSignatures()
                ) {
            System.out.println("--------- Message Signature "+ str);
            System.out.println("--------- Message Signature addr "+ evidenceFace.verifySignedMessage(evi.getEvidenceHash(),str));
        }
        for (String str:evi.getPublicKeys()
                ) {
            System.out.println("--------- Message Signature "+ str);
        }


        //boolean b = evidenceFace.sendSignatureToBlockChain("0xa199a1dd9d0747ee167fd8faa11263e57b85f0d6","37822db715b9b16ae970251e247466bf42937e1ffa5c2c7f94629e5e07204a74","1b7afda0a52f6ef83f59164a9429fd3c015062d9b964c3c7b81eb82cae819a0bb9363a8bfa1a82b409e16591ff5bd73bfdd6a104df4f205afc357b845685eed3df");
        //System.out.println("--------- Message Signature {}"+ b);
    }
}
