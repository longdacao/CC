package org.bcos.fiscocc.onbc.util;

import org.bcos.fiscocc.onbc.dto.PageData;


/**
 * <pre>
 * *********************************************
 * Copyright.
 * All rights reserved.
 * Description:
 * HISTORY
 * *********************************************
 *  ID     REASON        PERSON          DATE
 *  1      Create   	 darwin du       2017年7月18日
 * *********************************************
 * </pre>
 */
public class ResultUtil {
	
	public static PageData success() {
        return success(null);
    }
	
	public static PageData success(Object object) {
		
		PageData pd = new PageData();
		pd.put(ConstResult.RESULT_CODE, Constants.SUCCESS.getErrorCode());
		pd.put(ConstResult.RESULT_MSG, Constants.SUCCESS.getErrorMsg());
		pd.put(ConstResult.RESULT_DATA, object);
        return pd;
    }

    public static PageData error(String msg, Integer code) {
    	
    	PageData pd = new PageData();
    	pd.put(ConstResult.RESULT_CODE, code);
		pd.put(ConstResult.RESULT_MSG, msg);
        return pd;
    }
}
