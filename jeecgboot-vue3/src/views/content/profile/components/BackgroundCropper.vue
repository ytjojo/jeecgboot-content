<template>
  <a-modal
    :open="visible"
    title="裁剪主页背景（16:9）"
    :width="720"
    :mask-closable="false"
    :confirm-loading="uploading"
    :ok-text="uploading ? '上传中…' : '上传'"
    :cancel-text="'取消'"
    :destroy-on-close="true"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <div class="bg-cropper">
      <p class="bg-cropper__hint">支持 JPG/PNG/WebP，≤5MB；裁剪比例固定 16:9。</p>
      <div class="bg-cropper__stage" :style="stageStyle">
        <img
          v-if="previewSrc"
          ref="imgRef"
          :src="previewSrc"
          :alt="'待裁剪图片'"
          class="bg-cropper__img"
        />
        <a-empty v-else description="请先选择本地图片" />
      </div>
      <a-upload
        :show-upload-list="false"
        :before-upload="beforeUpload"
        :custom-request="onFileSelected"
        accept="image/*"
      >
        <a-button :disabled="uploading" class="bg-cropper__picker">
          <Icon icon="mdi:image-plus" /> 选择图片
        </a-button>
      </a-upload>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount, computed } from 'vue';
import { message } from 'ant-design-vue';
import { Icon } from '/@/components/Icon';
import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.css';
import { uploadApi } from '/@/api/sys/upload';
import { ASPECT_16_9 } from './backgroundCropper';

const props = withDefaults(
  defineProps<{
    visible: boolean;
    maxBytes?: number;
  }>(),
  {
    maxBytes: 5 * 1024 * 1024,
  }
);

const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void;
  (e: 'uploaded', url: string): void;
}>();

const previewSrc = ref<string>('');
const imgRef = ref<HTMLImageElement | null>(null);
let cropper: Cropper | null = null;
let lastBlob: Blob | null = null;
const uploading = ref(false);

const stageStyle = computed(() => ({
  height: '360px',
  width: '100%',
}));

function beforeUpload(file: File): boolean {
  if (file.size > props.maxBytes) {
    message.error(`图片大小不能超过 ${Math.round(props.maxBytes / 1024 / 1024)}MB`);
    return false;
  }
  if (!file.type.startsWith('image/')) {
    message.error('请选择图片文件');
    return false;
  }
  return true;
}

async function onFileSelected(options: any) {
  const file: File | undefined = options?.file;
  if (!file) return;
  if (!beforeUpload(file)) return;
  destroyCropper();
  // 释放旧 URL
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
    aspectRatio: ASPECT_16_9,
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
    minContainerWidth: 320,
    minContainerHeight: 180,
  });
}

function destroyCropper() {
  if (cropper) {
    cropper.destroy();
    cropper = null;
  }
}

watch(
  () => props.visible,
  (v) => {
    if (!v) {
      destroyCropper();
      if (previewSrc.value && previewSrc.value.startsWith('blob:')) {
        URL.revokeObjectURL(previewSrc.value);
      }
      previewSrc.value = '';
      lastBlob = null;
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
  emit('update:visible', false);
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
  lastBlob = blob;
  uploading.value = true;
  try {
    const file = new File([blob], `homepage-bg-${Date.now()}.png`, { type: 'image/png' });
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
      emit('uploaded', url);
      emit('update:visible', false);
      message.success('背景图上传成功');
    } else {
      message.error('上传失败：未返回 URL');
    }
  } catch (e: any) {
    message.error(e?.message || '上传失败');
  } finally {
    uploading.value = false;
  }
}
</script>

<style scoped>
.bg-cropper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.bg-cropper__hint {
  color: rgba(0, 0, 0, 0.55);
  font-size: 13px;
  margin: 0;
}
.bg-cropper__stage {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bg-cropper__img {
  display: block;
  max-width: 100%;
  max-height: 100%;
}
.bg-cropper__picker {
  align-self: flex-start;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
:deep(.cropper-view-box),
:deep(.cropper-face) {
  border-radius: 0;
}
</style>
