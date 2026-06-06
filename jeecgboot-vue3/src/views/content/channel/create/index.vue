<template>
  <div class="channel-create">
    <a-page-header title="创建频道" :back-icon="true" @back="goBack" />

    <!-- 状态检查 -->
    <a-alert
      v-if="disabledReason"
      type="warning"
      :message="disabledReason"
      show-icon
      class="channel-create__alert"
    />

    <a-card :bordered="false" class="channel-create__card">
      <ChannelCreateSteps v-if="!disabledReason && !submitted" ref="stepsRef" @submit="onSubmit" />
      <ReviewWaiting v-if="submitted && pendingChannelId" :channel-id="pendingChannelId" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { message } from 'ant-design-vue';
  import ChannelCreateSteps from './ChannelCreateSteps.vue';
  import ReviewWaiting from './ReviewWaiting.vue';
  import { createChannel } from '/@/api/content/channel';
  import { useUserStore } from '/@/store/modules/user';
  import type { ChannelCreateReq } from '/@/api/content/channel/model/channelModel';

  const router = useRouter();
  const userStore = useUserStore();
  const stepsRef = ref<InstanceType<typeof ChannelCreateSteps>>();
  const submitted = ref(false);
  const pendingChannelId = ref('');
  const submitting = ref(false);

  const disabledReason = computed(() => {
    // 未完成账号验证
    if (!userStore.getUserInfo?.phone) {
      return '请先完成手机号验证后再创建频道';
    }
    // 账号冻结
    if ((userStore.getUserInfo as any)?.status === 'frozen') {
      return '您的账号已被冻结，无法创建频道';
    }
    return '';
  });

  function goBack() {
    router.back();
  }

  async function onSubmit(data: ChannelCreateReq) {
    if (submitting.value) return;
    submitting.value = true;
    try {
      const result = await createChannel(data);
      pendingChannelId.value = result.id;
      submitted.value = true;
      message.success('频道创建成功，等待审核');
    } catch (e: any) {
      message.error(e?.message || '创建失败，请重试');
    } finally {
      submitting.value = false;
    }
  }
</script>

<style scoped lang="less">
  .channel-create {
    padding: 24px;

    &__alert {
      margin-bottom: 16px;
    }

    &__card {
      min-height: 400px;
    }
  }
</style>
