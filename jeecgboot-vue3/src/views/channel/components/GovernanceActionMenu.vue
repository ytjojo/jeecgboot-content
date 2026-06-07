<template>
  <Dropdown :trigger="['click']">
    <Button size="small">更多</Button>
    <template #overlay>
      <Menu @click="handleMenuClick">
        <Menu.Item key="pin" v-if="!isPinned">置顶</Menu.Item>
        <Menu.Item key="unpin" v-if="isPinned">取消置顶</Menu.Item>
        <Menu.Item key="feature" v-if="!isFeatured">标记精华</Menu.Item>
        <Menu.Item key="unfeature" v-if="isFeatured">取消精华</Menu.Item>
        <Menu.Item key="move">移出频道</Menu.Item>
        <Menu.Item key="editAssist">编辑协助</Menu.Item>
        <Menu.Item key="delete" danger>删除</Menu.Item>
      </Menu>
    </template>
  </Dropdown>
</template>

<script lang="ts" setup>
import { Dropdown, Button, Menu } from 'ant-design-vue';

defineProps<{
  isPinned: boolean;
  isFeatured: boolean;
}>();

const emit = defineEmits<{
  (e: 'action', action: string): void;
}>();

const handleMenuClick = ({ key }: { key: string }) => emit('action', key);
</script>
