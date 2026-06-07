import { ref, createApp, defineComponent, h } from 'vue';

vi.mock('/@/api/content/channel', () => ({
  getChannelDetail: vi.fn(),
}));

vi.mock('/@/api/content/channelRelation', () => ({
  getUserChannelRelation: vi.fn(),
}));

import { useChannelContext, useChannelContextInject } from '/@/composables/useChannelContext';
import { getChannelDetail } from '/@/api/content/channel';
import { getUserChannelRelation } from '/@/api/content/channelRelation';

const mockGetChannelDetail = vi.mocked(getChannelDetail);
const mockGetUserChannelRelation = vi.mocked(getUserChannelRelation);

/** Helper: mount a component that runs a composable in proper setup context */
function withSetup(composable: () => any) {
  let result: any;
  const app = createApp(
    defineComponent({
      setup() {
        result = composable();
        return () => {};
      },
    }),
  );
  app.mount(document.createElement('div'));
  return { result, app };
}

beforeEach(() => {
  vi.clearAllMocks();
});

describe('useChannelContext', () => {
  describe('loadContext', () => {
    it('should call getChannelDetail and getUserChannelRelation, then update refs', async () => {
      const mockInfo = {
        id: 'ch1',
        name: 'Test Channel',
        privacyType: 'PRIVATE',
        joinMethod: 'REVIEW',
        isSystem: false,
      };
      const mockRelation = {
        isSubscribed: true,
        role: 'ADMIN',
        isMuted: false,
        isBlacklisted: false,
      };
      mockGetChannelDetail.mockResolvedValue(mockInfo);
      mockGetUserChannelRelation.mockResolvedValue(mockRelation);

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();

      expect(mockGetChannelDetail).toHaveBeenCalledWith('ch1');
      expect(mockGetUserChannelRelation).toHaveBeenCalledWith('ch1');
      expect(result.channelInfo.value).toEqual(mockInfo);
      expect(result.userRelation.value).toEqual(mockRelation);
      expect(result.privacyType.value).toBe('PRIVATE');
      expect(result.joinMethod.value).toBe('REVIEW');
      expect(result.isSubscribed.value).toBe(true);
      expect(result.memberRole.value).toBe('ADMIN');
      expect(result.isMuted.value).toBe(false);
      expect(result.isBlacklisted.value).toBe(false);
    });

    it('should always use channelId ref value', async () => {
      mockGetChannelDetail.mockResolvedValue({ id: 'ch1', name: 'X', privacyType: 'PUBLIC', joinMethod: 'FREE', isSystem: false });
      mockGetUserChannelRelation.mockResolvedValue({ isSubscribed: false, role: null, isMuted: false, isBlacklisted: false });

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();

      expect(mockGetChannelDetail).toHaveBeenCalledWith('ch1');
      expect(mockGetUserChannelRelation).toHaveBeenCalledWith('ch1');
    });

    it('should set channelNotFound on 404 error', async () => {
      const error404 = { response: { status: 404 } };
      mockGetChannelDetail.mockRejectedValue(error404);
      mockGetUserChannelRelation.mockRejectedValue(error404);

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();

      expect(result.channelNotFound.value).toBe(true);
      expect(result.loadError.value).toBe(false);
    });

    it('should set loadError on non-404 errors', async () => {
      const error500 = { response: { status: 500 } };
      mockGetChannelDetail.mockRejectedValue(error500);
      mockGetUserChannelRelation.mockRejectedValue(error500);

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();

      expect(result.loadError.value).toBe(true);
      expect(result.channelNotFound.value).toBe(false);
    });

    it('should set loading true during execution and false after', async () => {
      let resolveDetail: any;
      mockGetChannelDetail.mockImplementation(() => new Promise((r) => (resolveDetail = r)));
      mockGetUserChannelRelation.mockResolvedValue({ isSubscribed: false, role: null, isMuted: false, isBlacklisted: false });

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      const loadPromise = result.loadContext();
      expect(result.loading.value).toBe(true);

      resolveDetail({ id: 'ch1', name: 'X', privacyType: 'PUBLIC', joinMethod: 'FREE', isSystem: false });
      await loadPromise;
      expect(result.loading.value).toBe(false);
    });

    it('should reset error flags before loading', async () => {
      // First load sets error
      mockGetChannelDetail.mockRejectedValueOnce({ response: { status: 500 } });
      mockGetUserChannelRelation.mockRejectedValueOnce({ response: { status: 500 } });

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();
      expect(result.loadError.value).toBe(true);

      // Second load succeeds and clears error
      mockGetChannelDetail.mockResolvedValue({ id: 'ch1', name: 'X', privacyType: 'PUBLIC', joinMethod: 'FREE', isSystem: false });
      mockGetUserChannelRelation.mockResolvedValue({ isSubscribed: false, role: null, isMuted: false, isBlacklisted: false });

      await result.loadContext();
      expect(result.loadError.value).toBe(false);
      expect(result.channelNotFound.value).toBe(false);
    });
  });

  describe('resetContext', () => {
    it('should reset all refs to defaults', async () => {
      mockGetChannelDetail.mockResolvedValue({ id: 'ch1', name: 'X', privacyType: 'PRIVATE', joinMethod: 'REVIEW', isSystem: false });
      mockGetUserChannelRelation.mockResolvedValue({ isSubscribed: true, role: 'ADMIN', isMuted: true, isBlacklisted: true });

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();
      // Confirm data loaded
      expect(result.channelInfo.value).not.toBeNull();

      result.resetContext();

      expect(result.channelInfo.value).toBeNull();
      expect(result.userRelation.value).toBeNull();
      expect(result.privacyType.value).toBe('PUBLIC');
      expect(result.joinMethod.value).toBe('FREE');
      expect(result.isSubscribed.value).toBe(false);
      expect(result.memberRole.value).toBeNull();
      expect(result.isMuted.value).toBe(false);
      expect(result.isBlacklisted.value).toBe(false);
      expect(result.channelNotFound.value).toBe(false);
      expect(result.loadError.value).toBe(false);
    });
  });

  describe('canManageMembers', () => {
    it('should be true for OWNER role', async () => {
      mockGetChannelDetail.mockResolvedValue({ id: 'ch1', name: 'X', privacyType: 'PUBLIC', joinMethod: 'FREE', isSystem: false });
      mockGetUserChannelRelation.mockResolvedValue({ isSubscribed: true, role: 'OWNER', isMuted: false, isBlacklisted: false });

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();
      expect(result.canManageMembers.value).toBe(true);
    });

    it('should be true for ADMIN role', async () => {
      mockGetChannelDetail.mockResolvedValue({ id: 'ch1', name: 'X', privacyType: 'PUBLIC', joinMethod: 'FREE', isSystem: false });
      mockGetUserChannelRelation.mockResolvedValue({ isSubscribed: true, role: 'ADMIN', isMuted: false, isBlacklisted: false });

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();
      expect(result.canManageMembers.value).toBe(true);
    });

    it('should be false for MEMBER role', async () => {
      mockGetChannelDetail.mockResolvedValue({ id: 'ch1', name: 'X', privacyType: 'PUBLIC', joinMethod: 'FREE', isSystem: false });
      mockGetUserChannelRelation.mockResolvedValue({ isSubscribed: true, role: 'MEMBER', isMuted: false, isBlacklisted: false });

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();
      expect(result.canManageMembers.value).toBe(false);
    });

    it('should be false when role is null', () => {
      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));
      expect(result.canManageMembers.value).toBe(false);
    });
  });

  describe('canPublish', () => {
    it('should be true when memberRole exists and not muted/blacklisted', async () => {
      mockGetChannelDetail.mockResolvedValue({ id: 'ch1', name: 'X', privacyType: 'PUBLIC', joinMethod: 'FREE', isSystem: false });
      mockGetUserChannelRelation.mockResolvedValue({ isSubscribed: true, role: 'MEMBER', isMuted: false, isBlacklisted: false });

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();
      expect(result.canPublish.value).toBe(true);
    });

    it('should be false when memberRole is null', () => {
      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));
      expect(result.canPublish.value).toBe(false);
    });

    it('should be false when muted', async () => {
      mockGetChannelDetail.mockResolvedValue({ id: 'ch1', name: 'X', privacyType: 'PUBLIC', joinMethod: 'FREE', isSystem: false });
      mockGetUserChannelRelation.mockResolvedValue({ isSubscribed: true, role: 'MEMBER', isMuted: true, isBlacklisted: false });

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();
      expect(result.canPublish.value).toBe(false);
    });

    it('should be false when blacklisted', async () => {
      mockGetChannelDetail.mockResolvedValue({ id: 'ch1', name: 'X', privacyType: 'PUBLIC', joinMethod: 'FREE', isSystem: false });
      mockGetUserChannelRelation.mockResolvedValue({ isSubscribed: true, role: 'MEMBER', isMuted: false, isBlacklisted: true });

      const channelId = ref('ch1');
      const { result } = withSetup(() => useChannelContext(channelId));

      await result.loadContext();
      expect(result.canPublish.value).toBe(false);
    });
  });
});

describe('useChannelContextInject', () => {
  it('should throw when no provider exists', () => {
    expect(() => {
      useChannelContextInject();
    }).toThrow('useChannelContextInject must be used within a component that provides channel context');
  });

  it('should return context when provided by parent', () => {
    const channelId = ref('ch1');
    let parentCtx: any;
    let injected: any;

    const child = defineComponent({
      setup() {
        injected = useChannelContextInject();
        return () => {};
      },
    });

    const parentApp = createApp(
      defineComponent({
        setup() {
          parentCtx = useChannelContext(channelId);
          return () => h(child);
        },
      }),
    );
    parentApp.mount(document.createElement('div'));

    expect(injected).toBeDefined();
    // Injected context should be the same object as what the parent provided
    expect(injected.loadContext).toBe(parentCtx.loadContext);
    expect(injected.canManageMembers).toBe(parentCtx.canManageMembers);
  });
});
