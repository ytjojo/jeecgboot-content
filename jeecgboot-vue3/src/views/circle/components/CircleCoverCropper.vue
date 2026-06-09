<template>
  <div class="circle-cover-cropper">
    <div class="cover-upload-area" @click="openModal">
      <img v-if="currentUrl" :src="currentUrl" alt="封面图" class="cover-preview-img" />
      <div v-else class="cover-upload-placeholder">
        <PlusOutlined />
        <span>上传封面图</span>
      </div>
    </div>
    <div class="upload-desc">建议尺寸 750x422，JPG/PNG/WebP，≤5MB</div>

    <a-modal
      :open="modalVisible"
      title="裁剪封面图（16:9）"
      :width="700"
      :mask-closable="false"
      :confirm-loading="uploading"
      :ok-text="uploading ? '上传中…' : '确认'"
      :cancel-text="'取消'"
      :destroy-on-close="true"
      @ok="handleConfirm"
      @cancel="handleCancel"
    >
      <div class="cropper-dialog">
        <p class="cropper-hint">支持 JPG/PNG/WebP，≤5MB；裁剪比例固定 16:9。</p>
        <div class="cropper-stage">
          <img
            v-if="previewSrc"
            ref="imgRef"
            :src="previewSrc"
            alt="待裁剪图片"
            class="cropper-img"
          />
          <a-empty v-else description="请先选择本地图片" />
        </div>
        <a-upload
          :show-upload-list="false"
          :before-upload="beforeUpload"
          :custom-request="onFileSelected"
          accept=".jpg,.jpeg,.png,.webp"
        >
          <a-button :disabled="uploading">
            <FileImageOutlined /> 选择图片
          </a-button>
        </a-upload>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, FileImageOutlined } from '@ant-design/icons-vue';
import Cropper from 'cropperjs';
import 'cropperjs/dist/cropper.css';
import { uploadApi } from '/@/api/sys/upload';

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_BYTES = 5 * 1024 * 1024;
const ASPECT_16_9 = 16 / 9;

const props = withDefaults(
  defineProps<{
    modelValue?: string;
  }>(),
  {}
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
  if (file.size > MAX_BYTES) {
    message.error('封面图大小不能超过5MB');
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
    const file = new File([blob], `circle-cover-${Date.now()}.png`, { type: 'image/png' });
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
      message.success('封面图上传成功');
    } else {
      message.error('上传失败：未返回 URL');
    }
  } catch (e: any) {
    message.error(e?.message || '上传失败');
  } finally {
    uploading.value = false;
  }
}

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
.circle-cover-cropper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cover-upload-area {
  width: 100%;
  max-width: 320px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
  background: #fafafa;
}

.cover-upload-area:hover {
  border-color: #1890ff;
}

.cover-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 13px;
}

.cover-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-desc {
  font-size: 12px;
  color: #999;
}

.cropper-dialog {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cropper-hint {
  color: rgba(0, 0, 0, 0.55);
  font-size: 13px;
  margin: 0;
}

.cropper-stage {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 360px;
  width: 100%;
}

.cropper-img {
  display: block;
  max-width: 100%;
  max-height: 100%;
}
</style>
