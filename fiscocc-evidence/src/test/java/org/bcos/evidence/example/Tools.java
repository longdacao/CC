package org.bcos.evidence.example;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bcos.evidence.sdk.EvidenceData;
import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.evidence.sdkImpl.EvidenceFaceImpl;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Utf8String;

/**
 * <pre>
 * *********************************************
 * Copyright WeBank.
 * All rights reserved.
 * Description:
 * HISTORY
 * *********************************************
 *  ID     REASON        PERSON          DATE
 *  1      Create   	 darwin du       2018年6月1日
 * *********************************************
 * </pre>
 */
public class Tools {
	
	public static void main(String[] args) throws Exception {
		
		//demoSDKSign("ectest.jks", "IDA1EQRG", "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
		//demoSDKSign("user.jks", "IDALHM0x", "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
		
		newEvidence("szt.jks", "IDA1EQRG", "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee", 1L);
	}
	

	public static void demoSDKSign(String file, String appId, String hashValue) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        //new 一个证据上链sdk的实现类
        EvidenceFace evidenceFace = new EvidenceFaceImpl();

        //读取keystore里的EC证书私钥，用来初始化SDK的实现类
        //证书可以用java工具keytool生成
        // keytool -genkeypair -alias ec -keyalg EC -keysize 256 -sigalg SHA256withECDSA  -validity 365 -storetype JKS -keystore ectest.jks -storepass 123456
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = org.bcos.evidence.demo.Demo.class.getClassLoader().getResourceAsStream(file);
        ks.load(ksInputStream, "123456".toCharArray()); //123456是密码
        Key key = ks.getKey("ec", "123456".toCharArray());

        String appid = appId;//需要微众分配
        evidenceFace.loadPrivateKey(appid, (ECPrivateKey) key);

        //String plaintext = "hello world";//明文字符串
        //String hashValue = evidenceFace.sha3(plaintext.getBytes());//得到hash值
        String signatureData = evidenceFace.signMessage(appid, hashValue);//得到签名值

        System.out.println("signatureData is:" + signatureData);

    }
	
	public static void newEvidence(String file, String appid, String evidenceHash, Long evidenceID) throws Exception{
		
		//new 一个证据上链sdk的实现类
        EvidenceFace evidenceSDK = new EvidenceFaceImpl();
        
        //读取keystore里的EC证书私钥，用来初始化SDK的实现类
        //证书可以用java工具keytool生成
        // keytool -genkeypair -alias ec -keyalg EC -keysize 256 -sigalg SHA256withECDSA  -validity 365 -storetype JKS -keystore ectest.jks -storepass 123456
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = org.bcos.evidence.demo.Demo.class.getClassLoader().getResourceAsStream(file);
        ks.load(ksInputStream, "123456".toCharArray()); //123456是密码
        Key key = ks.getKey("ec", "123456".toCharArray());
        evidenceSDK.loadPrivateKey(appid, (ECPrivateKey) key);
        
		String signData = evidenceSDK.signMessage(appid, evidenceHash);
		//logger.info("当前机构请求上链签名: 证据ID: {}, appId: {}, evidenceHash:{}, signData: {} ", evidenceID, appid, evidenceHash, signData);

		//证据上链，返回证据地址
		Address address = evidenceSDK.newEvidence(
				appid,
				BigInteger.valueOf(evidenceID.longValue()),
				evidenceHash,
				evidenceID.toString(),
				evidenceID.toString(),
				signData);
		System.out.println("++++++address:" + address.toString());
		Map map = verifyEvidence(file, appid, evidenceID, address.toString());
		System.out.println("++++++map:" + map);
	}
	
	public static Map<String, Object> verifyEvidence(String file, String appId, Long evidenceId, String evidenceHash)  throws Exception{
		
		EvidenceFace evidenceSDK = new EvidenceFaceImpl();
		//读取keystore里的EC证书私钥，用来初始化SDK的实现类
        //证书可以用java工具keytool生成
        // keytool -genkeypair -alias ec -keyalg EC -keysize 256 -sigalg SHA256withECDSA  -validity 365 -storetype JKS -keystore ectest.jks -storepass 123456
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = org.bcos.evidence.demo.Demo.class.getClassLoader().getResourceAsStream(file);
        ks.load(ksInputStream, "123456".toCharArray()); //123456是密码
        Key key = ks.getKey("ec", "123456".toCharArray());
        evidenceSDK.loadPrivateKey(appId, (ECPrivateKey) key);
		
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> appIdPublicMap = new HashMap<String, Object>();
		EvidenceData data = null;
		try {
			// 获取证据详情
			data = evidenceSDK.getMessagebyHash(appId, BigInteger.valueOf(evidenceId), evidenceHash);
			map.put("evidenceData", data);

			// 获取签名信息
			StringBuilder sbSignData = new StringBuilder();
			// 已签名公钥
			ArrayList<String> signedList = new ArrayList<>();
			for (String str : data.getSignatures()) {
				sbSignData.append(str + "|");
				signedList.add(evidenceSDK.verifySignedMessage(appId, data.getEvidenceHash(), str));
			}
			
			//校验是否签名完成
			boolean verifyResult = evidenceSDK.verifyEvidence(appId, data);
			if (verifyResult) {
				map.put("isSigned", "true");
				map.put("unSignedInfo", appIdPublicMap);
				return map;
			}
			map.put("isSigned", "false");
			// 获取公钥
			for (String addr : data.getPublicKeys()) {
				boolean flag = false;
	            for (String str : signedList) {
	                if (str.equals(addr)) {
	                    flag = true;
	                    break;
	                }
	            }
	            // 存在未签名机构公钥
	            if (!flag) {
	            	//查询需要发送签名的appId
					//String appid = appRunner.publicKeyAppIdMap.get(addr);
					appIdPublicMap.put(addr, addr);
	            }
			}
			map.put("unSignedInfo", appIdPublicMap);
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (SignatureException e) {
		}
		return map;
	}
}
