<template>
  <div class="profile-privacy">
    <a-page-header title="隐私设置" :back-icon="true" @back="$router.back()" />
    <a-alert
      v-if="loading"
      type="info"
      message="正在加载..."
      show-icon
    />
    <a-alert
      v-else-if="loadError"
      type="error"
      message="加载失败，请稍后重试"
      show-icon
      class="profile-privacy__alert"
    />
    <a-form v-else layout="vertical" :model="form">
      <a-card title="基础资料" :bordered="false" class="profile-privacy__card">
        <a-row :gutter="16">
          <a-col v-for="f in baseFields" :key="f" :span="isMobile ? 24 : 12">
            <a-form-item :label="fieldLabels[f]">
              <a-select v-if="!isMobile" v-model:value="form[f]" :options="options" />
              <a-button v-else block @click="openActionSheet(f, options)">
                {{ getOptionLabel(options, form[f]) }}
              </a-button>
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="扩展资料" :bordered="false" class="profile-privacy__card">
        <a-row :gutter="16">
          <a-col v-for="f in extensionFields" :key="f" :span="isMobile ? 24 : 12">
            <a-form-item :label="fieldLabels[f]">
              <a-select v-if="!isMobile" v-model:value="form[f]" :options="options" />
              <a-button v-else block @click="openActionSheet(f, options)">
                {{ getOptionLabel(options, form[f]) }}
              </a-button>
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="主页" :bordered="false" class="profile-privacy__card">
        <a-row :gutter="16">
          <a-col v-for="f in homepageFields" :key="f" :span="isMobile ? 24 : 12">
            <a-form-item :label="fieldLabels[f]">
              <a-select v-if="!isMobile" v-model:value="form[f]" :options="options" />
              <a-button v-else block @click="openActionSheet(f, options)">
                {{ getOptionLabel(options, form[f]) }}
              </a-button>
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="认证" :bordered="false" class="profile-privacy__card">
        <a-row :gutter="16">
          <a-col v-for="f in certificationFields" :key="f" :span="isMobile ? 24 : 12">
            <a-form-item :label="fieldLabels[f]">
              <a-select v-if="!isMobile" v-model:value="form[f]" :options="options" />
              <a-button v-else block @click="openActionSheet(f, options)">
                {{ getOptionLabel(options, form[f]) }}
              </a-button>
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="活动" :bordered="false" class="profile-privacy__card">
        <a-row :gutter="16">
          <a-col v-for="f in activityFields" :key="f" :span="isMobile ? 24 : 12">
            <a-form-item :label="fieldLabels[f]">
              <a-select v-if="!isMobile" v-model:value="form[f]" :options="options" />
              <a-button v-else block @click="openActionSheet(f, options)">
                {{ getOptionLabel(options, form[f]) }}
              </a-button>
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="在线状态" :bordered="false" class="profile-privacy__card">
        <a-form-item :label="fieldLabels.onlineStatusVisibility">
          <a-select v-if="!isMobile" v-model:value="form.onlineStatusVisibility" :options="onlineStatusOptions" />
          <a-button v-else block @click="openActionSheet('onlineStatusVisibility', onlineStatusOptions)">
            {{ getOptionLabel(onlineStatusOptions, form.onlineStatusVisibility) }}
          </a-button>
        </a-form-item>
      </a-card>

      <!-- Mobile ActionSheet: bottom drawer -->
      <a-drawer
        v-model:open="actionSheetVisible"
        placement="bottom"
        :height="actionSheetHeight"
        :closable="false"
        class="profile-privacy__action-sheet"
      >
        <div class="profile-privacy__action-sheet-header">
          <span>{{ actionSheetLabel }}</span>
          <a-button type="link" @click="actionSheetVisible = false">取消</a-button>
        </div>
        <div class="profile-privacy__action-sheet-list">
          <div
            v-for="item in actionSheetItems"
            :key="item.key"
            class="profile-privacy__action-sheet-item"
            :class="{ 'profile-privacy__action-sheet-item--active': actionSheetCurrentValue === item.value }"
            @click="onActionSheetSelect(item.value)"
          >
            {{ item.label }}
            <span v-if="actionSheetCurrentValue === item.value" class="profile-privacy__action-sheet-check">&#10003;</span>
          </div>
        </div>
      </a-drawer>

      <a-card title="展示偏好" :bordered="false" class="profile-privacy__card">
        <a-form-item label="显示互关数">
          <a-switch v-model:checked="form.showMutualFollowersCount" />
        </a-form-item>
        <a-form-item label="高亮显示近期活动">
          <a-switch v-model:checked="form.showRecentActivityHighlight" />
        </a-form-item>
      </a-card>

      <a-space class="profile-privacy__actions">
        <a-button type="primary" :loading="saving" @click="onSave">保存</a-button>
        <a-button @click="onReset">重置</a-button>
      </a-space>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  PRIVACY_VISIBILITY_OPTIONS,
  ONLINE_STATUS_VISIBILITY_OPTIONS,
  PrivacyVisibility,
  OnlineStatusVisibility,
} from '/@/enums/profileEnum';
import {
  getProfileDetail,
  updatePrivacy,
} from '/@/api/content/profile';
import type { ContentUserPrivacyUpdateReq, ContentUserProfileVO } from '/@/api/content/profile/types';
import { useResponsiveSelect, mapOptionsToActionItems } from './useResponsiveSelect';
import type { ActionSheetItem } from './useResponsiveSelect';

