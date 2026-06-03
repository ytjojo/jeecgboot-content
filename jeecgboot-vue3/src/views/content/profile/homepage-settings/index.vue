<template>
  <div class="profile-homepage-settings">
    <a-page-header title="主页设置" :back-icon="true" @back="$router.back()" />

    <a-form v-if="!loading" layout="vertical" :model="form">
      <a-card title="背景与主题" :bordered="false" class="profile-homepage-settings__card">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="主页背景图">
              <a-input-group compact>
                <a-input
                  v-model:value="form.homepageBackground"
                  placeholder="OSS CDN URL（≤512 字符）"
                  :maxlength="512"
                  style="width: calc(100% - 96px)"
                />
                <a-button style="width: 96px" @click="cropperVisible = true">
                  <Icon icon="mdi:upload" /> 上传
                </a-button>
              </a-input-group>
              <div class="profile-homepage-settings__bg-preview" v-if="form.homepageBackground">
                <img :src="form.homepageBackground" alt="背景预览" />
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="主题色（#RRGGBB）">
              <a-input
                v-model:value="form.themeColor"
                placeholder="#1677ff"
                :maxlength="16"
                :status="themeInputStatus"
              />
              <div class="profile-homepage-settings__contrast" v-if="themeContrastInfo">
                <span :class="['profile-homepage-settings__contrast-badge', themeContrastInfo.cls]">
                  对比度 {{ themeContrastInfo.ratio.toFixed(2) }}:1 {{ themeContrastInfo.symbol }}
                  {{ themeContrastInfo.label }}
                </span>
                <span class="profile-homepage-settings__contrast-text">
                  自动文字色：<code>{{ themeContrastInfo.textColor }}</code>
                </span>
              </div>
              <div class="profile-homepage-settings__presets" role="listbox" aria-label="主题色预设">
                <button
                  v-for="c in PRESET_THEME_COLORS"
                  :key="c"
                  type="button"
                  class="profile-homepage-settings__preset"
                  :class="{ 'is-active': form.themeColor && form.themeColor.toLowerCase() === c }"
                  :style="{ backgroundColor: c }"
                  :aria-label="`预设主题色 ${c}`"
                  @click="onSelectPreset(c)"
                >
                  <Icon
                    v-if="form.themeColor && form.themeColor.toLowerCase() === c"
                    icon="mdi:check"
                    :color="getContrastingTextColor(c)"
                  />
                </button>
              </div>
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="主页模块" :bordered="false" class="profile-homepage-settings__card">
        <template #extra>
          <a-space>
            <a-button size="small" @click="onRestoreDefaults" :loading="restoring">恢复默认</a-button>
            <a-button size="small" type="primary" @click="onSaveModules" :loading="saving" :disabled="!!validateModulesForSave(modules)">保存排序</a-button>
          </a-space>
        </template>
        <a-empty v-if="modules.length === 0" description="暂无可配置模块" />
        <draggable
          v-else
          v-model="modules"
          item-key="moduleKey"
          handle=".drag-handle"
          ghost-class="profile-homepage-settings__module--ghost"
          class="profile-homepage-settings__list"
          @end="onDragEnd"
        >
          <template #item="{ element, index }">
            <a-list-item class="profile-homepage-settings__module">
              <a-list-item-meta :title="element.moduleName" :description="element.moduleKey">
                <template #avatar>
                  <Icon
                    icon="mdi:drag-vertical"
                    :size="22"
                    class="drag-handle profile-homepage-settings__handle"
                    :title="'拖拽手柄'"
                  />
                </template>
              </a-list-item-meta>
              <a-space>
                <a-switch
                  :checked="element.visible"
                  checked-children="显示"
                  un-checked-children="隐藏"
                  @change="(v: boolean) => onModuleVisibleChange(index, v)"
                />
                <a-space.Compact>
                  <a-button size="small" :disabled="index === 0" @click="onMove(index, -1)">
                    <Icon icon="mdi:arrow-up" />
                  </a-button>
                  <a-button
                    size="small"
                    :disabled="index === modules.length - 1"
                    @click="onMove(index, 1)"
                  >
                    <Icon icon="mdi:arrow-down" />
                  </a-button>
                </a-space.Compact>
              </a-space>
            </a-list-item>
          </template>
        </draggable>
        <p
          v-if="modules.length > 0 && !modules.some((m) => m.visible)"
          class="profile-homepage-settings__warn"
        >
          至少需要保留一个模块
        </p>
      </a-card>

      <a-space class="profile-homepage-settings__actions">
        <a-button type="primary" :loading="savingBg" @click="onSaveBg">保存</a-button>
      </a-space>
    </a-form>
    <a-skeleton v-else active />

    <BackgroundCropper
      v-model:visible="cropperVisible"
      @uploaded="onBackgroundUploaded"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { message, Modal } from 'ant-design-vue';
