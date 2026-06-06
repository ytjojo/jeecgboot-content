<template>
  <a-tooltip v-if="tooltipText" :title="tooltipText">
    <a-tag :color="colorMap[status]" class="status-tag">{{ labelMap[status] || status }}</a-tag>
  </a-tooltip>
  <a-tag v-else :color="colorMap[status]" class="status-tag">{{ labelMap[status] || status }}</a-tag>
</template>

<script lang="ts" setup>
import type { UserStatusEnum } from '/@/api/content/model/userStatusModel';

defineProps({
  status: {
    type: String as PropType<UserStatusEnum | string>,
    required: true,
  },
  showTooltip: {
    type: Boolean,
    default: false,
  },
  tooltipText: {
    type: String,
    default: '',
  },
});

const colorMap: Record<string, string> = {
  GUEST: 'default',
  REGISTERED_INCOMPLETE: 'orange',
  NORMAL: 'green',
  MUTED: 'gold',
  RESTRICTED_RECOMMEND: 'lime',
  FROZEN: 'blue',
  BANNED: 'red',
  DEACTIVATING: 'purple',
  DEACTIVATED: 'default',
};

const labelMap: Record<string, string> = {
  GUEST: '游客',
  REGISTERED_INCOMPLETE: '注册未完善',
  NORMAL: '正常',
  MUTED: '禁言',
  RESTRICTED_RECOMMEND: '限制推荐',
  FROZEN: '冻结',
  BANNED: '封禁',
  DEACTIVATING: '注销中',
  DEACTIVATED: '已注销',
};
</script>

<style scoped>
.status-tag {
  cursor: default;
}
</style>
