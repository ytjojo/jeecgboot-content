import { vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';

// -- Mock functions --
const mockFetchNotificationConfig = vi.fn().mockResolvedValue(undefined);
const mockSaveConfig = vi.fn().mockResolvedValue(undefined);
const mockFetchGlobalNotificationDefault = vi.fn().mockResolvedValue(undefined);

// Mutable store state for current/global config
let currentNotificationConfig: Record<string, any> | null = null;
let globalNotificationDefault: Record<string, any> | null = null;

// Mutable route query
let routeQuery: Record<string, any> = {};

// -- Module mocks --
vi.mock('/@/store/modules/subscribe', () => ({
  useSubscribeStore: () => ({
    currentNotificationConfig,
    globalNotificationDefault,
    fetchNotificationConfig: mockFetchNotificationConfig,
    fetchGlobalNotificationDefault: mockFetchGlobalNotificationDefault,
    saveConfig: mockSaveConfig,
  }),
}));

vi.mock('/@/store/modules/user', () => ({
  useUserStore: () => ({ getUserInfo: { userId: 'u-1' } }),
}));

vi.mock('/@/store', () => ({ store: {} }));

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: routeQuery }),
  useRouter: () => ({ push: vi.fn() }),
}));

vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({
    createMessage: { success: vi.fn(), error: vi.fn() },
  }),
}));

vi.mock('/@/hooks/event/useBreakpoint', () => ({
  useBreakpoint: () => ({ screenRef: { value: 'MD' } }),
}));

// -- Stubs --
const stubs = {
  'a-switch': {
    template: '<input type="checkbox" class="switch-stub" :checked="checked" />',
    props: ['checked', 'disabled'],
  },
  'a-radio-group': {
    template: '<div class="radio-group-stub"><slot /></div>',
    props: ['value'],
  },
  'a-radio': {
    template: '<label class="radio-stub"><slot /></label>',
    props: ['value'],
  },
  'a-time-picker': {
    template: '<input class="time-picker-stub" />',
    props: ['value', 'format', 'placeholder', 'disabled'],
  },
  'a-button': {
    template: '<button class="btn-stub" :class="{ \'btn-loading\': loading }"><slot /></button>',
    props: ['type', 'loading', 'disabled'],
  },
  'a-spin': {
    template: '<div class="spin-stub"><slot /></div>',
    props: ['spinning'],
  },
  'a-divider': {
    template: '<hr class="divider-stub" />',
    props: ['orientation'],
  },
  'a-form': {
    template: '<form class="form-stub"><slot /></form>',
    props: ['labelCol', 'wrapperCol'],
  },
  'a-form-item': {
    template: '<div class="form-item-stub"><slot /></div>',
    props: ['label', 'wrapperCol'],
  },
  'a-tag': {
    template: '<span class="tag-stub"><slot /></span>',
    props: ['color'],
  },
};

// -- Helper --
async function mountPage(overrides: Record<string, any> = {}, query: Record<string, any> = {}) {
  routeQuery = query;
  const fullConfig = {
    channelInApp: true,
    channelPush: false,
    channelEmail: false,
    frequency: 'realtime',
    quietStart: '',
    quietEnd: '',
    ...overrides,
  };
  // Component reads globalNotificationDefault when no sourceId, currentNotificationConfig when sourceId present
  currentNotificationConfig = fullConfig;
  globalNotificationDefault = fullConfig;
  const { default: NotificationPage } = await import('/@/views/social/subscribe/notification.vue');
  const wrapper = mount(NotificationPage, { global: { stubs } });
  await flushPromises();
  return wrapper;
}

// -- Tests --
describe('notification.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    routeQuery = {};
    currentNotificationConfig = null;
    globalNotificationDefault = null;
  });

  it('renders page title "通知设置"', async () => {
    const wrapper = await mountPage();
    const title = wrapper.find('.notification-config-page__title');
    expect(title.exists()).toBe(true);
    // Default (no sourceId) shows "全局通知设置"
    expect(title.text()).toBe('全局通知设置');
  });

  it('calls fetchNotificationConfig on mount', async () => {
    await mountPage({}, { sourceId: 'src-1' });
    expect(mockFetchNotificationConfig).toHaveBeenCalledWith('u-1', 'src-1');
  });

  it('calls fetchGlobalNotificationDefault when no sourceId', async () => {
    await mountPage();
    expect(mockFetchGlobalNotificationDefault).toHaveBeenCalled();
  });

  it('shows in-app channel switch', async () => {
    const wrapper = await mountPage({ channelInApp: true });
    const switches = wrapper.findAll('.switch-stub');
    expect(switches.length).toBeGreaterThanOrEqual(1);
    expect(switches[0].attributes('checked')).toBeDefined();
  });

  it('shows push channel switch', async () => {
    const wrapper = await mountPage({ channelPush: false });
    const switches = wrapper.findAll('.switch-stub');
    expect(switches.length).toBeGreaterThanOrEqual(2);
  });

  it('shows email channel switch', async () => {
    const wrapper = await mountPage({ channelEmail: false });
    const switches = wrapper.findAll('.switch-stub');
    expect(switches.length).toBeGreaterThanOrEqual(3);
  });

  it('shows frequency radio group', async () => {
    const wrapper = await mountPage();
    expect(wrapper.find('.radio-group-stub').exists()).toBe(true);
  });

  it('has save button', async () => {
    const wrapper = await mountPage();
    const buttons = wrapper.findAll('.btn-stub');
    const saveBtn = buttons.find((b) => b.text().includes('保存'));
    expect(saveBtn).toBeDefined();
  });

  it('shows quiet time pickers', async () => {
    const wrapper = await mountPage();
    const pickers = wrapper.findAll('.time-picker-stub');
    expect(pickers.length).toBeGreaterThanOrEqual(2);
  });

  it('displays current config values', async () => {
    const wrapper = await mountPage({ channelPush: true, channelEmail: true });
    const switches = wrapper.findAll('.switch-stub');
    // Three switches: in-app, push, email
    expect(switches.length).toBe(3);
    // With checked attribute reflecting the config values
    expect(switches[0].attributes('checked')).toBeDefined(); // channelInApp: true
    expect(switches[1].attributes('checked')).toBeDefined(); // channelPush: true
    expect(switches[2].attributes('checked')).toBeDefined(); // channelEmail: true
  });
});
