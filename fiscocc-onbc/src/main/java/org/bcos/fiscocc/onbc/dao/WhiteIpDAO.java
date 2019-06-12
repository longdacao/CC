package org.bcos.fiscocc.onbc.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.bcos.fiscocc.onbc.dto.WhiteIp;
import org.springframework.stereotype.Repository;

@Repository
public interface WhiteIpDAO {


	/**
	 * 通过ip查询是否存在ip
	 * @param ip 
	 * @return ip列表
	 */
	public List<WhiteIp> checkIp(@Param("ip")String ip);
	

}
