<template>
  <div class="circle-analytics-page">
    <a-spin :spinning="checking" tip="验证权限中...">
      <a-result
        v-if="permissionError === 'forbidden'"
        status="403"
        title="无访问权限"
        sub-title="仅创建者和版主可查看数据统计"
      >
        <template #extra>
          <a-button type="primary" @click="goBack">返回圈子</a-button>
        </template>
      </a-result>

      <a-result
        v-else-if="permissionError === 'not-found'"
        status="404"
        title="圈子不存在"
        sub-title="请检查链接是否正确"
      >
        <template #extra>
          <a-button type="primary" @click="goList">返回圈子列表</a-button>
        </template>
      </a-result>

      <DataAnalyticsPanel v-else-if="hasPermission" />
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getCircleDetail } from '/@/api/content/circle';
import DataAnalyticsPanel from './components/DataAnalyticsPanel.vue';

const route = useRoute();
const router = useRouter();

const checking = ref(true);
const hasPermission = ref(false);
const permissionError = ref<string | null>(null);

function isForbiddenError(e: any): boolean {
  return e?.code === 403001 || e?.response?.data?.code === 403001;
}

function isNotFoundError(e: any): boolean {
  return e?.code === 404001 || e?.code === 404002 || e?.response?.data?.code === 404001 || e?.response?.data?.code === 404002;
}

async function checkPermission() {
  const circleId = route.params.id as string;
  if (!circleId) {
    permissionError.value = 'not-found';
    checking.value = false;
    return;
  }

  checking.value = true;
  permissionError.value = null;
  try {
    await getCircleDetail(circleId);
    hasPermission.value = true;
  } catch (e: any) {
    hasPermission.value = false;
    if (isForbiddenError(e)) {
      permissionError.value = 'forbidden';
    } else if (isNotFoundError(e)) {
      permissionError.value = 'not-found';
    } else {
      permissionError.value = 'forbidden';
    }
  } finally {
    checking.value = false;
  }
}

function goBack() {
  const circleId = route.params.id as string;
  router.push(`/circle/${circleId}`);
}

function goList() {
  router.push('/circle/list');
}

onMounted(() => {
  checkPermission();
});
</script>

<style lang="less" scoped>
.circle-analytics-page {
  min-height: calc(100vh - 64px);
  background: #f0f2f5;
}
</style>
