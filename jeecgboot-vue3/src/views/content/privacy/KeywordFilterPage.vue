<template>
  <div class="keyword-filter-page">
    <a-page-header title="屏蔽词设置" @back="router.back()" />

    <div class="add-section">
      <a-input-group compact>
        <a-select v-model:value="addType" style="width: 120px">
          <a-select-option value="KEYWORD">关键词</a-select-option>
          <a-select-option value="REGEX">正则表达式</a-select-option>
        </a-select>
        <a-input
          v-model:value="addValue"
          :placeholder="addType === 'REGEX' ? '输入正则，如 /广告/' : '输入关键词'"
          style="width: 260px"
          @pressEnter="handleAdd"
        />
        <a-button type="primary" @click="handleAdd" :disabled="!addValue.trim()">添加</a-button>
      </a-input-group>
      <div v-if="addType === 'REGEX'" class="regex-hint">
        正则表达式格式：以 / 开头和结尾，如 /广告|推广/
      </div>
      <div v-if="list.length >= 100" class="limit-hint">
        屏蔽词数量已达上限（100个）
      </div>
    </div>

    <a-list :data-source="list" :loading="loading" item-layout="horizontal" class="keyword-list">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta>
            <template #title>
              <span>{{ item.value }}</span>
              <a-tag :color="item.ruleType === 'REGEX' ? 'purple' : 'blue'" style="margin-left: 8px">
                {{ item.ruleType === 'REGEX' ? '正则' : '关键词' }}
              </a-tag>
            </template>
            <template #description>添加于 {{ item.createdAt }}</template>
          </a-list-item-meta>
          <template #actions>
            <a-button type="link" danger @click="handleDelete(item)">删除</a-button>
          </template>
        </a-list-item>
      </template>
      <template #empty><a-empty description="暂无屏蔽词" /></template>
    </a-list>

    <!-- 撤销提示条 -->
    <div v-if="undoVisible" class="undo-bar">
      <span>已删除屏蔽词「{{ undoItem?.value }}」</span>
      <a-button type="link" @click="handleUndo">撤销</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { getFilterRuleList, addFilterRule, deleteFilterRule } from '/@/api/content/filterRule';
import type { FilterRuleItemVO } from '/@/api/content/filterRule';

const router = useRouter();

const loading = ref(false);
const list = ref<FilterRuleItemVO[]>([]);
const addType = ref('KEYWORD');
const addValue = ref('');

// Undo state
const undoVisible = ref(false);
const undoItem = ref<FilterRuleItemVO | null>(null);
let undoTimer: ReturnType<typeof setTimeout> | null = null;

async function getCurrentUserId(): Promise<string> {
  const { useUserStore } = await import('/@/store/modules/user');
  const userStore = useUserStore();
  return String((userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '');
}

async function loadList() {
  loading.value = true;
  try {
    const userId = await getCurrentUserId();
    const res = await getFilterRuleList(userId, undefined, 1, 100);
    list.value = res.records;
  } catch { /* ignore */ } finally { loading.value = false; }
}

async function handleAdd() {
  const val = addValue.value.trim();
  if (!val) return;

  if (list.value.length >= 100) {
    message.warning('屏蔽词数量已达上限');
    return;
  }

  // Duplicate check
  if (list.value.some(item => item.value === val && item.ruleType === addType.value)) {
    message.warning('该屏蔽词已存在');
    return;
  }

  // Regex validation
  if (addType.value === 'REGEX') {
    if (!/^\/.+\/$/.test(val)) {
      message.error('正则表达式格式错误，请检查');
      return;
    }
  }

  try {
    const userId = await getCurrentUserId();
    await addFilterRule(userId, addType.value, val);
    message.success('已添加');
    addValue.value = '';
    await loadList();
  } catch (e: any) {
    message.error(e?.message || '添加失败');
  }
}

async function handleDelete(item: FilterRuleItemVO) {
  try {
    const userId = await getCurrentUserId();
    await deleteFilterRule(userId, item.ruleId);
    list.value = list.value.filter(i => i.ruleId !== item.ruleId);

    // Show undo bar
    undoItem.value = item;
    undoVisible.value = true;
    if (undoTimer) clearTimeout(undoTimer);
    undoTimer = setTimeout(() => {
      undoVisible.value = false;
      undoItem.value = null;
    }, 3000);
  } catch (e: any) {
    message.error(e?.message || '删除失败');
  }
}

async function handleUndo() {
  if (!undoItem.value) return;
  try {
    const userId = await getCurrentUserId();
    await addFilterRule(userId, undoItem.value.ruleType, undoItem.value.value);
    message.success('已恢复');
    undoVisible.value = false;
    undoItem.value = null;
    if (undoTimer) clearTimeout(undoTimer);
    await loadList();
  } catch (e: any) {
    message.error(e?.message || '恢复失败');
  }
}

onMounted(loadList);
</script>

<style scoped>
.keyword-filter-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
}
.add-section {
  margin-bottom: 24px;
}
.regex-hint {
  margin-top: 8px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
.limit-hint {
  margin-top: 8px;
  color: #faad14;
  font-size: 12px;
}
.undo-bar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: #333;
  color: #fff;
  padding: 12px 24px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 1000;
}
</style>
