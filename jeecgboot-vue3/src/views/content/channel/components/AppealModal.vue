<template>
  <a-modal
    v-model:open="visible"
    title="提交申诉"
    :confirmLoading="loading"
    okText="提交申诉"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :maskClosable="false"
  >
    <div class="appeal-modal">
      <!-- 处罚信息 -->
      <a-descriptions :column="1" size="small" bordered>
        <a-descriptions-item label="处罚类型">{{ penaltyType || '-' }}</a-descriptions-item>
        <a-descriptions-item label="处罚说明">{{ penaltyInfo || '-' }}</a-descriptions-item>
      </a-descriptions>

      <!-- 申诉说明 -->
      <a-form-item label="申诉说明" required class="appeal-modal__explain">
        <a-textarea
          v-model:value="explain"
          placeholder="请详细说明申诉理由"
          :rows="4"
          :maxlength="500"
          showCount
        />
      </a-form-item>

      <!-- 补充材料上传 -->
      <a-form-item label="补充材料">
        <a-upload
          v-model:file-list="fileList"
          :maxCount="5"
          :beforeUpload="() => false"
          listType="picture"
        >
          <a-button>
            <UploadOutlined />
            上传材料
          </a-button>
        </a-upload>
      </a-form-item>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue';
import { UploadOutlined } from '@ant-design/icons-vue';
import type { UploadProps } from 'ant-design-vue';

const props = withDefaults(defineProps<{
  visible: boolean;
  penaltyType?: string;
  penaltyInfo?: string;
  loading?: boolean;
}>(), {
  penaltyType: '',
  penaltyInfo: '',
  loading: false,
});

const emit = defineEmits<{
  'update:visible': [value: boolean];
  submit: [data: { explain: string; materials: string[] }];
  cancel: [];
}>();

const visible = ref(props.visible);
const explain = ref('');
const fileList = ref<UploadProps['fileList']>([]);

watch(() => props.visible, (val) => {
  visible.value = val;
  if (val) {
    explain.value = '';
    fileList.value = [];
  }
});

watch(visible, (val) => {
  emit('update:visible', val);
});

function handleSubmit() {
  const materials = fileList.value
    ?.filter((f: any) => f.status !== 'error')
    .map((f: any) => f.url || f.name) || [];
  emit('submit', {
    explain: explain.value,
    materials,
  });
}

function handleCancel() {
  emit('cancel');
}
</script>

<style lang="less" scoped>
.appeal-modal {
  &__explain {
    margin-top: 16px;
  }
}
</style>
