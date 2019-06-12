package org.bcos.evidence.sdkImpl;

import org.fisco.bcos.channel.client.ChannelPushCallback;
import org.fisco.bcos.channel.dto.ChannelPush;
import org.fisco.bcos.channel.dto.ChannelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PushCallback extends ChannelPushCallback {
    static Logger logger = LoggerFactory.getLogger(PushCallback.class);
    @Override
    public void onPush(ChannelPush push) {
        logger.debug("### callback PushCallback extend ChannelPushCallback");
        ChannelResponse response = new ChannelResponse();
        try {
            logger.debug("### callback PushCallback extend ChannelPushCallback: before evidenceFaceImpl");
            if (evidenceFaceImpl != null)
            {
                logger.debug("### callback PushCallback extend ChannelPushCallback: before evidenceFaceImpl.onPush");
                evidenceFaceImpl.onPush(push);
                logger.debug("### callback PushCallback extend ChannelPushCallback: after evidenceFaceImpl.onPush");
            }
            response.setErrorCode(0);
        }catch (Exception e)
        {
            logger.debug("### callback PushCallback extend ChannelPushCallback: catch");
            response.setErrorCode(-1);
        }
        logger.debug("### callback PushCallback extend ChannelPushCallback: sendResponse");
        response.setContent("receive request seq:" + String.valueOf(push.getMessageID()) + ", content:" + push.getContent());
        push.sendResponse(response);
        logger.debug("### callback PushCallback extend ChannelPushCallback: after sendResponse");
    }

    public void setSDK(EvidenceFaceImpl evidenceFace) {
        this.evidenceFaceImpl = evidenceFace;
    }

    EvidenceFaceImpl evidenceFaceImpl;
}
