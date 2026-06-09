<template>
  <a-modal
    :visible="visible"
    :title="mode === 'password' ? '密码加入' : '申请加入'"
    :confirm-loading="loading"
    :mask-closable="false"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <!-- 密码加入模式 -->
    <template v-if="mode === 'password'">
      <a-form :model="form" layout="vertical">
        <a-form-item label="圈子密码" required>
          <a-input-password
            v-model:value="form.password"
            placeholder="请输入6-20位密码"
            :maxlength="20"
            :disabled="passwordLocked"
            autocomplete="off"
            aria-label="请输入圈子密码"
          />
        </a-form-item>
        <!-- 密码强度指示器 -->
        <div v-if="form.password" class="password-strength">
          <div class="password-strength-bar">
            <div :class="['strength-fill', `strength-${strengthLevel}`]" :style="{ width: strengthPercent + '%' }" />
          </div>
          <span class="strength-text">{{ strengthLabel }}</span>
        </div>
        <a-alert v-if="errorMsg" :message="errorMsg" type="error" show-icon class="join-error" />
      </a-form>
    </template>

    <!-- 申请加入模式 -->
    <template v-else>
      <p class="apply-confirm-text">确认申请加入该圈子？提交申请后需等待圈子创建者审核通过。</p>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, watch } from 'vue';
import { usePasswordStrength } from '../composables/usePasswordStrength';

const props = defineProps<{
  visible: boolean;
  mode: 'password' | 'apply';
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  confirm: [data: { password?: string }];
}>();

const loading = ref(false);
const errorMsg = ref('');
const passwordLocked = ref(false);

const form = reactive({
  password: '',
});

// 密码强度（使用共享 composable）
const passwordComputed = computed(() => form.password);
const { strengthLevel, strengthPercent, strengthLabel } = usePasswordStrength(passwordComputed);

watch(() => props.visible, (val) => {
  if (val) {
    form.password = '';
    errorMsg.value = '';
    loading.value = false;
    passwordLocked.value = false;
  }
});

function handleOk() {
  if (props.mode === 'password') {
    if (!form.password) {
      errorMsg.value = '请输入密码';
      return;
    }
    emit('confirm', { password: form.password });
  } else {
    emit('confirm', {});
  }
}

function handleCancel() {
  emit('update:visible', false);
}

// 暴露给父组件：设置错误/锁定
function setError(msg: string) {
  errorMsg.value = msg;
  form.password = '';
}

function setLocked(locked: boolean) {
  passwordLocked.value = locked;
}

function setLoading(val: boolean) {
  loading.value = val;
}

defineExpose({ setError, setLocked, setLoading });
</script>

<style lang="less" scoped>
.password-strength {
  margin-top: -16px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;

  &-bar {
    flex: 1;
    height: 4px;
    background: var(--background-color-base, #f0f0f0);
    border-radius: 2px;
    overflow: hidden;
  }
}

.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s;

  &.strength-weak {
    background: #ff4d4f;
  }
  &.strength-medium {
    background: #faad14;
  }
  &.strength-strong {
    background: #52c41a;
  }
}

.strength-text {
  font-size: 12px;
  color: var(--text-color-secondary, #666);
  min-width: 20px;
}

.apply-confirm-text {
  font-size: 14px;
  color: var(--text-color-secondary, #666);
  line-height: 1.6;
  margin: 0;
}

.join-error {
  margin-top: 8px;
}
</style>
