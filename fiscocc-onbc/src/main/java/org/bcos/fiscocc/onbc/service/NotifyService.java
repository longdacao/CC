package org.bcos.fiscocc.onbc.service;

import java.math.BigInteger;

import org.fisco.bcos.channel.client.ChannelResponseCallback2;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.dto.ChannelRequest;
import org.fisco.bcos.channel.dto.ChannelResponse;
import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.fiscocc.onbc.dto.ConfigInfoDTO;
import org.bcos.fiscocc.onbc.dto.NotifyRequest;
import org.bcos.fiscocc.onbc.dto.NotifyRequestMesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 通知服务
 */
@Component
public class NotifyService {
	private Logger logger = LoggerFactory.getLogger(NotifyService.class);
	
	/**
	 * 发送加签通知
	 * @date 2018年6月6日
	 * @author darwin du
	 * @param appid 当前机构
	 * @param seq 证据id
	 * @param address 证据地址
	 * @throws JsonProcessingException
	 */
	public void sendNotify(String appid, BigInteger seq, String address) throws JsonProcessingException {
		logger.info("----------------发送通知----------------");
		Service service = evidenceSDK.getService(appid, seq);
		
        String meshSwitch = configInfoDTO.getMeshSwitch();
        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.setFromOrg("WB");
        channelRequest.setMessageID(service.newSeq());
        if ("false".equals(meshSwitch)) {
        	NotifyRequest notifyRequest = new NotifyRequest();
            notifyRequest.setEvidenceAddress(address);
            channelRequest.setContent(mapper.writeValueAsString(notifyRequest));
        } else {
        	NotifyRequestMesh notifyRequestMesh = new NotifyRequestMesh();
        	notifyRequestMesh.setEvidenceAddress(address);
        	notifyRequestMesh.setAppid(appid);
        	notifyRequestMesh.setSeq(seq);
            channelRequest.setContent(mapper.writeValueAsString(notifyRequestMesh));
        }
		
        String[] topics = configInfoDTO.getTopics().split(",");
        for (int i = 0; i < topics.length; i++) {
        	//给非当前机构发送加签通知
        	if (!appid.equals(topics[i].split("\\.")[0])) {
        		channelRequest.setToTopic(topics[i].split("\\.")[1]);
        		channelRequest.setMessageID(service.newSeq());
        		logger.info("通知sendNotify from>appid:{} to>appid:{} topic:{} seq:{}", appid, topics[i].split("\\.")[0], topics[i].split("\\.")[1], channelRequest.getMessageID());
        		service.asyncSendChannelMessage2(channelRequest, new ChannelResponseCallback2() {
        			@Override
        			public void onResponseMessage(ChannelResponse response) {
        				if(response.getErrorCode() == 0) {
        					logger.info("seq:{} 通知成功", response.getMessageID());
        				} else {
        					logger.error("seq:{} 通知失败:Code {} Message {}", response.getMessageID(), response.getErrorCode(), response.getErrorMessage());
        				}
        			}
        		});
        	}
        }
	}
	
	/**
	 * 再次发送加签通知，证据已经上链
	 * @date 2018年6月6日
	 * @author darwin du
	 * @param appid
	 * @param seq
	 * @param address
	 * @param topic
	 * @throws JsonProcessingException
	 */
	public void sendNotify(String appid, String toAppid, BigInteger seq, String address, String topic) throws JsonProcessingException {
		logger.info("----------------再次发送通知----------------");
		Service service = evidenceSDK.getService(appid, seq);
		
        String meshSwitch = configInfoDTO.getMeshSwitch();
        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.setFromOrg("WB");
        channelRequest.setMessageID(service.newSeq());
        if ("false".equals(meshSwitch)) {
        	NotifyRequest notifyRequest = new NotifyRequest();
            notifyRequest.setEvidenceAddress(address);
            channelRequest.setContent(mapper.writeValueAsString(notifyRequest));
        } else {
        	NotifyRequestMesh notifyRequestMesh = new NotifyRequestMesh();
        	notifyRequestMesh.setEvidenceAddress(address);
        	notifyRequestMesh.setAppid(appid);
        	notifyRequestMesh.setSeq(seq);
            channelRequest.setContent(mapper.writeValueAsString(notifyRequestMesh));
        }
		
        channelRequest.setToTopic(topic);
		channelRequest.setMessageID(service.newSeq());
		logger.info("再次通知sendNotify from>appid:{} to>appid:{} topic:{} seq:{}", appid, toAppid, topic, channelRequest.getMessageID());
		service.asyncSendChannelMessage2(channelRequest, new ChannelResponseCallback2() {
			@Override
			public void onResponseMessage(ChannelResponse response) {
				if(response.getErrorCode() == 0) {
					logger.info("seq:{} 通知成功", response.getMessageID());
				} else {
					logger.error("seq:{} 通知失败:Code {} Message {}", response.getMessageID(), response.getErrorCode(), response.getErrorMessage());
				}
			}
		});
	}
	
	private ObjectMapper mapper = new ObjectMapper();
	@Autowired
	private EvidenceFace evidenceSDK;
	@Autowired
	private ConfigInfoDTO configInfoDTO;
}
