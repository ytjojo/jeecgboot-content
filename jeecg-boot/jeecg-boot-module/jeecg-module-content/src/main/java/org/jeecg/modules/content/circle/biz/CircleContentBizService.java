package org.jeecg.modules.content.circle.biz;

import jakarta.annotation.Resource;
import org.jeecg.modules.content.circle.entity.CircleContent;
import org.jeecg.modules.content.circle.mapper.CircleContentMapper;
import org.jeecg.modules.content.circle.vo.CircleContentVO;
import org.jeecg.modules.content.user.service.IContentUserBadgeService;
import org.jeecg.modules.content.user.vo.ContentUserBadgeVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 圈子内容业务编排服务。
 * 查询帖子列表并填充作者佩戴的勋章信息。
 */
@Service
public class CircleContentBizService {

    @Resource
    private CircleContentMapper circleContentMapper;

    @Resource
    private IContentUserBadgeService contentUserBadgeService;

    /**
     * 查询圈子帖子列表，携带作者佩戴的勋章。
     *
     * @param circleId 圈子ID
     * @return 帖子列表（含 authorBadges）
     */
    public List<CircleContentVO> listPostsWithAuthorBadges(String circleId) {
        List<CircleContent> posts = circleContentMapper.selectCircleContentList(circleId);
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }

        // 收集所有作者ID，批量查询佩戴勋章
        List<String> userIds = posts.stream()
                .map(CircleContent::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, List<ContentUserBadgeVO>> badgesByUser = userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> contentUserBadgeService.listWornBadges(userId),
                        (a, b) -> a
                ));

        return posts.stream().map(post -> {
            CircleContentVO vo = new CircleContentVO();
            vo.setId(post.getId());
            vo.setCircleId(post.getCircleId());
            vo.setUserId(post.getUserId());
            vo.setContent(post.getContent());
            vo.setContentType(post.getContentType());
            vo.setStatus(post.getStatus());
            vo.setIsPinned(post.getIsPinned());
            vo.setPinnedAt(post.getPinnedAt());
            vo.setIsFeatured(post.getIsFeatured());
            vo.setFeaturedAt(post.getFeaturedAt());
            vo.setDeleted(post.getDeleted());
            vo.setCreateBy(post.getCreateBy());
            vo.setCreateTime(post.getCreateTime());
            vo.setUpdateBy(post.getUpdateBy());
            vo.setUpdateTime(post.getUpdateTime());
            vo.setAuthorBadges(badgesByUser.getOrDefault(post.getUserId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }
}
