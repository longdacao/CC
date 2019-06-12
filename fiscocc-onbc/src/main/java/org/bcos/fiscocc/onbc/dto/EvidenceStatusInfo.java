package org.bcos.fiscocc.onbc.dto;

/**
 * 证据状态信息
 */
public class EvidenceStatusInfo {
	private Long evidenceID; 			// 证据ID
	private String appID; 				// 用户鉴权ID
	private String userInfo; 			// 用户信息
	private String customerType; 		// 用户类型 0:个人 1:企业
	private String userName; 			// 用户姓名
	private String identificationType;	// 证件类型 0:身份证 1:护照
	private String identificationNo; 	// 证件号码
	private String evidenceHash; 		// 证据Hash
	private String exData;              // 证据的扩展字段
	private Integer setSide; 			// 存证发起方 0:当期机构 1:其他机构
	private Integer signOrNot; 			// 当期机构签名与否 0:需要 1:不需要
	private String signData; 			// 请求签名数据
	private String evidenceAddress; 	// 证据链上地址
	private Integer signFlag; 			// 签名收集是否完成
	private Integer notifyCount;		// 通知次数
	private Integer chainCount; 		// 请求上链次数
	private Integer version; 			// 乐观锁，版本号

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Long getEvidenceID() {
		return evidenceID;
	}

	public void setEvidenceID(Long evidenceID) {
		this.evidenceID = evidenceID;
	}
	
	public String getExData() {
		return exData;
	}

	public void setExData(String exData) {
		this.exData = exData;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	public String getCustomerType() {
		return customerType;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIdentificationType() {
		return identificationType;
	}

	public void setIdentificationType(String identificationType) {
		this.identificationType = identificationType;
	}

	public String getIdentificationNo() {
		return identificationNo;
	}

	public void setIdentificationNo(String identificationNo) {
		this.identificationNo = identificationNo;
	}

	public String getEvidenceHash() {
		return evidenceHash;
	}

	public void setEvidenceHash(String evidenceHash) {
		this.evidenceHash = evidenceHash;
	}

	public Integer getSetSide() {
		return setSide;
	}

	public void setSetSide(Integer setSide) {
		this.setSide = setSide;
	}

	public Integer getSignOrNot() {
		return signOrNot;
	}

	public void setSignOrNot(Integer signOrNot) {
		this.signOrNot = signOrNot;
	}

	public String getSignData() {
		return signData;
	}

	public void setSignData(String signData) {
		this.signData = signData;
	}

	public String getEvidenceAddress() {
		return evidenceAddress;
	}

	public void setEvidenceAddress(String evidenceAddress) {
		this.evidenceAddress = evidenceAddress;
	}

	public Integer getSignFlag() {
		return signFlag;
	}

	public void setSignFlag(Integer signFlag) {
		this.signFlag = signFlag;
	}

	public Integer getNotifyCount() {
		return notifyCount;
	}

	public void setNotifyCount(Integer notifyCount) {
		this.notifyCount = notifyCount;
	}

	public Integer getChainCount() {
		return chainCount;
	}

	public void setChainCount(Integer chainCount) {
		this.chainCount = chainCount;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{");
		sb.append("\"evidenceID\":\"").append(evidenceID).append('\"');
		sb.append(",\"appID\":\"").append(appID).append('\"');
		sb.append(",\"userInfo\":\"").append(userInfo).append('\"');
		sb.append(",\"customerType\":\"").append(customerType).append('\"');
		sb.append(",\"userName\":\"").append(userName).append('\"');
		sb.append(",\"identificationType\":\"").append(identificationType).append('\"');
		sb.append(",\"identificationNo\":\"").append(identificationNo).append('\"');
		sb.append(",\"evidenceHash\":\"").append(evidenceHash).append('\"');
		sb.append(",\"setSide\":\"").append(setSide).append('\"');
		sb.append(",\"signOrNot\":\"").append(signOrNot).append('\"');
		sb.append(",\"signData\":\"").append(signData).append('\"');
		sb.append(",\"evidenceAddress\":\"").append(evidenceAddress).append('\"');
		sb.append(",\"signFlag\":\"").append(signFlag).append('\"');
		sb.append(",\"notifyCount\":\"").append(notifyCount).append('\"');
		sb.append(",\"chainCount\":\"").append(chainCount).append('\"');
		sb.append('}');
		return sb.toString();
	}
}
