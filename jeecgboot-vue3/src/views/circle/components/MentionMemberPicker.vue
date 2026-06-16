<template>
  <div v-if="state.isOpen" class="mention-picker" data-testid="mention-picker">
    <!-- 搜索框 -->
    <div class="mention-picker-search">
      <a-input
        :value="state.searchKeyword"
        placeholder="搜索成员"
        allow-clear
        @update:value="handleSearch"
      />
    </div>

    <!-- 加载中 -->
    <div v-if="state.loading" class="mention-picker-loading" data-testid="mention-loading">
      加载中...
    </div>

    <!-- 错误 -->
    <div v-else-if="state.error" class="mention-picker-error" data-testid="mention-error">
      {{ state.error }}
    </div>

    <!-- 空状态 -->
    <div v-else-if="state.members.length === 0" class="mention-picker-empty" data-testid="mention-empty">
      {{ state.searchKeyword ? '无匹配成员' : '暂无成员' }}
    </div>

    <!-- 成员列表 -->
    <div v-else class="mention-picker-list">
      <div
        v-for="(member, index) in state.members"
        :key="member.id"
        class="mention-picker-item"
        :class="{ 'mention-picker-item--active': index === state.selectedIndex }"
        :data-user-id="member.userId"
        @click="$emit('select', member)"
      >
        <a-avatar :src="member.avatar" :size="28">
          {{ getInitial(member.nickname) }}
        </a-avatar>
        <span class="mention-picker-item-name">{{ member.nickname }}</span>
        <a-tag v-if="member.role === 'CREATOR'" color="gold" size="small">创建者</a-tag>
        <a-tag v-else-if="member.role === 'MODERATOR'" color="blue" size="small">版主</a-tag>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import type { MentionMember, MentionState } from '../composables/useMention';

defineProps<{
  state: MentionState;
}>();

const emit = defineEmits<{
  select: [member: MentionMember];
  search: [keyword: string];
}>();

function getInitial(nickname: string): string {
  return nickname?.charAt(0) || '?';
}

function handleSearch(value: string) {
  emit('search', value);
}
</script>

<style lang="less" scoped>
.mention-picker {
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  margin-bottom: 8px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  max-height: 320px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 1000;

  &-search {
    padding: 8px 10px;
    border-bottom: 1px solid #f0f0f0;
  }

  &-loading,
  &-error,
  &-empty {
    padding: 20px 16px;
    text-align: center;
    color: #999;
    font-size: 13px;
  }

  &-error {
    color: #ff4d4f;
  }

  &-list {
    overflow-y: auto;
    flex: 1;
  }

  &-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #f5f5f5;
    }

    &--active {
      background-color: #e6f7ff;
    }

    &-name {
      flex: 1;
      font-size: 14px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}
</style>
