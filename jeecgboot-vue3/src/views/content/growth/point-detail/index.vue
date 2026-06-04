<template>
  <div class="point-detail-page">
    <a-page-header title="积分明细" />

    <!-- 积分余额卡片 -->
    <PointBalance :balance="balance" :loading="balanceLoading" style="margin-bottom: 16px" />

    <!-- 筛选区域 -->
    <div class="point-detail-page__filter">
      <a-select
        v-model:value="filterChangeType"
        placeholder="变动类型"
        allow-clear
        style="width: 160px"
        @change="onFilterChange"
      >
        <a-select-option value="EARN">获取</a-select-option>
        <a-select-option value="SPEND">消耗</a-select-option>
        <a-select-option value="EXPIRE">过期</a-select-option>
        <a-select-option value="GIFT">赠送</a-select-option>
      </a-select>
      <a-range-picker v-model:value="filterDateRange" style="width: 280px" @change="onFilterChange" />
    </div>

    <!-- 积分明细表格 -->
    <a-table
      :columns="columns"
      :data-source="ledgerData"
      :loading="tableLoading"
      :pagination="pagination"
      row-key="ledgerId"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'amount'">
          <span :style="{ color: record.amount > 0 ? '#52c41a' : '#f5222d' }">
            {{ record.amount > 0 ? '+' : '' }}{{ record.amount }}
          </span>
        </template>
        <template v-if="column.dataIndex === 'createdAt'">
          {{ record.createdAt }}
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import type { Dayjs } from 'dayjs';
  import dayjs from 'dayjs';
  import { PointBalance } from '/@/components/content/PointBalance';
  import { usePointStore } from '/@/store/modules/point';
  import { useGrowthStore } from '/@/store/modules/growth';
  import type { PointLedgerVO } from '/@/api/content/growth/point-types';

  type RangeValue = [Dayjs, Dayjs];

  const pointStore = usePointStore();
  const growthStore = useGrowthStore();

  // 余额
  const balance = ref(0);
  const balanceLoading = ref(false);

  // 筛选
  const filterChangeType = ref<string | undefined>(undefined);
  const filterDateRange = ref<RangeValue | null>(null);

  // 表格
  const tableLoading = ref(false);
  const ledgerData = ref<PointLedgerVO[]>([]);
  const pagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  });

  // 防抖定时器
  let debounceTimer: ReturnType<typeof setTimeout> | null = null;

  const columns = [
    { title: '类型', dataIndex: 'changeType', width: 100 },
    { title: '变动积分', dataIndex: 'amount', width: 120, align: 'center' as const },
    { title: '余额', dataIndex: 'balance', width: 120, align: 'center' as const },
    { title: '说明', dataIndex: 'remark', ellipsis: true },
    { title: '时间', dataIndex: 'createdAt', width: 180 },
  ];

  /** 加载余额 */
  async function loadBalance() {
    balanceLoading.value = true;
    try {
      await growthStore.loadSummary();
      balance.value = growthStore.pointBalance;
      pointStore.setBalance(balance.value);
    } finally {
      balanceLoading.value = false;
    }
  }

  /** 加载积分明细 */
  async function loadLedger() {
    tableLoading.value = true;
    try {
      const params = {
        page: pagination.current,
        pageSize: pagination.pageSize,
        changeType: filterChangeType.value || undefined,
        startTime: filterDateRange.value ? filterDateRange.value[0].format('YYYY-MM-DD 00:00:00') : undefined,
        endTime: filterDateRange.value ? filterDateRange.value[1].format('YYYY-MM-DD 23:59:59') : undefined,
      };
      const data = await pointStore.loadLedger(params);
      ledgerData.value = data;
      // 如果 API 返回分页信息，可在此解析；否则使用数组长度
      pagination.total = data.length;
    } finally {
      tableLoading.value = false;
    }
  }

  /** 筛选变更 → 防抖 300ms → 重新加载 */
  function onFilterChange() {
    if (debounceTimer) clearTimeout(debounceTimer);
    pagination.current = 1;
    debounceTimer = setTimeout(() => {
      loadLedger();
    }, 300);
  }

  /** 分页变更 */
  function handleTableChange(pag: { current?: number; pageSize?: number }) {
    pagination.current = pag.current ?? 1;
    pagination.pageSize = pag.pageSize ?? 20;
    loadLedger();
  }

  onMounted(() => {
    loadBalance();
    loadLedger();
  });
</script>

<style scoped lang="less">
  .point-detail-page {
    padding: 16px;

    &__filter {
      display: flex;
      gap: 12px;
      margin-bottom: 16px;
      flex-wrap: wrap;
    }
  }
</style>
