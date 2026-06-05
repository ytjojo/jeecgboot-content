<template>
  <a-modal
    :open="visible"
    title="举报"
    :width="520"
    :confirm-loading="submitting"
    :mask-closable="false"
    @cancel="handleClose"
    @ok="handleSubmit"
  >
    <div class="report-modal">
      <!-- 被举报对象摘要 -->
      <div class="target-summary">
        <span class="label">举报对象：</span>
        <span class="value">{{ targetSummary }}</span>
      </div>

      <!-- 举报类型 -->
      <a-form :model="formData" layout="vertical">
        <a-form-item label="举报类型" name="reportType" required>
          <a-radio-group v-model:value="formData.reportType" @change="handleTypeChange">
            <a-radio v-for="item in reportTypes" :key="item.value" :value="item.value" class="report-type-radio">
              <div>
                <div>{{ item.label }}</div>
                <div class="type-desc">{{ item.description }}</div>
              </div>
            </a-radio>
          </a-radio-group>
        </a-form-item>

        <!-- 举报说明 -->
        <a-form-item label="举报说明">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请补充说明（选填）"
            :maxlength="500"
            :rows="3"
            show-count
          />
        </a-form-item>

        <!-- 证据上传 -->
        <a-form-item label="证据上传">
          <a-upload
            v-model:file-list="fileList"
            :before-upload="beforeUpload"
            :custom-request="handleUpload"
            accept="image/*,video/*"
            list-type="text"
          >
            <a-button>
              <upload-outlined />
              上传文件
            </a-button>
            <template #tip>
              <div class="upload-tip">支持图片/视频，单文件最大 10MB，最多 5 个文件</div>
            </template>
          </a-upload>
        </a-form-item>
      </a-form>
    </div>

    <template #footer>
      <a-button @click="handleClose">取消</a-button>
      <a-button
        type="primary"
        :loading="submitting"
        :disabled="uploading"
        data-testid="submit-btn"
        @click="handleSubmit"
      >
        提交举报
      </a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { UploadOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { createReport, type ReportCreateParams } from '/@/api/support/report';
import { uploadFile } from '/@/api/sys/upload';

const props = defineProps<{
  visible: boolean;
  targetType: string;
  targetId: string;
  targetSummary: string;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'success', reportNo: string): void;
}>();

const reportTypes = [
  { value: 'porn', label: '色情内容', description: '包含色情、裸露等不当内容' },
  { value: 'violence', label: '暴力内容', description: '包含血腥、恐怖等暴力内容' },
  { value: 'fraud', label: '诈骗信息', description: '涉及欺诈、钓鱼等行为' },
  { value: 'harassment', label: '骚扰行为', description: '包含辱骂、威胁、骚扰等' },
  { value: 'other', label: '其他', description: '其他违规行为' },
];

const formData = reactive<ReportCreateParams>({
  targetType: props.targetType,
  targetId: props.targetId,
  reportType: '',
  description: '',
  evidenceUrls: [],
});

const fileList = ref<any[]>([]);
const submitting = ref(false);
const uploading = ref(false);

watch(
  () => props.visible,
  (val) => {
    if (val) {
      formData.targetType = props.targetType;
      formData.targetId = props.targetId;
      formData.reportType = '';
      formData.description = '';
      formData.evidenceUrls = [];
      fileList.value = [];
    }
  },
);

const handleTypeChange = () => {
  // 类型变更时的处理
};

const beforeUpload = (file: File) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB');
    return false;
  }
  if (fileList.value.length >= 5) {
    message.error('最多上传 5 个文件');
    return false;
  }
  return true;
};

const handleUpload = async (options: any) => {
  uploading.value = true;
  try {
    const res = await uploadFile(options.file);
    formData.evidenceUrls.push(res.result.url);
    options.onSuccess(res);
  } catch (err) {
    options.onError(err);
  } finally {
    uploading.value = false;
  }
};

const handleSubmit = async () => {
  if (!formData.reportType) {
    message.warning('请选择举报类型');
    return;
  }
  submitting.value = true;
  try {
    const res = await createReport(formData);
    message.success('举报已提交，我们将尽快处理');
    emit('success', res.result.reportNo);
    handleClose();
  } catch (err: any) {
    if (err?.code === 'DUPLICATE_REPORT') {
      message.warning('您已举报过该内容，请勿重复举报');
    } else {
      message.error('提交失败，请重试');
    }
  } finally {
    submitting.value = false;
  }
};

const handleClose = () => {
  emit('close');
};
</script>

<style scoped lang="less">
.report-modal {
  .target-summary {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 6px;
    margin-bottom: 16px;

    .label {
      color: #666;
    }
    .value {
      color: #333;
      font-weight: 500;
    }
  }

  .report-type-radio {
    display: block;
    margin-bottom: 8px;

    .type-desc {
      font-size: 12px;
      color: #999;
      margin-top: 2px;
    }
  }

  .upload-tip {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }
}
</style>
