package org.bcos.fiscocc.onbc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

//@Service
public class EncryptString {
	private static Logger logger = LoggerFactory.getLogger(EncryptString.class);

	public String getSysPubKeyFile() {
		return sysPubKeyFile;
	}

	public void setSysPubKeyFile(String sysPubKeyFile) {
		this.sysPubKeyFile = sysPubKeyFile;
	}

	public String getAppKeyFile() {
		return appKeyFile;
	}

	public void setAppKeyFile(String appKeyFile) {
		this.appKeyFile = appKeyFile;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getDBPasswd() {
		try {
			File sysFile = ResourceUtils.getFile(sysPubKeyFile);
			File appFile = ResourceUtils.getFile(appKeyFile);

			String sysKey = copyFileToString(new FileReader(sysFile));
			String appKey = copyFileToString(new FileReader(appFile));

			if (StringUtils.hasText(passwd) && passwd.length() > RSA_PREFIX.length() && passwd.startsWith(RSA_PREFIX)) {

				String text = passwd.startsWith(RSA_PREFIX) ? passwd.substring(RSA_PREFIX.length()) : passwd;

				String dec = passwd;//EncryptUtil.decrypt(ParamType.STRING, sysKey, ParamType.STRING, appKey, ParamType.STRING, text);

				return dec;
			}
		} catch (Exception e) {
			logger.error("解密DB密码失败", e);
		}

		return passwd;
	}

	private static String copyFileToString(FileReader r) throws IOException {
		BufferedReader reader = null;
		StringBuffer sb = new StringBuffer();
		try {
			reader = new BufferedReader(r);
			String tmp = null;
			while ((tmp = reader.readLine()) != null) {
				if (!tmp.startsWith(COMMENT_PREFIX)) {
					sb.append(tmp);
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
					logger.error("配置异常", ex);
				}
			}
		}
		return sb.toString();
	}

	private final static String COMMENT_PREFIX = "-----";
	private final static String RSA_PREFIX = "{RSA}";
	private String sysPubKeyFile;
	private String appKeyFile;
	private String passwd;
}
