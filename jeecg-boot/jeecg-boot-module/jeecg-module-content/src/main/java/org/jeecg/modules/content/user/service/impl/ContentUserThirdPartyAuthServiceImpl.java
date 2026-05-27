package org.jeecg.modules.content.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserThirdPartyAuth;
import org.jeecg.modules.content.user.mapper.ContentUserThirdPartyAuthMapper;
import org.jeecg.modules.content.user.service.ContentThirdPartyTokenRevocationPort;
import org.jeecg.modules.content.user.service.IContentUserThirdPartyAuthService;
import org.jeecg.modules.content.user.vo.ContentThirdPartyAuthVO;
import org.jeecg.modules.content.user.vo.ContentThirdPartyAuthorizationDetailVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 内容社区用户第三方授权服务实现。
 */
@Service
public class ContentUserThirdPartyAuthServiceImpl implements IContentUserThirdPartyAuthService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource
    private ContentUserThirdPartyAuthMapper thirdPartyAuthMapper;

    @Resource
    private ContentThirdPartyTokenRevocationPort tokenRevocationPort;

    /**
     * 查询指定用户的所有活跃授权列表。
     */
    @Override
    public List<ContentThirdPartyAuthVO> listActiveAuths(String userId) {
        List<ContentUserThirdPartyAuth> auths = thirdPartyAuthMapper.selectActiveByUserId(userId);
        return auths.stream().map(this::toVO).toList();
    }

    /**
     * 撤销指定授权。撤销后 token hash 清除，状态标记为 REVOKED。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeAuth(String userId, String authId) {
        // 校验授权记录存在且属于当前用户
        ContentUserThirdPartyAuth auth = thirdPartyAuthMapper.selectByAuthIdAndUserId(authId, userId);
        if (auth == null) {
            throw new JeecgBootException("授权记录不存在");
        }
        if (!"ACTIVE".equals(auth.getStatus())) {
            throw new JeecgBootException("授权已被撤销");
        }
        int rows = thirdPartyAuthMapper.revokeByAuthIdAndUserId(authId, userId, new Date());
        // 数据库撤销成功后，调用第三方 token 撤销端口
        tokenRevocationPort.revokeTokens(authId, auth.getTokenHash(), auth.getRefreshTokenHash());
        return rows > 0;
    }

    /**
     * 查询指定授权的详情。
     */
    @Override
    public ContentThirdPartyAuthorizationDetailVO getAuthDetail(String userId, String authId) {
        ContentUserThirdPartyAuth auth = thirdPartyAuthMapper.selectByAuthIdAndUserId(authId, userId);
        if (auth == null) {
            throw new JeecgBootException("授权记录不存在");
        }
        return new ContentThirdPartyAuthorizationDetailVO()
            .setAuthId(auth.getId())
            .setAppName(auth.getAppName() != null ? auth.getAppName() : "未知应用")
            .setAuthTime(auth.getAuthTime())
            .setScopes(parseScopes(auth.getScopes()))
            .setStatus(auth.getStatus())
            .setRevokedAt(auth.getRevokedAt());
    }

    private ContentThirdPartyAuthVO toVO(ContentUserThirdPartyAuth auth) {
        return new ContentThirdPartyAuthVO()
            .setAuthId(auth.getId())
            .setAppName(auth.getAppName() != null ? auth.getAppName() : "未知应用")
            .setAuthTime(auth.getAuthTime())
            .setScopes(parseScopes(auth.getScopes()))
            .setStatus(auth.getStatus());
    }

    private List<String> parseScopes(String scopesJson) {
        if (scopesJson == null || scopesJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(scopesJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
