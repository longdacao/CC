package org.bcos.fiscocc.onbc.util;

import org.bcos.evidence.sdk.EvidenceFace;
import org.bcos.fiscocc.onbc.factory.EvidenceSDKFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class BeanConfig {
	
	private static Logger logger = LoggerFactory.getLogger(BeanConfig.class);
	
	/*@Bean
	public EncryptString encryptString() {
		EncryptString encryptString = new EncryptString();
		encryptString.setSysPubKeyFile(env.getProperty("spring.datasource.sysPubKeyFile"));
		encryptString.setAppKeyFile(env.getProperty("spring.datasource.appKeyFile"));
		encryptString.setPasswd(env.getProperty("spring.datasource.password"));
		return encryptString;
	}*/

	@Bean
	public EvidenceSDKFactory evidenceSDKFactory() {
		EvidenceSDKFactory evidenceSDKFactory = new EvidenceSDKFactory();
		evidenceSDKFactory.setKeyStorePath(env.getProperty("sdk.keyStorePath"));
		evidenceSDKFactory.setKeyStorePassword(env.getProperty("sdk.keyStorePassword"));
		return evidenceSDKFactory;
	}

	/**
	 * 初始化sdk
	 * @date 2018年5月29日
	 * @author darwin du
	 * @return
	 */
	@Bean
	public EvidenceFace evidenceFace() {
		logger.info("#########开始初始化存证SDK");
		return evidenceSDKFactory.getEvidenceFace();
	}

	/**
	 * DataSource设置 当数据库连接不使用的时候,就把该连接重新放到数据池中,方便下次使用调用
	 * @return
	 */
/*	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		DruidDataSource dataSource = new DruidDataSource();
		dataSource.setUrl(env.getProperty("spring.datasource.url"));
		dataSource.setUsername(env.getProperty("spring.datasource.username"));
//		dataSource.setPassword(env.getProperty("spring.datasource.password"));
		dataSource.setPassword(encryptString.getDBPasswd());
		dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
		dataSource.setInitialSize(10);
		dataSource.setMinIdle(0);
		dataSource.setMaxActive(10000);
		dataSource.setMaxWait(60000);
		dataSource.setValidationQuery("SELECT 1");
		dataSource.setTestOnBorrow(false);
		dataSource.setTestWhileIdle(true);
		dataSource.setPoolPreparedStatements(false);
		
		return dataSource;
	}*/

	@Autowired
	private Environment env;
	//@Autowired
	//private EncryptString encryptString;
	@Autowired
	private EvidenceSDKFactory evidenceSDKFactory;
}
