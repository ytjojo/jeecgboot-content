<template>
  <div class="avatar-cropper">
    <a-avatar :size="size" :src="currentUrl" />
    <a-button size="small" class="avatar-cropper__btn" @click="openModal">
      <Icon icon="mdi:camera" /> 更换头像
    </a-button>

    <a-modal
      :open="modalVisible"
      title="裁剪头像（1:1）"
      :width="480"
      :mask-closable="false"
      :confirm-loading="uploading"
      :ok-text="uploading ? '上传中…' : '确认'"
      :cancel-text="'取消'"
      :destroy-on-close="true"
      @ok="handleConfirm"
      @cancel="handleCancel"
    >
      <div class="avatar-cropper__dialog">
        <p class="avatar-cropper__hint">支持 JPG/PNG/WebP，≤5MB；裁剪比例固定 1:1。</p>
        <div class="avatar-cropper__stage">
          <img
            v-if="previewSrc"
            ref="imgRef"
            :src="previewSrc"
            alt="待裁剪图片"
            class="avatar-cropper__img"
          />
          <a-empty v-else description="请先选择本地图片" />
        </div>
        <a-upload
          :show-upload-list="false"
          :before-upload="beforeUpload"
          :custom-request="onFileSelected"
          accept=".jpg,.jpeg,.png,.webp"
        >
          <a-button :disabled="uploading" class="avatar-cropper__picker">
            <Icon icon="mdi:image-plus" /> 选择图片
          </a-button>
        </a-upload>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount } from 'vue';
import { message } from 'ant-design-vue';
import { Icon } from '/@/components/Icon';
import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.css';
import { uploadApi } from '/@/api/sys/upload';

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

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
const modalVisible = ref(false);
const previewSrc = ref<string>('');
const imgRef = ref<HTMLImageElement | null>(null);
let cropper: Cropper | null = null;
const uploading = ref(false);

function openModal() {
  modalVisible.value = true;
}

function beforeUpload(file: File): boolean {
  if (!ALLOWED_TYPES.includes(file.type)) {
    message.error('仅支持 JPG、PNG、WebP 格式');
    return false;
  }
  if (file.size > props.maxBytes) {
    message.error(`图片大小不能超过 ${Math.round(props.maxBytes / 1024 / 1024)}MB`);
    return false;
  }
  return true;
}

async function onFileSelected(options: any) {
  const file: File | undefined = options?.file;
  if (!file) return;
  if (!beforeUpload(file)) return;
  destroyCropper();
  if (previewSrc.value && previewSrc.value.startsWith('blob:')) {
    URL.revokeObjectURL(previewSrc.value);
  }
  previewSrc.value = URL.createObjectURL(file);
  await nextTick();
  initCropper();
}

function initCropper() {
  if (!imgRef.value) return;
  cropper = new Cropper(imgRef.value, {
    aspectRatio: 1,
    autoCrop: true,
    viewMode: 1,
    dragMode: 'move',
    background: false,
    responsive: true,
    restore: false,
    guides: true,
    center: true,
    movable: true,
    zoomable: true,
    rotatable: false,
    scalable: false,
    minContainerWidth: 280,
    minContainerHeight: 280,
  });
}

function destroyCropper() {
  if (cropper) {
    cropper.destroy();
    cropper = null;
  }
}

watch(
  () => modalVisible.value,
  (v) => {
    if (!v) {
      destroyCropper();
      if (previewSrc.value && previewSrc.value.startsWith('blob:')) {
        URL.revokeObjectURL(previewSrc.value);
      }
      previewSrc.value = '';
    }
  }
);

onBeforeUnmount(() => {
  destroyCropper();
  if (previewSrc.value && previewSrc.value.startsWith('blob:')) {
    URL.revokeObjectURL(previewSrc.value);
  }
});

function handleCancel() {
  if (uploading.value) return;
  modalVisible.value = false;
}

async function handleConfirm() {
  if (!cropper) {
    message.warning('请先选择图片');
    return;
  }
  const canvas = cropper.getCroppedCanvas({
    imageSmoothingEnabled: true,
    imageSmoothingQuality: 'high',
  });
  if (!canvas) {
    message.error('裁剪失败');
    return;
  }
  const blob: Blob | null = await new Promise((resolve) => {
    canvas.toBlob((b) => resolve(b), 'image/png', 0.95);
  });
  if (!blob) {
    message.error('生成图片失败');
    return;
  }
  uploading.value = true;
  try {
    const file = new File([blob], `avatar-${Date.now()}.png`, { type: 'image/png' });
    const result = await uploadApi(
      {
        name: 'file',
        file,
        filename: file.name,
      },
      () => {}
    );
    const url: string | undefined = result?.url;
    if (url) {
      currentUrl.value = url;
      emit('update:modelValue', url);
      emit('change', url);
      modalVisible.value = false;
      message.success('头像上传成功');
    } else {
      message.error('上传失败：未返回 URL');
    }
  } catch (e: any) {
    message.error(e?.message || '上传失败');
  } finally {
    uploading.value = false;
  }
}

// Keep currentUrl in sync when parent updates modelValue
watch(
  () => props.modelValue,
  (v) => {
    if (v !== undefined && v !== currentUrl.value) {
      currentUrl.value = v;
    }
  }
);
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
.avatar-cropper__dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.avatar-cropper__hint {
  color: rgba(0, 0, 0, 0.55);
  font-size: 13px;
  margin: 0;
}
.avatar-cropper__stage {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 320px;
  width: 100%;
}
.avatar-cropper__img {
  display: block;
  max-width: 100%;
  max-height: 100%;
}
.avatar-cropper__picker {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
:deep(.cropper-view-box),
:deep(.cropper-face) {
  border-radius: 50%;
}
</style>