import draggable from 'vuedraggable';
import { Icon } from '/@/components/Icon';
import {
  getProfileDetail,
  updateHomepage,
  getHomepageModules,
  restoreHomepageDefaults,
} from '/@/api/content/profile';
import type { ContentUserHomepageModuleVO } from '/@/api/content/profile/types';
import BackgroundCropper from '../components/BackgroundCropper.vue';
import {
  PRESET_THEME_COLORS,
  isValidHex,
  parseThemeColor,
  getContrastRatio,
  meetsWcagAA,
  getContrastingTextColor,
  validateModulesForSave,
} from '../components/themeColor';

interface BgForm {
  homepageBackground?: string;
  themeColor?: string;
}

const form = reactive<BgForm>({});
const modules = ref<ContentUserHomepageModuleVO[]>([]);
const loading = ref(false);
const saving = ref(false);
const savingBg = ref(false);
const restoring = ref(false);
const cropperVisible = ref(false);
const userId = ref<string>('');

const themeContrastInfo = computed(() => {
  const c = (form.themeColor || '').trim();
  if (!isValidHex(c)) return null;
  let parsed;
  try {
    parsed = parseThemeColor(c);
  } catch {
    return null;
  }
  const fg = getContrastingTextColor(parsed.hex);
  const ratio = getContrastRatio(fg, parsed.hex);
  const passes = meetsWcagAA(fg, parsed.hex);
  return {
    ratio,
    passes,
    textColor: fg,
    label: passes ? 'AA' : '不达标',
    symbol: passes ? '✓' : '✗',
    cls: passes ? 'is-pass' : 'is-fail',
  };
});

const themeInputStatus = computed(() => {
  const c = (form.themeColor || '').trim();
  if (!c) return '';
  if (!isValidHex(c)) return 'error';
  if (themeContrastInfo.value && !themeContrastInfo.value.passes) return 'warning';
  return '';
});

onMounted(async () => {
  loading.value = true;
  try {
    const { useUserStore } = await import('/@/store/modules/user');
    const userStore = useUserStore();
    const uid = (userStore.userInfo as any)?.id || (userStore.userInfo as any)?.userId || '';
    if (!uid) {
      message.error('未识别当前用户');
      return;
    }
    userId.value = uid;
    const detail = await getProfileDetail(uid, uid);
    form.homepageBackground = detail.homepageBackground;
    form.themeColor = detail.themeColor;
    const modRes = await getHomepageModules(uid);
    modules.value = modRes || [];
  } finally {
    loading.value = false;
  }
});

function onModuleVisibleChange(index: number, visible: boolean) {
  modules.value[index].visible = visible;
}

function onMove(index: number, dir: number) {
  const next = index + dir;
  if (next < 0 || next >= modules.value.length) return;
  const tmp = modules.value[index];
  modules.value[index] = modules.value[next];
  modules.value[next] = tmp;
  reassignSortOrder();
}

function reassignSortOrder() {
  modules.value.forEach((m, i) => (m.sortOrder = i));
}

function onDragEnd() {
  reassignSortOrder();
}

function onSelectPreset(c: string) {
  form.themeColor = c;
}

