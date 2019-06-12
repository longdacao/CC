package org.bcos.fiscocc.onbc.entity.rsp;

import java.util.List;

/**
 * 证件号获取证据信息返回实体类
 * 
 * @author v_sflkchen
 *
 */
public class RspGetEvidenceByQueryType {

	private List<RspEvidenceInfo> evidenceList; // 证据列表

	public List<RspEvidenceInfo> getEvidenceList() {
		return evidenceList;
	}

	public void setEvidenceList(List<RspEvidenceInfo> evidenceList) {
		this.evidenceList = evidenceList;
	}
}
