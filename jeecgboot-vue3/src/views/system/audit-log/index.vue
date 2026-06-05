<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <span class="table-title">审计日志</span>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="system-audit-log" setup>
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { listAuditLog } from '/@/api/content/governance';
  import { columns, searchFormSchema } from './audit-log.data';

  const { tableContext } = useListPage({
    designScope: 'audit-log-template',
    tableProps: {
      title: '审计日志列表',
      api: listAuditLog,
      columns,
      formConfig: {
        labelWidth: 80,
        rowProps: { gutter: 24 },
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      rowSelection: null,
      defSort: {
        column: 'createTime',
        order: 'desc',
      },
    },
  });

  const [registerTable] = tableContext;
</script>

<style scoped>
  .table-title {
    font-size: 16px;
    font-weight: 600;
  }
</style>
