<template>
  <div class="profile-edit">
    <a-page-header title="编辑资料" :back-icon="true" @back="onBack" />

    <a-alert
      v-if="reviewStatus === 'REJECTED' && reviewReason"
      type="error"
      :message="`资料被拒：${reviewReason}`"
      show-icon
      class="profile-edit__alert"
    />
    <a-alert
      v-else-if="reviewStatus === 'PENDING'"
      type="warning"
      message="资料审核中，通过后展示"
      show-icon
      class="profile-edit__alert"
    />

    <a-form v-if="!loading" layout="vertical" :model="form" :disabled="isFormDisabled(reviewStatus)">
      <a-card title="头像" :bordered="false" class="profile-edit__card">
        <AvatarCropper v-model="form.avatar" :size="96" />
      </a-card>

      <a-card title="基础资料" :bordered="false" class="profile-edit__card">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="昵称" required>
              <a-input v-model:value="form.nickname" :maxlength="30" show-count />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="性别">
              <a-select
                v-model:value="form.gender"
                :options="[
                  { label: '男', value: 'MALE' },
                  { label: '女', value: 'FEMALE' },
                  { label: '其他', value: 'OTHER' },
                  { label: '不显示', value: 'UNKNOWN' },
                ]"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="生日">
              <a-date-picker v-model:value="form.birthday" value-format="YYYY-MM-DD" style="width:100%" :disabled-date="(d: any) => d && d.isAfter(dayjs())" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="地区">
              <a-input v-model:value="form.region" :maxlength="64" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="职业">
              <a-input v-model:value="form.profession" :maxlength="64" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="个人链接">
              <a-input v-model:value="form.personalLink" :maxlength="256" placeholder="https://" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="个人简介">
              <a-textarea v-model:value="form.bio" :maxlength="500" show-count :rows="4" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="认证信息" :bordered="false" class="profile-edit__card">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="认证类型">
              <a-input v-model:value="form.certificationType" :maxlength="32" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="认证标签">
              <a-input v-model:value="form.certificationLabel" :maxlength="64" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="认证说明">
              <a-input v-model:value="form.certificationDescription" :maxlength="512" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-space class="profile-edit__actions">
        <a-button type="primary" :loading="saving" :disabled="isSaveDisabled(reviewStatus)" @click="onSave">保存</a-button>
        <a-button @click="onReset">重置</a-button>
      </a-space>
    </a-form>
    <a-skeleton v-else active />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import dayjs from 'dayjs';
import AvatarCropper from '/@/views/content/profile/components/AvatarCropper.vue';
import { getProfileDetail, updateProfile } from '/@/api/content/profile';
import type { ContentUserProfileUpdateReq, ContentUserProfileVO } from '/@/api/content/profile/types';
import { validateProfileForm } from '/@/views/content/profile/validators/profileForm';
import { isFormDisabled, isSaveDisabled } from './pendingLock';

function defaultForm(): ContentUserProfileUpdateReq {
  return {
    nickname: '',
    avatar: '',
    bio: undefined,
    gender: undefined,
    birthday: undefined,
    region: undefined,
    profession: undefined,
    personalLink: undefined,
    certificationType: undefined,
    certificationLabel: undefined,
    certificationDescription: undefined,
  };
}

const form = reactive<ContentUserProfileUpdateReq>(defaultForm());
const initial = ref<string>('');
const loading = ref(false);
const saving = ref(false);
const userId = ref<string>('');
const reviewStatus = ref<string>('');
const reviewReason = ref<string>('');

onMounted(async () => {
  loading.value = true;
  try {
    const { useUserStore } = await import('/@/store/modules/user');
    const userStore = useUserStore();
    const uid = (userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '';
    if (!uid) {
      message.error('未识别当前用户');
      return;
    }
    userId.value = uid;
    const detail: ContentUserProfileVO = await getProfileDetail(uid, uid);
    Object.assign(form, {
      nickname: detail.nickname || '',
      avatar: detail.avatar || '',
      bio: detail.bio,
      gender: detail.gender,
      birthday: detail.birthday,
      region: detail.region,
      profession: detail.profession,
      personalLink: detail.personalLink,
      certificationType: detail.certificationType,
      certificationLabel: detail.certificationLabel,
      certificationDescription: detail.certificationDescription,
    });
    reviewStatus.value = detail.profileReviewStatus || '';
    reviewReason.value = detail.profileReviewReason || '';
    initial.value = JSON.stringify(form);
  } finally {
    loading.value = false;
  }
});

function validate(): string | null {
  return validateProfileForm(form);
}

async function onSave() {
  if (!userId.value) return;
  const err = validate();
  if (err) {
    message.error(err);
    return;
  }
  saving.value = true;
  try {
    await updateProfile(userId.value, form);
    const { useUserStore } = await import('/@/store/modules/user');
    await useUserStore().refreshProfileSnapshot(userId.value);
    initial.value = JSON.stringify(form);
    reviewStatus.value = 'PENDING';
    message.success('已保存，等待审核');
  } catch (e: any) {
    message.error(e?.message || '保存失败');
  } finally {
    saving.value = false;
  }
}

function onReset() {
  if (initial.value) {
    Object.assign(form, JSON.parse(initial.value));
  }
}

function isDirty(): boolean {
  return initial.value !== JSON.stringify(form);
}

function onBack() {
  if (isDirty()) {
    Modal.confirm({
      title: '确认离开？',
      content: '您有未保存的修改，确定离开吗？',
      okText: '确定离开',
      cancelText: '继续编辑',
      onOk: () => window.history.back(),
    });
  } else {
    window.history.back();
  }
}
</script>

<style scoped>
.profile-edit__card {
  margin-bottom: 16px;
}
.profile-edit__actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.profile-edit__alert {
  margin-bottom: 16px;
}
@media (max-width: 768px) {
  .profile-edit :deep(.ant-col) {
    max-width: 100%;
    flex: 0 0 100%;
  }
}
@media (min-width: 769px) and (max-width: 1024px) {
  .profile-edit :deep(.ant-col-lg-12),
  .profile-edit :deep(.ant-col-md-12) {
    max-width: 50%;
    flex: 0 0 50%;
  }
}
</style>
