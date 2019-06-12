package org.bcos.fiscocc.onbc.runner;

import java.util.HashMap;
import java.util.Map;

import org.bcos.fiscocc.onbc.dto.ConfigInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * <pre>
 * *********************************************
 * Copyright .
 * All rights reserved.
 * Description: 程序启动后加载项
 * HISTORY
 * *********************************************
 *  ID     REASON        PERSON          DATE
 *  1      Create   	 darwin du       2018年5月31日
 * *********************************************
 * </pre>
 */
@Component
public class AppRunner implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(AppRunner.class);
	
	@Autowired
    private ConfigInfoDTO configInfoDTO;
	
	public Map<String, String> appIdTopicMap;
	public Map<String, String> publicKeyAppIdMap;
	
	@Override
	public void run(String... args) throws Exception {
		
		logger.info("++++++++++初始化appIdTopicMap、appIdPublicKeyMap map start");
		//key=appid, value=topic
		appIdTopicMap = new HashMap<String, String>();
		String[] topicArr = this.configInfoDTO.getTopics().split(",");
		for(String topic : topicArr) {
			appIdTopicMap.put(topic.split("\\.")[0], topic.split("\\.")[1]);
        }
		
		//key=publicKey, value=appid
		publicKeyAppIdMap = new HashMap<String, String>();
		String[] publicKeyArr = this.configInfoDTO.getPublickeys().split(",");
		for(String publicKey : publicKeyArr) {
			publicKeyAppIdMap.put(publicKey.split("\\.")[1], publicKey.split("\\.")[0]);
        }
		logger.info("++++++++++appIdTopicMap、appIdPublicKeyMap map end, appIdTopicMap: {}, publicKeyAppIdMap: {}", appIdTopicMap, publicKeyAppIdMap);
	}

}
