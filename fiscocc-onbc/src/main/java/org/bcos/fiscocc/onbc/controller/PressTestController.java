package org.bcos.fiscocc.onbc.controller;

import static org.bcos.fiscocc.onbc.util.Constants.APPID_ERROR;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_ERR_S2008;
import static org.bcos.fiscocc.onbc.util.Constants.CODE_MONI_10006;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_ERR_S2008;
import static org.bcos.fiscocc.onbc.util.Constants.MSG_MONI_10006;
import static org.bcos.fiscocc.onbc.util.Constants.OVER_MAXID;
import static org.bcos.fiscocc.onbc.util.Constants.SUCCESS;
import static org.bcos.fiscocc.onbc.util.Constants.SYSTEM_EXCEPTION;
import static org.bcos.fiscocc.onbc.util.LogUtils.getErrorLogger;
import static org.bcos.fiscocc.onbc.util.LogUtils.getMonitorLogger;

import java.util.Arrays;

import org.bcos.fiscocc.onbc.dao.EvidenceStatusInfoDAO;
import org.bcos.fiscocc.onbc.dto.ConfigInfoDTO;
import org.bcos.fiscocc.onbc.dto.EvidenceStatusInfo;
import org.bcos.fiscocc.onbc.dto.PageData;
import org.bcos.fiscocc.onbc.entity.base.BaseRspEntity;
import org.bcos.fiscocc.onbc.entity.rsp.RspSetEvidenceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

@SpringBootApplication
@RestController
@RequestMapping(value = "fiscocc-onbc/press")
public class PressTestController {
	
	private static Logger logger = LoggerFactory.getLogger(PressTestController.class);
	
	@Autowired
	private EvidenceStatusInfoDAO evidenceStatusInfoDAO;
	@Autowired
	private ConfigInfoDTO configInfoDTO;

	
	@ResponseBody
	@RequestMapping("sendBatchOnbc")
	public BaseRspEntity sendBatchOnbc(@RequestBody PageData param) {
		long startTime = System.currentTimeMillis();
		logger.info("压测存证上链：", JSON.toJSONString(param));
		// 初始化返回实体类
		BaseRspEntity rspEntity = new BaseRspEntity(SUCCESS);
		RspSetEvidenceEntity resp = new RspSetEvidenceEntity();
		
		try {
			String appid = param.getString("appId");
			// 业务id校验
			String[] appidArr = this.configInfoDTO.getAppids().split(",");
			if (!Arrays.asList(appidArr).contains(appid)) {
				logger.info("setevidence时appid:{} 错误", appid);
				rspEntity = new BaseRspEntity(APPID_ERROR);
				return rspEntity;
			}
			
			EvidenceStatusInfo evidenceStatusInfo_ = evidenceStatusInfoDAO.getMaxId();
			Long size = evidenceStatusInfo_ == null ?  0L : evidenceStatusInfo_.getEvidenceID();
			//证据ID值大于100000，请扩容证据链
			if(size > this.configInfoDTO.getEvidenceMaxId()) {
				rspEntity = new BaseRspEntity(OVER_MAXID);
				return rspEntity;
			}
			
			Long evidenceIdNum = Long.valueOf(param.getString("evidenceIdNum"));
			
			long i = 1;
			for(; i < evidenceIdNum; i ++ ) {
				
				// 封装数据库参数
				EvidenceStatusInfo EvidenceStatusInfo = new EvidenceStatusInfo();
				EvidenceStatusInfo.setAppID(appid);
				//0：个人；1企业；默认企业
				EvidenceStatusInfo.setCustomerType("1");
				EvidenceStatusInfo.setUserName("张三");
				EvidenceStatusInfo.setIdentificationType("0");
				EvidenceStatusInfo.setIdentificationNo("360121198601027231");
				EvidenceStatusInfo.setUserInfo("{\"customerType\":\"1\",\"userName\":\"张三\",\"identificationType\":\"0\",\"identificationNo\":\"360121198601027231\"}");
				EvidenceStatusInfo.setEvidenceHash("0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
				// 证据入库
				evidenceStatusInfoDAO.addEvidence(EvidenceStatusInfo);
			}

			// 返回实体设置
			logger.debug("压测存证上链 EvidenceID:{}", i);
			resp.setEvidenceId(String.valueOf(i));
			rspEntity.setData(resp);
			long endTime = System.currentTimeMillis();
			getMonitorLogger().info(CODE_MONI_10006, endTime - startTime, MSG_MONI_10006);
			return rspEntity;

		} catch (Exception e) {
			logger.error("压测存证上链 处理异常", e);
			getErrorLogger().error(CODE_ERR_S2008, MSG_ERR_S2008);
			rspEntity = new BaseRspEntity(SYSTEM_EXCEPTION);
			return rspEntity;
		}
	}

}
