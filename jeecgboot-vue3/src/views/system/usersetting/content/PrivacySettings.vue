<template>
  <div :class="[`${prefixCls}`]">
    <div class="privacy-title">隐私设置</div>

    <!-- 动态可见性设置 -->
    <div class="privacy-section">
      <div class="section-label">动态可见性</div>

      <div class="privacy-row">
        <div class="row-label">浏览记录</div>
        <a-select
          v-model:value="form.browseHistoryVisibility"
          style="width: 180px"
          @change="handleVisibilityChange('browseHistoryVisibility', $event)"
        >
          <a-select-option value="PUBLIC">所有人可见</a-select-option>
          <a-select-option value="FOLLOWERS_ONLY">仅关注者可见</a-select-option>
          <a-select-option value="PRIVATE">仅自己可见</a-select-option>
        </a-select>
      </div>
      <a-alert
        v-if="form.browseHistoryVisibility === 'PRIVATE'"
        message="设置为仅自己可见后，其他用户将无法看到您的浏览记录。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <div class="privacy-row">
        <div class="row-label">点赞动态</div>
        <a-select
          v-model:value="form.likeActivityVisibility"
          style="width: 180px"
          @change="handleVisibilityChange('likeActivityVisibility', $event)"
        >
          <a-select-option value="PUBLIC">所有人可见</a-select-option>
          <a-select-option value="FOLLOWERS_ONLY">仅关注者可见</a-select-option>
          <a-select-option value="PRIVATE">仅自己可见</a-select-option>
        </a-select>
      </div>
      <a-alert
        v-if="form.likeActivityVisibility === 'PRIVATE'"
        message="设置为仅自己可见后，其他用户将无法看到您的点赞动态。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <div class="privacy-row">
        <div class="row-label">收藏夹</div>
        <a-select
          v-model:value="form.favoriteVisibility"
          style="width: 180px"
          @change="handleVisibilityChange('favoriteVisibility', $event)"
        >
          <a-select-option value="PUBLIC">所有人可见</a-select-option>
          <a-select-option value="FOLLOWERS_ONLY">仅关注者可见</a-select-option>
          <a-select-option value="PRIVATE">仅自己可见</a-select-option>
        </a-select>
      </div>
      <a-alert
        v-if="form.favoriteVisibility === 'PRIVATE'"
        message="设置为仅自己可见后，其他用户将无法看到您的收藏夹内容。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
    </div>

    <!-- 在线状态可见性 -->
    <div class="privacy-section">
      <div class="section-label">在线状态可见性</div>
      <div class="privacy-row">
        <a-select
          v-model:value="form.onlineStatusVisibility"
          style="width: 180px"
          @change="handleFieldChange"
        >
          <a-select-option value="PUBLIC">公开</a-select-option>
          <a-select-option value="HIDDEN">隐藏</a-select-option>
          <a-select-option value="MUTUAL_ONLY">仅互关可见</a-select-option>
        </a-select>
      </div>
    </div>

    <!-- 搜索引擎索引 -->
    <div class="privacy-section">
      <div class="privacy-row">
        <div class="row-label">允许搜索引擎索引</div>
        <a-switch v-model:checked="form.searchEngineIndex" @change="handleFieldChange" />
      </div>
    </div>

    <!-- 用户搜索 -->
    <div class="privacy-section">
      <div class="privacy-row">
        <div class="row-label">允许用户搜索</div>
        <a-switch v-model:checked="form.userSearchable" @change="handleFieldChange" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, reactive } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { getPrivacySetting, updatePrivacySetting } from '/@/api/content/settings';

  const { prefixCls } = useDesign('j-privacy-setting-container');
  const { createMessage } = useMessage();
  const userStore = useUserStore();

  /** 表单数据 */
  const form = reactive({
    browseHistoryVisibility: 'PUBLIC',
    likeActivityVisibility: 'PUBLIC',
    favoriteVisibility: 'PUBLIC',
    onlineStatusVisibility: 'PUBLIC',
    searchEngineIndex: true,
    userSearchable: true,
  });

  /** 获取当前用户ID */
  function getUserId(): string {
    return userStore.getUserInfo?.id || '';
  }

  /**
   * 可见性字段变更，保存到后端
   * @param field 字段名
   * @param value 新值
   */
  function handleVisibilityChange(field: string, value: string) {
    form[field] = value;
    handleFieldChange();
  }

  /** 通用保存 */
  function handleFieldChange() {
    const userId = getUserId();
    if (!userId) return;
    updatePrivacySetting(userId, { ...form }).then((res) => {
      if (res.success) {
        createMessage.success('保存成功');
      } else {
        createMessage.warning(res.message || '保存失败');
      }
    });
  }

  /** 加载隐私设置 */
  function loadPrivacySetting() {
    const userId = getUserId();
    if (!userId) return;
    getPrivacySetting(userId).then((res) => {
      if (res.success && res.result) {
        Object.assign(form, res.result);
      }
    });
  }

  onMounted(() => {
    loadPrivacySetting();
  });
</script>

<style lang="less">
  @prefix-cls: ~'@{namespace}-j-privacy-setting-container';

  .@{prefix-cls} {
    padding: 30px 40px 0 20px;

    .privacy-title {
      font-size: 17px;
      font-weight: 700;
      color: @text-color;
      margin-bottom: 24px;
    }

    .privacy-section {
      margin-bottom: 24px;

      .section-label {
        font-size: 15px;
        font-weight: 700;
        color: @text-color;
        margin-bottom: 16px;
      }
    }

    .privacy-row {
      display: flex;
      align-items: center;
      margin-bottom: 16px;

      .row-label {
        width: 160px;
        font-size: 13px;
        color: @text-color;
      }
    }
  }
</style>
