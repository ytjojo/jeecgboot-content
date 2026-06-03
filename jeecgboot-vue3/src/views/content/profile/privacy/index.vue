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
          <a-col v-for="f in baseFields" :key="f" :span="12">
            <a-form-item :label="fieldLabels[f]">
              <a-select v-model:value="form[f]" :options="options" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="扩展资料" :bordered="false" class="profile-privacy__card">
        <a-row :gutter="16">
          <a-col v-for="f in extensionFields" :key="f" :span="12">
            <a-form-item :label="fieldLabels[f]">
              <a-select v-model:value="form[f]" :options="options" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="主页" :bordered="false" class="profile-privacy__card">
        <a-row :gutter="16">
          <a-col v-for="f in homepageFields" :key="f" :span="12">
            <a-form-item :label="fieldLabels[f]">
              <a-select v-model:value="form[f]" :options="options" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="认证" :bordered="false" class="profile-privacy__card">
        <a-row :gutter="16">
          <a-col v-for="f in certificationFields" :key="f" :span="12">
            <a-form-item :label="fieldLabels[f]">
              <a-select v-model:value="form[f]" :options="options" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="活动" :bordered="false" class="profile-privacy__card">
        <a-row :gutter="16">
          <a-col v-for="f in activityFields" :key="f" :span="12">
            <a-form-item :label="fieldLabels[f]">
              <a-select v-model:value="form[f]" :options="options" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="在线状态" :bordered="false" class="profile-privacy__card">
        <a-form-item :label="fieldLabels.onlineStatusVisibility">
          <a-select v-model:value="form.onlineStatusVisibility" :options="onlineStatusOptions" />
        </a-form-item>
      </a-card>

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
import { reactive, ref, onMounted } from 'vue';
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

const form = reactive<ContentUserPrivacyUpdateReq>(defaultForm());
const initial = ref<string>(JSON.stringify(defaultForm()));
const loading = ref(false);
const loadError = ref(false);
const saving = ref(false);
const viewerUserId = ref<string>('');
const ownerUserId = ref<string>('');

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
</style>
