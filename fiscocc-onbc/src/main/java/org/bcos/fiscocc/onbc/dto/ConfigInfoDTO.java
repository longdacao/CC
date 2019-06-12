package org.bcos.fiscocc.onbc.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 加载配置信息
 */
@Component("configInfoDTO")
public class ConfigInfoDTO {
	@Value("${chain.count.limit}")
	private String chainCount;
	@Value("${nodify.count.limit}")
	private String nodifyCount;
	@Value("${select.count.limit}")
	private String selectCount;
	@Value("${select.chainTime}")
	private String chainTime;
	@Value("${select.internalSleepTime}")
	private String internalSleepTime;
	@Value("${schedule.time}")
	private String scheduleTime;
	@Value("${my.corePoolSize}")
	private int myCorePoolSize;
	@Value("${my.maxPoolSize}")
	private int myMaxPoolSize;
	@Value("${my.queueCapacity}")
	private int myQueueCapacity;
	@Value("${my.keepAlive}")
	private int myKeepAlive;
	@Value("${channel.corePoolSize}")
	private int channelCorePoolSize;
	@Value("${channel.maxPoolSize}")
	private int channelMaxPoolSize;
	@Value("${channel.queueCapacity}")
	private int channelQueueCapacity;
	@Value("${channel.keepAlive}")
	private int channelKeepAlive;
	@Value("${sdk.topics}")
	private String topics;
	@Value("${sdk.appids}")
    private String appids;
	@Value("${sdk.routeAddresses}")
    private String routeAddresses;
    @Value("${sdk.switch}")
    private String meshSwitch;
    @Value("${sdk.publickeys}")
    private String publickeys;
    @Value("${sdk.wb.publickey}")
    private String wbPublickey;
    @Value("${isEnable.whiteIp}")
    private String isEnableWhiteIp;
    @Value("${evidence.maxId}")
    private Long evidenceMaxId;
	
	public Long getEvidenceMaxId() {
		return evidenceMaxId;
	}
	public void setEvidenceMaxId(Long evidenceMaxId) {
		this.evidenceMaxId = evidenceMaxId;
	}
	public String getIsEnableWhiteIp() {
		return isEnableWhiteIp;
	}
	public void setIsEnableWhiteIp(String isEnableWhiteIp) {
		this.isEnableWhiteIp = isEnableWhiteIp;
	}
	public String getChainCount() {
		return chainCount;
	}
	public void setChainCount(String chainCount) {
		this.chainCount = chainCount;
	}
	public String getNodifyCount() {
		return nodifyCount;
	}
	public void setNodifyCount(String nodifyCount) {
		this.nodifyCount = nodifyCount;
	}
	public String getSelectCount() {
		return selectCount;
	}
	public void setSelectCount(String selectCount) {
		this.selectCount = selectCount;
	}
	public String getChainTime() {
		return chainTime;
	}
	public void setChainTime(String chainTime) {
		this.chainTime = chainTime;
	}
	public String getInternalSleepTime() {
		return internalSleepTime;
	}
	public void setInternalSleepTime(String internalSleepTime) {
		this.internalSleepTime = internalSleepTime;
	}
	public String getScheduleTime() {
		return scheduleTime;
	}
	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}
	public int getMyCorePoolSize() {
		return myCorePoolSize;
	}
	public void setMyCorePoolSize(int myCorePoolSize) {
		this.myCorePoolSize = myCorePoolSize;
	}
	public int getMyMaxPoolSize() {
		return myMaxPoolSize;
	}
	public void setMyMaxPoolSize(int myMaxPoolSize) {
		this.myMaxPoolSize = myMaxPoolSize;
	}
	public int getMyQueueCapacity() {
		return myQueueCapacity;
	}
	public void setMyQueueCapacity(int myQueueCapacity) {
		this.myQueueCapacity = myQueueCapacity;
	}
	public int getMyKeepAlive() {
		return myKeepAlive;
	}
	public void setMyKeepAlive(int myKeepAlive) {
		this.myKeepAlive = myKeepAlive;
	}
	public int getChannelCorePoolSize() {
		return channelCorePoolSize;
	}
	public void setChannelCorePoolSize(int channelCorePoolSize) {
		this.channelCorePoolSize = channelCorePoolSize;
	}
	public int getChannelMaxPoolSize() {
		return channelMaxPoolSize;
	}
	public void setChannelMaxPoolSize(int channelMaxPoolSize) {
		this.channelMaxPoolSize = channelMaxPoolSize;
	}
	public int getChannelQueueCapacity() {
		return channelQueueCapacity;
	}
	public void setChannelQueueCapacity(int channelQueueCapacity) {
		this.channelQueueCapacity = channelQueueCapacity;
	}
	public int getChannelKeepAlive() {
		return channelKeepAlive;
	}
	public void setChannelKeepAlive(int channelKeepAlive) {
		this.channelKeepAlive = channelKeepAlive;
	}
	public String getTopics() {
		return topics;
	}
	public void setTopics(String topics) {
		this.topics = topics;
	}
	public String getAppids() {
        return appids;
    }
    public void setAppids(String appids) {
        this.appids = appids;
    }
    public String getRouteAddresses() {
		return routeAddresses;
	}
	public void setRouteAddresses(String routeAddresses) {
		this.routeAddresses = routeAddresses;
	}
	public String getMeshSwitch() {
		return meshSwitch;
	}
	public void setMeshSwitch(String meshSwitch) {
		this.meshSwitch = meshSwitch;
	}
	public String getPublickeys() {
		return publickeys;
	}
	public void setPublickeys(String publickeys) {
		this.publickeys = publickeys;
	}
	public String getWbPublickey() {
		return wbPublickey;
	}
	public void setWbPublickey(String wbPublickey) {
		this.wbPublickey = wbPublickey;
	}
}
