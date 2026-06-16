<template>
  <a-modal
    :visible="visible"
    title="公告管理"
    :mask-closable="false"
    :footer="false"
    :destroy-on-close="true"
    @cancel="handleCancel"
  >
    <a-form :model="form" layout="vertical">
      <a-form-item
        label="公告内容"
        required
        :validate-status="errors.content ? 'error' : undefined"
        :help="errors.content || undefined"
      >
        <a-textarea
          v-model:value="form.content"
          placeholder="请输入公告内容"
          :rows="6"
          :maxlength="2000"
          show-count
        />
      </a-form-item>
      <a-form-item
        label="有效期"
        required
        :validate-status="errors.expireAt ? 'error' : undefined"
        :help="errors.expireAt || undefined"
      >
        <a-date-picker
          v-model:value="form.expireAt"
          show-time
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="请选择截止时间"
          style="width: 100%"
        />
      </a-form-item>
    </a-form>

    <div class="announcement-modal-footer">
      <a-button @click="handleCancel">取消</a-button>
      <a-button
        v-if="announcement"
        danger
        :loading="deleteLoading"
        @click="handleDelete"
      >
        删除公告
      </a-button>
      <a-button type="primary" :loading="publishLoading" @click="handlePublish">
        {{ announcement ? '更新公告' : '发布公告' }}
      </a-button>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, watch } from 'vue';
import { Modal } from 'ant-design-vue';
import { useMessage } from '/@/hooks/web/useMessage';
import {
  getActiveCircleAnnouncement,
  publishCircleAnnouncement,
  deleteCircleAnnouncement,
} from '/@/api/content/circle/announcement';
import type { CircleAnnouncementVO } from '/@/api/content/circle/model/circleAnnouncementModel';

const props = defineProps<{
  circleId: string;
  visible: boolean;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  published: [];
  deleted: [];
}>();

const { createMessage } = useMessage();

const announcement = ref<CircleAnnouncementVO | null>(null);
const publishLoading = ref(false);
const deleteLoading = ref(false);

const form = reactive({
  content: '',
  expireAt: undefined as string | undefined,
});

const errors = reactive({
  content: '',
  expireAt: '',
});

// 每次打开时加载当前公告
watch(() => props.visible, async (val) => {
  if (val) {
    await loadActiveAnnouncement();
  } else {
    clearForm();
  }
});

async function loadActiveAnnouncement() {
  try {
    const res = await getActiveCircleAnnouncement(props.circleId);
    if (res) {
      // 检查过期
      if (res.expireAt && Date.now() > new Date(res.expireAt).getTime()) {
        announcement.value = null;
        form.content = '';
        form.expireAt = undefined;
        return;
      }
      announcement.value = res;
      form.content = res.content || '';
      form.expireAt = res.expireAt || undefined;
    } else {
      announcement.value = null;
      form.content = '';
      form.expireAt = undefined;
    }
  } catch {
    announcement.value = null;
  }
}

function clearForm() {
  form.content = '';
  form.expireAt = undefined;
  errors.content = '';
  errors.expireAt = '';
  announcement.value = null;
}

function validate(): boolean {
  let valid = true;

  if (!form.content.trim()) {
    errors.content = '请输入公告内容';
    valid = false;
  } else {
    errors.content = '';
  }

  if (form.expireAt) {
    const expireTime = new Date(form.expireAt).getTime();
    if (expireTime <= Date.now()) {
      errors.expireAt = '有效期不得早于当前时间';
      valid = false;
    } else {
      errors.expireAt = '';
    }
  } else {
    errors.expireAt = '请选择有效期';
    valid = false;
  }

  return valid;
}

async function handlePublish() {
  if (!validate()) return;

  // 已有公告时弹出替换确认
  if (announcement.value) {
    const confirmed = await new Promise<boolean>((resolve) => {
      Modal.confirm({
        title: '确认发布',
        content: '当前已有生效公告，发布新公告将替换旧公告，是否继续？',
        onOk: () => resolve(true),
        onCancel: () => resolve(false),
      });
    });
    if (!confirmed) return;
  }

  publishLoading.value = true;
  try {
    await publishCircleAnnouncement({
      circleId: props.circleId,
      content: form.content.trim(),
      expireAt: form.expireAt,
    });
    createMessage.success('公告已发布');
    emit('update:visible', false);
    emit('published');
  } catch {
    createMessage.error('发布失败，请重试');
  } finally {
    publishLoading.value = false;
  }
}

async function handleDelete() {
  if (!announcement.value) return;

  const confirmed = await new Promise<boolean>((resolve) => {
    Modal.confirm({
      title: '确认删除',
      content: '确认删除该公告？',
      onOk: () => resolve(true),
      onCancel: () => resolve(false),
    });
  });
  if (!confirmed) return;

  deleteLoading.value = true;
  try {
    await deleteCircleAnnouncement(announcement.value.id);
    createMessage.success('公告已删除');
    emit('update:visible', false);
    emit('deleted');
  } catch {
    createMessage.error('删除失败，请重试');
  } finally {
    deleteLoading.value = false;
  }
}

function handleCancel() {
  emit('update:visible', false);
}
</script>

<style lang="less" scoped>
.announcement-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color-base, #f0f0f0);
}
</style>
