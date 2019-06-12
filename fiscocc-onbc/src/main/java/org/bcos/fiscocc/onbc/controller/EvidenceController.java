package org.bcos.fiscocc.onbc.controller;

import static org.bcos.fiscocc.onbc.util.Constants.APPID_ERROR;
import static org.bcos.fiscocc.onbc.util.Constants.APPID_UN_EMPTY;
import static org.bcos.fiscocc.onbc.util.Constants.CHAIN_ARRIVE_LIMIT;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_ERR_S2008;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_ERR_S2009;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_MONI_10006;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_MONI_10007;
import static org.bcos.fiscocc.onbc.util.Constants.EVIDENCE_NOT_EXIST;
import static org.bcos.fiscocc.onbc.util.Constants.EVIDENCE_UN_CHAIN;
import static org.bcos.fiscocc.onbc.util.Constants.EVIDENCE_UN_SIGN;
import static org.bcos.fiscocc.onbc.util.Constants.ID_NO_UN_EMPTY;
import static org.bcos.fiscocc.onbc.util.Constants.ID_TYPE_UN_EMPTY;
import static org.bcos.fiscocc.onbc.util.Constants.ID_UN_MATCHED;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_ERR_S2008;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_ERR_S2009;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_MONI_10006;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_MONI_10007;
import static org.bcos.fiscocc.onbc.util.Constants.OVER_MAXID;
import static org.bcos.fiscocc.onbc.util.Constants.QUERY_ID_UN_EMPTY;
import static org.bcos.fiscocc.onbc.util.Constants.SET_HASH_UN_EMPTY;
import static org.bcos.fiscocc.onbc.util.Constants.SIGN_ARRIVE_LIMIT;
import static org.bcos.fiscocc.onbc.util.Constants.SUCCESS;
import static org.bcos.fiscocc.onbc.util.Constants.SYSTEM_EXCEPTION;
import static org.bcos.fiscocc.onbc.util.Constants.USER_NAME_UN_EMPTY;
import static org.bcos.fiscocc.onbc.util.LogUtils.getErrorLogger;
import static org.bcos.fiscocc.onbc.util.LogUtils.getMonitorLogger;

import java.util.Arrays;
import java.util.Map;

import org.bcos.evidence.sdk.EvidenceData;
import org.bcos.fiscocc.onbc.dao.EvidenceStatusInfoDAO;
import org.bcos.fiscocc.onbc.dto.ConfigInfoDTO;
import org.bcos.fiscocc.onbc.dto.EvidenceInfoParam;
import org.bcos.fiscocc.onbc.dto.EvidenceStatusInfo;
import org.bcos.fiscocc.onbc.dto.PageData;
import org.bcos.fiscocc.onbc.entity.base.BaseRspEntity;
import org.bcos.fiscocc.onbc.entity.req.ReqGetEvidenceEntity;
import org.bcos.fiscocc.onbc.entity.req.ReqSetEvidenceEntity;
import org.bcos.fiscocc.onbc.entity.rsp.RspGetEvidenceEntity;
import org.bcos.fiscocc.onbc.entity.rsp.RspSetEvidenceEntity;
import org.bcos.fiscocc.onbc.exception.BizException;
import org.bcos.fiscocc.onbc.service.EvidenceService;
import org.bcos.fiscocc.onbc.util.Constants;
import org.bcos.fiscocc.onbc.util.ResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;

@SpringBootApplication
@RestController
@RequestMapping(value = "fiscocc-onbc/evidence")
public class EvidenceController {
	
	private static Logger logger = LoggerFactory.getLogger(EvidenceController.class);
	
	@Autowired
	private EvidenceService evidenceService;

	@RequestMapping("hi")
	@ResponseBody
	public String hi() {
		return "hello world !";
	}
	
