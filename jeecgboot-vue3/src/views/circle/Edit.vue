<template>
  <div class="circle-edit-page">
    <!-- 403 权限检查 -->
    <a-result v-if="forbidden" status="403" title="您没有权限访问此页面" sub-title="仅圈子创建者可以编辑圈子信息">
      <template #extra>
        <a-button type="primary" @click="router.back()">返回</a-button>
      </template>
    </a-result>

    <div v-else class="edit-container">
      <h2 class="edit-title">编辑圈子</h2>
      <a-spin :spinning="loading">
        <a-form v-if="circle" ref="formRef" :model="form" :rules="rules" layout="vertical">
          <!-- 圈子名称（只读） -->
          <a-form-item label="圈子名称">
            <a-input :value="circle.name" disabled aria-label="圈子名称" />
            <div class="name-readonly-hint">名称不可修改</div>
          </a-form-item>

          <!-- 圈子简介 -->
          <a-form-item label="圈子简介" name="description" required>
            <a-textarea
              v-model:value="form.description"
              placeholder="请输入圈子简介（10-500字）"
              :maxlength="500"
              :rows="4"
              show-count
              aria-label="圈子简介"
            />
          </a-form-item>

          <!-- 分类标签 -->
          <a-form-item label="分类标签" name="category">
            <a-select v-model:value="form.category" placeholder="请选择分类标签" allow-clear aria-label="分类标签">
              <a-select-option v-for="cat in categoryOptions" :key="cat.value" :value="cat.value">
                {{ cat.label }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <!-- 圈子图标（1:1 裁剪） -->
          <a-form-item label="圈子图标">
            <CircleIconCropper v-model="form.iconUrl" />
          </a-form-item>

          <!-- 封面图（16:9 裁剪） -->
          <a-form-item label="封面图">
            <CircleCoverCropper v-model="form.coverUrl" />
          </a-form-item>
        </a-form>
      </a-spin>

      <!-- 提交按钮 -->
      <div v-if="!forbidden && circle" class="edit-actions">
        <a-button @click="router.back()">取消</a-button>
        <a-button type="primary" :loading="submitting" @click="handleSubmit">保存修改</a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getCircleDetail, updateCircle } from '/@/api/content/circle';
import { useMessage } from '/@/hooks/web/useMessage';
import CircleIconCropper from './components/CircleIconCropper.vue';
import CircleCoverCropper from './components/CircleCoverCropper.vue';
import { categoryOptions } from './constants';
import { useCircleStoreWithOut } from '/@/store/modules/circle';
import type { Rule } from 'ant-design-vue/es/form';

const route = useRoute();
const router = useRouter();
const { createMessage } = useMessage();
const circleStore = useCircleStoreWithOut();

const loading = ref(true);
const submitting = ref(false);
const forbidden = ref(false);
const circle = ref<any>(null);
const formRef = ref();

const form = reactive({
  description: '',
  category: undefined as string | undefined,
  iconUrl: undefined as string | undefined,
  coverUrl: undefined as string | undefined,
});

// 分类选项（共享常量）

const rules: Record<string, Rule[]> = {
  description: [
    { required: true, message: '请输入圈子简介', trigger: 'blur' },
    { min: 10, max: 500, message: '简介长度需在10-500字之间', trigger: 'blur' },
  ],
};

onMounted(async () => {
  const id = route.params.id as string;
  try {
    const detail = await getCircleDetail(id);
    if (!detail) {
      forbidden.value = true;
      return;
    }
    // 权限校验：仅创建者
    if (detail.myRole !== 'CREATOR') {
      forbidden.value = true;
      return;
    }
    circle.value = detail;
    form.description = detail.description || '';
    form.category = detail.category || undefined;
    form.iconUrl = detail.iconUrl || undefined;
    form.coverUrl = detail.coverUrl || undefined;
  } catch {
    forbidden.value = true;
  } finally {
    loading.value = false;
  }
});

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }
  submitting.value = true;
  try {
    const req: any = { id: circle.value.id, description: form.description };
    if (form.category) req.category = form.category;
    if (form.iconUrl) req.iconUrl = form.iconUrl;
    if (form.coverUrl) req.coverUrl = form.coverUrl;
    await updateCircle(req);
    createMessage.success('编辑成功');
    router.push(`/circle/${circle.value.id}`);
  } catch (error: any) {
    createMessage.error(error?.message || '编辑失败');
  } finally {
    submitting.value = false;
  }
}
</script>

<style lang="less" scoped>
.circle-edit-page {
  min-height: calc(100vh - 64px);
  padding: 24px 16px;
  background: var(--background-color-base, #f5f5f5);
}

.edit-container {
  max-width: 640px;
  margin: 0 auto;
  background: var(--component-background, #fff);
  border-radius: 12px;
  padding: 32px;
}

.edit-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 24px;
}

.name-readonly-hint {
  font-size: 12px;
  color: var(--text-color-tertiary, #999);
  margin-top: 4px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color-base, #f0f0f0);
}

.upload-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-color-secondary, #666);
}

@media (max-width: 768px) {
  .edit-container {
    padding: 20px 16px;
  }
}
</style>
