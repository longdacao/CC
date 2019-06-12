package org.bcos.evidence.utils;

import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


public class Tools {
    static public Sign.SignatureData stringToSignatureData(String signatureData)
    {
        byte[] byte_3 = Numeric.hexStringToByteArray(signatureData);
        byte[] signR = new byte[32];
        System.out.print("length of R:" + signR.length);
        System.out.print("### byte_3: " + byte_3.toString());
        System.arraycopy(byte_3, 1, signR, 0, signR.length);
        byte[] signS = new byte[32];
        System.arraycopy(byte_3, 1+signR.length, signS, 0, signS.length);
        return  new Sign.SignatureData(byte_3[0],signR,signS);
    }

    static public  String  signatureDataToString(Sign.SignatureData signatureData)
    {
        byte[] byte_3 = new byte[1+signatureData.getR().length+signatureData.getS().length];
        byte_3[0] = signatureData.getV();
        System.arraycopy(signatureData.getR(), 0, byte_3, 1, signatureData.getR().length);
        System.arraycopy(signatureData.getS(), 0, byte_3, signatureData.getR().length+1, signatureData.getS().length);
        return  Numeric.toHexString(byte_3,0,byte_3.length,false);
    }

    public  static String byte32ToString(byte[] data) throws UnsupportedEncodingException {
        int offset = searchByte(data,(byte)0);
        String info2 = new String(data, 0, offset,"UTF-8");
        return info2;
    }

    public  static int searchByte(byte[] data, byte value) {
        int size = data.length;
        for (int i = 0; i < size; ++i) {
            if (data[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
