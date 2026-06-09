<template>
  <div class="join-status-button">
    <!-- 已加入 -->
    <a-button v-if="circle.joined" type="default" disabled aria-label="已加入">
      已加入
    </a-button>

    <!-- 申请中 -->
    <a-button v-else-if="circle.applyStatus === 'PENDING'" type="default" disabled aria-label="申请中">
      申请中
    </a-button>

    <!-- 满员 -->
    <a-tooltip v-else-if="isFull" title="圈子已满员，无法加入">
      <a-button type="default" disabled aria-label="满员">
        满员
      </a-button>
    </a-tooltip>

    <!-- 仅限邀请（非受邀用户） -->
    <a-tooltip v-else-if="circle.joinType === 'INVITE' && !circle.isInvited" title="该圈子仅限受邀用户加入">
      <a-button type="default" disabled aria-label="仅限邀请加入">
        仅限邀请加入
      </a-button>
    </a-tooltip>

    <!-- 直接加入 / 受邀用户加入 -->
    <a-button v-else-if="circle.joinType === 'DIRECT' || (circle.joinType === 'INVITE' && circle.isInvited)" type="primary" :loading="loading" @click="handleJoin" aria-label="加入">
      加入
    </a-button>

    <!-- 申请加入 -->
    <a-button v-else-if="circle.joinType === 'APPROVAL'" type="primary" :loading="loading" @click="$emit('apply')" aria-label="申请加入">
      {{ circle.applyStatus === 'REJECTED' ? '重新申请' : '申请加入' }}
    </a-button>

    <!-- 密码加入 -->
    <a-button v-else-if="circle.joinType === 'PASSWORD'" type="primary" @click="$emit('password-join')" aria-label="密码加入">
      密码加入
    </a-button>

    <!-- 默认 -->
    <a-button v-else type="primary" :loading="loading" @click="handleJoin" aria-label="加入">
      加入
    </a-button>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { joinCircle } from '/@/api/content/circle';
import { useMessage } from '/@/hooks/web/useMessage';
import type { CircleVO } from '/@/api/content/model/circleModel';

const props = defineProps<{
  circle: CircleVO;
}>();

const emit = defineEmits<{
  'join-success': [];
  apply: [];
  'password-join': [];
}>();

const { createMessage } = useMessage();
const loading = ref(false);

const isFull = computed(() => {
  return props.circle.maxMemberCount > 0 && props.circle.memberCount >= props.circle.maxMemberCount;
});

async function handleJoin() {
  loading.value = true;
  try {
    await joinCircle({ circleId: props.circle.id });
    createMessage.success('加入成功');
    emit('join-success');
  } catch (error: any) {
    const msg = error?.message || '加入失败';
    createMessage.error(msg);
  } finally {
    loading.value = false;
  }
}
</script>

<style lang="less" scoped>
.join-status-button {
  flex-shrink: 0;
}
</style>
