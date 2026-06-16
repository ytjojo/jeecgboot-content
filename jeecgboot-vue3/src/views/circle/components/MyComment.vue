<template>
  <div class="my-comment" ref="containerRef">
    <div class="my-comment-input-wrap">
      <textarea
        ref="textareaRef"
        v-model="commentText"
        :placeholder="placeholder"
        class="my-comment-input"
        :rows="3"
        @input="handleInputChange"
        @keydown="handleKeydown"
      />
      <!-- @mention 成员选择器 -->
      <MentionMemberPicker
        :state="mentionState"
        @select="handleMemberSelected"
        @search="handleSearch"
      />
    </div>
    <div class="my-comment-footer">
      <span class="my-comment-hint">支持 @提及成员</span>
      <a-button type="primary" size="small" :disabled="!canSubmit" @click="handleSubmit">
        {{ submitLabel }}
      </a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, toRef } from 'vue';
import { useMention } from '../composables/useMention';
import type { MentionMember } from '../composables/useMention';
import MentionMemberPicker from './MentionMemberPicker.vue';

const props = withDefaults(defineProps<{
  circleId: string;
  placeholder?: string;
  submitLabel?: string;
}>(), {
  placeholder: '输入评论...',
  submitLabel: '发送',
});

const emit = defineEmits<{
  submit: [content: string];
}>();

const containerRef = ref<HTMLElement | null>(null);
const textareaRef = ref<HTMLTextAreaElement | null>(null);
const commentText = ref('');

// 创建 circleId ref 给 useMention
const circleIdRef = toRef(props, 'circleId');
const { mentionState, onInput, searchMembers, selectMember, closePicker, navigateKeyboard } =
  useMention(circleIdRef);

// 能否提交
const canSubmit = computed(() => commentText.value.trim().length > 0);

// 输入处理：检测 @ 触发
function handleInputChange() {
  const textarea = textareaRef.value;
  if (!textarea) return;
  const cursorPos = textarea.selectionStart;
  onInput(commentText.value, cursorPos);
}

// 键盘处理
function handleKeydown(e: KeyboardEvent) {
  if (mentionState.isOpen) {
    // 浮层打开时，导航键交给 mention
    const navKeys = ['ArrowUp', 'ArrowDown', 'Enter', 'Escape'];
    if (navKeys.includes(e.key)) {
      e.preventDefault();
      if (e.key === 'Enter') {
        // Enter：选择当前高亮成员
        const member = mentionState.members[mentionState.selectedIndex];
        if (member) {
          insertMention(member);
        }
        closePicker();
      } else if (e.key === 'Escape') {
        closePicker();
      } else {
        navigateKeyboard(e.key as 'ArrowUp' | 'ArrowDown' | 'Enter' | 'Escape');
      }
    }
  } else {
    // 浮层关闭时，Enter 提交（Shift+Enter 换行）
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  }
}

// 选择成员回调
function handleMemberSelected(member: MentionMember) {
  insertMention(member);
}

// 插入提及标记到 textarea
function insertMention(member: MentionMember) {
  const textarea = textareaRef.value;
  if (!textarea) return;

  const mentionText = selectMember(member);
  const cursorPos = textarea.selectionStart;
  // 找到最后一个 @ 的位置（在当前光标之前）
  const beforeCursor = commentText.value.slice(0, cursorPos);
  const atIndex = beforeCursor.lastIndexOf('@');

  if (atIndex !== -1) {
    // 替换 @ 及其后面的搜索文本为完整的 mention 标记
    const before = commentText.value.slice(0, atIndex);
    const after = commentText.value.slice(cursorPos);
    commentText.value = before + mentionText + after;
  } else {
    // 没有找到 @，在光标位置插入
    commentText.value =
      commentText.value.slice(0, cursorPos) + mentionText + commentText.value.slice(cursorPos);
  }

  // 恢复焦点和光标位置
  textarea.focus();
  const newCursorPos = (atIndex !== -1 ? atIndex : cursorPos) + mentionText.length;
  textarea.setSelectionRange(newCursorPos, newCursorPos);
}

// 搜索处理
function handleSearch(keyword: string) {
  searchMembers(keyword);
}

// 提交
function handleSubmit() {
  const content = commentText.value.trim();
  if (!content) return;

  emit('submit', content);
  commentText.value = '';
  closePicker();
}
</script>

<style lang="less" scoped>
.my-comment {
  background: #fff;
  border-radius: 8px;
  padding: 12px;

  &-input-wrap {
    position: relative;
  }

  &-input {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    font-size: 14px;
    line-height: 1.6;
    resize: vertical;
    outline: none;
    font-family: inherit;

    &:focus {
      border-color: #1677ff;
      box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
    }
  }

  &-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 8px;
  }

  &-hint {
    font-size: 12px;
    color: #999;
  }
}
</style>
