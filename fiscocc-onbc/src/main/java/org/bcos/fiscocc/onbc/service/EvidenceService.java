package org.bcos.fiscocc.onbc.service;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bcos.evidence.sdk.EvidenceData;
import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.fiscocc.onbc.exception.BizException;
import org.bcos.fiscocc.onbc.runner.AppRunner;
import org.bcos.fiscocc.onbc.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <pre>
 * *********************************************
 * Copyright.
 * All rights reserved.
 * Description:
 * HISTORY
 * *********************************************
 *  ID     REASON        PERSON          DATE
 *  1      Create   	 darwin du       2018年5月31日
 * *********************************************
 * </pre>
 */
@Service
public class EvidenceService {
	
	private Logger logger = LoggerFactory.getLogger(EvidenceService.class);

	@Autowired
	private EvidenceFace evidenceSDK;
	@Autowired
	private AppRunner appRunner;
	
	/**
	 * 查询区块链证据明细
	 * @date 2018年5月31日
	 * @author darwin du
	 * @param appId
	 * @param seq
	 * @param address
	 */
	public EvidenceData findEvidenceInfoToChain(String appId, Long evidenceId, String evidenceAddress) throws Exception {
	
		EvidenceData data = null;
		try {
			//获取证据详情
			data = evidenceSDK.getMessagebyHash(appId, BigInteger.valueOf(evidenceId), evidenceAddress);
		} catch (InterruptedException e) {
			throw new BizException(Constants.FIND_EVIDENCE_EXP1.getErrorMsg(), Constants.FIND_EVIDENCE_EXP1.getErrorCode(), e);
		} catch (ExecutionException e) {
			throw new BizException(Constants.FIND_EVIDENCE_EXP2.getErrorMsg(), Constants.FIND_EVIDENCE_EXP2.getErrorCode(), e);
		}
		return data;
	}
	
	/**
	 * 校验签名数据
	 * @date 2018年6月5日
	 * @author darwin du
	 * @param appId
	 * @param seq
	 * @param address
	 * @return
	 */
	public Map<String, Object> verifyEvidence(String appId, Long evidenceId, String evidenceAddress) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> appIdPublicMap = new HashMap<String, Object>();
		EvidenceData data = null;
		try {
			// 获取证据详情
			data = evidenceSDK.getMessagebyHash(appId, BigInteger.valueOf(evidenceId), evidenceAddress);
			map.put("evidenceData", data);

			// 获取签名信息
			StringBuilder sbSignData = new StringBuilder();
			// 已签名公钥
			ArrayList<String> signedList = new ArrayList<>();
			for (String str : data.getSignatures()) {
				sbSignData.append(str + "|");
				signedList.add(evidenceSDK.verifySignedMessage(appId, data.getEvidenceHash(), str));
			}
			logger.info("证据:{} 已签名机构公钥:{}", evidenceId, signedList.toString());
			
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
					String appid = appRunner.publicKeyAppIdMap.get(addr);
					appIdPublicMap.put(addr, appid);
	            }
			}
			map.put("unSignedInfo", appIdPublicMap);
		} catch (InterruptedException e) {
			throw new BizException(Constants.FIND_EVIDENCE_EXP1.getErrorMsg(), Constants.FIND_EVIDENCE_EXP1.getErrorCode(), e);
		} catch (ExecutionException e) {
			throw new BizException(Constants.FIND_EVIDENCE_EXP2.getErrorMsg(), Constants.FIND_EVIDENCE_EXP2.getErrorCode(), e);
		} catch (SignatureException e) {
			throw new BizException(Constants.FIND_EVIDENCE_EXP3.getErrorMsg(), Constants.FIND_EVIDENCE_EXP3.getErrorCode(), e);
		}
		return map;
	}
}
