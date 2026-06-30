package org.jeecg.modules.content.auth.biz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.auth.entity.ContentCancellationRequest;
import org.jeecg.modules.content.auth.entity.ContentUserAccount;
import org.jeecg.modules.content.auth.mapper.ContentCancellationRequestMapper;
import org.jeecg.modules.content.auth.mapper.ContentUserAccountMapper;
import org.jeecg.modules.content.auth.req.ContentCancelApplyReq;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 账号注销业务编排服务实现。
 */
@Slf4j
@Service
public class ContentAccountCancellationBizServiceImpl implements IContentAccountCancellationBizService {

    /** 注销申请状态：待处理 */
    private static final String REQUEST_STATUS_PENDING = "PENDING";
    /** 注销申请状态：已撤销 */
    private static final String REQUEST_STATUS_REVOKED = "REVOKED";
    /** 注销申请状态：已完成 */
    private static final String REQUEST_STATUS_COMPLETED = "COMPLETED";

    /** 账号注销状态：无注销 */
    private static final String CANCELLATION_STATUS_NONE = "NONE";
    /** 账号注销状态：注销中 */
    private static final String CANCELLATION_STATUS_CANCELLING = "CANCELLING";
    /** 账号注销状态：已注销 */
    private static final String CANCELLATION_STATUS_CANCELLED = "CANCELLED";

    /** 账号状态：正常 */
    private static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";
    /** 账号状态：禁用 */
    private static final String ACCOUNT_STATUS_DISABLED = "DISABLED";

    @Resource
    private ContentCancellationRequestMapper cancellationRequestMapper;

    @Resource
    private ContentUserAccountMapper accountMapper;

    @Lazy
    @Resource
    private IContentAccountCancellationBizService self;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyCancellation(ContentCancelApplyReq req) {
        // 1. 校验冷静期天数
        validateCooldownDays(req.getCooldownDays());

        // 2. 检查是否已有进行中的注销申请
        ContentCancellationRequest existingRequest = findPendingRequest(req.getUserId());
        if (existingRequest != null) {
            throw new JeecgBootException("已有进行中的注销申请");
        }

        // 3. 检查账号状态是否正常
        ContentUserAccount account = accountMapper.selectActiveByUserId(req.getUserId());
        if (account == null) {
            throw new JeecgBootException("账号不存在或已注销");
        }

        // 4. 创建注销申请记录
        Date now = new Date();
        Date cooldownDeadline = new Date(now.getTime() + TimeUnit.DAYS.toMillis(req.getCooldownDays()));

        ContentCancellationRequest request = new ContentCancellationRequest()
                .setUserId(req.getUserId())
                .setStatus(REQUEST_STATUS_PENDING)
                .setApplyReason(req.getReason())
                .setApplyTime(now)
                .setCooldownDays(req.getCooldownDays())
                .setCooldownDeadline(cooldownDeadline)
                .setAnonymized(false);
        cancellationRequestMapper.insert(request);

        // 5. 更新账号注销状态
        account.setCancellationStatus(CANCELLATION_STATUS_CANCELLING)
                .setCancelApplyTime(now);
        accountMapper.updateById(account);

        log.info("用户注销申请已创建, userId={}, cooldownDays={}, deadline={}",
                req.getUserId(), req.getCooldownDays(), cooldownDeadline);
    }

