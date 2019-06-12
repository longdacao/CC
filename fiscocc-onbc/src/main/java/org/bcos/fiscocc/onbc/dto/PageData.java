package org.bcos.fiscocc.onbc.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.bcos.fiscocc.onbc.util.ReflectHelper;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class PageData extends HashMap<String,Object> implements Map<String,Object>{
	
	private static final long serialVersionUID = 1L;
	HttpServletRequest request;
	
	public PageData(HttpServletRequest request){
		this.request = request;
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) ((request instanceof MultipartHttpServletRequest) ? (ReflectHelper.getValueByFieldName(request, "multipartParameters")) : request.getParameterMap());
		if(map == null ||map.isEmpty()){
			return;
		}
		String value;
		for (Map.Entry<String, Object> e : map.entrySet()) {
			value = "";
			if (e.getValue() != null) {
				if (e.getValue() instanceof String[]) {
					for (String v : (String[]) e.getValue()) {
						value += "," + v;
					}
					value = value.substring(1);
				} else {
					value = e.getValue().toString();
				}
			}
			super.put(e.getKey(), value.trim());
		}
	}
	
	public PageData() {
	}
	
	
	public String getString(String key) {
		return get(key) == null ? "" : get(key).toString();
	}
	
	/**
	 * 去除空格
	 * @date 2016年10月19日
	 * @author 杜志星
	 */
	public void trim() {
		
		for(String key : (Set<String>)this.keySet()) {
			
			Object object = this.get(key);
			if(object instanceof String ) {
				this.put(key, ((String)object).trim());
			}
		}
	}
	
	/**
	 * 将PD中为""的属性转换成null
	 */
	public void transEmptyToNull() {
		List<String> keys = new ArrayList<String>();
		for(String key : (Set<String>)this.keySet()) {
			if(StringUtils.isEmpty(this.getString(key))) {
				keys.add(key);
			}
		}
		for (String key : keys) {
			this.remove(key);
		}
	}
	
	/**
	 * 将PD中为""的属性转换成null
	 */
	public void transEmptyToNull(String[] colNames) {
		
		if(colNames != null && colNames.length > 0) {
			List<String> keys = new ArrayList<String>();
			for(String key : colNames) {
				if(StringUtils.isEmpty(this.getString(key))) {
					keys.add(key);
				}
			}
			for (String key : keys) {
				this.remove(key);
			}
		}
	}
	
	/**
	 * 将PD中为[]的属性转换成null
	 * @date 2017年7月18日
	 * @author darwin du
	 */
	public void transBracketToNull() {
		for(String key : (Set<String>)this.keySet()) {
			if("[]".equals(this.getString(key))) {
				super.put(key, null);
			}
		}
	}

	public static PageData parseJson(String json){
		PageData pd = new PageData();
		JSONObject jsonObject =  JSONObject.parseObject(json);
		pd.putAll(jsonObject);
		return pd;
	}
	
	public String toString(){
		
		/* 
        QuoteFieldNames———-输出key时是否使用双引号,默认为true 
        WriteMapNullValue——–是否输出值为null的字段,默认为false 
        WriteNullNumberAsZero—-数值字段如果为null,输出为0,而非null 
        WriteNullListAsEmpty—–List字段如果为null,输出为[],而非null 
        WriteNullStringAsEmpty—字符类型字段如果为null,输出为”“,而非null 
        WriteNullBooleanAsFalse–Boolean字段如果为null,输出为false,而非null 
        */
		return JSON.toJSONString(this, SerializerFeature.WriteMapNullValue);
	}

}
