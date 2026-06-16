<template>
  <Dropdown :trigger="['click']" :disabled="loading">
    <Button size="small" :loading="loading">更多</Button>
    <template #overlay>
      <Menu @click="handleMenuClick">
        <!-- 管理员：置顶/精华操作 -->
        <template v-if="isAdmin">
          <Menu.Item key="pin" v-if="!isPinned">置顶</Menu.Item>
          <Menu.Item key="unpin" v-if="isPinned">取消置顶</Menu.Item>
          <Menu.Divider />
          <Menu.Item key="feature" v-if="!isFeatured">标记精华</Menu.Item>
          <Menu.Item key="unfeature" v-if="isFeatured">取消精华</Menu.Item>
          <Menu.Divider />
          <Menu.Item key="delete" danger>删除</Menu.Item>
        </template>
        <!-- 普通成员：仅举报 -->
        <Menu.Item key="report" v-else>举报</Menu.Item>
      </Menu>
    </template>
  </Dropdown>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { Dropdown, Button, Menu } from 'ant-design-vue';
import { useCircleStoreWithOut } from '/@/store/modules/circle';

const props = defineProps<{
  isPinned: boolean;
  isFeatured: boolean;
  loading?: boolean;
}>();

const emit = defineEmits<{
  (e: 'action', action: string): void;
}>();

const circleStore = useCircleStoreWithOut();
const isAdmin = computed(() => circleStore.isCreator || circleStore.isModerator);

const handleMenuClick = ({ key }: { key: string }) => emit('action', key);
</script>