function onBackgroundUploaded(url: string) {
  form.homepageBackground = url;
  message.success('已更新背景图');
}

async function onSaveModules() {
  if (!userId.value) return;
  const err = validateModulesForSave(modules.value);
  if (err) {
    message.error(err);
    return;
  }
  saving.value = true;
  try {
    const moduleOrderJson = JSON.stringify(
      modules.value.map((m, i) => ({ key: m.moduleKey, visible: m.visible, order: i }))
    );
    await updateHomepage(userId.value, { moduleOrderJson });
    message.success('已保存');
  } catch (e: any) {
    message.error(e?.message || '保存失败');
  } finally {
    saving.value = false;
  }
}

async function onSaveBg() {
  if (!userId.value) return;
  if (form.themeColor && !isValidHex(form.themeColor)) {
    message.error('主题色必须为 #RRGGBB 格式');
    return;
  }
  savingBg.value = true;
  try {
    await updateHomepage(userId.value, {
      homepageBackground: form.homepageBackground,
      themeColor: form.themeColor,
    });
    message.success('已保存');
  } catch (e: any) {
    message.error(e?.message || '保存失败');
  } finally {
    savingBg.value = false;
  }
}

function onRestoreDefaults() {
  Modal.confirm({
    title: '确认恢复默认？',
    content: '将重置模块排序与显隐，自定义主题色与背景也会被清空。',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      if (!userId.value) return;
      restoring.value = true;
      try {
        await restoreHomepageDefaults(userId.value);
        form.homepageBackground = undefined;
        form.themeColor = undefined;
        const modRes = await getHomepageModules(userId.value);
        modules.value = modRes || [];
        message.success('已恢复默认');
      } catch (e: any) {
        message.error(e?.message || '操作失败');
      } finally {
        restoring.value = false;
      }
    },
  });
}
</script>

<style scoped>
.profile-homepage-settings__card {
  margin-bottom: 16px;
}
.profile-homepage-settings__actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.profile-homepage-settings__list {
  display: flex;
  flex-direction: column;
}
.profile-homepage-settings__module {
  padding: 12px 0;
}
.profile-homepage-settings__module--ghost {
  opacity: 0.4;
  background: #fafafa;
}
.profile-homepage-settings__handle {
  cursor: grab;
  touch-action: none;
  user-select: none;
}
.profile-homepage-settings__handle:active {
  cursor: grabbing;
}
.profile-homepage-settings__warn {
  color: #fa8c16;
  font-size: 13px;
  margin: 8px 0 0;
}
.profile-homepage-settings__bg-preview {
  margin-top: 8px;
  width: 100%;
  max-width: 480px;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  background: #f5f5f5;
}
.profile-homepage-settings__bg-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.profile-homepage-settings__presets {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}
.profile-homepage-settings__preset {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #d9d9d9;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transition: transform 0.15s ease;
}
.profile-homepage-settings__preset:hover {
  transform: scale(1.08);
}
.profile-homepage-settings__preset.is-active {
  box-shadow: 0 0 0 2px #1677ff;
}
.profile-homepage-settings__contrast {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  font-size: 12px;
  flex-wrap: wrap;
}
.profile-homepage-settings__contrast-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}
.profile-homepage-settings__contrast-badge.is-pass {
  background: rgba(82, 196, 26, 0.12);
  color: #389e0d;
}
.profile-homepage-settings__contrast-badge.is-fail {
  background: rgba(250, 173, 20, 0.16);
  color: #d48806;
}
.profile-homepage-settings__contrast-text {
  color: rgba(0, 0, 0, 0.55);
}
.profile-homepage-settings__contrast-text code {
  background: #f5f5f5;
  padding: 0 4px;
  border-radius: 2px;
}
@media (max-width: 768px) {
  .profile-homepage-settings :deep(.ant-col) {
    max-width: 100%;
    flex: 0 0 100%;
  }
  .profile-homepage-settings__module :deep(.ant-space) {
    flex-wrap: wrap;
  }
}
</style>
