<!-- jeecgboot-vue3/src/views/channel/settings/PrivacySettings.vue -->
<template>
  <div class="privacy-settings">
    <div class="setting-label">频道隐私</div>
    <div>
      <Radio.Group v-model:value="currentPrivacy" :disabled="isSystem || saving" @change="handlePrivacyChange">
        <Radio value="PUBLIC">公开</Radio>
        <Radio value="PRIVATE">私有</Radio>
      </Radio.Group>
      <Alert
        v-if="isSystem"
        type="info"
        message="系统频道必须公开，不允许设置为私有"
        show-icon
        :style="{ marginTop: '8px' }"
      />
      <div class="privacy-desc">
        {{ currentPrivacy === 'PUBLIC' ? '频道内容对所有人可见，可被搜索和推荐' : '仅频道成员可浏览受限内容' }}
      </div>
    </div>

    <Modal
      v-model:open="confirmModalVisible"
      :title="confirmTitle"
      :confirmLoading="saving"
      @ok="handleConfirm"
      @cancel="confirmModalVisible = false"
    >
      <p>{{ confirmContent }}</p>
      <template #footer>
        <Button @click="confirmModalVisible = false">取消</Button>
        <Button type="primary" danger :loading="saving" @click="handleConfirm">确认</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, watch } from 'vue';
  import { Radio, Alert, Modal, Button } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { updateChannelPrivacy } from '/@/api/content/channelPrivacy';

  const props = defineProps<{
    channelId: string;
    initialPrivacy: 'PUBLIC' | 'PRIVATE';
    isSystem: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'updated', privacy: 'PUBLIC' | 'PRIVATE'): void;
  }>();

  const { createMessage } = useMessage();
  const currentPrivacy = ref<'PUBLIC' | 'PRIVATE'>(props.initialPrivacy);
  const pendingPrivacy = ref<'PUBLIC' | 'PRIVATE'>('PUBLIC');
  const confirmModalVisible = ref(false);
  const saving = ref(false);

  const confirmTitle = computed(() =>
    pendingPrivacy.value === 'PRIVATE' ? '确认设为私有频道？' : '确认设为公开频道？',
  );
  const confirmContent = computed(() =>
    pendingPrivacy.value === 'PRIVATE'
      ? '频道将退出公开搜索和推荐，非成员将无法浏览受限内容。当前订阅者不受影响。'
      : '频道内容将对所有人可见，可被搜索和推荐。',
  );

  watch(() => props.initialPrivacy, (val) => {
    currentPrivacy.value = val;
  });

  function handlePrivacyChange(e: any) {
    pendingPrivacy.value = e.target.value;
    confirmModalVisible.value = true;
  }

  async function handleConfirm() {
    saving.value = true;
    try {
      await updateChannelPrivacy({ channelId: props.channelId, privacyType: pendingPrivacy.value });
      currentPrivacy.value = pendingPrivacy.value;
      confirmModalVisible.value = false;
      createMessage.success('隐私设置已更新');
      emit('updated', currentPrivacy.value);
    } catch {
      createMessage.error('隐私设置更新失败');
    } finally {
      saving.value = false;
    }
  }
</script>

<style scoped>
.privacy-settings {
  padding: 16px 0;
}
.setting-label {
  font-weight: 500;
  margin-bottom: 12px;
}
.privacy-desc {
  color: #999;
  margin-top: 8px;
  font-size: 13px;
}
</style>
