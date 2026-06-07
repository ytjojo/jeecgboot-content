<!-- jeecgboot-vue3/src/views/channel/components/ChannelContextProvider.vue -->
<template>
  <div class="channel-context-provider">
    <slot />
  </div>
</template>

<script setup lang="ts">
  import { toRef, watch, onMounted } from 'vue';
  import { onBeforeRouteUpdate } from 'vue-router';
  import { useChannelContext } from '/@/composables/useChannelContext';

  const props = defineProps<{ channelId: string }>();

  const channelIdRef = toRef(props, 'channelId');
  const { loadContext, resetContext } = useChannelContext(channelIdRef);

  // 初始加载
  onMounted(loadContext);

  // channelId 变化时重新加载
  watch(channelIdRef, () => {
    resetContext();
    loadContext();
  });

  // 路由参数变化时重置并重新加载上下文
  onBeforeRouteUpdate((to, from) => {
    if (to.params.id !== from.params.id) {
      resetContext();
      loadContext(to.params.id);
    }
  });

</script>
