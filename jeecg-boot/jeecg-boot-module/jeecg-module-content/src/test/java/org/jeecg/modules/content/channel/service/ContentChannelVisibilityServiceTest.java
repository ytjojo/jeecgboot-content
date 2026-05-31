package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.dto.ChannelVisibilityDTO;
import org.jeecg.modules.content.channel.enums.ChannelStatus;
import org.jeecg.modules.content.channel.service.impl.ContentChannelVisibilityServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ContentChannelVisibilityServiceTest {

    @InjectMocks
    private ContentChannelVisibilityServiceImpl visibilityService;

    @Test
    void isDiscoverable_shouldReturnTrueForPublicActiveChannel() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        assertThat(visibilityService.isDiscoverable(dto)).isTrue();
    }

    @Test
    void isDiscoverable_shouldReturnFalseForPrivateChannel() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setPrivacy(2);
        assertThat(visibilityService.isDiscoverable(dto)).isFalse();
    }

    @Test
    void isDiscoverable_shouldReturnFalseForNullPrivacy() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setPrivacy(null);
        assertThat(visibilityService.isDiscoverable(dto)).isFalse();
    }

    @Test
    void isDiscoverable_shouldReturnFalseForDraftChannel() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setStatus(ChannelStatus.DRAFT);
        assertThat(visibilityService.isDiscoverable(dto)).isFalse();
    }

    @Test
    void isDiscoverable_shouldReturnFalseForPendingReviewChannel() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setStatus(ChannelStatus.PENDING_REVIEW);
        assertThat(visibilityService.isDiscoverable(dto)).isFalse();
    }

    @Test
    void isDiscoverable_shouldReturnFalseForRejectedChannel() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setStatus(ChannelStatus.REJECTED);
        assertThat(visibilityService.isDiscoverable(dto)).isFalse();
    }

    @Test
    void isDiscoverable_shouldReturnFalseForDeletedChannel() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setStatus(ChannelStatus.DELETED);
        assertThat(visibilityService.isDiscoverable(dto)).isFalse();
    }

    @Test
    void isDiscoverable_shouldReturnFalseForNull() {
        assertThat(visibilityService.isDiscoverable(null)).isFalse();
    }

    @Test
    void filterDiscoverable_shouldFilterOutNonDiscoverable() {
        ChannelVisibilityDTO public1 = createPublicActiveChannel();
        ChannelVisibilityDTO private1 = createPublicActiveChannel();
        private1.setPrivacy(2);
        ChannelVisibilityDTO draft1 = createPublicActiveChannel();
        draft1.setStatus(ChannelStatus.DRAFT);

        List<ChannelVisibilityDTO> result = visibilityService.filterDiscoverable(
                List.of(public1, private1, draft1));

        assertThat(result).hasSize(1);
    }

    private ChannelVisibilityDTO createPublicActiveChannel() {
        ChannelVisibilityDTO dto = new ChannelVisibilityDTO();
        dto.setChannelId("test-channel");
        dto.setStatus(ChannelStatus.ACTIVE);
        dto.setPrivacy(1);
        return dto;
    }
}
