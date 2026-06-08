import { ref, onMounted, onUnmounted } from 'vue';

type MobileBreakpoint = 'sm' | 'md';

const BREAKPOINTS: Record<MobileBreakpoint, number> = {
  sm: 576,
  md: 768,
};

/**
 * 响应式移动端检测 composable
 * @param breakpoint 断点阈值，默认 'md' (768px)
 * @returns isMobile — 是否小于断点宽度
 */
export function useIsMobile(breakpoint: MobileBreakpoint = 'md') {
  const threshold = BREAKPOINTS[breakpoint];
  const isMobile = ref(false);

  function check() {
    isMobile.value = window.innerWidth < threshold;
  }

  onMounted(() => {
    check();
    window.addEventListener('resize', check);
  });

  onUnmounted(() => {
    window.removeEventListener('resize', check);
  });

  return { isMobile };
}