	/**
	 * 存证
	 * 
	 * @param req
	 * @param result
	 * @return
	 */
	@ResponseBody
	@PostMapping(value = "/setevidence")
	public BaseRspEntity setEvidence(@Validated @RequestBody ReqSetEvidenceEntity req, BindingResult result) {
		long startTime = System.currentTimeMillis();
		logger.info("存证setEvidence reqEntity:{}", JSON.toJSONString(req));
		// 初始化返回实体类
		BaseRspEntity rspEntity = new BaseRspEntity(SUCCESS);
		RspSetEvidenceEntity resp = new RspSetEvidenceEntity();
		try {
			String appid = req.getAppId();
			// 非空校验
			if (StringUtils.isEmpty(appid)) {
				rspEntity = new BaseRspEntity(APPID_UN_EMPTY);
				return rspEntity;
			}
			/*if (req.getUserInfo().getCustomerType() == null || req.getUserInfo().getCustomerType() == "") {
				rspEntity = new BaseRspEntity(USER_TYPE_UN_EMPTY);
				return rspEntity;
			}*/
			if (StringUtils.isEmpty(req.getUserInfo().getUserName())) {
				rspEntity = new BaseRspEntity(USER_NAME_UN_EMPTY);
				return rspEntity;
			}
			if (StringUtils.isEmpty(req.getUserInfo().getIdentificationType())) {
				rspEntity = new BaseRspEntity(ID_TYPE_UN_EMPTY);
				return rspEntity;
			}
			if (StringUtils.isEmpty(req.getUserInfo().getIdentificationNo())) {
				rspEntity = new BaseRspEntity(ID_NO_UN_EMPTY);
				return rspEntity;
			}
			if (StringUtils.isEmpty(req.getHash())) {
				rspEntity = new BaseRspEntity(SET_HASH_UN_EMPTY);
				return rspEntity;
			}
//			if (req.getExData() == null || req.getExData() == "") {
//				rspEntity = new BaseRspEntity(SET_EXDATA_UN_EMPTY);
//				return rspEntity;
//			}
			// 业务id校验
			String[] appidArr = this.configInfoDTO.getAppids().split(",");
			if (!Arrays.asList(appidArr).contains(appid)) {
				logger.info("setevidence时appid:{} 错误", appid);
				rspEntity = new BaseRspEntity(APPID_ERROR);
				return rspEntity;
			}
			// 身份证校验
			/*String IDStr = req.getUserInfo().getIdentificationNo();
			if ("0".equals(req.getUserInfo().getCustomerType()) && "0".equals(req.getUserInfo().getIdentificationType())) {
				String checkResult = UserNoUtils.IDCardValidate(IDStr);
				if (!"".equals(checkResult)) {
					logger.info("setevidence时身份证:{} 错误:{} ", IDStr, checkResult);
					rspEntity = new BaseRspEntity(IDENTIFICATION_NO_ERROR);
					return rspEntity;
				}
			}*/
			
			EvidenceStatusInfo evidenceStatusInfo_ = evidenceStatusInfoDAO.getMaxId();
			Long size = evidenceStatusInfo_ == null ?  0L : evidenceStatusInfo_.getEvidenceID();
			//证据ID值大于100000，请扩容证据链
			if(size > this.configInfoDTO.getEvidenceMaxId()) {
				rspEntity = new BaseRspEntity(OVER_MAXID);
				return rspEntity;
			}
			
			// 封装数据库参数
			EvidenceStatusInfo EvidenceStatusInfo = new EvidenceStatusInfo();
			EvidenceStatusInfo.setAppID(req.getAppId());
			EvidenceStatusInfo.setUserInfo(req.getUserInfo().toString());
			//0：个人；1企业；默认企业
			EvidenceStatusInfo.setCustomerType("1");
			EvidenceStatusInfo.setUserName(req.getUserInfo().getUserName());
			EvidenceStatusInfo.setIdentificationType(req.getUserInfo().getIdentificationType());
			EvidenceStatusInfo.setIdentificationNo(req.getUserInfo().getIdentificationNo());
			EvidenceStatusInfo.setEvidenceHash(req.getHash());
			EvidenceStatusInfo.setExData(req.getExData());
			// 证据入库
			evidenceStatusInfoDAO.addEvidence(EvidenceStatusInfo);

			// 返回实体设置
			logger.debug("存证setEvidence EvidenceID:{}", EvidenceStatusInfo.getEvidenceID());
			resp.setEvidenceId(String.valueOf(EvidenceStatusInfo.getEvidenceID()));
			rspEntity.setData(resp);
			long endTime = System.currentTimeMillis();
			getMonitorLogger().info(CODE_MONI_10006, endTime - startTime, MSG_MONI_10006);
			return rspEntity;

		} catch (Exception e) {
			logger.error("setevidence证据hash:{} 处理异常", req.getHash(), e);
			getErrorLogger().error(CODE_ERR_S2008, MSG_ERR_S2008);
			rspEntity = new BaseRspEntity(SYSTEM_EXCEPTION);
			return rspEntity;
		}
	}


