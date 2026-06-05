<!-- src/views/support/appeal/create.vue -->
<template>
  <div class="appeal-create-page">
    <a-card title="提交申诉">
      <a-breadcrumb>
        <a-breadcrumb-item>个人中心</a-breadcrumb-item>
        <a-breadcrumb-item>我的申诉</a-breadcrumb-item>
        <a-breadcrumb-item>提交申诉</a-breadcrumb-item>
      </a-breadcrumb>

      <a-form :model="formData" layout="vertical" class="appeal-form">
        <a-form-item label="申诉类型" required>
          <a-select v-model:value="formData.appealType" placeholder="请选择申诉类型">
            <a-select-option value="content_delete">内容删除</a-select-option>
            <a-select-option value="account_ban">账号封禁</a-select-option>
            <a-select-option value="points_deduct">积分扣除</a-select-option>
            <a-select-option value="badge_deduct">勋章扣除</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="关联记录">
          <a-input :value="relatedSummary" disabled />
        </a-form-item>

        <a-form-item label="申诉理由" required>
          <a-textarea
            v-model:value="formData.reason"
            placeholder="请详细描述您的申诉理由"
            :maxlength="1000"
            :rows="5"
            show-count
          />
        </a-form-item>

        <a-form-item label="附件上传">
          <a-upload
            v-model:file-list="fileList"
            :before-upload="beforeUpload"
            :custom-request="handleUpload"
            accept="image/*,video/*"
          >
            <a-button>
              <upload-outlined />
              上传附件
            </a-button>
          </a-upload>
        </a-form-item>

        <!-- 申诉次数提示 -->
        <div class="appeal-count-tip">
          <a-alert
            :message="`本次为第 ${appealCount}/3 次申诉`"
            :type="appealCount >= 3 ? 'warning' : 'info'"
            show-icon
          />
        </div>
      </a-form>

      <div class="form-actions">
        <a-space>
          <a-button @click="handleCancel">取消</a-button>
          <a-button
            type="primary"
            :loading="submitting"
            :disabled="!formData.reason"
            data-testid="submit-btn"
            @click="handleSubmit"
          >
            提交申诉
          </a-button>
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { UploadOutlined } from '@ant-design/icons-vue';
import { Modal, message } from 'ant-design-vue';
import { createAppeal } from '/@/api/support/appeal';
import { uploadFile } from '/@/api/sys/upload';

const router = useRouter();
const route = useRoute();

const formData = reactive({
  appealType: undefined as string | undefined,
  relatedId: (route.query.reportId || route.query.punishmentId || '') as string,
  reason: '',
  attachmentUrls: [] as string[],
});

const relatedSummary = ref('从处罚通知或举报详情自动带入');
const fileList = ref<any[]>([]);
const submitting = ref(false);
const appealCount = ref(1);

const beforeUpload = (file: File) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB');
    return false;
  }
  return true;
};

const handleUpload = async (options: any) => {
  try {
    const res = await uploadFile(options.file);
    formData.attachmentUrls.push(res.result.url);
    options.onSuccess(res);
  } catch (err) {
    options.onError(err);
  }
};

const handleSubmit = async () => {
  if (!formData.appealType) {
    message.warning('请选择申诉类型');
    return;
  }
  if (!formData.reason) {
    message.warning('请填写申诉理由');
    return;
  }

  if (appealCount.value >= 3) {
    const confirmed = await new Promise<boolean>((resolve) => {
      Modal.confirm({
        title: '确认提交',
        content: '这是最后一次申诉机会，确认提交？',
        onOk: () => resolve(true),
        onCancel: () => resolve(false),
      });
    });
    if (!confirmed) return;
  }

  submitting.value = true;
  try {
    await createAppeal(formData);
    message.success('申诉已提交');
    router.push('/user/appeals');
  } catch {
    message.error('提交失败');
  } finally {
    submitting.value = false;
  }
};

const handleCancel = () => {
  router.back();
};

onMounted(() => {
  // TODO: 从 API 获取当前申诉次数
});
</script>

<style scoped lang="less">
.appeal-create-page {
  padding: 16px;

  .appeal-form {
    max-width: 600px;
    margin-top: 16px;
  }

  .appeal-count-tip {
    margin-bottom: 16px;
  }

  .form-actions {
    margin-top: 24px;
  }
}
</style>
