package org.bcos.fiscocc.onbc.util;

import org.bcos.fiscocc.onbc.interceptor.WhiteIpHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
@Configuration
public class DefinedInterceptorConfig extends WebMvcConfigurerAdapter {
	
	private static Logger logger = LoggerFactory.getLogger(DefinedInterceptorConfig.class);

	@Autowired
	private WhiteIpHandlerInterceptor whiteIpHandlerInterceptor;
	/**
	 * ip白名单拦截器
	 * @date 2018年5月28日
	 * @author darwin du
	 * @param registry
	 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	logger.info("#########开始注入ip白名单拦截器");
        registry.addInterceptor(whiteIpHandlerInterceptor).excludePathPatterns("/fiscocc-onbc/home/**");
        logger.info("#########注入ip白名单拦截器完成");
    }
}