	/**
	 * 查询证据地址-查询本地证据地址
	 * @param req
	 * @param result
	 * @return
	 */
	@ResponseBody
	@PostMapping(value = "/getevidence")
	public BaseRspEntity getEvidence(@Validated @RequestBody ReqGetEvidenceEntity req, BindingResult result) {
		long startTime = System.currentTimeMillis();
		logger.info("取证getEvidence reqEntity:{}", JSON.toJSONString(req));
		// 初始化返回实体类
		BaseRspEntity rspEntity = new BaseRspEntity(SUCCESS);
		RspGetEvidenceEntity resp = new RspGetEvidenceEntity();
		try {
			// 参数校验
			String evidenceId = req.getEvidenceId();
			if (StringUtils.isEmpty(evidenceId)) {
				logger.info("getevidence时证据evidenceID为空！");
				rspEntity = new BaseRspEntity(QUERY_ID_UN_EMPTY);
				return rspEntity;
			}
			
			// 校验身份证
			/*String IDStr = req.getUserInfo().getIdentificationNo();
			if ("0".equals(req.getUserInfo().getCustomerType()) && "0".equals(req.getUserInfo().getIdentificationType())) {
				String checkResult = UserNoUtils.IDCardValidate(IDStr);
				if (!"".equals(checkResult)) {
					logger.info("getevidence时身份证:{} 错误:{} ", IDStr, checkResult);
					rspEntity = new BaseRspEntity(IDENTIFICATION_NO_ERROR);
					return rspEntity;
				}
			}*/
			// 获取配置信息
			int chainCountMax = Integer.valueOf(configInfoDTO.getChainCount());
			int notifyCountMax = Integer.valueOf(configInfoDTO.getNodifyCount());
			// 根据id查询证据信息
			String appID = req.getAppId();
			EvidenceStatusInfo evidenceInfo = new EvidenceStatusInfo();
			evidenceInfo = evidenceStatusInfoDAO.getEvidenceInfo(Long.valueOf(evidenceId));
			if (evidenceInfo == null) {
				logger.info("getevidence证据evidenceID:{} 不存在！", evidenceId);
				rspEntity = new BaseRspEntity(EVIDENCE_NOT_EXIST);
				long endTime = System.currentTimeMillis();
				getMonitorLogger().info(CODE_MONI_10007, endTime - startTime, MSG_MONI_10007);
				return rspEntity;
			} else if (!appID.equals(evidenceInfo.getAppID())) {
				logger.info("getevidence证据evidenceID:{} 不匹配！", evidenceId);
				rspEntity = new BaseRspEntity(ID_UN_MATCHED);
				long endTime = System.currentTimeMillis();
				getMonitorLogger().info(CODE_MONI_10007, endTime - startTime, MSG_MONI_10007);
				return rspEntity;
			} else if (StringUtils.isEmpty(evidenceInfo.getEvidenceAddress())) {
				if (evidenceInfo.getChainCount() <= chainCountMax) {
					logger.info("getevidence证据evidenceID:{} 还未上链！", evidenceId);
					rspEntity = new BaseRspEntity(EVIDENCE_UN_CHAIN);
				} else {
					logger.info("getevidence证据evidenceID:{} 还未上链，并且请求次数已达上限！", evidenceId);
					rspEntity = new BaseRspEntity(CHAIN_ARRIVE_LIMIT);
				}
				long endTime = System.currentTimeMillis();
				getMonitorLogger().info(CODE_MONI_10007, endTime - startTime, MSG_MONI_10007);
				return rspEntity;
			} else if ((!StringUtils.isEmpty(evidenceInfo.getEvidenceAddress())) && evidenceInfo.getSignFlag() == 0) {
				if (evidenceInfo.getNotifyCount() <= notifyCountMax) {
					logger.info("getevidence证据evidenceID:{} 还未签名！", evidenceId);
					rspEntity = new BaseRspEntity(EVIDENCE_UN_SIGN);
				} else {
					logger.info("getevidence证据evidenceID:{} 还未签名，并且请求次数已达上限！", evidenceId);
					rspEntity = new BaseRspEntity(SIGN_ARRIVE_LIMIT);
				}
				long endTime = System.currentTimeMillis();
				getMonitorLogger().info(CODE_MONI_10007, endTime - startTime, MSG_MONI_10007);
				return rspEntity;
			} else {
				resp.setEvidenceAddress(evidenceInfo.getEvidenceAddress());
				rspEntity.setData(resp);
				long endTime = System.currentTimeMillis();
				getMonitorLogger().info(CODE_MONI_10007, endTime - startTime, MSG_MONI_10007);
				return rspEntity;
			}

		} catch (Exception e) {
			logger.error("getevidence证据evidenceID:{} 处理异常", req.getEvidenceId(), e);
			getErrorLogger().error(CODE_ERR_S2009, MSG_ERR_S2009);
			rspEntity = new BaseRspEntity(SYSTEM_EXCEPTION);
			return rspEntity;
		}
	}
	
