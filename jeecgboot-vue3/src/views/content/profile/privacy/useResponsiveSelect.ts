/**
 * C9: 移动端 ActionSheet 响应式选择器辅助模块
 * - isMobileViewport(width): 判断是否为移动端视口
 * - resolveSelectComponent(isMobile): 返回 'action-sheet' 或 'select'
 * - mapOptionsToActionItems(options): 将 select options 映射为 ActionSheet items
 * - useResponsiveSelect(): Vue composable，响应式监听视口宽度
 */
import { ref, onMounted, onUnmounted } from 'vue';

const MOBILE_BREAKPOINT = 768;

/** 判断给定宽度是否属于移动端 */
export function isMobileViewport(width: number): boolean {
  return width < MOBILE_BREAKPOINT;
}

/** 根据是否移动端返回应使用的组件类型 */
export function resolveSelectComponent(isMobile: boolean): 'action-sheet' | 'select' {
  return isMobile ? 'action-sheet' : 'select';
}

/** ActionSheet item 接口 */
export interface ActionSheetItem {
  key: string;
  label: string;
  value: string;
}

/** 将 ant-design select options 转为 ActionSheet items */
export function mapOptionsToActionItems(
  options: { value: string; label: string }[],
): ActionSheetItem[] {
  return options.map((opt) => ({
    key: opt.value,
    label: opt.label,
    value: opt.value,
  }));
}

/** Vue composable：响应式检测移动端，返回 isMobile ref */
export function useResponsiveSelect() {
  const isMobile = ref(isMobileViewport(window.innerWidth));

  function onResize() {
    isMobile.value = isMobileViewport(window.innerWidth);
  }

  onMounted(() => {
    window.addEventListener('resize', onResize);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', onResize);
  });

  return { isMobile };
}
