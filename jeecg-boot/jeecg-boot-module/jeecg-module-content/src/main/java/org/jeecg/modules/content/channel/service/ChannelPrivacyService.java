package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.enums.PrivacyType;

public interface ChannelPrivacyService {

    void updatePrivacy(String channelId, PrivacyType privacyType, String operatorId);
}
