package org.bcos.fiscocc.onbc.entity.rsp;

/**
 * 证据信息
 * 
 * @author v_sflkchen
 *
 */
public class RspEvidenceInfo {

	private String evidenceID;
	private String appID;
	private String customerType;
	private String userName;
	private String identificationType;
	private String identificationNo;
	private String evidenceHash;
	private String signFlag;
	private String evidenceCreateTime;
	private String evidenceUpdateTime;
	
	public String getEvidenceID() {
		return evidenceID;
	}
	public String getAppID() {
		return appID;
	}
	public String getCustomerType() {
		return customerType;
	}
	public String getUserName() {
		return userName;
	}
	public String getIdentificationType() {
		return identificationType;
	}
	public String getIdentificationNo() {
		return identificationNo;
	}
	public String getEvidenceHash() {
		return evidenceHash;
	}
	public String getSignFlag() {
		return signFlag;
	}
	public String getEvidenceCreateTime() {
		return evidenceCreateTime;
	}
	public String getEvidenceUpdateTime() {
		return evidenceUpdateTime;
	}
	public void setEvidenceID(String evidenceID) {
		this.evidenceID = evidenceID;
	}
	public void setAppID(String appID) {
		this.appID = appID;
	}
	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public void setIdentificationType(String identificationType) {
		this.identificationType = identificationType;
	}
	public void setIdentificationNo(String identificationNo) {
		this.identificationNo = identificationNo;
	}
	public void setEvidenceHash(String evidenceHash) {
		this.evidenceHash = evidenceHash;
	}
	public void setSignFlag(String signFlag) {
		this.signFlag = signFlag;
	}
	public void setEvidenceCreateTime(String evidenceCreateTime) {
		this.evidenceCreateTime = evidenceCreateTime;
	}
	public void setEvidenceUpdateTime(String evidenceUpdateTime) {
		this.evidenceUpdateTime = evidenceUpdateTime;
	}
	
}