    @Override
    public String checkCooldownStatus(String userId) {
        // 1. 查询进行中的注销申请
        ContentCancellationRequest request = findPendingRequest(userId);
        if (request == null) {
            return null;
        }

        // 2. 检查是否在冷静期内
        Date now = new Date();
        if (request.getCooldownDeadline() != null && now.before(request.getCooldownDeadline())) {
            long remainingMillis = request.getCooldownDeadline().getTime() - now.getTime();
            long remainingDays = TimeUnit.MILLISECONDS.toDays(remainingMillis) + 1;
            return "COOLDOWN_ACTIVE:剩余" + remainingDays + "天";
        }

        // 3. 已过冷静期
        return "READY_TO_CANCEL";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeCancellation(String userId) {
        // 1. 查找PENDING状态的注销申请
        ContentCancellationRequest request = findPendingRequest(userId);
        if (request == null) {
            throw new JeecgBootException("无进行中的注销申请");
        }

        // 2. 更新申请状态为已撤销
        request.setStatus(REQUEST_STATUS_REVOKED)
                .setRevokeTime(new Date());
        cancellationRequestMapper.updateById(request);

        // 3. 更新账号注销状态
        ContentUserAccount account = accountMapper.selectOne(
                new LambdaQueryWrapper<ContentUserAccount>()
                        .eq(ContentUserAccount::getUserId, userId)
        );
        if (account != null) {
            account.setCancellationStatus(CANCELLATION_STATUS_NONE)
                    .setCancelApplyTime(null);
            accountMapper.updateById(account);
        }

        log.info("用户注销申请已撤销, userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeCancellation(String userId) {
        // 1. 查找PENDING状态的注销申请
        ContentCancellationRequest request = findPendingRequest(userId);
        if (request == null) {
            throw new JeecgBootException("无进行中的注销申请");
        }

        // 2. 检查冷静期是否已过
        Date now = new Date();
        if (request.getCooldownDeadline() != null && now.before(request.getCooldownDeadline())) {
            throw new JeecgBootException("冷静期未结束");
        }

        // 3. 更新申请状态为已完成
        request.setStatus(REQUEST_STATUS_COMPLETED)
                .setCompleteTime(now)
                .setAnonymized(true);
        cancellationRequestMapper.updateById(request);

        // 4. 更新账号状态
        ContentUserAccount account = accountMapper.selectOne(
                new LambdaQueryWrapper<ContentUserAccount>()
                        .eq(ContentUserAccount::getUserId, userId)
        );
        if (account != null) {
            account.setCancellationStatus(CANCELLATION_STATUS_CANCELLED)
                    .setAccountStatus(ACCOUNT_STATUS_DISABLED)
                    .setCancelCompleteTime(now);
            accountMapper.updateById(account);
        }

        log.info("用户账号已注销, userId={}", userId);
    }

    @Override
    public void validateCooldownDays(Integer days) {
        if (days == null) {
            throw new JeecgBootException("冷静期天数不能为空");
        }
        if (days < 7) {
            throw new JeecgBootException("冷静期最少7天");
        }
        if (days > 30) {
            throw new JeecgBootException("冷静期最多30天");
        }
    }

    /**
     * 查找用户待处理的注销申请。
     */
    private ContentCancellationRequest findPendingRequest(String userId) {
        return cancellationRequestMapper.selectOne(
                new LambdaQueryWrapper<ContentCancellationRequest>()
                        .eq(ContentCancellationRequest::getUserId, userId)
                        .eq(ContentCancellationRequest::getStatus, REQUEST_STATUS_PENDING)
        );
    }

    @Override
    public Map<String, Object> checkEligibility(String userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> checks = new ArrayList<>();
        boolean eligible = true;

        // 检查账号状态
        ContentUserAccount account = accountMapper.selectActiveByUserId(userId);
        if (account == null) {
            eligible = false;
            Map<String, Object> check = new HashMap<>();
            check.put("name", "账号状态");
            check.put("passed", false);
            check.put("reason", "账号不存在或已注销");
            checks.add(check);
        } else {
            Map<String, Object> accountCheck = new HashMap<>();
            accountCheck.put("name", "账号状态");
            accountCheck.put("passed", true);
            checks.add(accountCheck);
        }

        result.put("eligible", eligible);
        result.put("checks", checks);
        return result;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void processExpiredCancellations() {
        log.info("开始处理冷静期到期的注销申请");
        Date now = new Date();
        List<ContentCancellationRequest> expiredRequests = cancellationRequestMapper.selectList(
                new LambdaQueryWrapper<ContentCancellationRequest>()
                        .eq(ContentCancellationRequest::getStatus, REQUEST_STATUS_PENDING)
                        .le(ContentCancellationRequest::getCooldownDeadline, now)
        );
        int successCount = 0;
        int failCount = 0;
        for (ContentCancellationRequest request : expiredRequests) {
            try {
                self.completeCancellation(request.getUserId());
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("自动注销失败, userId={}, error={}", request.getUserId(), e.getMessage(), e);
            }
        }
        log.info("处理冷静期到期注销申请完成, 到期数={}, 成功={}, 失败={}",
                expiredRequests.size(), successCount, failCount);
    }
}
