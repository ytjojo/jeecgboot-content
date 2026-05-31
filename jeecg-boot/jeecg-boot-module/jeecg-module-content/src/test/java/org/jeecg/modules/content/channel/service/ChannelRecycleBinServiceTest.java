package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ChannelRecycleBin;
import org.jeecg.modules.content.channel.mapper.ChannelRecycleBinMapper;
import org.jeecg.modules.content.channel.service.impl.ChannelRecycleBinServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelRecycleBinServiceTest {

    @InjectMocks
    private ChannelRecycleBinServiceImpl service;

    @Mock
    private ChannelRecycleBinMapper recycleBinMapper;

    @Test
    void addToRecycleBin_shouldSetExpireTime30DaysLater() {
        when(recycleBinMapper.insert(any(ChannelRecycleBin.class))).thenReturn(1);

        ChannelRecycleBin bin = service.addToRecycleBin("ch-1", "content-1", "article", "author-1", "admin-1", "违规内容");
        assertNotNull(bin.getExpireTime());
        assertEquals("ch-1", bin.getChannelId());
        assertFalse(bin.getIsRestored());
    }

    @Test
    void restore_shouldMarkAsRestored() {
        ChannelRecycleBin bin = new ChannelRecycleBin();
        bin.setId("bin-1");
        bin.setIsRestored(false);
        bin.setExpireTime(new Date(System.currentTimeMillis() + 86400000L));
        when(recycleBinMapper.selectById("bin-1")).thenReturn(bin);
        when(recycleBinMapper.updateById(any(ChannelRecycleBin.class))).thenReturn(1);

        boolean result = service.restore("bin-1", "admin-1");
        assertTrue(result);
        assertTrue(bin.getIsRestored());
    }

    @Test
    void restore_shouldFailWhenExpired() {
        ChannelRecycleBin bin = new ChannelRecycleBin();
        bin.setId("bin-1");
        bin.setIsRestored(false);
        bin.setExpireTime(new Date(System.currentTimeMillis() - 86400000L));
        when(recycleBinMapper.selectById("bin-1")).thenReturn(bin);

        boolean result = service.restore("bin-1", "admin-1");
        assertFalse(result);
    }
}