const options = PRIVACY_VISIBILITY_OPTIONS;
const onlineStatusOptions = ONLINE_STATUS_VISIBILITY_OPTIONS;

const baseFields = [
  'bioVisibility',
  'genderVisibility',
  'birthdayVisibility',
  'regionVisibility',
  'professionVisibility',
] as const;

const extensionFields = ['personalLinkVisibility'] as const;

const homepageFields = [
  'homepageBackgroundVisibility',
  'themeColorVisibility',
  'homepageModuleVisibility',
] as const;

const certificationFields = [
  'certificationVisibility',
  'verificationBadgesVisibility',
] as const;

const activityFields = [
  'profileCompletionVisibility',
  'profileReviewStatusVisibility',
  'recentActivityVisibility',
] as const;

const fieldLabels: Record<string, string> = {
  bioVisibility: '个人简介',
  genderVisibility: '性别',
  birthdayVisibility: '生日',
  regionVisibility: '地区',
  professionVisibility: '职业',
  personalLinkVisibility: '个人链接',
  homepageBackgroundVisibility: '主页背景',
  themeColorVisibility: '主题色',
  homepageModuleVisibility: '主页模块',
  certificationVisibility: '认证信息',
  verificationBadgesVisibility: '认证徽章',
  profileCompletionVisibility: '资料完善度',
  profileReviewStatusVisibility: '审核状态',
  recentActivityVisibility: '近期活动',
  onlineStatusVisibility: '在线状态',
};

function defaultForm(): ContentUserPrivacyUpdateReq {
  return {
    bioVisibility: PrivacyVisibility.PUBLIC,
    genderVisibility: PrivacyVisibility.PUBLIC,
    birthdayVisibility: PrivacyVisibility.FOLLOWERS_ONLY,
    regionVisibility: PrivacyVisibility.PUBLIC,
    professionVisibility: PrivacyVisibility.PUBLIC,
    personalLinkVisibility: PrivacyVisibility.PUBLIC,
    homepageBackgroundVisibility: PrivacyVisibility.PUBLIC,
    themeColorVisibility: PrivacyVisibility.PUBLIC,
    homepageModuleVisibility: PrivacyVisibility.PUBLIC,
    certificationVisibility: PrivacyVisibility.PUBLIC,
    verificationBadgesVisibility: PrivacyVisibility.PUBLIC,
    profileCompletionVisibility: PrivacyVisibility.PRIVATE,
    profileReviewStatusVisibility: PrivacyVisibility.PRIVATE,
    recentActivityVisibility: PrivacyVisibility.FOLLOWERS_ONLY,
    onlineStatusVisibility: OnlineStatusVisibility.PUBLIC,
    showMutualFollowersCount: true,
    showRecentActivityHighlight: true,
  };
}

const { isMobile } = useResponsiveSelect();

const form = reactive<ContentUserPrivacyUpdateReq>(defaultForm());
const initial = ref<string>(JSON.stringify(defaultForm()));
const loading = ref(false);
const loadError = ref(false);
const saving = ref(false);
const viewerUserId = ref<string>('');
const ownerUserId = ref<string>('');

