package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentInviteCode;
import org.jeecg.modules.content.user.entity.ContentInviteRecord;
import org.jeecg.modules.content.user.entity.ContentUserProfile;
import org.jeecg.modules.content.user.entity.ContentUserRewardRule;
import org.jeecg.modules.content.user.mapper.ContentInviteCodeMapper;
import org.jeecg.modules.content.user.mapper.ContentInviteRecordMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileMapper;
import org.jeecg.modules.content.user.service.IContentInviteService;
import org.jeecg.modules.content.user.service.IContentUserRewardRuleService;
import org.jeecg.modules.content.user.vo.ContentInviteCodeVO;
import org.jeecg.modules.content.user.vo.ContentInviteInfoVO;
import org.jeecg.modules.content.user.vo.ContentInviteRecordPageVO;
import org.jeecg.modules.content.user.vo.ContentInviteStatsVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 内容社区邀请服务实现。
 */
@Service
public class ContentInviteServiceImpl implements IContentInviteService {

    private static final String INVITE_RULE_CODE = "INVITE_REGISTER";
    private static final String INVITE_URL_PREFIX = "/invite?code=";

    @Resource
    private ContentInviteCodeMapper inviteCodeMapper;

    @Resource
    private ContentInviteRecordMapper inviteRecordMapper;

    @Resource
    private ContentUserProfileMapper profileMapper;

    @Resource
    private IContentUserRewardRuleService rewardRuleService;

