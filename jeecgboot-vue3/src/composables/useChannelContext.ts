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
  const privacyType = ref<'PUBLIC' | 'PRIVATE'>('PUBLIC');
  const joinMethod = ref<'FREE' | 'REVIEW' | 'INVITE'>('FREE');
  const isSubscribed = ref(false);
  const memberRole = ref<string | null>(null);
  const isMuted = ref(false);
  const isBlacklisted = ref(false);
  const loading = ref(false);
  const channelNotFound = ref(false);
  const loadError = ref(false);

  const canManageMembers = computed(() => {
    const role = userRelation.value?.role;
    return role === 'OWNER' || role === 'ADMIN';
  });

  const canPublish = computed(() => {
    return !!memberRole.value && !isMuted.value && !isBlacklisted.value;
  });

  async function loadContext() {
    loading.value = true;
    channelNotFound.value = false;
    loadError.value = false;
    try {
      const [info, relation] = await Promise.all([
        getChannelDetail(channelId.value),
        getUserChannelRelation(channelId.value),
      ]);
      channelInfo.value = info;
      userRelation.value = relation;
      privacyType.value = info.privacyType;
      joinMethod.value = info.joinMethod;
      isSubscribed.value = relation.isSubscribed;
      memberRole.value = relation.role;
      isMuted.value = relation.isMuted;
      isBlacklisted.value = relation.isBlacklisted;
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
    privacyType.value = 'PUBLIC';
    joinMethod.value = 'FREE';
    isSubscribed.value = false;
    memberRole.value = null;
    isMuted.value = false;
    isBlacklisted.value = false;
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
