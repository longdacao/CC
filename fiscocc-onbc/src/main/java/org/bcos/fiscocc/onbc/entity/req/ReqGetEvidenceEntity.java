package org.bcos.fiscocc.onbc.entity.req;

/**
 * 取证实体类
 * 
 * @author v_sflkchen
 *
 */
public class ReqGetEvidenceEntity {

	private String appId; 				// 用户鉴权ID
	private UserInfoDto userInfo; 		// 用户信息
	private String queryType; 			// 查询方式
	private String queryParam; 			// 查询参数
	private String evidenceId; 			// 存证回执
	private String hash;				// hash值

	public ReqGetEvidenceEntity() {
	}

	public ReqGetEvidenceEntity(String appId, UserInfoDto userInfo, String queryType, String queryParam,
			String evidenceId, String hash) {
		this.appId = appId;
		this.userInfo = userInfo;
		this.queryType = queryType;
		this.queryParam = queryParam;
		this.evidenceId = evidenceId;
		this.hash = hash;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public UserInfoDto getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfoDto userInfo) {
		this.userInfo = userInfo;
	}
	
	public String getQueryType() {
		return queryType;
	}

	public String getQueryParam() {
		return queryParam;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}

	public void setQueryParam(String queryParam) {
		this.queryParam = queryParam;
	}

	public String getEvidenceId() {
		return evidenceId;
	}

	public void setEvidenceId(String evidenceId) {
		this.evidenceId = evidenceId;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{");
		sb.append("\"appID\":\"").append(appId).append('\"');
		sb.append(",\"userInfo\":\"").append(userInfo).append('\"');
		sb.append(",\"queryType\":\"").append(queryType).append('\"');
		sb.append(",\"queryParam\":\"").append(queryParam).append('\"');
		sb.append(",\"evidenceID\":\"").append(evidenceId).append('\"');
		sb.append(",\"hash\":\"").append(hash).append('\"');
		sb.append('}');
		return sb.toString();
	}
}
