<template>
  <div class="channel-create-steps">
    <a-steps :current="currentStep" size="small" class="channel-create-steps__bar">
      <a-step title="选择频道类型" />
      <a-step title="填写频道信息" />
    </a-steps>

    <div class="channel-create-steps__body">
      <!-- Step 1: 选择频道类型 -->
      <div v-if="currentStep === 0" class="channel-create-steps__types">
        <a-row :gutter="24">
          <a-col :span="12">
            <div
              class="type-card"
              :class="{ 'type-card--selected': selectedType === 'personal' }"
              @click="selectType('personal')"
            >
              <div class="type-card__icon">
                <UserOutlined style="font-size: 36px; color: #52c41a" />
              </div>
              <div class="type-card__title">个人频道</div>
              <div class="type-card__desc">以个人身份创建的频道，用于发布个人内容</div>
            </div>
          </a-col>
          <a-col :span="12">
            <div
              class="type-card"
              :class="{ 'type-card--selected': selectedType === 'organization' }"
              @click="selectType('organization')"
            >
              <div class="type-card__icon">
                <TeamOutlined style="font-size: 36px; color: #722ed1" />
              </div>
              <div class="type-card__title">组织频道</div>
              <div class="type-card__desc">以组织身份创建的频道，需绑定组织信息</div>
            </div>
          </a-col>
        </a-row>
        <div class="channel-create-steps__actions">
          <a-button type="primary" :disabled="!selectedType" @click="nextStep">下一步</a-button>
        </div>
      </div>

      <!-- Step 2: 填写表单 -->
      <div v-if="currentStep === 1">
        <ChannelForm
          ref="formRef"
          :channel-type="selectedType!"
          :is-edit="false"
          @submit="handleSubmit"
          @cancel="prevStep"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { UserOutlined, TeamOutlined } from '@ant-design/icons-vue';
  import ChannelForm from '/@/components/jeecg/channel/ChannelForm.vue';
  import type { ChannelType, ChannelCreateReq } from '/@/api/content/channel/model/channelModel';

  const emit = defineEmits<{
    submit: [data: ChannelCreateReq];
  }>();

  const currentStep = ref(0);
  const selectedType = ref<ChannelType | null>(null);
  const formRef = ref<InstanceType<typeof ChannelForm>>();

  function selectType(type: ChannelType) {
    selectedType.value = type;
  }

  function nextStep() {
    if (selectedType.value) {
      currentStep.value = 1;
    }
  }

  function prevStep() {
    currentStep.value = 0;
  }

  function handleSubmit(data: ChannelCreateReq) {
    emit('submit', data);
  }

  defineExpose({
    validate: () => formRef.value?.validate(),
  });
</script>

<style scoped lang="less">
  .channel-create-steps {
    max-width: 720px;
    margin: 0 auto;

    &__bar {
      margin-bottom: 32px;
    }

    &__types {
      padding: 16px 0;
    }

    &__actions {
      margin-top: 24px;
      text-align: center;
    }
  }

  .type-card {
    border: 2px solid #f0f0f0;
    border-radius: 8px;
    padding: 32px 24px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #d9d9d9;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    &--selected {
      border-color: #1890ff;
      background: #e6f7ff;
    }

    &__icon {
      margin-bottom: 16px;
    }

    &__title {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 8px;
    }

    &__desc {
      color: #8c8c8c;
      font-size: 14px;
    }
  }
</style>