    @Override
    public ContentInviteCodeVO generateOrGetInviteCode(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new JeecgBootException("用户ID不能为空");
        }
        ContentInviteCode existing = inviteCodeMapper.selectOne(
            Wrappers.<ContentInviteCode>lambdaQuery()
                .eq(ContentInviteCode::getUserId, userId)
        );
        if (existing != null) {
            return new ContentInviteCodeVO()
                .setInviteCode(existing.getInviteCode())
                .setInviteUrl(INVITE_URL_PREFIX + existing.getInviteCode());
        }
        String code = generateUniqueCode(userId);
        ContentInviteCode entity = new ContentInviteCode()
            .setUserId(userId)
            .setInviteCode(code);
        inviteCodeMapper.insert(entity);
        return new ContentInviteCodeVO()
            .setInviteCode(code)
            .setInviteUrl(INVITE_URL_PREFIX + code);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindInviteRelation(String inviteCode, String inviteeUserId) {
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new JeecgBootException("邀请码不能为空");
        }
        if (inviteeUserId == null || inviteeUserId.isBlank()) {
            throw new JeecgBootException("被邀请人ID不能为空");
        }
        ContentInviteCode inviteCodeEntity = inviteCodeMapper.selectOne(
            Wrappers.<ContentInviteCode>lambdaQuery()
                .eq(ContentInviteCode::getInviteCode, inviteCode)
        );
        if (inviteCodeEntity == null) {
            throw new JeecgBootException("邀请码无效");
        }
        String inviterUserId = inviteCodeEntity.getUserId();
        if (inviterUserId.equals(inviteeUserId)) {
            throw new JeecgBootException("不能邀请自己");
        }
        ContentInviteRecord existingRecord = inviteRecordMapper.selectOne(
            Wrappers.<ContentInviteRecord>lambdaQuery()
                .eq(ContentInviteRecord::getInviteeUserId, inviteeUserId)
        );
        if (existingRecord != null) {
            throw new JeecgBootException("该用户已被邀请");
        }
        Optional<ContentUserRewardRule> ruleOpt = rewardRuleService.getEnabledRule(INVITE_RULE_CODE);
        int rewardPoint = 0;
        String rewardStatus = "PENDING";
        if (ruleOpt.isPresent()) {
            ContentUserRewardRule rule = ruleOpt.get();
            rewardPoint = rule.getPointAmount() != null ? rule.getPointAmount() : 0;
            if (rewardPoint > 0) {
                int dailyCap = rule.getDailyPointCap() != null ? rule.getDailyPointCap() : Integer.MAX_VALUE;
                int todayTotal = sumTodayRewardPoints(inviterUserId);
                if (todayTotal + rewardPoint > dailyCap) {
                    rewardPoint = Math.max(0, dailyCap - todayTotal);
                }
                if (rewardPoint > 0) {
                    grantPoints(inviterUserId, rewardPoint);
                    rewardStatus = "GRANTED";
                }
            }
        }
        ContentInviteRecord record = new ContentInviteRecord()
            .setInviterUserId(inviterUserId)
            .setInviteeUserId(inviteeUserId)
            .setInviteCode(inviteCode)
            .setRegisteredAt(new Date())
            .setRewardPoint(rewardPoint)
            .setRewardStatus(rewardStatus);
        inviteRecordMapper.insert(record);
    }

    @Override
    public ContentInviteRecordPageVO listInviteRecords(String userId, Long pageNo, Long pageSize) {
        if (userId == null || userId.isBlank()) {
            throw new JeecgBootException("用户ID不能为空");
        }
        pageNo = pageNo != null && pageNo > 0 ? pageNo : 1L;
        pageSize = pageSize != null && pageSize > 0 ? pageSize : 10L;
        Page<ContentInviteRecord> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<ContentInviteRecord> wrapper = Wrappers.<ContentInviteRecord>lambdaQuery()
            .eq(ContentInviteRecord::getInviterUserId, userId)
            .orderByDesc(ContentInviteRecord::getRegisteredAt);
        Page<ContentInviteRecord> result = inviteRecordMapper.selectPage(page, wrapper);
        List<ContentInviteRecordPageVO.Item> items = result.getRecords().stream()
            .map(r -> new ContentInviteRecordPageVO.Item()
                .setId(r.getId())
                .setInviteeUserId(r.getInviteeUserId())
                .setInviteCode(r.getInviteCode())
                .setRegisteredAt(r.getRegisteredAt())
                .setRewardPoint(r.getRewardPoint())
                .setRewardStatus(r.getRewardStatus()))
            .toList();
        return new ContentInviteRecordPageVO()
            .setCurrent(result.getCurrent())
            .setSize(result.getSize())
            .setTotal(result.getTotal())
            .setRecords(items);
    }

    @Override
    public ContentInviteStatsVO getInviteStats(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new JeecgBootException("用户ID不能为空");
        }
        Long totalInvites = inviteRecordMapper.selectCount(
            Wrappers.<ContentInviteRecord>lambdaQuery()
                .eq(ContentInviteRecord::getInviterUserId, userId)
        );
        Long successful = inviteRecordMapper.selectCount(
            Wrappers.<ContentInviteRecord>lambdaQuery()
                .eq(ContentInviteRecord::getInviterUserId, userId)
                .eq(ContentInviteRecord::getRewardStatus, "GRANTED")
        );
        List<ContentInviteRecord> records = inviteRecordMapper.selectList(
            Wrappers.<ContentInviteRecord>lambdaQuery()
                .eq(ContentInviteRecord::getInviterUserId, userId)
                .eq(ContentInviteRecord::getRewardStatus, "GRANTED")
        );
        int totalPoints = records.stream()
            .mapToInt(r -> r.getRewardPoint() != null ? r.getRewardPoint() : 0)
            .sum();
        return new ContentInviteStatsVO()
            .setTotalInvites(totalInvites)
            .setSuccessfulRegistrations(successful)
            .setTotalPointsEarned(totalPoints);
    }

    private String generateUniqueCode(String userId) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int hash = Long.hashCode(((long) userId.hashCode()) * 31 + System.currentTimeMillis() + attempt);
            String code = toBase36(Math.abs(hash), 8);
            Long count = inviteCodeMapper.selectCount(
                Wrappers.<ContentInviteCode>lambdaQuery()
                    .eq(ContentInviteCode::getInviteCode, code)
            );
            if (count == 0) {
                return code;
            }
        }
        throw new JeecgBootException("邀请码生成失败，请重试");
    }

    private String toBase36(int value, int length) {
        String raw = Integer.toString(value, 36).toUpperCase();
        if (raw.length() >= length) {
            return raw.substring(0, length);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = raw.length(); i < length; i++) {
            sb.append('0');
        }
        sb.append(raw);
        return sb.toString();
    }

    private int sumTodayRewardPoints(String inviterUserId) {
        String todayBucket = new SimpleDateFormat("yyyyMMdd").format(new Date());
        List<ContentInviteRecord> todayRecords = inviteRecordMapper.selectList(
            Wrappers.<ContentInviteRecord>lambdaQuery()
                .eq(ContentInviteRecord::getInviterUserId, inviterUserId)
                .eq(ContentInviteRecord::getRewardStatus, "GRANTED")
                .apply("date_format(registered_at, '%Y%m%d') = {0}", todayBucket)
        );
        return todayRecords.stream()
            .mapToInt(r -> r.getRewardPoint() != null ? r.getRewardPoint() : 0)
            .sum();
    }

    private void grantPoints(String userId, int points) {
        ContentUserProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new JeecgBootException("用户资料不存在");
        }
        int newBalance = (profile.getPointBalance() != null ? profile.getPointBalance() : 0) + points;
        profile.setPointBalance(newBalance);
        profileMapper.updateById(profile);
    }

    @Override
    public ContentInviteInfoVO getInviteInfo(String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()) {
            return new ContentInviteInfoVO()
                .setValid(false)
                .setExpired(false)
                .setMaxReached(false);
        }
        ContentInviteCode inviteCodeEntity = inviteCodeMapper.selectOne(
            Wrappers.<ContentInviteCode>lambdaQuery()
                .eq(ContentInviteCode::getInviteCode, inviteCode)
        );
        if (inviteCodeEntity == null) {
            return new ContentInviteInfoVO()
                .setValid(false)
                .setExpired(false)
                .setMaxReached(false);
        }
        // 查询邀请人信息
        ContentUserProfile inviterProfile = profileMapper.selectByUserId(inviteCodeEntity.getUserId());
        String inviterNickname = inviterProfile != null ? inviterProfile.getNickname() : "未知用户";
        String inviterAvatar = inviterProfile != null ? inviterProfile.getAvatar() : null;
        // 查询邀请统计
        Long totalInvites = inviteRecordMapper.selectCount(
            Wrappers.<ContentInviteRecord>lambdaQuery()
                .eq(ContentInviteRecord::getInviterUserId, inviteCodeEntity.getUserId())
        );
        // 假设最大邀请数为 100（可配置）
        int maxInviteCount = 100;
        boolean maxReached = totalInvites >= maxInviteCount;
        // 查询奖励规则
        Optional<ContentUserRewardRule> ruleOpt = rewardRuleService.getEnabledRule(INVITE_RULE_CODE);
        String rewardInfo = "邀请好友注册可获得积分奖励";
        if (ruleOpt.isPresent()) {
            ContentUserRewardRule rule = ruleOpt.get();
            int pointAmount = rule.getPointAmount() != null ? rule.getPointAmount() : 0;
            if (pointAmount > 0) {
                rewardInfo = "邀请好友注册可获得 " + pointAmount + " 积分奖励";
            }
        }
        return new ContentInviteInfoVO()
            .setValid(true)
            .setExpired(false)
            .setMaxReached(maxReached)
            .setInviterNickname(inviterNickname)
            .setInviterAvatar(inviterAvatar)
            .setRewardInfo(rewardInfo);
    }
}
