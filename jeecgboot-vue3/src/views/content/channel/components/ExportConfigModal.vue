<template>
  <a-modal
    v-model:open="visible"
    title="导出数据"
    :confirmLoading="loading"
    okText="开始导出"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <div class="export-config-modal">
      <!-- 格式选择 -->
      <a-form-item label="导出格式" required>
        <a-radio-group v-model:value="format">
          <a-radio value="EXCEL">Excel (.xlsx)</a-radio>
          <a-radio value="CSV">CSV (.csv)</a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 时间范围 -->
      <a-form-item label="时间范围">
        <a-range-picker
          v-model:value="dateRange"
          :presets="datePresets"
          style="width: 100%"
        />
      </a-form-item>

      <!-- 字段选择 -->
      <a-form-item label="导出字段">
        <a-checkbox-group v-model:value="selectedFields">
          <a-row :gutter="[8, 8]">
            <a-col :span="8" v-for="field in availableFields" :key="field.value">
              <a-checkbox :value="field.value">{{ field.label }}</a-checkbox>
            </a-col>
          </a-row>
        </a-checkbox-group>
        <div class="export-config-modal__field-actions">
          <a-button size="small" type="link" @click="selectAllFields">全选</a-button>
          <a-button size="small" type="link" @click="clearAllFields">清空</a-button>
        </div>
      </a-form-item>

      <!-- 预计行数 -->
      <a-form-item label="预计行数" v-if="estimatedRows > 0">
        <span class="export-config-modal__estimate">
          {{ estimatedRows.toLocaleString() }} 行
          <a-tag v-if="estimatedRows <= 10000" color="green">直接下载</a-tag>
          <a-tag v-else color="orange">异步处理</a-tag>
        </span>
      </a-form-item>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, computed, watch } from 'vue';
import dayjs from 'dayjs';

interface ExportField {
  label: string;
  value: string;
}

const props = withDefaults(defineProps<{
  visible: boolean;
  availableFields?: ExportField[];
  loading?: boolean;
}>(), {
  availableFields: () => [],
  loading: false,
});

const emit = defineEmits<{
  'update:visible': [value: boolean];
  confirm: [data: { format: string; dateRange: [string, string]; fields: string[] }];
  cancel: [];
}>();

const visible = ref(props.visible);
const format = ref<'EXCEL' | 'CSV'>('EXCEL');
const dateRange = ref<[string, string] | null>(null);
const selectedFields = ref<string[]>([]);
const estimatedRows = ref(0);

const datePresets = computed(() => [
  { label: '最近7天', value: [dayjs().subtract(7, 'day'), dayjs()] },
  { label: '最近30天', value: [dayjs().subtract(30, 'day'), dayjs()] },
  { label: '最近90天', value: [dayjs().subtract(90, 'day'), dayjs()] },
  { label: '本年', value: [dayjs().startOf('year'), dayjs()] },
]);

watch(() => props.visible, (val) => {
  visible.value = val;
  if (val) {
    // 默认全选
    selectedFields.value = props.availableFields.map((f) => f.value);
    dateRange.value = null;
  }
});

watch(visible, (val) => {
  emit('update:visible', val);
});

function selectAllFields() {
  selectedFields.value = props.availableFields.map((f) => f.value);
}

function clearAllFields() {
  selectedFields.value = [];
}

function handleConfirm() {
  emit('confirm', {
    format: format.value,
    dateRange: dateRange.value || ['', ''],
    fields: selectedFields.value,
  });
}

function handleCancel() {
  emit('cancel');
}
</script>

<style lang="less" scoped>
.export-config-modal {
  &__field-actions {
    margin-top: 4px;
  }

  &__estimate {
    font-weight: 500;
  }
}
</style>
