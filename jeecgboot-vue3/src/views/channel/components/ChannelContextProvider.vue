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

  onMounted(loadContext);

  watch(channelIdRef, () => {
    resetContext();
    loadContext();
  });

</script>
