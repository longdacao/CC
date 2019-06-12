package org.bcos.fiscocc.onbc.entity.req;

/**
 * 存证实体类
 * 
 * @author v_sflkchen
 *
 */
public class ReqSetEvidenceEntity {

	private String appId;				// 用户鉴权ID
	private UserInfoDto userInfo;		// 用户信息
	private String hash;				// hash值
	private String signDataByOrg;		// 机构签名数据
	private String signOrNot;			// 当期机构签名与否
	private String exData;

	public ReqSetEvidenceEntity() {}

	public UserInfoDto getUserInfo() {
		return userInfo;
	}

	public String getHash() {
		return hash;
	}
	public String getExData() {
		return exData;
	}

	public void setExData(String exData) {
		this.exData = exData;
	}

	public String getSignDataByOrg() {
		return signDataByOrg;
	}

	public String getSignOrNot() {
		return signOrNot;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public void setUserInfo(UserInfoDto userInfo) {
		this.userInfo = userInfo;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setSignDataByOrg(String signDataByOrg) {
		this.signDataByOrg = signDataByOrg;
	}

	public void setSignOrNot(String signOrNot) {
		this.signOrNot = signOrNot;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{");
		sb.append("\"appId\":\"").append(appId).append('\"');
		sb.append(",\"userInfo\":\"").append(userInfo).append('\"');
		sb.append(",\"hash\":\"").append(hash).append('\"');
		sb.append(",\"signDataByOrg\":\"").append(signDataByOrg).append('\"');
		sb.append(",\"signOrNot\":\"").append(signOrNot).append('\"');
		sb.append('}');
		return sb.toString();
	}
}
