<template>
  <div :class="prefixCls">
    <a-spin :spinning="loading">
      <a-card title="隐私设置" :bordered="false">
        <!-- 动态可见性设置 -->
        <div :class="`${prefixCls}__section`">
          <h3 :class="`${prefixCls}__section-title`">动态可见性</h3>

          <!-- 浏览记录 -->
          <div :class="`${prefixCls}__item`">
            <div :class="`${prefixCls}__item-header`">
              <span :class="`${prefixCls}__item-label`">浏览记录</span>
              <a-select
                v-model:value="form.browseHistoryVisibility"
                :class="`${prefixCls}__item-select`"
                style="width: 180px"
              >
                <a-select-option value="PUBLIC">公开</a-select-option>
                <a-select-option value="FOLLOWERS_ONLY">仅关注者可见</a-select-option>
                <a-select-option value="MUTUAL_ONLY">仅互关可见</a-select-option>
                <a-select-option value="PRIVATE">仅自己可见</a-select-option>
              </a-select>
            </div>
            <a-alert
              v-if="form.browseHistoryVisibility === 'PRIVATE'"
              message="其他用户将无法在你的主页看到此内容"
              type="info"
              show-icon
              :class="`${prefixCls}__hint`"
            />
          </div>

          <!-- 点赞动态 -->
          <div :class="`${prefixCls}__item`">
            <div :class="`${prefixCls}__item-header`">
              <span :class="`${prefixCls}__item-label`">点赞动态</span>
              <a-select
                v-model:value="form.likeActivityVisibility"
                :class="`${prefixCls}__item-select`"
                style="width: 180px"
              >
                <a-select-option value="PUBLIC">公开</a-select-option>
                <a-select-option value="FOLLOWERS_ONLY">仅关注者可见</a-select-option>
                <a-select-option value="MUTUAL_ONLY">仅互关可见</a-select-option>
                <a-select-option value="PRIVATE">仅自己可见</a-select-option>
              </a-select>
            </div>
            <a-alert
              v-if="form.likeActivityVisibility === 'PRIVATE'"
              message="其他用户将无法在你的主页看到此内容"
              type="info"
              show-icon
              :class="`${prefixCls}__hint`"
            />
          </div>

          <!-- 收藏夹 -->
          <div :class="`${prefixCls}__item`">
            <div :class="`${prefixCls}__item-header`">
              <span :class="`${prefixCls}__item-label`">收藏夹</span>
              <a-select
                v-model:value="form.favoritesVisibility"
                :class="`${prefixCls}__item-select`"
                style="width: 180px"
              >
                <a-select-option value="PUBLIC">公开</a-select-option>
                <a-select-option value="FOLLOWERS_ONLY">仅关注者可见</a-select-option>
                <a-select-option value="MUTUAL_ONLY">仅互关可见</a-select-option>
                <a-select-option value="PRIVATE">仅自己可见</a-select-option>
              </a-select>
            </div>
            <a-alert
              v-if="form.favoritesVisibility === 'PRIVATE'"
              message="其他用户将无法在你的主页看到此内容"
              type="info"
              show-icon
              :class="`${prefixCls}__hint`"
            />
          </div>
        </div>

        <a-divider />

        <!-- 在线状态 -->
        <div :class="`${prefixCls}__section`">
          <h3 :class="`${prefixCls}__section-title`">在线状态</h3>
          <a-radio-group v-model:value="form.onlineStatusVisibility" :class="`${prefixCls}__radio-group`">
            <a-radio value="PUBLIC">公开</a-radio>
            <a-radio value="HIDDEN">
              隐藏
              <span :class="`${prefixCls}__radio-hint`">其他用户将看到你为离线状态</span>
            </a-radio>
            <a-radio value="MUTUAL_ONLY">
              仅互关可见
              <span :class="`${prefixCls}__radio-hint`">仅与你互关的好友可看到你的在线状态</span>
            </a-radio>
          </a-radio-group>
        </div>

        <a-divider />

        <!-- 搜索引擎索引 -->
        <div :class="`${prefixCls}__section`">
          <h3 :class="`${prefixCls}__section-title`">搜索引擎索引</h3>
          <div :class="`${prefixCls}__switch-row`">
            <div :class="`${prefixCls}__switch-info`">
              <span>允许搜索引擎收录</span>
              <span :class="`${prefixCls}__switch-desc`">开启后，你的个人主页可被搜索引擎收录</span>
            </div>
            <a-switch v-model:checked="form.allowSearchEngineIndex" />
          </div>
        </div>

        <a-divider />

        <!-- 保存按钮 -->
        <div :class="`${prefixCls}__actions`">
          <a-button type="primary" :loading="saving" @click="handleSave">保存设置</a-button>
        </div>
      </a-card>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getPrivacySetting, updatePrivacySetting } from '/@/api/content/settings';

  const { prefixCls } = useDesign('privacy-settings');
  const { createMessage } = useMessage();
  const userStore = useUserStore();

  const loading = ref(true);
  const saving = ref(false);

  const form = reactive({
    browseHistoryVisibility: 'PUBLIC',
    likeActivityVisibility: 'PUBLIC',
    favoritesVisibility: 'PUBLIC',
    onlineStatusVisibility: 'PUBLIC',
    allowSearchEngineIndex: false,
  });

  /** 加载隐私设置 */
  async function loadSettings() {
    loading.value = true;
    try {
      const userId = String((userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '');
      const res = await getPrivacySetting(userId);
      if (res) {
        form.browseHistoryVisibility = res.browseHistoryVisibility || 'PUBLIC';
        form.likeActivityVisibility = res.likeActivityVisibility || 'PUBLIC';
        form.favoritesVisibility = res.favoriteVisibility || 'PUBLIC';
        form.onlineStatusVisibility = res.onlineStatusVisibility || 'PUBLIC';
        form.allowSearchEngineIndex = res.allowSearchEngineIndex ?? false;
      }
    } catch (e: any) {
      createMessage.error(e?.message || '加载隐私设置失败');
    } finally {
      loading.value = false;
    }
  }

  /** 保存隐私设置 */
  async function handleSave() {
    saving.value = true;
    try {
      const userId = String((userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '');
      await updatePrivacySetting(userId, {
        browseHistoryVisibility: form.browseHistoryVisibility,
        likeActivityVisibility: form.likeActivityVisibility,
        favoritesVisibility: form.favoritesVisibility,
        onlineStatusVisibility: form.onlineStatusVisibility,
        allowSearchEngineIndex: form.allowSearchEngineIndex,
      });
      createMessage.success('隐私设置已保存');
    } catch (e: any) {
      createMessage.error(e?.message || '保存失败');
    } finally {
      saving.value = false;
    }
  }

  onMounted(() => {
    loadSettings();
  });
</script>

<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-privacy-settings';

  .@{prefix-cls} {
    max-width: 640px;
    margin: 0 auto;
    padding: 16px;

    &__section {
      margin-bottom: 8px;
    }

    &__section-title {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 16px;
      color: rgba(0, 0, 0, 0.85);
    }

    &__item {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }
    }

    &__item-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    &__item-label {
      font-size: 14px;
      color: rgba(0, 0, 0, 0.65);
    }

    &__hint {
      margin-top: 8px;
    }

    &__radio-group {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    &__radio-hint {
      margin-left: 8px;
      font-size: 12px;
      color: rgba(0, 0, 0, 0.45);
    }

    &__switch-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    &__switch-info {
      display: flex;
      flex-direction: column;
    }

    &__switch-desc {
      font-size: 12px;
      color: rgba(0, 0, 0, 0.45);
      margin-top: 4px;
    }

    &__actions {
      text-align: right;
    }

    @media (max-width: 768px) {
      padding: 8px;

      &__item-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }

      &__item-select {
        width: 100% !important;
      }

      &__radio-group {
        gap: 16px;
      }

      .ant-card {
        width: 100%;
      }
    }
  }
</style>
