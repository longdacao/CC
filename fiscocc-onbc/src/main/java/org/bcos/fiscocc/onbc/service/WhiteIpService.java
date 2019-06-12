package org.bcos.fiscocc.onbc.service;

import java.util.List;

import org.bcos.fiscocc.onbc.dao.WhiteIpDAO;
import org.bcos.fiscocc.onbc.dto.WhiteIp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;

/**
 * <pre>
 * *********************************************
 * Copyright .
 * All rights reserved.
 * Description:
 * HISTORY
 * *********************************************
 *  ID     REASON        PERSON          DATE
 *  1      Create   	 darwin du       2018年5月28日
 * *********************************************
 * </pre>
 */
@Service
public class WhiteIpService {

	private static Logger logger = LoggerFactory.getLogger(WhiteIpService.class);
	
	@Autowired
	private WhiteIpDAO whiteIpDAO;
	
	public boolean checkIp(String ip) {
		
		if(StringUtils.isEmpty(ip)) {
			logger.info("#########ip is not null");
			return false;
		}
		List<WhiteIp> list = whiteIpDAO.checkIp(ip);
		if(list == null || list.size() == 0) {
			logger.info("#########ip is not exist, Permission denied");
			return false;
		}
		return true;
	}
}
