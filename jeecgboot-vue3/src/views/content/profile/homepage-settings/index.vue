<template>
  <div class="profile-homepage-settings">
    <a-page-header title="主页设置" :back-icon="true" @back="$router.back()" />

    <a-form v-if="!loading" layout="vertical" :model="form">
      <a-card title="背景与主题" :bordered="false" class="profile-homepage-settings__card">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="主页背景图">
              <a-input
                v-model:value="form.homepageBackground"
                placeholder="OSS CDN URL（≤512 字符）"
                :maxlength="512"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="主题色（#RRGGBB）">
              <a-input
                v-model:value="form.themeColor"
                placeholder="#1677ff"
                :maxlength="16"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>

      <a-card title="主页模块" :bordered="false" class="profile-homepage-settings__card">
        <template #extra>
          <a-space>
            <a-button size="small" @click="onRestoreDefaults" :loading="restoring">恢复默认</a-button>
            <a-button size="small" type="primary" @click="onSaveModules" :loading="saving">保存排序</a-button>
          </a-space>
        </template>
        <a-empty v-if="modules.length === 0" description="暂无可配置模块" />
        <a-list v-else :data-source="modules" :split="false">
          <template #renderItem="{ item, index }">
            <a-list-item class="profile-homepage-settings__module">
              <a-list-item-meta :title="item.moduleName" :description="item.moduleKey">
                <template #avatar>
                  <Icon icon="mdi:drag" :size="20" />
                </template>
              </a-list-item-meta>
              <a-space>
                <a-switch
                  :checked="item.visible"
                  checked-children="显示"
                  un-checked-children="隐藏"
                  @change="(v) => onModuleVisibleChange(index, v as boolean)"
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
        </a-list>
      </a-card>

      <a-space class="profile-homepage-settings__actions">
        <a-button type="primary" :loading="savingBg" @click="onSaveBg">保存</a-button>
      </a-space>
    </a-form>
    <a-skeleton v-else active />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { Icon } from '/@/components/Icon';
import {
  getProfileDetail,
  updateHomepage,
  getHomepageModules,
  restoreHomepageDefaults,
} from '/@/api/content/profile';
import type { ContentUserHomepageModuleVO } from '/@/api/content/profile/types';

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
const userId = ref<string>('');

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
  modules.value.forEach((m, i) => (m.sortOrder = i));
}

async function onSaveModules() {
  if (!userId.value) return;
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
  if (form.themeColor && !/^#[0-9A-Fa-f]{6}$/.test(form.themeColor)) {
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
.profile-homepage-settings__module {
  padding: 12px 0;
}
</style>
