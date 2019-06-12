package org.bcos.fiscocc.onbc.util;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.ECPrivateKey;

import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;


/**
 * <pre>
 * *********************************************
 * Copyright.
 * All rights reserved.
 * Description: 生成公钥工具
 * HISTORY
 * *********************************************
 *  ID     REASON        PERSON          DATE
 *  1      Create   	 darwin du       2018年5月30日
 * *********************************************
 * </pre>
 */
public class SignPulibcKeyUtil {

	public static void main(String[] args) throws Exception {
		
		//String[] aa = new String[]{"getPublicKey","szt.jks","123456","123456"};
		//args=aa;
		if(args.length<4) {
			System.out.println("输入参数最小为4");
			System.exit(0);
		}
		switch (args[0]) {
		 	case "getPublicKey":
		 		String publicKey=getPublicKey(args[1], args[2], args[3]);
		 		System.out.println("publicKey:"+publicKey);
		 		break;
		 	default:
                 break;
		}
		System.exit(0);
	}
	
	public static Credentials loadkey(String keyStoreFileName,String keyStorePassword, String keyPassword) throws Exception{
    	InputStream ksInputStream = null;
    	try {
    		 KeyStore ks = KeyStore.getInstance("JKS");
    		 ksInputStream =  SignPulibcKeyUtil.class.getClassLoader().getResourceAsStream(keyStoreFileName);
    		 ks.load(ksInputStream, keyStorePassword.toCharArray());
    		 Key key = ks.getKey("ec", keyPassword.toCharArray());
    		 ECKeyPair keyPair = ECKeyPair.create(((ECPrivateKey) key).getS());
    		 Credentials credentials = Credentials.create(keyPair);	
    		 if(credentials!=null){
    		    return credentials;
    		 }else{
    			 System.out.println("秘钥参数输入有误！");
    		 }
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			ksInputStream.close();
		}
	    return null;
    }
    
    public static String getPublicKey(String keyStoreFileName,String keyStorePassword, String keyPassword) throws Exception{
    	Credentials credentials=loadkey(keyStoreFileName, keyStorePassword, keyPassword);
    	return credentials.getAddress();
    }
}
