<template>
  <BasicModal v-bind="$attrs" title="审计日志详情" :width="600" :footer="null">
    <a-descriptions :column="2" bordered size="small">
      <a-descriptions-item label="日志ID">{{ detail.id }}</a-descriptions-item>
      <a-descriptions-item label="用户ID">{{ detail.userId }}</a-descriptions-item>
      <a-descriptions-item label="用户名">{{ detail.userName }}</a-descriptions-item>
      <a-descriptions-item label="操作类型">{{ detail.action }}</a-descriptions-item>
      <a-descriptions-item label="原状态">
        <StatusTag v-if="detail.fromStatus" :status="detail.fromStatus" />
      </a-descriptions-item>
      <a-descriptions-item label="目标状态">
        <StatusTag v-if="detail.toStatus" :status="detail.toStatus" />
      </a-descriptions-item>
      <a-descriptions-item label="原因" :span="2">{{ detail.reason }}</a-descriptions-item>
      <a-descriptions-item label="操作人ID">{{ detail.operatorId }}</a-descriptions-item>
      <a-descriptions-item label="操作人">{{ detail.operatorName }}</a-descriptions-item>
      <a-descriptions-item label="操作类型">{{ detail.operatorType }}</a-descriptions-item>
      <a-descriptions-item label="IP地址">{{ detail.ipAddress }}</a-descriptions-item>
      <a-descriptions-item label="操作时间" :span="2">{{ detail.createdAt }}</a-descriptions-item>
      <a-descriptions-item v-if="detail.remark" label="备注" :span="2">{{ detail.remark }}</a-descriptions-item>
    </a-descriptions>
  </BasicModal>
</template>

<script lang="ts" setup>
import { BasicModal } from '/@/components/Modal';
import StatusTag from './StatusTag.vue';
import type { AuditLogDetail as AuditLogDetailType } from '/@/api/content/model/userStatusModel';

defineProps({
  detail: {
    type: Object as PropType<AuditLogDetailType>,
    required: true,
  },
});
</script>
