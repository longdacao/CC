package org.bcos.fiscocc.onbc.service;

import static org.bcos.fiscocc.onbc.util.Constants.CODE_ERR_S2002;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_ERR_S2003;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_ERR_S2005;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_ERR_S2007;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_MONI_10002;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_MONI_10003;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_MONI_10004;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_ERR_S2002;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_ERR_S2003;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_ERR_S2005;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_ERR_S2007;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_MONI_10002;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_MONI_10003;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_MONI_10004;
import static org.bcos.fiscocc.onbc.util.LogUtils.getErrorLogger;
import static org.bcos.fiscocc.onbc.util.LogUtils.getMonitorLogger;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.bcos.evidence.sdk.EvidenceData;
import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.fiscocc.onbc.dao.EvidenceStatusInfoDAO;
import org.bcos.fiscocc.onbc.dto.ConfigInfoDTO;
import org.bcos.fiscocc.onbc.dto.EvidenceStatusInfo;
import org.bcos.fiscocc.onbc.runner.AppRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * 轮询未签名数据
 */
@Component
public class SignStatusService {
	
	private Logger logger = LoggerFactory.getLogger(SignStatusService.class);
	@Autowired
	private AppRunner appRunner;
	
	public void signCheckStatus() {
		logger.debug("轮询未签名数据start signCheckStatus.");
		
		Integer notifyCountMax = Integer.valueOf(configInfoDTO.getNodifyCount());
		Integer selectCount = Integer.valueOf(configInfoDTO.getSelectCount());
		Integer chainTime = Integer.valueOf(configInfoDTO.getChainTime());
		Integer internalSleepTime = Integer.valueOf(configInfoDTO.getInternalSleepTime());
			
		String wbPublickey = this.configInfoDTO.getWbPublickey();
        //查询需要发送签名的appId
		String localAppid = appRunner.publicKeyAppIdMap.get(wbPublickey);
		// 获取未签名数据
		List<EvidenceStatusInfo> unsignInfoList = evidenceChainInfoDAO.getUnsignEvidence(notifyCountMax, selectCount,chainTime,localAppid);
		if (unsignInfoList == null || unsignInfoList.isEmpty()) {
			logger.info("本次轮询未查出未签名数据.");
		} else {
			for (EvidenceStatusInfo unsignInfo : unsignInfoList) {
				try {
					Thread.sleep(internalSleepTime);
					if (verifierExecutor == null) {
						logger.debug("未配置线程池，单线程执行signStatus.");
						signStatus(unsignInfo, notifyCountMax);
					} else {
						logger.debug("新线程开始处理signStatus.");
						verifierExecutor.execute(new Runnable() {
							@Override
							public void run() {
								signStatus(unsignInfo, notifyCountMax);
							}
						});
					}
				} catch (RejectedExecutionException e) {
					logger.error("线程池已满，拒绝请求", e);
				} catch (java.lang.InterruptedException e) {
					logger.error("signCheckStatus InterruptedException", e);
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public void signStatus(EvidenceStatusInfo unsignInfo,Integer notifyCountMax) {
		try {
			logger.info("未签名信息unsignInfo:{} ", unsignInfo.toString());
			// 更新通知次数
			logger.info("证据:{} 已通知次数为 {} 次", unsignInfo.getEvidenceID(), unsignInfo.getNotifyCount());
			Long lockIndex = evidenceChainInfoDAO.updateNotifyCountAndVersion(unsignInfo.getEvidenceID(), unsignInfo.getNotifyCount() + 1, unsignInfo.getVersion());
			if(lockIndex <= 0L) {
				logger.info("#########证据:{} 已被执行", unsignInfo.getEvidenceID());
				return;
			}
			
			// 获取签名数据
			long stGetEvidence = System.currentTimeMillis();
			EvidenceData data = evidenceSDK.getMessagebyHash(unsignInfo.getAppID(), BigInteger.valueOf(unsignInfo.getEvidenceID().longValue()), unsignInfo.getEvidenceAddress());
			long etGetEvidence = System.currentTimeMillis();
			getMonitorLogger().info(CODE_MONI_10003, etGetEvidence-stGetEvidence, MSG_MONI_10003);
			
			// 获取签名信息
			StringBuilder sbSignData = new StringBuilder();
			//已签名公钥
			ArrayList<String> signedList = new ArrayList<>();
			for (String str : data.getSignatures()) {
				sbSignData.append(str + "|");
				signedList.add(evidenceSDK.verifySignedMessage(unsignInfo.getAppID(), data.getEvidenceHash(), str));
			}
			logger.info("证据:{} 已签名机构公钥:{}", unsignInfo.getEvidenceID(), signedList.toString());
			
			// 校验签名数据
			long stVerifyEvidence = System.currentTimeMillis();
			boolean verifyResult = evidenceSDK.verifyEvidence(unsignInfo.getAppID(), data);
			long etVerifyEvidence = System.currentTimeMillis();
			getMonitorLogger().info(CODE_MONI_10004, etVerifyEvidence-stVerifyEvidence, MSG_MONI_10004);
			
			// 判断签名校验结果
			if (verifyResult) {
				logger.info("签名信息sbSignData:{} ", sbSignData.toString());
				evidenceChainInfoDAO.updateSignInfo(unsignInfo.getEvidenceID(), sbSignData.deleteCharAt(sbSignData.length()-1).toString());
				logger.info("更新证据:{} 签名状态已完成", unsignInfo.getEvidenceID());
				evidenceChainInfoDAO.updateNotifyCount(unsignInfo.getEvidenceID(), unsignInfo.getNotifyCount());
			} else if (unsignInfo.getNotifyCount() < notifyCountMax) {
				// 获取appid对应机构公钥及topic
				String[] publickeysArr = configInfoDTO.getPublickeys().split(",");
				
				Map<String, String> keyMap = new  ConcurrentHashMap<String, String>();
				for (int i = 0; i < publickeysArr.length; i++) {
					if (unsignInfo.getAppID().equals(publickeysArr[i].split("\\.")[0])) {
						keyMap.put(publickeysArr[i].split("\\.")[1], publickeysArr[i].split("\\.")[0]);
						break;
					}
				}
				logger.info("配置文件机构公钥keyMap:{} ", keyMap);
				
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
		            	logger.info("证据:{} 未签名机构公钥:{}", unsignInfo.getEvidenceID(), addr);
		            	// 发起方为其他机构时且需要当期机构签名且未签名公钥为当期机构公钥---可以忽略该场景
						if (unsignInfo.getSetSide() == 1 && unsignInfo.getSignOrNot() == 0 && addr.equals(configInfoDTO.getWbPublickey())) {
							// 当期机构签名数据
				            String wbSignData = evidenceSDK.signMessage(unsignInfo.getAppID(), unsignInfo.getEvidenceHash());
				            logger.info("当期机构签名数据wbSignData:{} ", wbSignData);
				            // 发送证据上链
				            evidenceSDK.sendSignatureToBlockChain(unsignInfo.getAppID(), BigInteger.valueOf(unsignInfo.getEvidenceID()), unsignInfo.getEvidenceAddress(), unsignInfo.getEvidenceHash(), wbSignData);
						} 
						// 发起方为其他机构时且不需要当前机构签名且公钥为当前机构---可以忽略该场景
						else if (unsignInfo.getSetSide() == 1 && unsignInfo.getSignOrNot() == 1 && addr.equals(configInfoDTO.getWbPublickey())) {
							logger.error("业务appid:{} 证据:{} 需要当期机构签名，请确认请求和配置！", unsignInfo.getAppID(), unsignInfo.getEvidenceID());
						} 
						else {
							boolean signResult = true;
							// 发起方为其他机构时且需要当期机构签名且已签名公钥列表不包括当期机构公钥---可以忽略该场景
							if (unsignInfo.getSetSide() == 1 && unsignInfo.getSignOrNot() == 0 && !signedList.contains(configInfoDTO.getWbPublickey())) {
								// 当期机构签名数据
					            String wbSignData = evidenceSDK.signMessage(unsignInfo.getAppID(), unsignInfo.getEvidenceHash());
					            logger.info("当期机构签名数据wbSignData:{} ", wbSignData);
					            // 发送证据上链
					            signResult = evidenceSDK.sendSignatureToBlockChain(unsignInfo.getAppID(), BigInteger.valueOf(unsignInfo.getEvidenceID()), unsignInfo.getEvidenceAddress(), unsignInfo.getEvidenceHash(), wbSignData);
							}
							if (signResult) {
								//查询需要发送签名的appId
								String appid = appRunner.publicKeyAppIdMap.get(addr);
								//获取topic
								String topic = appRunner.appIdTopicMap.get(appid);
								if (topic != null) {
									// 重发通知
									long stPushMsg = System.currentTimeMillis();
									notifyService.sendNotify(unsignInfo.getAppID(), appid, BigInteger.valueOf(unsignInfo.getEvidenceID().longValue()), unsignInfo.getEvidenceAddress(), topic);
									long etPushMsg = System.currentTimeMillis();
									getMonitorLogger().info(CODE_MONI_10002, etPushMsg-stPushMsg, MSG_MONI_10002);
								} else {
									logger.error("业务appid:{} 证据:{} 参数sdk.publickeys公钥:{} 对应配置错误！", unsignInfo.getAppID(), unsignInfo.getEvidenceID(),  addr);
								}
							} else {
								logger.error("业务appid:{} 证据:{} 当期机构发送签名数据上链错误！", unsignInfo.getAppID(), unsignInfo.getEvidenceID());
							}
						}
		            }
		        }
			} else {
				if (unsignInfo.getNotifyCount() == notifyCountMax) {
					logger.info("证据:{} 签名未完成，通知次数已达 {} 次", unsignInfo.getEvidenceID(), notifyCountMax);
					getErrorLogger().error(CODE_ERR_S2005, MSG_ERR_S2005);
				}
			}
		} catch (ExecutionException e) {
			logger.error("轮询未签名数据时证据:{} 获取签名数据异常", unsignInfo.getEvidenceID(), e);
			getErrorLogger().error(CODE_ERR_S2002, MSG_ERR_S2002);
		} catch (SignatureException e) {
			logger.error("轮询未签名数据时证据:{} 验证签名异常", unsignInfo.getEvidenceID(), e);
			getErrorLogger().error(CODE_ERR_S2003, MSG_ERR_S2003);
		} catch (Exception e) {
			logger.error("轮询未签名数据:{} 异常", unsignInfo.getEvidenceID(), e);
			getErrorLogger().error(CODE_ERR_S2007, MSG_ERR_S2007);
		}
	}

	@Autowired
	private EvidenceFace evidenceSDK;
	@Autowired
	private EvidenceStatusInfoDAO evidenceChainInfoDAO;
	@Autowired
	private NotifyService notifyService;
	@Autowired
	private ThreadPoolTaskExecutor verifierExecutor;
	@Autowired
	private ConfigInfoDTO configInfoDTO;
}
