<template>
  <div class="circle-create-page">
    <div class="create-container">
      <!-- 步骤条 -->
      <a-steps :current="currentStep" class="create-steps">
        <a-step title="基础信息" />
        <a-step title="隐私设置" />
        <a-step title="创建成功" />
      </a-steps>

      <div class="create-content">
        <!-- Step 1: 基础信息 -->
        <div v-show="currentStep === 0">
          <a-form ref="form1Ref" :model="form" :rules="step1Rules" layout="vertical">
            <!-- 圈子名称 -->
            <a-form-item label="圈子名称" name="name" required>
              <a-input
                v-model:value="form.name"
                placeholder="请输入圈子名称（2-30字符）"
                :maxlength="30"
                show-count
                aria-label="圈子名称"
                @blur="handleNameBlur"
              >
                <template v-if="nameChecking" #suffix>
                  <a-spin size="small" />
                </template>
              </a-input>
              <div v-if="nameCheckResult === 'available'" class="name-check-success">该名称可用</div>
              <div v-else-if="nameCheckResult === 'exists'" class="name-check-error">该圈子名称已存在，请修改</div>
            </a-form-item>

            <!-- 圈子简介 -->
            <a-form-item label="圈子简介" name="description" required>
              <a-textarea
                v-model:value="form.description"
                placeholder="请输入圈子简介（10-500字）"
                :maxlength="500"
                :rows="4"
                show-count
                aria-label="圈子简介"
              />
            </a-form-item>

            <!-- 分类标签 -->
            <a-form-item label="分类标签" name="category">
              <a-select
                v-model:value="form.category"
                placeholder="请选择分类标签"
                allow-clear
                aria-label="分类标签"
              >
                <a-select-option v-for="cat in categoryOptions" :key="cat.value" :value="cat.value">
                  {{ cat.label }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <!-- 圈子图标（1:1 裁剪） -->
            <a-form-item label="圈子图标" name="iconUrl">
              <CircleIconCropper v-model="form.iconUrl" />
            </a-form-item>

            <!-- 封面图（16:9 裁剪） -->
            <a-form-item label="封面图" name="coverUrl">
              <CircleCoverCropper v-model="form.coverUrl" />
            </a-form-item>
          </a-form>
        </div>

        <!-- Step 2: 隐私与加入方式设置 -->
        <div v-show="currentStep === 1">
          <a-form ref="form2Ref" :model="form" :rules="step2Rules" layout="vertical">
            <!-- 隐私类型 -->
            <a-form-item label="隐私类型" name="privacyType" required>
              <a-radio-group v-model:value="form.privacyType" @change="handlePrivacyChange">
                <a-radio-button value="PUBLIC" aria-label="公开">公开</a-radio-button>
                <a-radio-button value="PRIVATE" aria-label="私有">私有</a-radio-button>
                <a-radio-button value="PASSWORD" aria-label="密码保护">密码保护</a-radio-button>
              </a-radio-group>
              <div class="privacy-desc">
                <template v-if="form.privacyType === 'PUBLIC'">所有人可发现和浏览</template>
                <template v-else-if="form.privacyType === 'PRIVATE'">仅成员可见，需申请加入</template>
                <template v-else>需密码才能加入</template>
              </div>
            </a-form-item>

            <!-- 加入方式 -->
            <a-form-item label="加入方式" name="joinType" required>
              <a-radio-group v-model:value="form.joinType" :disabled="joinTypeLocked">
                <a-radio-button value="DIRECT" :disabled="joinTypeLocked" aria-label="直接加入">
                  直接加入
                </a-radio-button>
                <a-radio-button value="APPROVAL" :disabled="joinTypeLocked" aria-label="申请审核">
                  申请审核
                </a-radio-button>
                <a-radio-button v-if="form.privacyType !== 'PASSWORD'" value="INVITE" :disabled="joinTypeLocked" aria-label="邀请加入">
                  邀请加入
                </a-radio-button>
                <a-radio-button
                  v-if="form.privacyType === 'PASSWORD'"
                  value="PASSWORD"
                  disabled
                  aria-label="密码加入"
                >
                  密码加入
                </a-radio-button>
              </a-radio-group>
            </a-form-item>

            <!-- 密码输入（密码保护模式） -->
            <a-form-item v-if="form.privacyType === 'PASSWORD'" label="圈子密码" name="password" required>
              <a-input-password
                v-model:value="form.password"
                placeholder="请设置6-20位密码"
                :maxlength="20"
                autocomplete="new-password"
                aria-label="圈子密码"
              />
              <!-- 密码强度 -->
              <div v-if="form.password" class="password-strength">
                <div class="password-strength-bar">
                  <div :class="['strength-fill', `strength-${strengthLevel}`]" :style="{ width: strengthPercent + '%' }" />
                </div>
                <span class="strength-text">{{ strengthLabel }}</span>
              </div>
            </a-form-item>
          </a-form>
        </div>

        <!-- Step 3: 创建成功 -->
        <div v-show="currentStep === 2" class="success-step">
          <a-result
            status="success"
            title="创建成功"
            sub-title="你的圈子已创建成功，快去看看吧"
          >
            <template #extra>
              <a-button type="primary" @click="goDetail">进入圈子</a-button>
              <a-button @click="goList">返回圈子列表</a-button>
            </template>
          </a-result>
        </div>
      </div>

      <!-- 底部操作按钮 -->
      <div v-if="currentStep < 2" class="create-actions">
        <a-button v-if="currentStep > 0" @click="prevStep">上一步</a-button>
        <a-button
          v-if="currentStep === 0"
          type="primary"
          :loading="submitting"
          @click="nextStep"
        >
          下一步
        </a-button>
        <a-button
          v-if="currentStep === 1"
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          提交创建
        </a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { createCircle, checkCircleName } from '/@/api/content/circle';
import { useMessage } from '/@/hooks/web/useMessage';
import CircleIconCropper from './components/CircleIconCropper.vue';
import CircleCoverCropper from './components/CircleCoverCropper.vue';
import { usePasswordStrength } from './composables/usePasswordStrength';
import { categoryOptions } from './constants';
import type { Rule } from 'ant-design-vue/es/form';
import type { PrivacyType, JoinType } from '/@/api/content/model/circleModel';

const router = useRouter();
const { createMessage } = useMessage();

// 步骤
const currentStep = ref(0);
const submitting = ref(false);
const createdCircleId = ref('');

// 表单
const form = reactive({
  name: '',
  description: '',
  category: undefined as string | undefined,
  iconUrl: undefined as string | undefined,
  coverUrl: undefined as string | undefined,
  privacyType: 'PUBLIC' as PrivacyType,
  joinType: 'DIRECT' as JoinType,
  password: '',
});

// 表单引用
const form1Ref = ref();
const form2Ref = ref();

// 名称校验
const nameChecking = ref(false);
const nameCheckResult = ref<'available' | 'exists' | null>(null);
let nameCheckTimer: ReturnType<typeof setTimeout> | null = null;

// 分类选项（共享常量）

// 加入方式是否锁定
const joinTypeLocked = computed(() => {
  return form.privacyType === 'PASSWORD';
});

// Step 1 校验规则
const step1Rules: Record<string, Rule[]> = {
  name: [
    { required: true, message: '请输入圈子名称', trigger: 'blur' },
    { min: 2, max: 30, message: '名称长度需在2-30字符之间', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入圈子简介', trigger: 'blur' },
    { min: 10, max: 500, message: '简介长度需在10-500字之间', trigger: 'blur' },
  ],
};

// Step 2 校验规则
const step2Rules: Record<string, Rule[]> = {
  privacyType: [{ required: true, message: '请选择隐私类型' }],
  joinType: [{ required: true, message: '请选择加入方式' }],
  password: [
    { required: true, message: '请设置圈子密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在6-20位之间', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        if (form.privacyType !== 'PASSWORD') return Promise.resolve();
        if (!value) return Promise.resolve();
        if (!/[a-zA-Z]/.test(value)) {
          return Promise.reject('密码需包含至少一个字母');
        }
        if (/^\d+$/.test(value)) {
          return Promise.reject('密码不能为纯数字，请包含字母');
        }
        return Promise.resolve();
      },
      trigger: 'blur',
    },
  ],
};

// 密码强度（使用共享 composable）
const passwordComputed = computed(() => form.password);
const { strengthLevel, strengthPercent, strengthLabel } = usePasswordStrength(passwordComputed);

// 名称失焦校验
async function handleNameBlur() {
  if (!form.name || form.name.length < 2) {
    nameCheckResult.value = null;
    return;
  }
  if (nameCheckTimer) clearTimeout(nameCheckTimer);
  nameCheckTimer = setTimeout(async () => {
    nameChecking.value = true;
    nameCheckResult.value = null;
    try {
      const available = await checkCircleName(form.name);
      nameCheckResult.value = available ? 'available' : 'exists';
    } catch {
      // 降级：不阻断流程
      nameCheckResult.value = null;
    } finally {
      nameChecking.value = false;
    }
  }, 500);
}

// 隐私类型变更
function handlePrivacyChange() {
  switch (form.privacyType) {
    case 'PRIVATE':
      form.joinType = 'APPROVAL';
      break;
    case 'PASSWORD':
      form.joinType = 'PASSWORD';
      break;
    default:
      form.joinType = 'DIRECT';
      break;
  }
}

// 步骤导航
async function nextStep() {
  try {
    await form1Ref.value?.validate();
    currentStep.value = 1;
  } catch {
    // 表单校验失败
  }
}

function prevStep() {
  currentStep.value = 0;
}

// 提交创建
async function handleSubmit() {
  try {
    await form2Ref.value?.validate();
  } catch {
    return;
  }

  submitting.value = true;
  try {
    const req: any = {
      name: form.name,
      description: form.description,
      privacyType: form.privacyType,
      joinType: form.joinType,
    };
    if (form.category) req.category = form.category;
    if (form.iconUrl) req.iconUrl = form.iconUrl;
    if (form.coverUrl) req.coverUrl = form.coverUrl;
    if (form.privacyType === 'PASSWORD' && form.password) req.password = form.password;

    const result = await createCircle(req);
    createdCircleId.value = result?.id || '';
    currentStep.value = 2;
  } catch (error: any) {
    const msg = error?.message || '创建失败，请重试';
    // 敏感词降级：不阻断提示成功
    if (msg.includes('敏感词') || error?.code === 'SENSITIVE_WORD') {
      createMessage.warning('创建成功，内容将在审核后公开');
      currentStep.value = 2;
    } else {
      createMessage.error(msg);
    }
  } finally {
    submitting.value = false;
  }
}

// 导航
function goDetail() {
  if (createdCircleId.value) {
    router.push(`/circle/${createdCircleId.value}`);
  }
}

function goList() {
  router.push('/circle/list');
}
</script>

<style lang="less" scoped>
.circle-create-page {
  min-height: calc(100vh - 64px);
  padding: 24px 16px;
  background: var(--background-color-base, #f5f5f5);
}

.create-container {
  max-width: 640px;
  margin: 0 auto;
  background: var(--component-background, #fff);
  border-radius: 12px;
  padding: 32px;
}

.create-steps {
  margin-bottom: 32px;
}

.create-content {
  min-height: 300px;
}

.create-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color-base, #f0f0f0);
}

// 名称校验
.name-check-success {
  color: #52c41a;
  font-size: 12px;
  margin-top: 4px;
}

.name-check-error {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}

// 隐私描述
.privacy-desc {
  color: var(--text-color-secondary, #666);
  font-size: 12px;
  margin-top: 6px;
}

// 密码强度
.password-strength {
  margin-top: 8px;
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
  &.strength-weak { background: #ff4d4f; }
  &.strength-medium { background: #faad14; }
  &.strength-strong { background: #52c41a; }
}

.strength-text {
  font-size: 12px;
  color: var(--text-color-secondary, #666);
  min-width: 20px;
}

// 上传
.upload-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-color-secondary, #666);
}

.upload-desc {
  font-size: 12px;
  color: var(--text-color-tertiary, #999);
  margin-top: 4px;
}

// 成功步骤
.success-step {
  padding: 40px 0;
}

// 响应式
@media (max-width: 768px) {
  .circle-create-page {
    padding: 8px;
  }

  .create-container {
    padding: 20px 16px;
    border-radius: 8px;
  }

  .create-steps {
    :deep(.ant-steps-item-title) {
      font-size: 13px;
    }
  }
}
</style>
