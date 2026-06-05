<template>
  <div class="mute-list-page">
    <a-page-header title="屏蔽列表" @back="router.back()" />

    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="user" tab="屏蔽用户">
        <a-list :data-source="userList" :loading="userLoading" :pagination="userPagination" item-layout="horizontal">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #avatar>
                  <a-avatar :src="item.avatar">{{ item.nickname?.charAt(0) }}</a-avatar>
                </template>
                <template #title>{{ item.nickname }}</template>
                <template #description>屏蔽于 {{ item.mutedAt }}</template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" @click="handleUnmuteUser(item)">取消屏蔽</a-button>
              </template>
            </a-list-item>
          </template>
          <template #empty><a-empty description="暂无屏蔽用户" /></template>
        </a-list>
      </a-tab-pane>

      <a-tab-pane key="topic" tab="屏蔽话题">
        <a-list :data-source="topicList" :loading="topicLoading" item-layout="horizontal">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #title>
                  <a-tag color="blue">{{ item.value }}</a-tag>
                </template>
                <template #description>屏蔽于 {{ item.createdAt }}</template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" @click="handleDeleteRule(item)">取消屏蔽</a-button>
              </template>
            </a-list-item>
          </template>
          <template #empty><a-empty description="暂无屏蔽话题" /></template>
        </a-list>
      </a-tab-pane>

      <a-tab-pane key="contentType" tab="屏蔽内容类型">
        <a-list :data-source="contentTypeList" :loading="contentTypeLoading" item-layout="horizontal">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #title>
                  <a-tag color="orange">{{ item.value }}</a-tag>
                </template>
                <template #description>屏蔽于 {{ item.createdAt }}</template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" @click="handleDeleteRule(item)">取消屏蔽</a-button>
              </template>
            </a-list-item>
          </template>
          <template #empty><a-empty description="暂无屏蔽内容类型" /></template>
        </a-list>
      </a-tab-pane>

      <a-tab-pane key="temporary" tab="临时屏蔽">
        <a-list :data-source="tempList" :loading="tempLoading" item-layout="horizontal">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #title>{{ item.value }}</template>
                <template #description>
                  <span v-if="item.expiresAt">到期时间：{{ item.expiresAt }}</span>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="link" @click="handleDeleteRule(item)">提前取消</a-button>
              </template>
            </a-list-item>
          </template>
          <template #empty><a-empty description="暂无临时屏蔽" /></template>
        </a-list>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { getMuteList, unmuteUser } from '/@/api/content/mute';
import { getFilterRuleList, deleteFilterRule } from '/@/api/content/filterRule';
import type { MuteListItemVO } from '/@/api/content/mute';
import type { FilterRuleItemVO } from '/@/api/content/filterRule';

const router = useRouter();

const activeTab = ref('user');

// Users
const userList = ref<MuteListItemVO[]>([]);
const userLoading = ref(false);
const userPagination = reactive({ current: 1, pageSize: 20, total: 0, onChange: (p: number) => { userPagination.current = p; loadUsers(); } });

// Topics
const topicList = ref<FilterRuleItemVO[]>([]);
const topicLoading = ref(false);

// Content types
const contentTypeList = ref<FilterRuleItemVO[]>([]);
const contentTypeLoading = ref(false);

// Temporary
const tempList = ref<FilterRuleItemVO[]>([]);
const tempLoading = ref(false);

async function getCurrentUserId(): Promise<string> {
  const { useUserStore } = await import('/@/store/modules/user');
  const userStore = useUserStore();
  return String((userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '');
}

async function loadUsers() {
  userLoading.value = true;
  try {
    const userId = await getCurrentUserId();
    const res = await getMuteList(userId, userPagination.current, userPagination.pageSize);
    userList.value = res.records;
    userPagination.total = res.total;
  } catch { /* ignore */ } finally { userLoading.value = false; }
}

async function loadTopics() {
  topicLoading.value = true;
  try {
    const userId = await getCurrentUserId();
    const res = await getFilterRuleList(userId, 'TOPIC');
    topicList.value = res.records;
  } catch { /* ignore */ } finally { topicLoading.value = false; }
}

async function loadContentTypes() {
  contentTypeLoading.value = true;
  try {
    const userId = await getCurrentUserId();
    const res = await getFilterRuleList(userId, 'CONTENT_TYPE');
    contentTypeList.value = res.records;
  } catch { /* ignore */ } finally { contentTypeLoading.value = false; }
}

async function loadTemporary() {
  tempLoading.value = true;
  try {
    const userId = await getCurrentUserId();
    const res = await getFilterRuleList(userId, 'TEMPORARY');
    tempList.value = res.records;
  } catch { /* ignore */ } finally { tempLoading.value = false; }
}

async function handleUnmuteUser(item: MuteListItemVO) {
  try {
    const userId = await getCurrentUserId();
    await unmuteUser(userId, item.userId);
    message.success('已取消屏蔽');
    userList.value = userList.value.filter(i => i.userId !== item.userId);
  } catch (e: any) { message.error(e?.message || '操作失败'); }
}

async function handleDeleteRule(item: FilterRuleItemVO) {
  try {
    const userId = await getCurrentUserId();
    await deleteFilterRule(userId, item.ruleId);
    message.success('已取消屏蔽');
    topicList.value = topicList.value.filter(i => i.ruleId !== item.ruleId);
    contentTypeList.value = contentTypeList.value.filter(i => i.ruleId !== item.ruleId);
    tempList.value = tempList.value.filter(i => i.ruleId !== item.ruleId);
  } catch (e: any) { message.error(e?.message || '操作失败'); }
}

watch(activeTab, (tab) => {
  if (tab === 'user') loadUsers();
  else if (tab === 'topic') loadTopics();
  else if (tab === 'contentType') loadContentTypes();
  else if (tab === 'temporary') loadTemporary();
}, { immediate: true });
</script>

<style scoped>
.mute-list-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 16px;
}
</style>
