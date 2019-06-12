package org.bcos.fiscocc.onbc.service;

import static org.bcos.fiscocc.onbc.util.Constants.CODE_ERR_S2001;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_ERR_S2004;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_ERR_S2006;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_MONI_10001;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_MONI_10002;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_ERR_S2001;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_ERR_S2004;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_ERR_S2006;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_MONI_10001;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_MONI_10002;
import static org.bcos.fiscocc.onbc.util.LogUtils.getErrorLogger;
import static org.bcos.fiscocc.onbc.util.LogUtils.getMonitorLogger;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.fiscocc.onbc.dao.EvidenceStatusInfoDAO;
import org.bcos.fiscocc.onbc.dto.ConfigInfoDTO;
import org.bcos.fiscocc.onbc.dto.EvidenceStatusInfo;
import org.bcos.fiscocc.onbc.runner.AppRunner;
import org.bcos.fiscocc.onbc.util.SignPulibcKeyUtil;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * 轮询未上链数据
 */
@Component
public class NewStatusService {
	
	private Logger logger = LoggerFactory.getLogger(NewStatusService.class);
	@Autowired
	private AppRunner appRunner;
	
	public void newCheckStatus() {
		logger.debug("轮询未上链数据start newCheckStatus.");

		Integer chainCountMax = Integer.valueOf(configInfoDTO.getChainCount());
		Integer selectCount = Integer.valueOf(configInfoDTO.getSelectCount());
		Integer chainTime = Integer.valueOf(configInfoDTO.getChainTime());
		Integer internalSleepTime = Integer.valueOf(configInfoDTO.getInternalSleepTime());

		String wbPublickey = this.configInfoDTO.getWbPublickey();
        //查询需要发送签名的appId
		String localAppid = appRunner.publicKeyAppIdMap.get(wbPublickey);
		
		String keyStorePath = env.getProperty("sdk.keyStorePath").split(":")[1];
		//通过私钥获取公钥
		String publicKeyStr = null;
		try {
			publicKeyStr = SignPulibcKeyUtil.getPublicKey(keyStorePath, env.getProperty("sdk.keyStorePassword"), env.getProperty("sdk.keyStorePassword"));
		} catch (Exception e) {
			logger.error("通过私钥文件获取当前机构公钥异常：", e);
		}
		//******************验证公私钥对
		if(wbPublickey.equals(publicKeyStr)) {

			// 获取未上链数据
			List<EvidenceStatusInfo> unChainInfoList = evidenceChainInfoDAO.getUnChainEvidence(chainCountMax, selectCount,chainTime,localAppid);
			if (unChainInfoList == null || unChainInfoList.isEmpty()) {
				logger.info("本次轮询未查出未上链数据.");
			} else {
				for (EvidenceStatusInfo unChainInfo : unChainInfoList) {
					try {
						Thread.sleep(internalSleepTime);
						if (verifierExecutor == null) {
							logger.debug("未配置线程池，单线程执行newStatus.");
							newStatus(unChainInfo, chainCountMax);
						} else {
							logger.debug("新线程开始处理newStatus.");
							verifierExecutor.execute(new Runnable() {
								@Override
								public void run() {
									newStatus(unChainInfo, chainCountMax);
								}
							});
						}
					} catch (RejectedExecutionException e) {
						logger.error("线程池已满，拒绝请求", e);
					} catch (java.lang.InterruptedException e) {
						logger.error("newCheckStatus InterruptedException", e);
						Thread.currentThread().interrupt();
					}
				}
			}
		} else {
			logger.error("通过私钥文件获取当前机构公钥，公钥与配置文件配置的公钥不匹配，请检查当前机构公私钥！");
		}
			
	}
	
