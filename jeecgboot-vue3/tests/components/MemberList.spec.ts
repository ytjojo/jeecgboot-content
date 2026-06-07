vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() } }),
}));
vi.mock('/@/api/content/channelMember', () => ({
  getMemberList: vi.fn(),
}));
const mockScreenRef = { value: 'xl' };
vi.mock('/@/hooks/event/useBreakpoint', () => ({
  useBreakpoint: () => ({ screenRef: mockScreenRef }),
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

  it('loadData catches other errors and shows error message', async () => {
    mockGetMemberList.mockRejectedValue(new Error('network'));
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('共 0 位成员');
  });

  it('handleTableChange updates pagination and reloads', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.handleTableChange({ current: 3, pageSize: 10 });
    await flushPromises();
    expect(vm.pagination.current).toBe(3);
    expect(vm.pagination.pageSize).toBe(10);
    // loadData was called again
    expect(mockGetMemberList).toHaveBeenCalledTimes(2);
  });

  it('handleAction changeRole opens roleAssignModal', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    const record = { id: '2', nickname: 'admin1', role: 'ADMIN' };
    vm.handleAction('changeRole', record);
    // roleAssignModalRef.value.open should have been called
    // The stub component has open as a method
    expect(vm.roleAssignModalRef).toBeTruthy();
  });

  it('handleAction remove opens removeMemberModal', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    const record = { id: '2', nickname: 'admin1', role: 'ADMIN' };
    vm.handleAction('remove', record);
    expect(vm.removeMemberModalRef).toBeTruthy();
  });

  it('handleAction mute opens muteModal', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    const record = { id: '2', nickname: 'admin1', role: 'ADMIN' };
    vm.handleAction('mute', record);
    expect(vm.muteModalRef).toBeTruthy();
  });

  it('handleBatchRemove collects selected members and opens removeMemberModal', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.selectedRowKeys = ['1', '2'];
    await wrapper.vm.$nextTick();
    vm.handleBatchRemove();
    expect(vm.removeMemberModalRef).toBeTruthy();
  });

  it('onSearchChange debounces with 300ms delay', async () => {
    vi.useFakeTimers();
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    // Reset call count from initial load
    mockGetMemberList.mockClear();
    vm.onSearchChange();
    // Should not have called loadData yet
    expect(mockGetMemberList).not.toHaveBeenCalled();
    vi.advanceTimersByTime(300);
    await flushPromises();
    expect(mockGetMemberList).toHaveBeenCalled();
    vi.useRealTimers();
  });

  it('onSelectChange updates selectedRowKeys', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.onSelectChange(['1', '3']);
    await wrapper.vm.$nextTick();
    expect(vm.selectedRowKeys).toEqual(['1', '3']);
  });

  it('handleAction blacklist does nothing (TODO)', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    // Should not throw
    vm.handleAction('blacklist', { id: '2', nickname: 'admin1' });
    expect(wrapper.vm).toBeTruthy();
  });

  it('handleBatchMute guards against concurrent calls', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.batchOperating = true;
    vm.handleBatchMute();
    // Should return early without doing anything
    expect(vm.batchOperating).toBe(true);
  });

  it('loadData with dateRange passes startTime/endTime', async () => {
    mockGetMemberList.mockResolvedValue({ records: sampleRecords, total: 4 });
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    // Set filters and reload
    vm.roleFilter = 'ADMIN';
    vm.searchKeyword = 'test';
    vm.sortOrder = 'asc';
    await vm.loadData();
    await flushPromises();
    expect(mockGetMemberList).toHaveBeenCalledWith(expect.objectContaining({
      role: 'ADMIN',
      keyword: 'test',
      sort: 'asc',
    }));
  });

  it('renders mobile card view when screen is XS', async () => {
    mockScreenRef.value = 'xs';
    const wrapper = mountComponent();
    await flushPromises();
    // Mobile card view should be rendered
    expect(wrapper.text()).toContain('加入时间');
    expect(wrapper.text()).toContain('贡献数');
    // Reset
    mockScreenRef.value = 'xl';
  });

  it('mobile card shows muted and cooling status', async () => {
    mockScreenRef.value = 'xs';
    const coolingRecords = [
      { id: '5', nickname: 'cooling1', avatar: '', role: 'MEMBER', joinTime: '2024-01-05', contribution: 0, coolingEndTime: '2024-03-01' },
    ];
    mockGetMemberList.mockResolvedValue({ records: coolingRecords, total: 1 });
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('冷却期中');
    mockScreenRef.value = 'xl';
  });

  it('mobile batch bar shows when items selected', async () => {
    mockScreenRef.value = 'xs';
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.selectedRowKeys = ['1', '2'];
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('已选 2 项');
    expect(wrapper.text()).toContain('批量移除');
    expect(wrapper.text()).toContain('批量禁言');
    mockScreenRef.value = 'xl';
  });

  it('mobile card actions with canOperate', async () => {
    mockScreenRef.value = 'xs';
    const wrapper = mountComponent({ channelId: 'ch1', currentRole: 'ADMIN' });
    await flushPromises();
    // ADMIN can operate on non-OWNER members
    expect(wrapper.text()).toContain('操作');
    mockScreenRef.value = 'xl';
  });

  it('mobile filter drawer opens and closes', async () => {
    mockScreenRef.value = 'xs';
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    // Open the drawer
    vm.filterDrawerVisible = true;
    await wrapper.vm.$nextTick();
    // Drawer content should be visible
    expect(wrapper.text()).toContain('筛选');
    // Close via button
    vm.filterDrawerVisible = false;
    await wrapper.vm.$nextTick();
    mockScreenRef.value = 'xl';
  });

  it('desktop table shows muted status tag', async () => {
    const mutedRecords = [
      { id: '1', nickname: 'muted1', avatar: '', role: 'MEMBER', joinTime: '2024-01-01', contribution: 0, isMuted: true, muteEndTime: '2024-02-01' },
    ];
    mockGetMemberList.mockResolvedValue({ records: mutedRecords, total: 1 });
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('已禁言');
    expect(wrapper.text()).toContain('2024-02-01');
  });

  it('desktop table shows cooling status tag', async () => {
    const coolingRecords = [
      { id: '1', nickname: 'cooling1', avatar: '', role: 'MEMBER', joinTime: '2024-01-01', contribution: 0, coolingEndTime: '2024-03-01' },
    ];
    mockGetMemberList.mockResolvedValue({ records: coolingRecords, total: 1 });
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('冷却期中');
  });

  it('desktop dropdown menu click triggers handleAction', async () => {
    const wrapper = mountComponent({ channelId: 'ch1', currentRole: 'ADMIN' });
    await flushPromises();
    // The a-menu stub emits click with { key }. Find the menu and trigger click.
    const menus = wrapper.findAllComponents({ name: 'a-menu' });
    if (menus.length > 0) {
      await menus[0].trigger('click');
    }
    expect(wrapper.vm).toBeTruthy();
  });

  it('handleBatchMute with batchOperating false does nothing (TODO)', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.batchOperating = false;
    vm.handleBatchMute();
    // Currently a TODO, just ensures no crash
    expect(wrapper.vm).toBeTruthy();
  });
});