// ActionSheet state (mobile only)
const actionSheetVisible = ref(false);
const actionSheetField = ref<string>('');
const actionSheetLabel = ref<string>('');
const actionSheetItems = ref<ActionSheetItem[]>([]);
const actionSheetCurrentValue = computed(() => (form as any)[actionSheetField.value] ?? '');
const actionSheetHeight = computed(() => Math.min(actionSheetItems.value.length * 48 + 56, 400));

function getOptionLabel(opts: { value: string; label: string }[], value: string): string {
  return opts.find((o) => o.value === value)?.label ?? value;
}

function openActionSheet(field: string, opts: { value: string; label: string }[]) {
  actionSheetField.value = field;
  actionSheetLabel.value = fieldLabels[field] ?? field;
  actionSheetItems.value = mapOptionsToActionItems(opts);
  actionSheetVisible.value = true;
}

function onActionSheetSelect(value: string) {
  (form as any)[actionSheetField.value] = value;
  actionSheetVisible.value = false;
}

onMounted(async () => {
  loading.value = true;
  loadError.value = false;
  try {
    const { useUserStore } = await import('/@/store/modules/user');
    const userStore = useUserStore();
    const uid = (userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '';
    if (!uid) {
      loadError.value = true;
      loading.value = false;
      return;
    }
    viewerUserId.value = uid;
    ownerUserId.value = uid;
    const detail: ContentUserProfileVO = await getProfileDetail(uid, uid);
    hydrateFromVo(detail);
    initial.value = JSON.stringify(form);
  } catch {
    loadError.value = true;
  } finally {
    loading.value = false;
  }
});

function hydrateFromVo(detail: ContentUserProfileVO) {
  const keys: (keyof ContentUserPrivacyUpdateReq)[] = [
    'bioVisibility',
    'genderVisibility',
    'birthdayVisibility',
    'regionVisibility',
    'professionVisibility',
    'personalLinkVisibility',
    'homepageBackgroundVisibility',
    'themeColorVisibility',
    'homepageModuleVisibility',
    'certificationVisibility',
    'verificationBadgesVisibility',
    'profileCompletionVisibility',
    'profileReviewStatusVisibility',
    'recentActivityVisibility',
    'onlineStatusVisibility',
    'showMutualFollowersCount',
    'showRecentActivityHighlight',
  ];
  for (const k of keys) {
    const v = (detail as any)[k];
    if (v !== undefined && v !== null) {
      (form as any)[k] = v;
    }
  }
  initial.value = JSON.stringify(form);
}

function isDirty(): boolean {
  return JSON.stringify(form) !== initial.value;
}

async function onSave() {
  if (!ownerUserId.value) {
    message.error('未识别当前用户');
    return;
  }
  if (!isDirty()) {
    message.info('未修改');
    return;
  }
  saving.value = true;
  try {
    await updatePrivacy(ownerUserId.value, form);
    const { useUserStore } = await import('/@/store/modules/user');
    await useUserStore().refreshProfileSnapshot(ownerUserId.value);
    initial.value = JSON.stringify(form);
    message.success('已保存');
  } catch (e: any) {
    message.error(e?.message || '保存失败');
  } finally {
    saving.value = false;
  }
}

function onReset() {
  const reset = defaultForm();
  Object.assign(form, reset);
}
</script>

<style scoped>
.profile-privacy__card {
  margin-bottom: 16px;
}
.profile-privacy__actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.profile-privacy__alert {
  margin-bottom: 16px;
}
@media (max-width: 768px) {
  .profile-privacy :deep(.ant-col) {
    max-width: 100%;
    flex: 0 0 100%;
  }
}

.profile-privacy__action-sheet :deep(.ant-drawer-body) {
  padding: 0;
}

.profile-privacy__action-sheet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 500;
}

.profile-privacy__action-sheet-list {
  max-height: 344px;
  overflow-y: auto;
}

.profile-privacy__action-sheet-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.profile-privacy__action-sheet-item:active {
  background-color: #f5f5f5;
}

.profile-privacy__action-sheet-item--active {
  color: #1677ff;
  font-weight: 500;
}

.profile-privacy__action-sheet-check {
  color: #1677ff;
}
</style>
