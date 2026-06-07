<!-- jeecgboot-vue3/src/views/channel/members/MemberList.vue -->
<template>
  <div class="member-list">
    <div class="page-header">
      <h3>成员管理 <span class="count">共 {{ total }} 位成员</span></h3>
    </div>

    <div class="filter-bar">
      <Space>
        <Select v-model:value="roleFilter" placeholder="角色筛选" style="width: 120px" @change="loadData" allowClear>
          <Select.Option value="">全部</Select.Option>
          <Select.Option value="OWNER">频道主</Select.Option>
          <Select.Option value="ADMIN">管理员</Select.Option>
          <Select.Option value="EDITOR">内容编辑</Select.Option>
          <Select.Option value="MEMBER">普通成员</Select.Option>
        </Select>
        <Input.Search v-model:value="searchKeyword" placeholder="搜索成员" style="width: 200px" @search="loadData" @change="onSearchChange" />
        <Select v-model:value="sortOrder" style="width: 150px" @change="loadData">
          <Select.Option value="desc">加入时间倒序</Select.Option>
          <Select.Option value="asc">加入时间正序</Select.Option>
        </Select>
      </Space>
      <div v-if="selectedRowKeys.length > 0" class="batch-actions">
        <span>已选 {{ selectedRowKeys.length }} 项</span>
        <Button type="link" danger @click="handleBatchRemove">批量移除</Button>
        <Button type="link" @click="handleBatchMute">批量禁言</Button>
      </div>
    </div>

    <Table
      :dataSource="memberList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :rowSelection="{ selectedRowKeys, onChange: onSelectChange }"
      rowKey="id"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'member'">
          <Space>
            <Avatar :src="record.avatar" size="small" />
            <span>{{ record.nickname }}</span>
          </Space>
        </template>
        <template v-if="column.dataIndex === 'role'">
          <Tag :color="getRoleColor(record.role)">{{ getRoleText(record.role) }}</Tag>
        </template>
        <template v-if="column.dataIndex === 'governanceStatus'">
          <Tag v-if="record.isMuted" color="orange">已禁言 {{ record.muteEndTime }}</Tag>
          <Tag v-else-if="record.coolingEndTime" color="default">冷却期中</Tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <Dropdown v-if="canOperate(record)">
            <Button type="link" size="small">操作</Button>
            <template #overlay>
              <Menu @click="({ key }) => handleAction(key, record)">
                <Menu.Item v-if="canChangeRole" key="changeRole">修改角色</Menu.Item>
                <Menu.Item key="remove">移除</Menu.Item>
                <Menu.Item key="mute">禁言</Menu.Item>
                <Menu.Item key="blacklist">加入黑名单</Menu.Item>
              </Menu>
            </template>
          </Dropdown>
        </template>
      </template>
    </Table>

    <RoleAssignModal ref="roleAssignModalRef" :channelId="channelId" @updated="loadData" />
    <RemoveMemberModal ref="removeMemberModalRef" :channelId="channelId" @removed="loadData" />
    <MuteModal ref="muteModalRef" :channelId="channelId" @muted="loadData" />
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { Table, Button, Space, Avatar, Tag, Select, Input, Dropdown, Menu } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getMemberList } from '/@/api/content/channelMember';
  import RoleAssignModal from './RoleAssignModal.vue';
  import RemoveMemberModal from './RemoveMemberModal.vue';
  import MuteModal from './MuteModal.vue';

  const props = defineProps<{
    channelId: string;
    currentRole: string; // 当前用户在频道中的角色
  }>();

  const { createMessage } = useMessage();
  const loading = ref(false);
  const memberList = ref<any[]>([]);
  const total = ref(0);
  const selectedRowKeys = ref<string[]>([]);
  const roleFilter = ref('');
  const searchKeyword = ref('');
  const sortOrder = ref('desc');
  const pagination = reactive({ current: 1, pageSize: 20, total: 0 });

  const roleAssignModalRef = ref();
  const removeMemberModalRef = ref();
  const muteModalRef = ref();

  const canChangeRole = props.currentRole === 'OWNER';

  const columns = [
    { title: '成员', dataIndex: 'member', key: 'member', width: 200 },
    { title: '角色', dataIndex: 'role', key: 'role', width: 120 },
    { title: '加入时间', dataIndex: 'joinTime', key: 'joinTime', width: 180 },
    { title: '贡献数', dataIndex: 'contribution', key: 'contribution', width: 100 },
    { title: '治理状态', dataIndex: 'governanceStatus', key: 'governanceStatus', width: 150 },
    { title: '操作', dataIndex: 'action', key: 'action', width: 100 },
  ];

  function getRoleColor(role: string) {
    const map: Record<string, string> = { OWNER: 'purple', ADMIN: 'blue', EDITOR: 'green', MEMBER: 'default' };
    return map[role] || 'default';
  }

  function getRoleText(role: string) {
    const map: Record<string, string> = { OWNER: '频道主', ADMIN: '管理员', EDITOR: '内容编辑', MEMBER: '普通成员' };
    return map[role] || role;
  }

  function canOperate(record: any) {
    if (record.role === 'OWNER') return false;
    if (props.currentRole === 'EDITOR' || props.currentRole === 'MEMBER') return false;
    return true;
  }

  let searchTimer: any;
  function onSearchChange() {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(loadData, 300);
  }

  async function loadData() {
    loading.value = true;
    try {
      const res = await getMemberList({
        channelId: props.channelId,
        role: roleFilter.value || undefined,
        keyword: searchKeyword.value || undefined,
        sort: sortOrder.value,
        pageNo: pagination.current,
        pageSize: pagination.pageSize,
      });
      memberList.value = res.records || res;
      total.value = res.total || memberList.value.length;
      pagination.total = total.value;
    } catch (error: any) {
      if (error?.response?.status === 404) {
        memberList.value = [];
        total.value = 0;
        createMessage.warning('频道不存在或已被删除');
      } else {
        createMessage.error('加载成员列表失败，请重试');
      }
    } finally {
      loading.value = false;
    }
  }

  function onSelectChange(keys: string[]) {
    selectedRowKeys.value = keys;
  }

  function handleTableChange(pag: any) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  function handleAction(key: string, record: any) {
    switch (key) {
      case 'changeRole':
        roleAssignModalRef.value.open(record);
        break;
      case 'remove':
        removeMemberModalRef.value.open([{ id: record.id, nickname: record.nickname }]);
        break;
      case 'mute':
        muteModalRef.value.open({ id: record.id, nickname: record.nickname });
        break;
      case 'blacklist':
        // TODO: implement blacklist from member list
        break;
    }
  }

  function handleBatchRemove() {
    const members = memberList.value
      .filter((m) => selectedRowKeys.value.includes(m.id))
      .map((m) => ({ id: m.id, nickname: m.nickname }));
    removeMemberModalRef.value.open(members);
  }

  const batchOperating = ref(false);

  function handleBatchMute() {
    if (batchOperating.value) return;
    // TODO: implement batch mute — when implemented, wrap in batchOperating guard:
    // batchOperating.value = true;
    // try { ... } finally { batchOperating.value = false; }
  }

  onMounted(loadData);
</script>

<style scoped>
.member-list { padding: 16px; }
.page-header { margin-bottom: 16px; }
.count { font-size: 14px; color: #999; font-weight: normal; }
.filter-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.batch-actions { display: flex; align-items: center; gap: 8px; }
</style>
