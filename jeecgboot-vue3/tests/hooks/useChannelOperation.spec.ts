import { createApp, defineComponent } from 'vue';

const mockSuccess = vi.fn();
const mockError = vi.fn();

vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({
    createMessage: {
      success: mockSuccess,
      error: mockError,
    },
  }),
}));

import { useChannelOperation } from '/@/hooks/web/useChannelOperation';

/** Helper: run composable in proper Vue setup context */
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
  return result;
}

beforeEach(() => {
  vi.clearAllMocks();
});

describe('useChannelOperation', () => {
  describe('optimisticExecute', () => {
    it('should call onOptimistic immediately before API resolves', async () => {
      let resolveApi: any;
      const apiCall = vi.fn(() => new Promise((r) => (resolveApi = r)));
      const onOptimistic = vi.fn();
      const onSuccess = vi.fn();

      const { optimisticExecute } = withSetup(() => useChannelOperation());

      const promise = optimisticExecute({
        apiCall,
        onOptimistic,
        onSuccess,
        onRollback: vi.fn(),
      });

      // onOptimistic should be called synchronously, before API resolves
      expect(onOptimistic).toHaveBeenCalledTimes(1);
      expect(apiCall).toHaveBeenCalledTimes(1);
      // onSuccess should NOT be called yet
      expect(onSuccess).not.toHaveBeenCalled();

      resolveApi('result');
      await promise;

      // Now onSuccess should be called
      expect(onSuccess).toHaveBeenCalledWith('result');
    });

    it('should call onSuccess and show success message on success', async () => {
      const apiCall = vi.fn().mockResolvedValue('data');
      const onSuccess = vi.fn();

      const { optimisticExecute } = withSetup(() => useChannelOperation());

      const result = await optimisticExecute({
        apiCall,
        onOptimistic: vi.fn(),
        onSuccess,
        onRollback: vi.fn(),
        successMessage: '操作成功',
      });

      expect(result).toBe(true);
      expect(onSuccess).toHaveBeenCalledWith('data');
      expect(mockSuccess).toHaveBeenCalledWith('操作成功');
    });

    it('should not show success message when successMessage is omitted', async () => {
      const apiCall = vi.fn().mockResolvedValue('data');

      const { optimisticExecute } = withSetup(() => useChannelOperation());

      await optimisticExecute({
        apiCall,
        onOptimistic: vi.fn(),
        onRollback: vi.fn(),
      });

      expect(mockSuccess).not.toHaveBeenCalled();
    });

    it('should call onRollback, show error message, and re-throw on failure', async () => {
      const apiError = new Error('network fail');
      const apiCall = vi.fn().mockRejectedValue(apiError);
      const onRollback = vi.fn();

      const { optimisticExecute } = withSetup(() => useChannelOperation());

      await expect(
        optimisticExecute({
          apiCall,
          onOptimistic: vi.fn(),
          onRollback,
          errorMessage: '自定义错误',
        }),
      ).rejects.toThrow('network fail');

      expect(onRollback).toHaveBeenCalledTimes(1);
      expect(mockError).toHaveBeenCalledWith('自定义错误');
    });

    it('should use default error message when errorMessage is omitted', async () => {
      const apiCall = vi.fn().mockRejectedValue(new Error('fail'));

      const { optimisticExecute } = withSetup(() => useChannelOperation());

      await expect(
        optimisticExecute({
          apiCall,
          onOptimistic: vi.fn(),
          onRollback: vi.fn(),
        }),
      ).rejects.toThrow();

      expect(mockError).toHaveBeenCalledWith('操作失败，请重试');
    });

    it('should set operating to true during execution and false after success', async () => {
      let resolveApi: any;
      const apiCall = vi.fn(() => new Promise((r) => (resolveApi = r)));

      const { optimisticExecute, operating } = withSetup(() => useChannelOperation());

      const promise = optimisticExecute({
        apiCall,
        onOptimistic: vi.fn(),
        onRollback: vi.fn(),
      });

      expect(operating.value).toBe(true);

      resolveApi('ok');
      await promise;

      expect(operating.value).toBe(false);
    });

    it('should set operating to false after failure', async () => {
      const apiCall = vi.fn().mockRejectedValue(new Error('fail'));

      const { optimisticExecute, operating } = withSetup(() => useChannelOperation());

      await optimisticExecute({
        apiCall,
        onOptimistic: vi.fn(),
        onRollback: vi.fn(),
      }).catch(() => {});

      expect(operating.value).toBe(false);
    });

    it('should prevent concurrent calls and return false', async () => {
      let resolveApi: any;
      const apiCall = vi.fn(() => new Promise((r) => (resolveApi = r)));

      const { optimisticExecute } = withSetup(() => useChannelOperation());

      // Start first call
      const firstPromise = optimisticExecute({
        apiCall,
        onOptimistic: vi.fn(),
        onRollback: vi.fn(),
      });

      // Second call should be rejected immediately
      const secondResult = await optimisticExecute({
        apiCall,
        onOptimistic: vi.fn(),
        onRollback: vi.fn(),
      });

      expect(secondResult).toBe(false);
      expect(apiCall).toHaveBeenCalledTimes(1); // only called once

      resolveApi('ok');
      await firstPromise;
    });
  });
});
