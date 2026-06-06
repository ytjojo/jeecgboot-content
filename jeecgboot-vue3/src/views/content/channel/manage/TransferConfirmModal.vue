<template>
  <a-modal
    v-model:open="visible"
    title="频道转让"
    :confirm-loading="submitting"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <!-- 第一步：搜索目标用户 -->
    <div v-if="step === 1">
      <p>请选择转让目标用户：</p>
      <a-select
        v-model:value="selectedUserId"
        show-search
        :placeholder="channel.channelType === 'organization' ? '搜索组织内管理员' : '搜索用户'"
        :filter-option="false"
        :loading="searching"
        :options="userOptions"
        @search="handleSearch"
        style="width: 100%"
      >
        <template #option="{ label, avatar, disabled }">
          <div :style="{ opacity: disabled ? 0.4 : 1 }">
            <a-avatar :src="avatar" :size="24" style="margin-right: 8px">{{ label?.charAt(0) }}</a-avatar>
            {{ label }}
          </div>
        </template>
      </a-select>
    </div>

    <!-- 第二步：确认 -->
    <div v-if="step === 2">
      <a-alert type="warning" show-icon>
        <template #message>
          <span>
            确认将频道 <strong>{{ channel.name }}</strong> 转让给
            <strong>{{ selectedUserName }}</strong> ？
            转让后您将降为管理员，此操作不可撤销。
          </span>
        </template>
      </a-alert>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { message } from 'ant-design-vue';
  import { transferChannel } from '/@/api/content/channel';
  import { useUserStore } from '/@/store/modules/user';
  import type { ChannelVO } from '/@/api/content/channel/model/channelModel';

  const props = defineProps<{ channel: ChannelVO }>();
  const emit = defineEmits<{ success: [] }>();

  const visible = defineModel<boolean>('open', { default: false });
  const step = ref(1);
  const selectedUserId = ref<string>('');
  const searching = ref(false);
  const submitting = ref(false);
  const userOptions = ref<{ label: string; value: string; avatar?: string; disabled?: boolean }[]>([]);

  const userStore = useUserStore();
  const currentUserId = computed(() => userStore.getUserInfo?.id);
  const selectedUserName = computed(() => {
    const opt = userOptions.value.find((o) => o.value === selectedUserId.value);
    return opt?.label || '';
  });

  let searchTimer: ReturnType<typeof setTimeout> | null = null;

  function handleSearch(keyword: string) {
    if (!keyword || keyword.length < 2) return;
    if (searchTimer) clearTimeout(searchTimer);
    searchTimer = setTimeout(async () => {
      searching.value = true;
      try {
        // TODO: 调用用户搜索 API，组织频道限定同组织管理员
        // 暂时使用空实现
        userOptions.value = [];
      } finally {
        searching.value = false;
      }
    }, 300);
  }

  async function handleOk() {
    if (step.value === 1) {
      if (!selectedUserId.value) {
        message.warning('请选择目标用户');
        return;
      }
      if (selectedUserId.value === currentUserId.value) {
        message.warning('不能转让给自己');
        return;
      }
      step.value = 2;
      return;
    }

    submitting.value = true;
    try {
      await transferChannel(props.channel.id, selectedUserId.value);
      message.success('转让请求已发送');
      visible.value = false;
      step.value = 1;
      selectedUserId.value = '';
      emit('success');
    } catch (e: any) {
      message.error(e?.message || '转让失败');
    } finally {
      submitting.value = false;
    }
  }

  function handleCancel() {
    visible.value = false;
    step.value = 1;
    selectedUserId.value = '';
  }
</script>
