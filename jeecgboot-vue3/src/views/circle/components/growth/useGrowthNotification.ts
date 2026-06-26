import { onMounted, onUnmounted } from 'vue';
import { onWebSocket, offWebSocket } from '/@/hooks/web/useWebSocket';
import { useMessage } from '/@/hooks/web/useMessage';
import { useCircleGrowthStoreWithOut } from '/@/store/modules/circleGrowth';

/**
 * WebSocket 消息类型定义
 */
interface GrowthNotificationMessage {
  type?: string;
  circleId?: string;
  level?: number;
  badgeName?: string;
  [key: string]: unknown;
}

/**
 * 成长通知监听 Composable
 * @param getCurrentCircleId 获取当前页面 circleId 的函数
 * @param onRefresh 收到通知后的刷新回调（可选，默认调用 fetchData 风格回调）
 */
export function useGrowthNotification(
  getCurrentCircleId: () => string | undefined,
  onRefresh?: () => void,
) {
  const { createMessage } = useMessage();
  const circleGrowthStore = useCircleGrowthStoreWithOut();

  /**
   * 处理 WebSocket 消息
   */
  function handleWebSocketMessage(data: GrowthNotificationMessage) {
    try {
      const messageType = data?.type;
      const circleId = data?.circleId;

      // 容错：缺少关键字段时仅展示通用提示，不触发刷新
      if (!messageType || !circleId) {
        createMessage.success('成长数据已更新');
        return;
      }

      // 仅处理当前圈子的通知
      const currentCircleId = getCurrentCircleId();
      if (!currentCircleId || currentCircleId !== circleId) {
        return;
      }

      let shouldRefresh = false;

      switch (messageType) {
        case 'CIRCLE_LEVEL_UP':
          createMessage.success(`恭喜！圈子等级提升至 L${data.level ?? ''}`);
          shouldRefresh = true;
          break;
        case 'MEMBER_LEVEL_UP':
          createMessage.success('恭喜！你在该圈子的等级提升了');
          shouldRefresh = true;
          break;
        case 'BADGE_EARNED':
          createMessage.success(`恭喜获得新徽章：${data.badgeName ?? ''}`);
          shouldRefresh = true;
          break;
        default:
          // 未知类型仅通用提示
          createMessage.success('成长数据已更新');
          break;
      }

      // 触发数据刷新
      if (shouldRefresh) {
        circleGrowthStore.refreshCircle(circleId);
        if (onRefresh) {
          onRefresh();
        }
      }
    } catch (err) {
      console.error('[GrowthNotification] 处理消息失败:', err);
    }
  }

  /**
   * 注册监听
   */
  function registerListener() {
    onWebSocket(handleWebSocketMessage);
  }

  /**
   * 移除监听
   */
  function unregisterListener() {
    offWebSocket(handleWebSocketMessage);
  }

  onMounted(() => {
    registerListener();
  });

  onUnmounted(() => {
    unregisterListener();
  });

  return {
    registerListener,
    unregisterListener,
  };
}
