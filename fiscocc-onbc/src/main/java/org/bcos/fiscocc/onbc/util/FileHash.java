package org.bcos.fiscocc.onbc.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.fisco.bcos.web3j.utils.Numeric;
import org.bouncycastle.jcajce.provider.digest.Keccak;

/**
 * <pre>
 * *********************************************
 * Copyright.
 * All rights reserved.
 * Description:参考web3sdk org.bcos.web3j.crypto.Hash，文件转hash
 * HISTORY
 * *********************************************
 *  ID     REASON        PERSON          DATE
 *  1      Create   	 darwin du       2018年6月9日
 * *********************************************
 * </pre>
 */
public class FileHash {
	
	/**
	 * 输入文件路径返回hash地址
	 * @date 2018年6月9日
	 * @author darwin du
	 * @param filePath
	 * @return
	 * @throws FileNotFoundException
	 */
	public static String sha3(String filePath) throws FileNotFoundException{    
		
		String hash = "";
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		//分多次将一个文件读入，对于大型文件而言，比较推荐这种方式，占用内存比较少。  
		byte[] buffer = new byte[1024];    
		try {    
			fis = new FileInputStream(filePath);
			bos = new ByteArrayOutputStream(1024 );
              
            int length = -1;    
            while ((length = fis.read(buffer, 0, 1024)) != -1) {    
            	bos.write(buffer, 0, length);
            }    
            buffer = bos.toByteArray();
        } catch (Exception e) {    
            e.printStackTrace();    
            return "";    
        }  finally {  
        	if (fis != null) {  
        		try {  
        			fis.close();  
        		} catch (IOException e1) {  
        			e1.printStackTrace();  
        		}  
        	}  
            if (bos != null) {  
                try {  
                    bos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
        }  
        hash = sha3(buffer);
        return hash;    
    }  
	
	/**
	 * 传入流返回hash
	 * @date 2018年6月9日
	 * @author darwin du
	 * @param input
	 * @return
	 */
	public static String sha3(byte[] input){
        if (input.length <= 0)
        {
            throw new java.lang.IllegalArgumentException();
        }
        byte[] output = sha3(input, 0, input.length);
        return Numeric.toHexString(output, 0, output.length, false);
    }

	/**
     * Keccak-256 hash function.
     * @param input binary encoded input data
     * @param offset of start of data
     * @param length of data
     * @return hash value
     */
    public static byte[] sha3(byte[] input, int offset, int length) {
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(input, offset, length);
        return kecc.digest();
    }
    
    public static void main(String[] args) throws Exception {
		
    	String[] filePath = new String[] {"D:\\data\\app\\logs\\bsp-ebcfs\\warn.log"};  
    	args = filePath ;
		if(args.length<1) {
			System.out.println("输入参数最小为4");
			System.exit(0);
		}
		String hash = sha3(args[0]);  
		System.out.println("hash:" + hash);
		System.exit(0);
	}
}
