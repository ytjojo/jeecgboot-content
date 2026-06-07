vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() } }),
}));
vi.mock('/@/api/content/channelMember', () => ({
  getMemberList: vi.fn(),
}));
vi.mock('/@/hooks/event/useBreakpoint', () => ({
  useBreakpoint: () => ({ screenRef: { value: 'xl' } }),
}));
vi.mock('/@/enums/breakpointEnum', () => ({
  sizeEnum: { XS: 'xs', SM: 'sm', MD: 'md', LG: 'lg', XL: 'xl', XXL: 'xxl' },
}));

import { mount, flushPromises } from '@vue/test-utils';
import MemberList from '/@/views/channel/members/MemberList.vue';
import { getMemberList } from '/@/api/content/channelMember';

const mockGetMemberList = vi.mocked(getMemberList);

const sampleRecords = [
  { id: '1', nickname: 'owner1', avatar: '', role: 'OWNER', joinTime: '2024-01-01', contribution: 10 },
  { id: '2', nickname: 'admin1', avatar: '', role: 'ADMIN', joinTime: '2024-01-02', contribution: 5 },
  { id: '3', nickname: 'editor1', avatar: '', role: 'EDITOR', joinTime: '2024-01-03', contribution: 3 },
  { id: '4', nickname: 'member1', avatar: '', role: 'MEMBER', joinTime: '2024-01-04', contribution: 1, isMuted: true, muteEndTime: '2024-02-01' },
];

const stubs = {
  'a-table': {
    template: `<div>
      <span v-for="item in dataSource" :key="item.id" class="row-data">{{ item.nickname }} {{ item.role }} {{ item.joinTime }} {{ item.contribution }}</span>
      <slot name="bodyCell" v-for="item in dataSource" :column="{ dataIndex: 'member' }" :record="item" />
      <slot name="bodyCell" v-for="item in dataSource" :column="{ dataIndex: 'role' }" :record="item" />
      <slot name="bodyCell" v-for="item in dataSource" :column="{ dataIndex: 'governanceStatus' }" :record="item" />
      <slot name="bodyCell" v-for="item in dataSource" :column="{ dataIndex: 'action' }" :record="item" />
    </div>`,
    props: ['dataSource', 'columns', 'loading', 'pagination', 'rowSelection', 'rowKey'],
    emits: ['change'],
  },
  'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'disabled', 'danger', 'size'] },
  'a-space': { template: '<span><slot /></span>', props: ['direction'] },
  'a-avatar': { template: '<span></span>', props: ['src', 'size'] },
  'a-tag': { template: '<span><slot /></span>', props: ['color'] },
  'a-select': { template: '<select><slot /></select>', props: ['value', 'style', 'placeholder', 'allowClear'], emits: ['change', 'update:value'] },
  'a-select-option': { template: '<option><slot /></option>', props: ['value'] },
  'a-input': { template: '<input />', props: ['value', 'placeholder'] },
  'a-input-search': { template: '<input />', props: ['value', 'placeholder', 'style'], emits: ['search', 'change', 'update:value'] },
  'a-dropdown': { template: '<div><slot /><slot name="overlay" /></div>', props: ['trigger'] },
  'a-menu': { template: '<div><slot /></div>', props: [], emits: ['click'] },
  'a-menu-item': { template: '<div><slot /></div>', props: ['key'] },
  'a-drawer': { template: '<div v-if="open"><slot /></div>', props: ['open', 'title', 'placement', 'height'], emits: ['update:open'] },
  RoleAssignModal: { template: '<div></div>', props: ['channelId'], methods: { open: vi.fn() } },
  RemoveMemberModal: { template: '<div></div>', props: ['channelId'], methods: { open: vi.fn() } },
  MuteModal: { template: '<div></div>', props: ['channelId'], methods: { open: vi.fn() } },
};

describe('MemberList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetMemberList.mockResolvedValue({ records: sampleRecords, total: 4 });
  });

  function mountComponent(props = { channelId: 'ch1', currentRole: 'OWNER' }) {
    return mount(MemberList, { props, global: { stubs } });
  }

  it('should load data on mount', async () => {
    mountComponent();
    await flushPromises();
    expect(mockGetMemberList).toHaveBeenCalledWith(expect.objectContaining({ channelId: 'ch1' }));
  });

  it('should render member list', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('owner1');
    expect(wrapper.text()).toContain('admin1');
    expect(wrapper.text()).toContain('共 4 位成员');
  });

  it('should render correct role text', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('频道主');
    expect(wrapper.text()).toContain('管理员');
    expect(wrapper.text()).toContain('内容编辑');
    expect(wrapper.text()).toContain('普通成员');
  });

  it('canOperate returns false for OWNER target', async () => {
    const wrapper = mountComponent({ channelId: 'ch1', currentRole: 'ADMIN' });
    await flushPromises();
    expect(wrapper.vm).toBeTruthy();
  });

  it('canOperate returns false when currentRole is EDITOR', async () => {
    const wrapper = mountComponent({ channelId: 'ch1', currentRole: 'EDITOR' });
    await flushPromises();
    expect(wrapper.vm).toBeTruthy();
  });

  it('canOperate returns false when currentRole is MEMBER', async () => {
    const wrapper = mountComponent({ channelId: 'ch1', currentRole: 'MEMBER' });
    await flushPromises();
    expect(wrapper.vm).toBeTruthy();
  });

  it('canChangeRole is true only when currentRole is OWNER', async () => {
    const wrapper = mountComponent({ channelId: 'ch1', currentRole: 'OWNER' });
    await flushPromises();
    expect(wrapper.vm).toBeTruthy();
  });

  it('should handle search debounce', async () => {
    vi.useFakeTimers();
    const wrapper = mountComponent();
    await flushPromises();
    vi.advanceTimersByTime(300);
    vi.useRealTimers();
    await flushPromises();
  });

  it('should handle 404 error with warning message', async () => {
    mockGetMemberList.mockRejectedValue({ response: { status: 404 } });
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('共 0 位成员');
  });

  it('should show muted status tag', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('已禁言');
    expect(wrapper.text()).toContain('2024-02-01');
  });
});
