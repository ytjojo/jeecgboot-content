<template>
  <div class="badge-manage">
    <a-page-header title="勋章管理" :back-icon="true" @back="$router.back()" />

    <!-- 搜索表单 -->
    <a-form layout="inline" class="badge-manage__search" @finish="onSearch">
      <a-form-item>
        <a-input
          v-model:value="query.userId"
          placeholder="用户 ID"
          allow-clear
          style="width: 180px"
        />
      </a-form-item>
      <a-form-item>
        <a-input
          v-model:value="query.badgeName"
          placeholder="勋章名称"
          allow-clear
          style="width: 180px"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="searching">查询</a-button>
      </a-form-item>
    </a-form>

    <!-- 数据表格 -->
    <a-table
      :columns="columns"
      :data-source="tableData"
      :loading="searching"
      :pagination="pagination"
      row-key="id"
      @change="onTableChange"
      class="badge-manage__table"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'action'">
          <a-popconfirm
            title="确认回收此勋章？"
            ok-text="确认"
            cancel-text="取消"
            @confirm="onRecycle(record)"
          >
            <a-button type="link" danger size="small">回收</a-button>
          </a-popconfirm>
        </template>
      </template>
    </a-table>

    <!-- 回收原因弹窗 -->
    <a-modal
      v-model:visible="recycleModalVisible"
      title="回收勋章"
      ok-text="确认回收"
      cancel-text="取消"
      :confirm-loading="recycling"
      @ok="onConfirmRecycle"
    >
      <a-form layout="vertical">
        <a-form-item label="回收原因" required>
          <a-textarea
            v-model:value="recycleReason"
            placeholder="请输入回收原因（必填）"
            :rows="3"
            :maxlength="200"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import type { AdminBadgeVO, AdminBadgeQuery } from '/@/api/content/growth/badge-types';
  import { listAdminBadges, recycleBadge } from '/@/api/content/growth/badge';

  const columns = [
    { title: '用户 ID', dataIndex: 'userId', width: 180 },
    { title: '勋章名称', dataIndex: 'badgeName', width: 160 },
    { title: '获得时间', dataIndex: 'earnedAt', width: 180 },
    { title: '状态', dataIndex: 'status', width: 100 },
    { title: '操作', dataIndex: 'action', width: 100, fixed: 'right' as const },
  ];

  const query = reactive<AdminBadgeQuery>({
    userId: '',
    badgeName: '',
    pageNo: 1,
    pageSize: 10,
  });

  const tableData = ref<AdminBadgeVO[]>([]);
  const searching = ref(false);
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  });

  // 回收相关
  const recycleModalVisible = ref(false);
  const recycleReason = ref('');
  const recycling = ref(false);
  const recycleTarget = ref<AdminBadgeVO | null>(null);

  onMounted(() => {
    loadData();
  });

  async function loadData() {
    searching.value = true;
    try {
      query.pageNo = pagination.current;
      query.pageSize = pagination.pageSize;
      const res = await listAdminBadges(query);
      tableData.value = res.records || [];
      pagination.total = res.total || 0;
    } catch (e: any) {
      message.error(e?.message || '查询失败');
    } finally {
      searching.value = false;
    }
  }

  function onSearch() {
    pagination.current = 1;
    loadData();
  }

  function onTableChange(pag: any) {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadData();
  }

  function onRecycle(record: AdminBadgeVO) {
    recycleTarget.value = record;
    recycleReason.value = '';
    recycleModalVisible.value = true;
  }

  async function onConfirmRecycle() {
    if (!recycleReason.value.trim()) {
      message.warning('请输入回收原因');
      return;
    }
    if (!recycleTarget.value) return;

    recycling.value = true;
    try {
      await recycleBadge({
        userId: recycleTarget.value.userId,
        badgeId: recycleTarget.value.badgeId,
        reason: recycleReason.value.trim(),
      });
      message.success('已回收');
      recycleModalVisible.value = false;
      loadData();
    } catch (e: any) {
      message.error(e?.message || '回收失败');
    } finally {
      recycling.value = false;
    }
  }
</script>

<style scoped>
  .badge-manage {
    padding: 0 16px 24px;
  }
  .badge-manage__search {
    margin-bottom: 16px;
  }
  .badge-manage__table {
    margin-top: 8px;
  }
</style>
