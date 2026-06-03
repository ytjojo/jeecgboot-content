<template>
  <div class="avatar-cropper">
    <a-avatar :size="size" :src="currentUrl" />
    <a-upload
      :show-upload-list="false"
      :before-upload="beforeUpload"
      :custom-request="customRequest"
      accept="image/*"
    >
      <a-button size="small" class="avatar-cropper__btn">
        <Icon icon="mdi:camera" /> 更换头像
      </a-button>
    </a-upload>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Icon } from '/@/components/Icon';
import { message } from 'ant-design-vue';
import { uploadApi } from '/@/api/sys/upload';
import type { UploadFileParams } from '/#/axios';

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    size?: number;
    maxBytes?: number;
  }>(),
  {
    size: 96,
    maxBytes: 5 * 1024 * 1024,
  }
);

const emit = defineEmits<{
  (e: 'update:modelValue', url: string): void;
  (e: 'change', url: string): void;
}>();

const currentUrl = ref(props.modelValue || '');

function beforeUpload(file: File): boolean {
  if (file.size > props.maxBytes) {
    message.error(`头像大小不能超过 ${Math.round(props.maxBytes / 1024 / 1024)}MB`);
    return false;
  }
  if (!file.type.startsWith('image/')) {
    message.error('请选择图片文件');
    return false;
  }
  return true;
}

async function customRequest(options: any) {
  const file = options.file as File;
  const params: UploadFileParams = {
    name: 'file',
    file,
    filename: file.name,
  };
  try {
    const result = await uploadApi(params, () => {});
    const url: string | undefined = result?.data?.url || (result as any)?.url;
    if (url) {
      currentUrl.value = url;
      emit('update:modelValue', url);
      emit('change', url);
      message.success('头像上传成功');
    } else {
      message.error('上传失败：未返回 URL');
    }
  } catch (e) {
    message.error('上传失败');
  }
}
</script>

<style scoped>
.avatar-cropper {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.avatar-cropper__btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>
