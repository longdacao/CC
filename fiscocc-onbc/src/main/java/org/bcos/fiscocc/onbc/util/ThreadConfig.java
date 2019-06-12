package org.bcos.fiscocc.onbc.util;

import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;

import org.bcos.fiscocc.onbc.dto.ConfigInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadConfig {

	@Bean(name = "verifierExecutor")
	public ThreadPoolTaskExecutor verifierExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(configInfoDTO.getMyCorePoolSize());
		executor.setMaxPoolSize(configInfoDTO.getMyMaxPoolSize());
		executor.setQueueCapacity(configInfoDTO.getMyQueueCapacity());
		executor.setKeepAliveSeconds(configInfoDTO.getMyKeepAlive());
		executor.setRejectedExecutionHandler(new AbortPolicy());
		executor.setThreadNamePrefix("verifierExecutor-");
		executor.initialize();
		return executor;
	}

	@Autowired
	private ConfigInfoDTO configInfoDTO;
}
