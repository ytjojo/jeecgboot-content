<template>
  <div class="group-manage-page">
    <div class="group-manage-page__header">
      <h2 class="group-manage-page__title">分组管理</h2>
      <a-button type="primary" @click="showCreateModal = true">新建分组</a-button>
    </div>

    <a-spin :spinning="loading">
      <div v-if="followStore.followGroups.length > 0" class="group-manage-page__list">
        <div v-for="group in followStore.followGroups" :key="group.id" class="group-manage-page__item">
          <div class="group-manage-page__item-info">
            <span class="group-manage-page__item-name">{{ group.name }}</span>
            <span class="group-manage-page__item-count">{{ group.memberCount }} 人</span>
            <a-tag v-if="group.isDefault" color="default">默认</a-tag>
          </div>
          <div class="group-manage-page__item-actions">
            <a-button type="link" size="small" @click="startRename(group)">重命名</a-button>
            <a-popconfirm
              v-if="!group.isDefault"
              title="确定删除该分组吗？分组内用户不会被取消关注。"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDeleteGroup(group.id)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </div>
        </div>
      </div>
      <a-empty v-else-if="!loading" description="暂无分组" />
    </a-spin>

    <!-- 创建分组弹窗 -->
    <a-modal v-model:open="showCreateModal" title="新建分组" :width="isMobile ? '95vw' : 520" :confirm-loading="createLoading" @ok="handleCreateGroup" @cancel="resetCreateForm">
      <a-form :label-col="{ span: 4 }">
        <a-form-item label="名称">
          <a-input v-model:value="newGroupName" placeholder="请输入分组名称" :maxlength="20" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 重命名弹窗 -->
    <a-modal v-model:open="showRenameModal" title="重命名分组" :width="isMobile ? '95vw' : 520" :confirm-loading="renameLoading" @ok="handleRenameGroup" @cancel="resetRenameForm">
      <a-form :label-col="{ span: 4 }">
        <a-form-item label="名称">
          <a-input v-model:value="renameValue" placeholder="请输入新名称" :maxlength="20" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useFollowStore } from '/@/store/modules/follow';
import { useUserStore } from '/@/store/modules/user';
import { useBreakpoint } from '/@/hooks/event/useBreakpoint';
import type { FollowGroup } from '/@/store/modules/follow';

const followStore = useFollowStore();
const userStore = useUserStore();
const { screenRef } = useBreakpoint();
const isMobile = computed(() => screenRef.value === 'XS' || screenRef.value === 'SM');

const currentUserId = computed(() => userStore.getUserInfo?.userId ?? '');
const loading = ref(false);

// 创建分组
const showCreateModal = ref(false);
const newGroupName = ref('');
const createLoading = ref(false);

// 重命名分组
const showRenameModal = ref(false);
const renameValue = ref('');
const renamingGroup = ref<FollowGroup | null>(null);
const renameLoading = ref(false);

function startRename(group: FollowGroup) {
  renamingGroup.value = group;
  renameValue.value = group.name;
  showRenameModal.value = true;
}

async function handleCreateGroup() {
  const name = newGroupName.value.trim();
  if (!name) {
    message.warning('请输入分组名称');
    return;
  }
  createLoading.value = true;
  try {
    await followStore.createGroup(currentUserId.value, name);
    message.success('分组创建成功');
    showCreateModal.value = false;
    newGroupName.value = '';
  } catch (error) {
    console.error('[GroupManage] create group failed:', error);
    message.error('创建分组失败');
  } finally {
    createLoading.value = false;
  }
}

async function handleRenameGroup() {
  const name = renameValue.value.trim();
  if (!name) {
    message.warning('请输入分组名称');
    return;
  }
  if (!renamingGroup.value) return;
  renameLoading.value = true;
  try {
    await followStore.updateGroup(currentUserId.value, renamingGroup.value.id, name);
    message.success('重命名成功');
    showRenameModal.value = false;
    renamingGroup.value = null;
    renameValue.value = '';
  } catch (error) {
    console.error('[GroupManage] rename group failed:', error);
    message.error('重命名失败');
  } finally {
    renameLoading.value = false;
  }
}

async function handleDeleteGroup(groupId: string) {
  try {
    await followStore.removeGroup(currentUserId.value, groupId);
    message.success('分组已删除');
  } catch (error) {
    console.error('[GroupManage] delete group failed:', error);
    message.error('删除分组失败');
  }
}

function resetCreateForm() {
  newGroupName.value = '';
}

function resetRenameForm() {
  renameValue.value = '';
  renamingGroup.value = null;
}

onMounted(async () => {
  loading.value = true;
  try {
    await followStore.fetchFollowGroups(currentUserId.value).catch(console.error);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped lang="less">
.group-manage-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 16px;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  &__list {
    border: 1px solid #f0f0f0;
    border-radius: 4px;
  }

  &__item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  &__item-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__item-name {
    font-size: 14px;
    font-weight: 500;
  }

  &__item-count {
    font-size: 13px;
    color: #999;
  }

  &__item-actions {
    display: flex;
    gap: 4px;
  }

  @media (max-width: 767px) {
    padding: 16px 12px;

    &__header {
      flex-wrap: wrap;
      gap: 12px;
    }

    &__item {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }

    &__item-actions {
      align-self: flex-end;
    }
  }
}
</style>
