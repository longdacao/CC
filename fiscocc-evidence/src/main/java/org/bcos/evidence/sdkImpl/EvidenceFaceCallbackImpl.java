package org.bcos.evidence.sdkImpl;

import java.math.BigInteger;

import org.bcos.evidence.sdk.Callback;
import org.bcos.evidence.sdk.EvidenceData;
import org.bcos.evidence.sdk.EvidenceFace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * 实现一个自己的Callback，并Override其中的onPush方法
 */
public class EvidenceFaceCallbackImpl implements Callback {

    static Logger logger = LoggerFactory.getLogger(EvidenceFaceCallbackImpl.class);

    @Override
    public void onPush(String addressPara) {
        try {
            JSONObject json = JSONObject.parseObject(addressPara);
            String address = json.getString("evidenceAddress");
            logger.debug("CallbackImpl onPush.evidenceAddress:{}", address);
            //用证据地址获取证据
            EvidenceData evidenceData = evidenceFace.getMessagebyHash(address);
            //证据签名
            String signatureData = evidenceFace.signMessage(evidenceData.getEvidenceHash());
            //发送证据上链
            evidenceFace.sendSignatureToBlockChain(address, evidenceData.getEvidenceHash(), signatureData);
        } catch (Exception e) {
            logger.error("onPush exception", e);
        }
    }

    @Override
    public void onPush(String appid, BigInteger seq, String address) {
        try {
            //用证据地址获取证据
            logger.info("EvidenceFaceCallbackImpl onPush.appid:{}, seq:{}, address:{}", appid, seq, address);
            EvidenceData evidenceData = evidenceFace.getMessagebyHash(appid, seq, address);
            logger.info("EvidenceFaceCallbackImpl onPush.evidenceData: {}", JSONObject.toJSON(evidenceData));
            //证据签名
            String signatureData = evidenceFace.signMessage(appid, evidenceData.getEvidenceHash());
            logger.info("EvidenceFaceCallbackImpl onPush.signatureData: {}", signatureData);
            //发送证据上链
            evidenceFace.sendSignatureToBlockChain(appid, seq, address, evidenceData.getEvidenceHash(), signatureData);
        } catch (Exception e) {
            logger.error("onPush exception", e);
        }
    }

    public void setSDK(EvidenceFace evidenceFace) {
        this.evidenceFace = evidenceFace;
    }

    EvidenceFace evidenceFace;
}