	/**
	 * 通过查询条件获取证据信息
	 * @param req
	 * @param result
	 * @return
	 */
	/*@ResponseBody
	@PostMapping(value = "/getEvidenceByQueryType")
	public BaseRspEntity getEvidenceByQueryType(@Validated @RequestBody ReqGetEvidenceEntity req, BindingResult result) {
		long startTime = System.currentTimeMillis();
		logger.info("取证getEvidenceByQueryType reqEntity:{}", JSON.toJSONString(req));
		// 初始化返回实体类
		BaseRspEntity rspEntity = new BaseRspEntity(SUCCESS);
		RspGetEvidenceByQueryType resp = new RspGetEvidenceByQueryType();
		String queryType = req.getQueryType();
		String queryParam = req.getQueryParam();
		if (queryType == null || queryType == "") {
			logger.info("getEvidenceByQueryType时queryType为空！");
			rspEntity = new BaseRspEntity(QUERY_TYPE_UN_EMPTY);
			return rspEntity;
		}
		if (queryParam == null || queryParam == "") {
			logger.info("getEvidenceByQueryType时queryParam为空！");
			rspEntity = new BaseRspEntity(QUERY_PARAM_UN_EMPTY);
			return rspEntity;
		}
		try {
			// 获取证据列表
			List<RspEvidenceInfo> evidenceList = evidenceStatusInfoDAO.getEvidenceListByQueryType(queryType, queryParam);
//			DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//			System.out.println(sdf.format(evidenceList.get(0).getEvidenceCreateTime()));
			// 返回实体设置
			resp.setEvidenceList(evidenceList);
			rspEntity.setData(resp);
			long endTime = System.currentTimeMillis();
			getMonitorLogger().info(CODE_MONI_10008, endTime - startTime, MSG_MONI_10008);
			return rspEntity;
		} catch (Exception e) {
			logger.error("getEvidenceByQueryType处理异常 查询方式:{} 查询参数:{} ", queryType, queryParam, e);
			getErrorLogger().error(CODE_ERR_S2010, MSG_ERR_S2010);
			rspEntity = new BaseRspEntity(SYSTEM_EXCEPTION);
			return rspEntity;
		}
	}*/
	
