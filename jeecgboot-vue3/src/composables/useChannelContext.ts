import { ref, computed, type Ref, provide, inject } from 'vue';
import { getChannelDetail } from '/@/api/content/channel';
import { getUserChannelRelation } from '/@/api/content/channelRelation';

export interface ChannelInfo {
  id: string;
  name: string;
  privacyType: 'PUBLIC' | 'PRIVATE';
  joinMethod: 'FREE' | 'REVIEW' | 'INVITE';
  isSystem: boolean;
}

export interface UserChannelRelation {
  isSubscribed: boolean;
  role: string | null;
  isMuted: boolean;
  isBlacklisted: boolean;
}

const CHANNEL_CONTEXT_KEY = Symbol('channelContext');

export function useChannelContext(channelId: Ref<string>) {
  const channelInfo = ref<ChannelInfo | null>(null);
  const userRelation = ref<UserChannelRelation | null>(null);
  const loading = ref(false);
  const channelNotFound = ref(false);
  const loadError = ref(false);

  const privacyType = computed(() => channelInfo.value?.privacyType ?? 'PUBLIC');
  const joinMethod = computed(() => channelInfo.value?.joinMethod ?? 'FREE');
  const isSubscribed = computed(() => userRelation.value?.isSubscribed ?? false);
  const memberRole = computed(() => userRelation.value?.role ?? null);
  const isMuted = computed(() => userRelation.value?.isMuted ?? false);
  const isBlacklisted = computed(() => userRelation.value?.isBlacklisted ?? false);

  const canManageMembers = computed(() => {
    const role = userRelation.value?.role;
    return role === 'OWNER' || role === 'ADMIN';
  });

  const canPublish = computed(() => {
    return !!memberRole.value && !isMuted.value && !isBlacklisted.value;
  });

  async function loadContext() {
    const id = channelId.value;
    loading.value = true;
    channelNotFound.value = false;
    loadError.value = false;
    try {
      const [info, relation] = await Promise.all([
        getChannelDetail(id),
        getUserChannelRelation(id),
      ]);
      channelInfo.value = info;
      userRelation.value = relation;
    } catch (error: any) {
      if (error?.response?.status === 404) {
        channelNotFound.value = true;
      } else {
        loadError.value = true;
      }
    } finally {
      loading.value = false;
    }
  }

  function resetContext() {
    channelInfo.value = null;
    userRelation.value = null;
    channelNotFound.value = false;
    loadError.value = false;
  }

  const context = {
    channelInfo, userRelation, privacyType, joinMethod,
    isSubscribed, memberRole, isMuted, isBlacklisted, loading,
    channelNotFound, loadError,
    canManageMembers, canPublish,
    loadContext, resetContext,
  };

  provide(CHANNEL_CONTEXT_KEY, context);

  return context;
}

export function useChannelContextInject() {
  const context = inject<ReturnType<typeof useChannelContext>>(CHANNEL_CONTEXT_KEY);
  if (!context) {
    throw new Error('useChannelContextInject must be used within a component that provides channel context');
  }
  return context;
}
