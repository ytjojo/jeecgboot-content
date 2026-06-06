<template>
  <a-modal
    v-model:open="visible"
    title="创建系统频道"
    :confirm-loading="submitting"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
      <a-form-item label="频道名称" name="name">
        <a-input v-model:value="form.name" :maxlength="50" show-count placeholder="请输入频道名称" />
      </a-form-item>
      <a-form-item label="频道简介" name="description">
        <a-textarea v-model:value="form.description" :maxlength="200" show-count placeholder="请输入频道简介" />
      </a-form-item>
      <a-form-item label="频道图标" name="iconUrl">
        <a-upload
          :before-upload="beforeIconUpload"
          :show-upload-list="false"
          @change="handleIconChange"
        >
          <div class="upload-area">
            <img v-if="form.iconUrl" :src="form.iconUrl" class="upload-preview" />
            <div v-else class="upload-placeholder">
              <PlusOutlined />
              <div>上传图标</div>
            </div>
          </div>
        </a-upload>
      </a-form-item>
      <a-form-item label="频道封面" name="coverUrl">
        <a-upload
          :before-upload="beforeCoverUpload"
          :show-upload-list="false"
          @change="handleCoverChange"
        >
          <div class="upload-area upload-area--cover">
            <img v-if="form.coverUrl" :src="form.coverUrl" class="upload-preview" />
            <div v-else class="upload-placeholder">
              <PlusOutlined />
              <div>上传封面</div>
            </div>
          </div>
        </a-upload>
      </a-form-item>
      <a-form-item label="分类" name="categoryName">
        <a-input v-model:value="form.categoryName" placeholder="请输入分类名称" />
      </a-form-item>
      <a-form-item label="置顶权重" name="topWeight">
        <a-input-number v-model:value="form.topWeight" :min="0" :max="100" style="width: 100%" placeholder="0-100" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue';
  import { message } from 'ant-design-vue';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import { createSystemChannel } from '/@/api/content/channel';
  import type { SystemChannelCreateReq } from '/@/api/content/channel/model/channelModel';
  import type { FormInstance, UploadChangeParam } from 'ant-design-vue';

  const emit = defineEmits<{ created: [] }>();

  const visible = ref(false);
  const submitting = ref(false);
  const formRef = ref<FormInstance>();

  const form = reactive<SystemChannelCreateReq>({
    name: '',
    description: '',
    iconUrl: '',
    coverUrl: '',
    categoryName: '',
    topWeight: 0,
  });

  const rules = {
    name: [
      { required: true, message: '请输入频道名称', trigger: 'blur' },
      { max: 50, message: '名称不超过 50 个字符', trigger: 'blur' },
    ],
    description: [
      { required: true, message: '请输入频道简介', trigger: 'blur' },
      { max: 200, message: '简介不超过 200 个字符', trigger: 'blur' },
    ],
    iconUrl: [{ required: true, message: '请上传频道图标', trigger: 'change' }],
    categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  };

  function open() {
    visible.value = true;
    form.name = '';
    form.description = '';
    form.iconUrl = '';
    form.coverUrl = '';
    form.categoryName = '';
    form.topWeight = 0;
  }

  function beforeIconUpload(file: File) {
    const isImage = file.type.startsWith('image/');
    if (!isImage) {
      message.error('只能上传图片文件');
    }
    const isLt2M = file.size / 1024 / 1024 <= 2;
    if (!isLt2M) {
      message.error('图标不能超过 2MB');
    }
    return isImage && isLt2M;
  }

  function beforeCoverUpload(file: File) {
    const isImage = file.type.startsWith('image/');
    if (!isImage) {
      message.error('只能上传图片文件');
    }
    const isLt5M = file.size / 1024 / 1024 <= 5;
    if (!isLt5M) {
      message.error('封面不能超过 5MB');
    }
    return isImage && isLt5M;
  }

  function handleIconChange(info: UploadChangeParam) {
    if (info.file.status === 'done') {
      form.iconUrl = info.file.response?.url || URL.createObjectURL(info.file.originFileObj!);
    }
  }

  function handleCoverChange(info: UploadChangeParam) {
    if (info.file.status === 'done') {
      form.coverUrl = info.file.response?.url || URL.createObjectURL(info.file.originFileObj!);
    }
  }

  async function handleOk() {
    try {
      await formRef.value?.validateFields();
    } catch {
      return;
    }

    submitting.value = true;
    try {
      await createSystemChannel(form);
      message.success('系统频道创建成功');
      visible.value = false;
      emit('created');
    } catch (e: any) {
      message.error(e?.message || '创建失败');
    } finally {
      submitting.value = false;
    }
  }

  function handleCancel() {
    visible.value = false;
  }

  defineExpose({ open });
</script>

<style scoped lang="less">
  .upload-area {
    width: 80px;
    height: 80px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    overflow: hidden;

    &--cover {
      width: 160px;
      height: 90px;
    }

    &:hover {
      border-color: #1890ff;
    }
  }

  .upload-preview {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .upload-placeholder {
    text-align: center;
    color: #8c8c8c;

    .anticon {
      font-size: 24px;
    }
  }
</style>
