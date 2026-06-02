<template>
  <a-modal :open="open" title="个性化推荐" :footer="null" :closable="false" :mask-closable="false" width="520px">
    <p class="interest-modal__desc">选择至少 3 个你感兴趣的话题，我们将为你推荐相关内容</p>
    <a-checkbox-group v-model:value="selected" class="interest-modal__tags">
      <a-checkbox v-for="t in topics" :key="t.id" :value="t.id" class="interest-modal__tag">{{ t.name }}</a-checkbox>
    </a-checkbox-group>
    <div class="interest-modal__actions">
      <a-button @click="onSkip">稍后设置</a-button>
      <a-button type="primary" :disabled="selected.length < 3" :loading="submitting" @click="onSubmit">开始探索 ({{ selected.length }}/3+)</a-button>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { message } from 'ant-design-vue';
import { saveUserTopicPreferences } from '/@/api/content/user/preferences';

defineProps<{ open: boolean }>();
const emit = defineEmits<{ (e: 'done'): void; (e: 'skip'): void }>();

const selected = ref<string[]>([]);
const submitting = ref(false);
const topics = ref([
  { id: 'tech', name: '科技' }, { id: 'design', name: '设计' }, { id: 'life', name: '生活' },
  { id: 'travel', name: '旅行' }, { id: 'food', name: '美食' }, { id: 'sport', name: '运动' },
  { id: 'music', name: '音乐' }, { id: 'movie', name: '电影' }, { id: 'book', name: '阅读' },
  { id: 'photography', name: '摄影' }, { id: 'finance', name: '理财' }, { id: 'edu', name: '教育' },
]);

async function onSubmit() {
  submitting.value = true;
  try {
    await saveUserTopicPreferences(selected.value);
    message.success('已为你个性化推荐');
    emit('done');
  } catch (e: any) {
    message.error(e?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
}

function onSkip() {
  emit('skip');
}
</script>

<style lang="less" scoped>
.interest-modal {
  &__desc { color: #666; margin-bottom: 16px; }
  &__tags { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
  &__tag { padding: 8px 12px; border: 1px solid #eee; border-radius: 8px; }
  &__actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 24px; }
}
</style>
