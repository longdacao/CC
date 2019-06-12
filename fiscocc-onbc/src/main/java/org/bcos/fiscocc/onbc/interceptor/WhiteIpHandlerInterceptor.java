package org.bcos.fiscocc.onbc.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bcos.fiscocc.onbc.dto.ConfigInfoDTO;
import org.bcos.fiscocc.onbc.service.WhiteIpService;
import org.bcos.fiscocc.onbc.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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
@Component
public class WhiteIpHandlerInterceptor implements HandlerInterceptor {
	
	private static Logger logger = LoggerFactory.getLogger(WhiteIpHandlerInterceptor.class);
	@Autowired
	private WhiteIpService whiteIpService;
	@Autowired
	private ConfigInfoDTO configInfoDTO;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		String isEnableWhiteIp = configInfoDTO.getIsEnableWhiteIp();
		//如果false则不开启ip白名单
		if("false".equals(isEnableWhiteIp)) {
			return true;
		}
		
		logger.debug("#########ip白名单校验开始");
		String ip = IpUtil.getIpAddr(request);
		boolean flag = whiteIpService.checkIp(ip);
		if(flag) {
			logger.debug("#########ip校验通过, ip:{}, result:{}", ip, flag);
			return true;
		}
		logger.debug("#########ip校验不通过, ip:{}, result:{}", ip, flag);
		response.sendRedirect(request.getContextPath()+"/fiscocc-onbc/home/permissionDenied");
		return false;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
