<template>
  <a-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    :label-col="{ span: 6 }"
    :wrapper-col="{ span: 16 }"
  >
    <a-form-item label="频道名称" name="name">
      <a-input v-model:value="formData.name" placeholder="请输入频道名称（1-50个字符）" :maxlength="50" show-count />
    </a-form-item>

    <a-form-item label="频道描述" name="description">
      <a-textarea
        v-model:value="formData.description"
        placeholder="请输入频道描述（1-200个字符）"
        :maxlength="200"
        :rows="4"
        show-count
      />
    </a-form-item>

    <a-form-item label="频道图标" name="iconUrl">
      <a-upload
        list-type="picture-card"
        :max-count="1"
        :before-upload="beforeIconUpload"
        :custom-request="handleIconUpload"
        @remove="() => (formData.iconUrl = '')"
      >
        <div v-if="!formData.iconUrl">
          <plus-outlined />
          <div style="margin-top: 8px">上传图标</div>
        </div>
      </a-upload>
      <div class="form-item-tip">支持 jpg/png，不超过 2MB</div>
    </a-form-item>

    <a-form-item label="封面图" name="coverUrl">
      <a-upload
        list-type="picture-card"
        :max-count="1"
        :before-upload="beforeCoverUpload"
        :custom-request="handleCoverUpload"
        @remove="() => (formData.coverUrl = '')"
      >
        <div v-if="!formData.coverUrl">
          <plus-outlined />
          <div style="margin-top: 8px">上传封面</div>
        </div>
      </a-upload>
      <div class="form-item-tip">支持 jpg/png，不超过 5MB（可选）</div>
    </a-form-item>

    <a-form-item label="频道分类" name="categoryName">
      <a-select
        v-model:value="formData.categoryName"
        placeholder="请选择频道分类"
        :options="categoryOptions"
      />
    </a-form-item>

    <a-form-item v-if="props.channelType === 'organization'" label="所属组织" name="orgName">
      <a-input :value="formData.orgName || '未关联组织'" disabled />
    </a-form-item>

    <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
      <a-space>
        <a-button type="primary" :loading="submitting" @click="handleSubmit">提交</a-button>
        <a-button @click="emit('cancel')">取消</a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import type { FormInstance, UploadProps } from 'ant-design-vue';
  import type { ChannelType, ChannelCreateReq } from '/@/api/content/channel/model/channelModel';
  import { message } from 'ant-design-vue';

  const props = withDefaults(
    defineProps<{
      channelType: ChannelType;
      initialValues?: Partial<ChannelCreateReq>;
      isEdit?: boolean;
      isSystemChannel?: boolean;
    }>(),
    {
      isEdit: false,
      isSystemChannel: false,
    },
  );

  const emit = defineEmits<{
    submit: [data: ChannelCreateReq];
    cancel: [];
  }>();

  const formRef = ref<FormInstance>();
  const submitting = ref(false);

  // 频道分类选项（静态，后续可改为接口获取）
  const categoryOptions = ref([
    { label: '技术', value: '技术' },
    { label: '生活', value: '生活' },
    { label: '娱乐', value: '娱乐' },
    { label: '学习', value: '学习' },
    { label: '其他', value: '其他' },
  ]);

  const formData = reactive<ChannelCreateReq>({
    name: '',
    description: '',
    channelType: props.channelType,
    iconUrl: '',
    coverUrl: '',
    categoryName: '',
    orgId: undefined,
  });

  const rules = {
    name: [
      { required: true, message: '请输入频道名称', trigger: 'blur' },
      { min: 1, max: 50, message: '频道名称长度为 1-50 个字符', trigger: 'blur' },
    ],
    description: [
      { required: true, message: '请输入频道描述', trigger: 'blur' },
      { min: 1, max: 200, message: '频道描述长度为 1-200 个字符', trigger: 'blur' },
    ],
    iconUrl: [{ required: true, message: '请上传频道图标', trigger: 'change' }],
    categoryName: [{ required: true, message: '请选择频道分类', trigger: 'change' }],
  };

  // 初始化表单数据
  onMounted(() => {
    if (props.initialValues) {
      Object.assign(formData, props.initialValues);
    }
  });

  // ===== 上传相关 =====
  function beforeIconUpload(file: File) {
    const isImage = file.type === 'image/jpeg' || file.type === 'image/png';
    if (!isImage) {
      message.error('只能上传 JPG/PNG 格式的图片');
      return false;
    }
    const isLt2M = file.size / 1024 / 1024 <= 2;
    if (!isLt2M) {
      message.error('图标文件大小不能超过 2MB');
      return false;
    }
    return true;
  }

  function beforeCoverUpload(file: File) {
    const isImage = file.type === 'image/jpeg' || file.type === 'image/png';
    if (!isImage) {
      message.error('只能上传 JPG/PNG 格式的图片');
      return false;
    }
    const isLt5M = file.size / 1024 / 1024 <= 5;
    if (!isLt5M) {
      message.error('封面文件大小不能超过 5MB');
      return false;
    }
    return true;
  }

  async function handleIconUpload(options: any) {
    // 实际上传逻辑由外部接入，这里模拟上传成功
    // TODO: 对接真实上传接口
    const url = URL.createObjectURL(options.file);
    formData.iconUrl = url;
    options.onSuccess?.({ url });
  }

  async function handleCoverUpload(options: any) {
    const url = URL.createObjectURL(options.file);
    formData.coverUrl = url;
    options.onSuccess?.({ url });
  }

  // ===== 表单提交 =====
  async function handleSubmit() {
    try {
      submitting.value = true;
      await formRef.value?.validateFields();
      emit('submit', { ...formData });
    } catch {
      // 校验失败，ant-design-vue 已自动提示
    } finally {
      submitting.value = false;
    }
  }

  // 暴露 validate 方法供外部调用
  async function validate() {
    return formRef.value?.validateFields();
  }

  defineExpose({ validate });
</script>

<style scoped>
  .form-item-tip {
    color: #999;
    font-size: 12px;
    margin-top: 4px;
  }
</style>
