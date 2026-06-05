<template>
  <a-popover v-model:open="visible" trigger="click" placement="bottom">
    <template #content>
      <div class="not-interested-options">
        <div class="popover-title">不感兴趣</div>
        <a-menu :bordered="false" @click="handleOptionClick">
          <a-menu-item v-if="category" key="category">
            <span>屏蔽此类内容</span>
          </a-menu-item>
          <a-menu-item v-for="topic in topics" :key="'topic:' + topic">
            <span>屏蔽话题：{{ topic }}</span>
          </a-menu-item>
          <a-menu-item key="dismiss">
            <span>知道了</span>
          </a-menu-item>
        </a-menu>
      </div>
    </template>
    <slot>
      <a-button type="text" size="small">不感兴趣</a-button>
    </slot>
  </a-popover>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { message } from 'ant-design-vue';
import { addFilterRule } from '/@/api/content/filterRule';

const props = defineProps({
  contentId: { type: String, required: true },
  contentType: { type: String, required: true },
  category: { type: String, default: '' },
  topics: { type: Array as () => string[], default: () => [] },
});

const visible = ref(false);

async function getCurrentUserId(): Promise<string> {
  const { useUserStore } = await import('/@/store/modules/user');
  const userStore = useUserStore();
  return String((userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '');
}

async function handleOptionClick({ key }: { key: string }) {
  visible.value = false;
  if (key === 'dismiss') return;

  try {
    const userId = await getCurrentUserId();
    if (key === 'category') {
      await addFilterRule(userId, 'CONTENT_TYPE', props.category);
      message.success('已屏蔽该类型内容');
    } else if (key.startsWith('topic:')) {
      const topic = key.substring(6);
      await addFilterRule(userId, 'TOPIC', topic);
      message.success('已屏蔽该话题');
    }
  } catch (e: any) {
    message.error(e?.message || '操作失败');
  }
}
</script>

<style scoped>
.not-interested-options {
  min-width: 180px;
}
.popover-title {
  font-weight: 500;
  margin-bottom: 8px;
  color: rgba(0, 0, 0, 0.85);
}
</style>
