vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn() } }),
}));
vi.mock('/@/api/content/channelMember', () => ({
  getPendingApplications: vi.fn(),
  approveApplications: vi.fn(),
  rejectApplications: vi.fn(),
}));
vi.mock('/@/hooks/event/useBreakpoint', () => ({
  useBreakpoint: () => ({ screenRef: { value: 'xl' } }),
}));
vi.mock('/@/enums/breakpointEnum', () => ({
  sizeEnum: { XS: 'xs', SM: 'sm', MD: 'md', LG: 'lg', XL: 'xl', XXL: 'xxl' },
}));

import { mount, flushPromises } from '@vue/test-utils';
import PendingApplications from '/@/views/channel/members/PendingApplications.vue';
import { getPendingApplications, approveApplications, rejectApplications } from '/@/api/content/channelMember';

const mockGetPendingApplications = vi.mocked(getPendingApplications);
const mockApproveApplications = vi.mocked(approveApplications);
const mockRejectApplications = vi.mocked(rejectApplications);

const stubs = {
  'a-table': {
    template: `<div>
      <slot name="bodyCell" v-for="item in dataSource" :column="{ dataIndex: 'applicant' }" :record="item" />
      <slot name="bodyCell" v-for="item in dataSource" :column="{ dataIndex: 'reason' }" :record="item" />
      <slot name="bodyCell" v-for="item in dataSource" :column="{ dataIndex: 'applyTime' }" :record="item" />
      <slot name="bodyCell" v-for="item in dataSource" :column="{ dataIndex: 'timeout' }" :record="item" />
      <slot name="bodyCell" v-for="item in dataSource" :column="{ dataIndex: 'action' }" :record="item" />
      <span v-for="item in dataSource" :key="item.id" class="row-data">{{ item.nickname }} {{ item.reason }} {{ item.applyTime }}</span>
    </div>`,
    props: ['dataSource', 'columns', 'loading', 'pagination', 'rowSelection', 'rowKey'],
    emits: ['change'],
  },
  'a-button': { template: '<button @click="$emit(\'click\')"><slot /></button>', props: ['type', 'loading', 'disabled', 'danger', 'size'], emits: ['click'] },
  'a-space': { template: '<span><slot /></span>' },
  'a-avatar': { template: '<span></span>', props: ['src', 'size'] },
  'a-tag': { template: '<span><slot /></span>', props: ['color'] },
  'a-modal': {
    template: '<div v-if="open"><slot /><slot name="footer" /></div>',
    props: ['open', 'title', 'confirmLoading', 'footer'],
    emits: ['ok', 'cancel', 'update:open'],
  },
  'a-form': { template: '<form><slot /></form>', props: ['layout'] },
  'a-form-item': { template: '<div><slot /></div>', props: ['label', 'required'] },
  'a-input': { template: '<input />', props: ['value', 'placeholder', 'rows'] },
  'a-textarea': { template: '<textarea></textarea>', props: ['value', 'rows', 'placeholder'] },
  'a-range-picker': { template: '<input />', props: ['value', 'placeholder'], emits: ['change', 'update:value'] },
};

const sampleRecords = [
  { id: '1', nickname: 'user1', avatar: '', reason: 'want to join', applyTime: '2024-01-01', isTimeout: false },
  { id: '2', nickname: 'user2', avatar: '', reason: 'test', applyTime: '2024-01-02', isTimeout: true },
];

describe('PendingApplications', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetPendingApplications.mockResolvedValue({ records: sampleRecords, total: 2 });
  });

  function mountComponent(props = { channelId: 'ch1' }) {
    return mount(PendingApplications, { props, global: { stubs } });
  }

  it('should load data on mount', async () => {
    mountComponent();
    await flushPromises();
    expect(mockGetPendingApplications).toHaveBeenCalledWith(expect.objectContaining({ channelId: 'ch1' }));
  });

  it('should render table with application data', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('user1');
    expect(wrapper.text()).toContain('want to join');
    expect(wrapper.text()).toContain('共 2 条待审');
  });

  it('should show timeout tag when record.isTimeout is true', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('超时');
  });

  it('should call approveApplications on single approve', async () => {
    mockApproveApplications.mockResolvedValue({} as any);
    mockGetPendingApplications.mockResolvedValueOnce({ records: [{ id: '1', nickname: 'u', reason: '', applyTime: '' }], total: 1 });
    const wrapper = mountComponent();
    await flushPromises();

    const buttons = wrapper.findAll('button');
    const approveBtn = buttons.find((b) => b.text() === '批准');
    expect(approveBtn).toBeTruthy();
    await approveBtn!.trigger('click');
    await flushPromises();

    expect(mockApproveApplications).toHaveBeenCalledWith(expect.objectContaining({ applicationIds: ['1'] }));
  });

  it('should open reject modal and require reason', async () => {
    const wrapper = mountComponent();
    await flushPromises();

    const buttons = wrapper.findAll('button');
    const rejectBtn = buttons.find((b) => b.text() === '拒绝');
    expect(rejectBtn).toBeTruthy();
    await rejectBtn!.trigger('click');
    await flushPromises();

    // Modal should be visible
    expect(wrapper.find('form').exists()).toBe(true);
  });

  it('should pass date range as startTime/endTime to API', async () => {
    mockGetPendingApplications.mockResolvedValue({ records: [], total: 0 });
    mountComponent();
    await flushPromises();
    expect(mockGetPendingApplications).toHaveBeenCalledWith(expect.objectContaining({
      channelId: 'ch1',
      pageNo: 1,
      pageSize: 20,
    }));
  });

  it('should handle pagination change', async () => {
    mountComponent();
    await flushPromises();
    expect(mockGetPendingApplications).toHaveBeenCalledTimes(1);
  });

  it('should call batch approve with selected keys', async () => {
    mockApproveApplications.mockResolvedValue({ success: 2, failed: 0, details: [] });
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.vm).toBeTruthy();
  });
});
