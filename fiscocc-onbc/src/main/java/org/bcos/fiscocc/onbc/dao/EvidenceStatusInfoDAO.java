package org.bcos.fiscocc.onbc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.bcos.fiscocc.onbc.dto.EvidenceStatusInfo;
import org.bcos.fiscocc.onbc.entity.rsp.RspEvidenceInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface EvidenceStatusInfoDAO {
	
	/**
	 * 证据入库
	 * @param evidenceStatusInfo
	 */
	public void addEvidence(EvidenceStatusInfo evidenceStatusInfo);
	
	/**
	 * 机构证据入库
	 * @param evidenceStatusInfo
	 */
	public void addEvidenceForOrgs(EvidenceStatusInfo evidenceStatusInfo);
	
	/**
	 * 获取未上链数据
	 * @param chainCountMax
	 * @param selectCount
	 * @param chainTime
	 * @return 未上链数据列表
	 */
	public List<EvidenceStatusInfo> getUnChainEvidence(@Param("chainCountMax")Integer chainCountMax, @Param("selectCount")Integer selectCount, @Param("chainTime")Integer chainTime,@Param("appId")String appId);
	
	/**
	 * 获取未签名数据
	 * @param notifyCountMax
	 * @param selectCount
	 * @param chainTime
	 * @return 未签名数据列表
	 */
	public List<EvidenceStatusInfo> getUnsignEvidence(@Param("notifyCountMax")Integer notifyCountMax, @Param("selectCount")Integer selectCount,@Param("chainTime")Integer chainTime,@Param("appId")String appId);
	
	/**
	 * 取证时获取证据信息
	 * @param evidenceID
	 * @return 
	 */
	public EvidenceStatusInfo getEvidenceInfo(@Param("evidenceID")Long evidenceID);
	
	/**
	 * 获取证据ID
	 * @param evidenceID
	 * @return
	 */
	public String getEvidenceID(@Param("evidenceID")Long evidenceID);
	
	/**
	 * 获取最大证据id
	 * @date 2018年6月7日
	 * @author darwin du
	 * @param evidenceID
	 * @return
	 */
	public EvidenceStatusInfo getMaxId();
	
	/**
	 * 获取证据列表
	 * @param queryType
	 * @param queryParam
	 * @return
	 */
	public List<RspEvidenceInfo> getEvidenceListByQueryType(@Param("queryType")String queryType, @Param("queryParam")String queryParam);
	
	/**
	 * 通过hash获取证据信息
	 * @param hash
	 * @return
	 */
	public List<EvidenceStatusInfo> getEvidenceInfoByHash(@Param("hash")String hash);
	
	/**
	 * 上链结果更新DB
	 * @param evidenceID
	 * @param evidenceAddress
	 */
	public void updateEvidenceAddress(@Param("evidenceID")Long evidenceID, @Param("evidenceAddress")String evidenceAddress);
	
	/**
	 * 更新签名和签名状态信息
	 * @param evidenceID
	 * @param signData
	 */
	public void updateSignInfo(@Param("evidenceID")Long evidenceID, @Param("signData")String signData);
	
	/**
	 * 更新通知次数
	 * @param evidenceID
	 * @param notifyCount
	 */
	public void updateNotifyCount(@Param("evidenceID")Long evidenceID, @Param("notifyCount")Integer notifyCount);
	
	/**
	 * 更新通知次数，加乐观锁
	 * @param evidenceID
	 * @param notifyCount
	 */
	public Long updateNotifyCountAndVersion(@Param("evidenceID")Long evidenceID, @Param("notifyCount")Integer notifyCount, @Param("version")Integer version);
	
	/**
	 * 更新请求上链次数
	 * @param evidenceID
	 * @param chainCount
	 */
	public void updateChainCount(@Param("evidenceID")Long evidenceID, @Param("chainCount")Integer chainCount);
	
	
	/**
	 * 更新请求上链次数，加乐观锁
	 * @param evidenceID
	 * @param chainCount
	 */
	public Long updateChainCountAndVersion(@Param("evidenceID")Long evidenceID, @Param("chainCount")Integer chainCount, @Param("version")Integer version);
}
