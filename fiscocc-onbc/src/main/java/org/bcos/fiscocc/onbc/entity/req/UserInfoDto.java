package org.bcos.fiscocc.onbc.entity.req;

import java.io.Serializable;

/**
 * 用户信息
 * @author v_sflkchen
 *
 */
public class UserInfoDto implements Serializable{
	private static final long serialVersionUID = 2011280606628109696L;
	
	private String customerType;		//用户类型 0:个人  1:企业
    private String userName;			//用户姓名
    private String identificationType;	//证件类型 0:身份证 1:护照
    private String identificationNo;	//证件号码

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"customerType\":\"")
                .append(customerType).append('\"');
        sb.append(",\"userName\":\"")
                .append(userName).append('\"');
        sb.append(",\"identificationType\":\"")
                .append(identificationType).append('\"');
        sb.append(",\"identificationNo\":\"")
                .append(identificationNo).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
