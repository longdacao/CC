package org.bcos.fiscocc.onbc.util;

import org.bcos.fiscocc.onbc.entity.base.RetCode;

/**
 * 常量定义
 */
public interface Constants {
	
    /** 证据已完成签名*/
	String CHECK_EVIDENCE_YES = "0";
	
	/** 证据未完成签名*/
	String CHECK_EVIDENCE_NO = "1";
	
	/** 证据不存在*/
	String HASH_NOT_EXIST = "2";

    /** MONITOR日志格式定义 */
    String MONITOR_LOG_FORMAT		= "[{\"CODE\":\"%s\",\"COST_TIME\":\"{}\",\"RES_CODE\":\"0\"}][{}]";
    
    /** 证据上链耗时记录 */
    String CODE_MONI_10001			= String.format(MONITOR_LOG_FORMAT, "10001");
    String MSG_MONI_10001			= "NewEvidence";
    
    /** 发送通知耗时记录 */
    String CODE_MONI_10002			= String.format(MONITOR_LOG_FORMAT, "10002");
    String MSG_MONI_10002			= "PushMsg";

    /** 获取签名数据耗时记录 */
    String CODE_MONI_10003			= String.format(MONITOR_LOG_FORMAT, "10003");
    String MSG_MONI_10003			= "GetSignData";
    
    /** 验证签名数据耗时记录 */
    String CODE_MONI_10004			= String.format(MONITOR_LOG_FORMAT, "10004");
    String MSG_MONI_10004			= "VerifyEvidence";

    /** 证据入DB耗时记录 */
    String CODE_MONI_10005			= String.format(MONITOR_LOG_FORMAT, "10005");
    String MSG_MONI_10005			= "InsertDBEvidence";
    
    /** 存证请求耗时记录 */
    String CODE_MONI_10006			= String.format(MONITOR_LOG_FORMAT, "10006");
    String MSG_MONI_10006			= "SetEvidence";
    
    /** 取证请求耗时记录 */
    String CODE_MONI_10007			= String.format(MONITOR_LOG_FORMAT, "10007");
    String MSG_MONI_10007			= "GetEvidence";
    
    /** 查询存证信息请求耗时记录 */
    String CODE_MONI_10008			= String.format(MONITOR_LOG_FORMAT, "10008");
    String MSG_MONI_10008			= "GetEvidenceByQueryType";
    
    /** 根据hash验证证据请求耗时记录 */
    String CODE_MONI_10009			= String.format(MONITOR_LOG_FORMAT, "10009");
    String MSG_MONI_10009			= "CheckEvidenceByHash";
    
    /**ERROR告警日志格式定义 */
    String ERROR_LOG_FORMAT			= "[{\"CODE\":\"%s\",\"RES_CODE\":\"1\"}][{}]";
    
    /** 证据上链异常 */
    String CODE_ERR_S2001			= String.format(ERROR_LOG_FORMAT, "S2001");
    String MSG_ERR_S2001			= "Error_New_Evidence_Exception";
    
    /** 获取签名数据异常 */
    String CODE_ERR_S2002			= String.format(ERROR_LOG_FORMAT, "S2002");
    String MSG_ERR_S2002			= "Error_Get_Sign_Exception";
    
    /** 验证签名异常 */
    String CODE_ERR_S2003			= String.format(ERROR_LOG_FORMAT, "S2003");
    String MSG_ERR_S2003			= "Error_Verify_Sign_Exception";

    /** 证据上链未完成，请求次数已达上限 */
    String CODE_ERR_S2004			= String.format(ERROR_LOG_FORMAT, "S2004");
    String MSG_ERR_S2004			= "Error_Request_Chain_Arrive_Limit";

    /** 证据签名未完成，通知次数已达上限 */
    String CODE_ERR_S2005			= String.format(ERROR_LOG_FORMAT, "S2005");
    String MSG_ERR_S2005			= "Error_Send_Notify_Arrive_Limit";
    
    /** 轮询未上链服务异常 */
    String CODE_ERR_S2006			= String.format(ERROR_LOG_FORMAT, "S2006");
    String MSG_ERR_S2006			= "Error_Chain_Verify_Service_Exception";
    
    /** 轮询未签名服务异常 */
    String CODE_ERR_S2007			= String.format(ERROR_LOG_FORMAT, "S2007");
    String MSG_ERR_S2007			= "Error_Sign_Verify_Service_Exception";
    
    /** 请求存证时系统异常 */
    String CODE_ERR_S2008			= String.format(ERROR_LOG_FORMAT, "S2008");
    String MSG_ERR_S2008			= "Error_Set_Evidence_System_Exception";
    
    /** 请求取证时系统异常 */
    String CODE_ERR_S2009			= String.format(ERROR_LOG_FORMAT, "S2009");
    String MSG_ERR_S2009			= "Error_Get_Evidence_System_Exception";
    
    /** 查询存证信息时系统异常 */
    String CODE_ERR_S2010			= String.format(ERROR_LOG_FORMAT, "S2010");
    String MSG_ERR_S2010			= "Error_Get_Evidence_By_Query_Type";
    
