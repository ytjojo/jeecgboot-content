package org.jeecg.modules.content.user.biz;

import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserBlock;
import org.jeecg.modules.content.user.mapper.ContentUserBlockMapper;
import org.springframework.stereotype.Service;

/**
 * 内容社区用户关系边界编排服务。
 */
@Service
public class ContentUserRelationBoundaryBizService {

    private static final int USER_ID_MAX_LENGTH = 64;
    private static final String ACTIVE_STATUS = "ACTIVE";

    @Resource
    private ContentUserBlockMapper blockMapper;

    /**
     * 判断任一方向是否存在生效拉黑关系。
     */
    public boolean isBlockedEitherWay(String userId, String targetUserId) {
        if (isBlank(userId) || isBlank(targetUserId)) {
            return false;
        }
        return isBlockedBy(userId, targetUserId) || isBlockedBy(targetUserId, userId);
    }

    /**
     * 判断 userId 是否主动拉黑了 targetUserId。
     */
    public boolean isBlockedBy(String userId, String targetUserId) {
        if (isBlank(userId) || isBlank(targetUserId)) {
            return false;
        }
        return isActiveBlock(blockMapper.selectByPair(userId, targetUserId));
    }

    /**
     * 断言当前用户可以与目标用户互动。
     */
    public void assertCanInteract(String userId, String targetUserId) {
        requireValidUserId(userId, "当前用户ID不能为空", "当前用户ID长度不能超过64位");
        requireValidUserId(targetUserId, "目标用户ID不能为空", "目标用户ID长度不能超过64位");
        if (isBlockedEitherWay(userId, targetUserId)) {
            throw new JeecgBootException("操作失败");
        }
    }

    private boolean isActiveBlock(ContentUserBlock block) {
        return block != null && ACTIVE_STATUS.equals(block.getStatus());
    }

    private void requireValidUserId(String userId, String blankMessage, String lengthMessage) {
        if (isBlank(userId)) {
            throw new JeecgBootException(blankMessage);
        }
        if (userId.length() > USER_ID_MAX_LENGTH) {
            throw new JeecgBootException(lengthMessage);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
