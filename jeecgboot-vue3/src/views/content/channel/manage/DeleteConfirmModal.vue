<template>
  <a-modal
    v-model:open="visible"
    title="删除频道"
    :footer="hasBlockers ? undefined : undefined"
    :confirm-loading="submitting"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-spin :spinning="checking">
      <div v-if="hasBlockers">
        <a-alert type="error" show-icon>
          <template #message>
            <div>无法删除频道，存在以下阻塞原因：</div>
            <ul style="margin: 8px 0 0; padding-left: 20px">
              <li v-for="(reason, idx) in blockReasons" :key="idx">{{ reason }}</li>
            </ul>
          </template>
        </a-alert>
      </div>

      <div v-else>
        <a-alert type="warning" show-icon style="margin-bottom: 16px">
          <template #message>
            <span>删除后频道将进入冷静期，冷静期内可撤销。冷静期结束后将永久删除。</span>
          </template>
        </a-alert>

        <p>请输入频道名称 <strong>{{ channel.name }}</strong> 以确认删除：</p>
        <a-input v-model:value="confirmName" placeholder="请输入频道名称" />
      </div>
    </a-spin>

    <template #footer>
      <a-button @click="handleCancel">取消</a-button>
      <a-button
        v-if="!hasBlockers"
        type="primary"
        danger
        :disabled="confirmName !== channel.name"
        :loading="submitting"
        @click="handleOk"
      >确认删除</a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import { checkDeletePrecondition, deleteChannel } from '/@/api/content/channel';
  import type { ChannelVO } from '/@/api/content/channel/model/channelModel';

  const props = defineProps<{ channel: ChannelVO }>();
  const emit = defineEmits<{ success: [] }>();

  const visible = defineModel<boolean>('open', { default: false });
  const checking = ref(false);
  const submitting = ref(false);
  const hasBlockers = ref(false);
  const blockReasons = ref<string[]>([]);
  const confirmName = ref('');

  watch(visible, async (open) => {
    if (open) {
      confirmName.value = '';
      hasBlockers.value = false;
      blockReasons.value = [];
      await checkPrecondition();
    }
  });

  async function checkPrecondition() {
    checking.value = true;
    try {
      const result = await checkDeletePrecondition(props.channel.id);
      if (!result.canDelete) {
        hasBlockers.value = true;
        blockReasons.value = result.blockReasons || [];
      }
    } catch {
      // 如果检查接口失败，允许继续删除流程
    } finally {
      checking.value = false;
    }
  }

  async function handleOk() {
    if (confirmName.value !== props.channel.name) return;
    submitting.value = true;
    try {
      await deleteChannel(props.channel.id);
      message.success('频道已进入删除冷静期');
      visible.value = false;
      emit('success');
    } catch (e: any) {
      message.error(e?.message || '删除失败');
    } finally {
      submitting.value = false;
    }
  }

  function handleCancel() {
    visible.value = false;
  }
</script>