    /** 通过hash验证证据时系统异常 */
    String CODE_ERR_S2011			= String.format(ERROR_LOG_FORMAT, "S2011");
    String MSG_ERR_S2011			= "Error_Check_Evidence_By_Hash";

	//private static final RetCode SET_EXDATA_UN_EMPTY = null;

    /** 返回成功*/
    RetCode SUCCESS = RetCode.mark(0,"成功！");
    
    /** 系统异常*/
    RetCode SYSTEM_EXCEPTION = RetCode.mark(4001,"系统异常！");
    
    /** 证据不存在!*/
    RetCode EVIDENCE_NOT_EXIST = RetCode.mark(4002,"证据不存在！");
    
    /** 证据还未上链!*/
    RetCode EVIDENCE_UN_CHAIN = RetCode.mark(4003,"证据还未上链！");
    
    /** 证据还未上链，并且请求次数已达上限!*/
    RetCode CHAIN_ARRIVE_LIMIT = RetCode.mark(4004,"证据还未上链，并且请求次数已达上限！");
    
    /** 证据还未签名!*/
    RetCode EVIDENCE_UN_SIGN = RetCode.mark(4005,"证据还未签名！");
    
    /** 证据还未签名，并且请求次数已达上限!*/
    RetCode SIGN_ARRIVE_LIMIT = RetCode.mark(4006,"证据还未签名，并且请求次数已达上限！");
    
    /** 证据还未签名，并且请求次数已达上限!*/
    RetCode ID_UN_MATCHED = RetCode.mark(4007,"APPID和证据ID不匹配，请确认！");
    
    /** 身份证错误*/
    RetCode IDENTIFICATION_NO_ERROR = RetCode.mark(4008,"身份证号错误，请确认！");
    
    /** hash不存在*/
//    RetCode HASH_NOT_EXIST = RetCode.mark(4009,"证据不存在，请确认！");
    
    /** HASH不能为空*/
    RetCode HASH_UN_EMPTY = RetCode.mark(4010,"所查询hash不能为空，请输入！");
    
    /** 查询方式不能为空*/
    RetCode QUERY_TYPE_UN_EMPTY = RetCode.mark(4011,"查询方式不能为空！");
    
    /** 查询参数不能为空*/
    RetCode QUERY_PARAM_UN_EMPTY = RetCode.mark(4012,"查询参数不能为空！");
    
    /** 查询ID不能为空*/
    RetCode QUERY_ID_UN_EMPTY = RetCode.mark(4013,"所查询存证ID不能为空，请确认！");
    
    /** 机构签名不能为空*/
    RetCode ORG_SIGN_DATA_UN_EMPTY = RetCode.mark(4014,"机构签名不能为空，请确认！");
    
    /** 是否需要当期机构签名不能为空*/
    RetCode SIGN_OR_NOT_UN_EMPTY = RetCode.mark(4015,"是否需要当期机构签名参数不能为空，请确认！");
    
    /** 业务appid错误*/
    RetCode APPID_ERROR = RetCode.mark(4016,"业务appid错误，请确认！");
    
    /** APPID不能为空*/
    RetCode APPID_UN_EMPTY = RetCode.mark(4017,"APPID不能为空，请确认！");
    
    /** 用户类型不能为空*/
    RetCode USER_TYPE_UN_EMPTY = RetCode.mark(4018,"用户类型不能为空，请确认！");
    
    /** 用户名不能为空*/
    RetCode USER_NAME_UN_EMPTY = RetCode.mark(4019,"用户名不能为空，请确认！");
    
    /** 证件类型不能为空*/
    RetCode ID_TYPE_UN_EMPTY = RetCode.mark(4020,"证件类型不能为空，请确认！");
    
    /** 证件号不能为空*/
    RetCode ID_NO_UN_EMPTY = RetCode.mark(4021,"证件名不能为空，请确认！");
    
    /** 存证hash不能为空*/
    RetCode SET_HASH_UN_EMPTY = RetCode.mark(4022,"证据hash不能为空，请确认！");
    

    /** 证据ID值已经超过最大值，请扩容证据链*/
    RetCode OVER_MAXID = RetCode.mark(4023,"证据ID值已经超过最大值，请扩容证据链");
    
    
    /** 5001: 参数请填写完整*/
    RetCode PARAM_NOT_COMPLETE = RetCode.mark(5001, "参数请填写完整");
    /** 5002: 区块链查询证据异常，InterruptedException：*/
    RetCode FIND_EVIDENCE_EXP1 = RetCode.mark(5002, "区块链查询证据异常，InterruptedException ");
    /** 5003: 区块链查询证据异常，ExecutionException：*/
    RetCode FIND_EVIDENCE_EXP2 = RetCode.mark(5003, "区块链查询证据异常，ExecutionException ");
    /** 5004: 区块链获取公钥异常，SignatureException：*/
    RetCode FIND_EVIDENCE_EXP3 = RetCode.mark(5004, "区块链获取公钥异常，SignatureException ");
    /** 5005: Permission denied, please add ip !*/
    RetCode PERMISSION_DENIED = RetCode.mark(5005, "Permission denied, please add ip !");
}
