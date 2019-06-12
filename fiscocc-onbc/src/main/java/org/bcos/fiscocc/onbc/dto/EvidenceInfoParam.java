package org.bcos.fiscocc.onbc.dto;

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
public class EvidenceInfoParam {
	
	private String appId;
	private Long evidenceId;
	private String evidenceAddress;
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public Long getEvidenceId() {
		return evidenceId;
	}
	public void setEvidenceId(Long evidenceId) {
		this.evidenceId = evidenceId;
	}
	public String getEvidenceAddress() {
		return evidenceAddress;
	}
	public void setEvidenceAddress(String evidenceAddress) {
		this.evidenceAddress = evidenceAddress;
	}

}
