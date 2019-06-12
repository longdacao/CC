package org.bcos.fiscocc.onbc.util;

import org.bcos.fiscocc.onbc.dto.ConfigInfoDTO;
import org.bcos.fiscocc.onbc.service.NewStatusService;
import org.bcos.fiscocc.onbc.service.SignStatusService;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ParseException;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {
	@Bean(name = "newStatusVerifier")
	public MethodInvokingJobDetailFactoryBean newStatusVerifier() {
		MethodInvokingJobDetailFactoryBean newStatusVerifier = new MethodInvokingJobDetailFactoryBean();
		newStatusVerifier.setTargetObject(newStatusService);
		newStatusVerifier.setTargetMethod("newCheckStatus");
		newStatusVerifier.setConcurrent(false);
		return newStatusVerifier;
	}

	@Bean(name = "onStatusVerifier")
	public MethodInvokingJobDetailFactoryBean onStatusVerifier() {
		MethodInvokingJobDetailFactoryBean onStatusVerifier = new MethodInvokingJobDetailFactoryBean();
		onStatusVerifier.setTargetObject(signStatusService);
		onStatusVerifier.setTargetMethod("signCheckStatus");
		onStatusVerifier.setConcurrent(false);
		return onStatusVerifier;
	}
	
	@Bean(name = "newCronTrigger")
	public CronTriggerFactoryBean newCronTrigger() {
		CronTriggerFactoryBean newCronTrigger = new CronTriggerFactoryBean();
		newCronTrigger.setJobDetail(newStatusVerifier().getObject());
		try {
			newCronTrigger.setCronExpression(configInfoDTO.getScheduleTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return newCronTrigger;
	}

	@Bean(name = "onCronTrigger")
	public CronTriggerFactoryBean onCronTrigger() {
		CronTriggerFactoryBean onCronTrigger = new CronTriggerFactoryBean();
		onCronTrigger.setJobDetail(onStatusVerifier().getObject());
		try {
			onCronTrigger.setCronExpression(configInfoDTO.getScheduleTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return onCronTrigger;
	}

	@Bean(name = "scheduler")
	public SchedulerFactoryBean schedulerFactory(Trigger... triggers) {
		SchedulerFactoryBean bean = new SchedulerFactoryBean();
		// 用于quartz集群,QuartzScheduler 启动时更新己存在的Job
		bean.setOverwriteExistingJobs(true);
		// 延时启动，应用启动10秒后
		bean.setStartupDelay(10);
		// 注册触发器
		bean.setTriggers(triggers);
		return bean;
	}
	
	@Autowired
	private NewStatusService newStatusService;
	@Autowired
	private SignStatusService signStatusService;
	@Autowired
	private ConfigInfoDTO configInfoDTO;
}
