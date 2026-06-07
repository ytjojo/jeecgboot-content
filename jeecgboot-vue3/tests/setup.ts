// jsdom 缺失的浏览器 API mock
Object.defineProperty(window, 'getComputedStyle', {
  value: () => ({
    getPropertyValue: () => '',
    width: '',
    height: '',
    paddingLeft: '',
    paddingRight: '',
    paddingTop: '',
    paddingBottom: '',
    marginLeft: '',
    marginRight: '',
    marginTop: '',
    marginBottom: '',
  }),
});

Object.defineProperty(window, 'matchMedia', {
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }),
});

// ResizeObserver mock
class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}
(window as any).ResizeObserver = ResizeObserverMock;

// IntersectionObserver mock
class IntersectionObserverMock {
  constructor() {}
  observe() {}
  unobserve() {}
  disconnect() {}
}
(window as any).IntersectionObserver = IntersectionObserverMock;

// scrollTo mock
window.scrollTo = () => {};

// Mock missing VITE_GLOB env vars for test environment
vi.mock('/@/utils/env', async (importOriginal) => {
  const mod = await importOriginal();
  return {
    ...mod,
    getAppEnvConfig: () => ({
      VITE_GLOB_APP_TITLE: 'JeecgBoot',
      VITE_GLOB_API_URL: '/jeecg-boot',
      VITE_GLOB_APP_SHORT_NAME: 'JeecgBoot_Pro',
      VITE_GLOB_API_URL_PREFIX: '',
      VITE_GLOB_APP_OPEN_SSO: 'false',
      VITE_GLOB_APP_OPEN_QIANKUN: 'false',
      VITE_GLOB_APP_CAS_BASE_URL: '',
      VITE_GLOB_DOMAIN_URL: 'http://localhost:3100',
      VITE_GLOB_ONLINE_VIEW_URL: '',
      VITE_GLOB_HIDE_LAYOUT_TYPES: '',
      VITE_GLOB_RUN_PLATFORM: '',
      VITE_GLOB_QIANKUN_MICRO_APP_NAME: '',
      VITE_GLOB_QIANKUN_MICRO_APP_ENTRY: '',
    }),
  };
});
