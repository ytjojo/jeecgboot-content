<template>
  <div class="publish-permission">
    <Card title="发布权限配置">
      <Form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <Form.Item label="发布权限模型" name="publishModel" required>
          <RadioGroup v-model:value="formData.publishModel" @change="handleModelChange">
            <Space direction="vertical">
              <Radio value="admin_only">
                <div>仅管理员可发布</div>
                <div class="radio-desc">频道主、管理员和内容编辑可发布</div>
              </Radio>
              <Radio value="all_members">
                <div>所有成员可发布</div>
                <div class="radio-desc">频道成员可直接发布</div>
              </Radio>
              <Radio value="open_submission">
                <div>公开投稿</div>
                <div class="radio-desc">非成员可投稿，需审核通过后展示</div>
              </Radio>
              <Radio value="review_first">
                <div>先审后发</div>
                <div class="radio-desc">所有内容均需审核通过后展示</div>
              </Radio>
            </Space>
          </RadioGroup>
        </Form.Item>
        <Form.Item v-if="impactText" class="impact-description">
          <Alert :message="impactText" type="info" show-icon />
        </Form.Item>
        <Divider />
        <Form.Item label="每小时发布上限" name="hourlyLimit">
          <InputNumber v-model:value="formData.hourlyLimit" :min="0" placeholder="0 表示不限制" style="width: 200px" />
        </Form.Item>
        <Form.Item label="每日发布上限" name="dailyLimit">
          <InputNumber v-model:value="formData.dailyLimit" :min="0" placeholder="0 表示不限制" style="width: 200px" />
        </Form.Item>
        <Form.Item label="内容字数下限" name="minWordCount">
          <InputNumber v-model:value="formData.minWordCount" :min="0" placeholder="0 表示不限制" style="width: 200px" />
        </Form.Item>
        <Form.Item :wrapper-col="{ offset: 6, span: 16 }">
          <Space>
            <Button type="primary" :loading="saving" @click="handleSave">保存</Button>
            <Button @click="handleCancel">取消</Button>
          </Space>
        </Form.Item>
      </Form>
    </Card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue';
import { Card, Form, RadioGroup, Radio, Space, InputNumber, Divider, Button, Alert } from 'ant-design-vue';
import { getPublishPermission, savePublishPermission } from '/@/api/content/channel/publish';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirmSync, createMessage } = useMessage();

const props = defineProps<{ channelId: string }>();

const impactText = ref('');
const saving = ref(false);
const formData = reactive({
  publishModel: 'all_members',
  hourlyLimit: 0,
  dailyLimit: 0,
  minWordCount: 0,
});

const nameMap: Record<string, string> = {
  admin_only: '仅管理员可发布',
  all_members: '所有成员可发布',
  open_submission: '公开投稿',
  review_first: '先审后发',
};

const impactMap: Record<string, string> = {
  admin_only: '切换后，普通成员和非成员将无法在本频道发布内容，仅频道主、管理员和内容编辑可发布。',
  all_members: '切换后，频道成员可直接发布内容，无需审核。',
  open_submission: '切换后，非成员也可投稿，但需审核通过后才会在频道中展示。',
  review_first: '切换后，所有内容（包括管理员发布的）均需审核通过后才会展示。',
};

const handleModelChange = (e: any) => {
  impactText.value = impactMap[e.target.value] || '';
};

const handleSave = () => {
  createConfirmSync({
    title: '确认保存',
    content: `确认将发布权限模型切换为"${nameMap[formData.publishModel]}"？`,
    onOk: async () => {
      saving.value = true;
      try {
        await savePublishPermission({ channelId: props.channelId, ...formData });
        createMessage.success('发布权限配置已保存');
      } finally {
        saving.value = false;
      }
    },
  });
};

const initialData = ref<any>(null);

const handleCancel = async () => {
  if (initialData.value) {
    Object.assign(formData, initialData.value);
    impactText.value = '';
  } else {
    const res = await getPublishPermission(props.channelId);
    if (res) {
      Object.assign(formData, res);
      impactText.value = '';
    }
  }
};

onMounted(async () => {
  const res = await getPublishPermission(props.channelId);
  if (res) {
    Object.assign(formData, res);
    initialData.value = { ...res };
  }
});
</script>

<style lang="less" scoped>
.publish-permission {
  .radio-desc { color: #999; font-size: 12px; margin-top: 2px; }
}
</style>
