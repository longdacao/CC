package org.bcos.evidence.sdk;


import java.math.BigInteger;

public interface Callback {
    void onPush(String address);
    void onPush(String appid, BigInteger seq, String address);
}