	/**
	 * 通过hash验证证据是否签名完成
	 * 
	 * @param req
	 * @param result
	 * @return
	 */
	/*@ResponseBody
	@PostMapping(value = "/checkEvidenceByHash")
	public BaseRspEntity checkEvidenceByHash(@Validated @RequestBody ReqGetEvidenceEntity req, BindingResult result) {
		long startTime = System.currentTimeMillis();
		logger.info("取证checkEvidenceByHash reqEntity:{}", JSON.toJSONString(req));
		// 初始化返回实体类
		BaseRspEntity rspEntity = new BaseRspEntity(SUCCESS);
		RspCheckEvidenceByHash resp = new RspCheckEvidenceByHash();
		String hash = req.getHash();
		if (hash == null || hash == "") {
			logger.info("checkEvidenceByHash 时 hash为空！");
			rspEntity = new BaseRspEntity(HASH_UN_EMPTY);
			return rspEntity;
		}
		try {
			// 获取证据
			List<EvidenceStatusInfo> evidenceList = evidenceStatusInfoDAO.getEvidenceInfoByHash(hash);
			
			if (evidenceList == null || evidenceList.size() == 0) {
				logger.info("checkEvidenceByHash时hash:{} 不存在！", hash);
				resp.setCheckResult(HASH_NOT_EXIST);
				rspEntity.setData(resp);
				long endTime = System.currentTimeMillis();
				getMonitorLogger().info(CODE_MONI_10009, endTime - startTime, CODE_MONI_10009);
				return rspEntity;
			} else {
				// 定义是否已签名变量
				boolean checkResult = false;
				for (EvidenceStatusInfo evidenceStatusInfo : evidenceList) {
					if (evidenceStatusInfo.getSignFlag() == 1) {
						checkResult = true;
						break;
					}
				}
				if (!checkResult) {
					logger.info("checkEvidenceByHash时hash:{} 对应证据签名未完成！", hash);
					resp.setCheckResult(CHECK_EVIDENCE_NO);
					rspEntity.setData(resp);
					long endTime = System.currentTimeMillis();
					getMonitorLogger().info(CODE_MONI_10009, endTime - startTime, CODE_MONI_10009);
					return rspEntity;
				} else {
					// 返回实体设置
					resp.setCheckResult(CHECK_EVIDENCE_YES);
					rspEntity.setData(resp);
					long endTime = System.currentTimeMillis();
					getMonitorLogger().info(CODE_MONI_10009, endTime - startTime, MSG_MONI_10009);
					return rspEntity;
				}
			}
		} catch (Exception e) {
			logger.error("checkEvidenceByHash时hash:{} 处理异常", req.getHash(), e);
			getErrorLogger().error(CODE_ERR_S2011, MSG_ERR_S2011);
			rspEntity = new BaseRspEntity(SYSTEM_EXCEPTION);
			return rspEntity;
		}
	}*/
	
	/**
	 * 链上查询证据信息
	 * @date 2018年6月5日
	 * @author darwin du
	 * @param param {"appId":"IDA1EQRG", "evidenceId":"48", "evidenceHash":"0xe6c1640d0fc9fd042d03f780026523ecc1e7147f"}
	 * @return
	 */
	@RequestMapping("findEvidenceInfoToChain")
	@ResponseBody
	public PageData findEvidenceInfoToChain(@RequestBody EvidenceInfoParam param) throws Exception {
		
		String appId = param.getAppId();
		Long evidenceId = param.getEvidenceId();
		String evidenceAddress = param.getEvidenceAddress();
		if(StringUtils.isEmpty(appId) || StringUtils.isEmpty(evidenceAddress) || evidenceId == null) {
			return ResultUtil.error(Constants.PARAM_NOT_COMPLETE.getErrorMsg(), Constants.PARAM_NOT_COMPLETE.getErrorCode());
		}
		EvidenceData data = null;
		try {
			
			data = evidenceService.findEvidenceInfoToChain(appId, evidenceId, evidenceAddress);
			// add exdata
//            if(data.getEvidenceInfo().contains("|")) {
//			 String  tempStr = data.getEvidenceInfo();
//             String [] tempArr = tempStr.split("[|]");
//             data.setEvidenceInfo(tempArr[0]);
//             data.setExData(tempArr[1]);
//            }
		} catch (BizException e) {
			return ResultUtil.error(e.getMessage(), e.getCode());
		}
		return ResultUtil.success(data);
	}
	
	/**
	 * 验签
	 * @date 2018年6月5日
	 * @author darwin du
	 * @param param
	 * @return
	 */
	@RequestMapping("verifyEvidence")
	@ResponseBody
	public PageData verifyEvidence(@RequestBody EvidenceInfoParam param) throws Exception {
		
		
		String appId = param.getAppId();
		Long evidenceId = param.getEvidenceId();
		String evidenceAddress = param.getEvidenceAddress();
		if(StringUtils.isEmpty(appId) || StringUtils.isEmpty(evidenceAddress) || evidenceId == null) {
			return ResultUtil.error(Constants.PARAM_NOT_COMPLETE.getErrorMsg(), Constants.PARAM_NOT_COMPLETE.getErrorCode());
		}
		Map<String, Object> map = null;
		try {
			
			map = evidenceService.verifyEvidence(appId, evidenceId, evidenceAddress);
		} catch (BizException e) {
			return ResultUtil.error(e.getMessage(), e.getCode());
		}
		return ResultUtil.success(map);
	}

	@Autowired
	private EvidenceStatusInfoDAO evidenceStatusInfoDAO;
	@Autowired
	private ConfigInfoDTO configInfoDTO;
}