	public void newStatus(EvidenceStatusInfo unChainInfo, Integer chainCountMax) {
		try {
			if (unChainInfo.getChainCount() < chainCountMax) {
				logger.info("未上链信息unChainInfo:{} ", unChainInfo.toString());
				// 更新请求上链次数
				logger.info("证据:{} 已请求上链次数为 {} 次", unChainInfo.getEvidenceID(), unChainInfo.getChainCount());
				Long lockIndex = evidenceChainInfoDAO.updateChainCountAndVersion(unChainInfo.getEvidenceID(),  unChainInfo.getChainCount() + 1, unChainInfo.getVersion());
				if(lockIndex <= 0L) {
					logger.info("#########证据:{} 已被执行", unChainInfo.getEvidenceID());
					return;
				}
				// 请求上链
				String signData = null;
				Address address = null;
				//setside==0，表示存证发起方为当前机构
				if (unChainInfo.getSetSide() == 0) {

					if(evidenceSDK == null)
						logger.error("evidenceSDK is null");
					//hash数据签名
					signData = evidenceSDK.signMessage(unChainInfo.getAppID(), unChainInfo.getEvidenceHash());
					logger.info("当前机构请求上链签名: 证据ID: {}, appId: {}, evidenceHash:{}, signData: {} ", unChainInfo.getEvidenceID(), unChainInfo.getAppID(), unChainInfo.getEvidenceHash(), signData);

					long stNewEvidence = System.currentTimeMillis();
					String exData = "hi";
					//证据上链，返回证据地址
					address = evidenceSDK.newEvidence(
							unChainInfo.getAppID(),
							BigInteger.valueOf(unChainInfo.getEvidenceID().longValue()),
							unChainInfo.getEvidenceHash(),
							unChainInfo.getEvidenceID().toString(),
							unChainInfo.getEvidenceID().toString(),
							signData);
					long etNewEvidence = System.currentTimeMillis();
					getMonitorLogger().info(CODE_MONI_10001, etNewEvidence-stNewEvidence, MSG_MONI_10001);
				} 
				//setside==1，表示存证发起方为其他机构---可以忽略该场景
				else {
					signData = unChainInfo.getSignData();
					logger.info("其他机构请求上链签名signData:{} ", signData);
					
					long stNewEvidence = System.currentTimeMillis();
					//证据上链，返回证据地址
					address = evidenceSDK.newEvidence(
							unChainInfo.getAppID(),
							BigInteger.valueOf(unChainInfo.getEvidenceID().longValue()),
							unChainInfo.getEvidenceHash(),
							unChainInfo.getEvidenceID().toString(),
							unChainInfo.getExData(),
							unChainInfo.getEvidenceID().toString(),
							signData);
					long etNewEvidence = System.currentTimeMillis();
					getMonitorLogger().info(CODE_MONI_10001, etNewEvidence-stNewEvidence, MSG_MONI_10001);
				}
				logger.info("Address111:{}", address);
				logger.info("证据:{} 上链返回address:{} 签名：{}", unChainInfo.getEvidenceID(), address.toString(), signData);
				
				// 上链结果更新DB
				evidenceChainInfoDAO.updateEvidenceAddress(unChainInfo.getEvidenceID(), address.toString());
				
				boolean signResult = true;
				// 发起方为其他机构时且需要代理机构签名，代理机构为当前机构---可以忽略该场景
				if (unChainInfo.getSetSide() == 1 && unChainInfo.getSignOrNot() == 0) {
					// 代理机构签名数据
		            String wbSignData = evidenceSDK.signMessage(unChainInfo.getAppID(), unChainInfo.getEvidenceHash());
		            logger.info("当前机构签名数据wbSignData:{} ", wbSignData);
		            // 发送签名数据上链
		            signResult = evidenceSDK.sendSignatureToBlockChain(unChainInfo.getAppID(), BigInteger.valueOf(unChainInfo.getEvidenceID()), address.toString(), unChainInfo.getEvidenceHash(), wbSignData);
				}
				
				if (signResult) {
					// 发送通知
					long stPushMsg = System.currentTimeMillis();
					notifyService.sendNotify(unChainInfo.getAppID(), BigInteger.valueOf(unChainInfo.getEvidenceID().longValue()), address.toString());
					long etPushMsg = System.currentTimeMillis();
					getMonitorLogger().info(CODE_MONI_10002, etPushMsg-stPushMsg, MSG_MONI_10002);
					
					// 更新通知次数
					evidenceChainInfoDAO.updateNotifyCount(unChainInfo.getEvidenceID(), 1);
	            } else {
	            	logger.error("业务appid:{} 证据:{} 当期机构发送签名数据上链错误！", unChainInfo.getAppID(), unChainInfo.getEvidenceID());
	            }
			} else {
				if (unChainInfo.getChainCount() == chainCountMax) {
					logger.info("证据:{} 上链未完成，请求次数已达 {} 次", unChainInfo.getEvidenceID(), chainCountMax);
					getErrorLogger().error(CODE_ERR_S2004, MSG_ERR_S2004);
				}
				evidenceChainInfoDAO.updateChainCount(unChainInfo.getEvidenceID(),  unChainInfo.getChainCount() + 1);
			}
		} catch (ExecutionException | SignatureException e) {
			logger.error("轮询未上链数据时证据:{} 上链异常", unChainInfo.getEvidenceID(), e);
			getErrorLogger().error(CODE_ERR_S2001, MSG_ERR_S2001);
		} catch (Exception e) {
			logger.error("轮询未上链数据:{} 异常", unChainInfo.getEvidenceID(), e);
			getErrorLogger().error(CODE_ERR_S2006, MSG_ERR_S2006);
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
	@Autowired
	private Environment env;
}
