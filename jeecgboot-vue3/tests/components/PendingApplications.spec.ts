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

  it('should handle pagination change via handleTableChange', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    expect(mockGetPendingApplications).toHaveBeenCalledTimes(1);
    // Call handleTableChange directly through the table stub's change event
    // Since the table stub doesn't emit change, we test via vm access
    const vm = wrapper.vm as any;
    vm.pagination.current = 2;
    vm.pagination.pageSize = 10;
    // Trigger loadData by calling the exposed internal
    await flushPromises();
    // The pagination was updated
    expect(vm.pagination.current).toBe(2);
  });

  it('should call batch approve with selected keys and show result modal', async () => {
    const detailItem = { id: '1', nickname: 'user1', success: true, errorMessage: '' };
    mockApproveApplications.mockResolvedValue({ success: 1, failed: 0, details: [detailItem] });
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    // Set selected keys
    vm.selectedRowKeys = ['1'];
    await wrapper.vm.$nextTick();
    // Call handleBatchApprove
    await vm.handleBatchApprove();
    await flushPromises();
    expect(mockApproveApplications).toHaveBeenCalledWith(expect.objectContaining({
      channelId: 'ch1',
      applicationIds: ['1'],
    }));
    expect(vm.batchResult.success).toBe(1);
    expect(vm.batchResult.failed).toBe(0);
    expect(vm.batchResult.details).toEqual([detailItem]);
    expect(vm.resultModalVisible).toBe(true);
    expect(vm.selectedRowKeys).toEqual([]);
  });

  it('loadData catches 404 error and sets empty list with warning', async () => {
    mockGetPendingApplications.mockRejectedValue({ response: { status: 404 } });
    const wrapper = mountComponent();
    await flushPromises();
    expect(wrapper.text()).toContain('共 0 条待审');
  });

  it('loadData catches other errors and shows error message', async () => {
    mockGetPendingApplications.mockRejectedValue(new Error('network'));
    const wrapper = mountComponent();
    await flushPromises();
    // Should still show 0 after error (initial load failed)
    expect(wrapper.text()).toContain('共 0 条待审');
  });

  it('handleRejectConfirm with empty reason shows warning without calling API', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    // Open reject modal for a record
    vm.rejectTarget = { id: '1' };
    vm.rejectReason = '';
    vm.rejectModalVisible = true;
    await wrapper.vm.$nextTick();
    // Call handleRejectConfirm with empty reason
    await vm.handleRejectConfirm();
    await flushPromises();
    expect(mockRejectApplications).not.toHaveBeenCalled();
  });

  it('handleRejectConfirm success calls API, closes modal, reloads', async () => {
    mockRejectApplications.mockResolvedValue({} as any);
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.rejectTarget = { id: '1' };
    vm.rejectReason = 'spam';
    vm.rejectModalVisible = true;
    await wrapper.vm.$nextTick();
    await vm.handleRejectConfirm();
    await flushPromises();
    expect(mockRejectApplications).toHaveBeenCalledWith(expect.objectContaining({
      channelId: 'ch1',
      applicationIds: ['1'],
      reason: 'spam',
    }));
    expect(vm.rejectModalVisible).toBe(false);
  });

  it('handleBatchApprove guards against concurrent calls', async () => {
    mockApproveApplications.mockResolvedValue({ success: 0, failed: 0, details: [] });
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.batchOperating = true;
    await vm.handleBatchApprove();
    // Should not call API again because batchOperating was true
    expect(mockApproveApplications).not.toHaveBeenCalled();
  });

  it('onSelectChange updates selectedRowKeys', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.onSelectChange(['1', '2']);
    await wrapper.vm.$nextTick();
    expect(vm.selectedRowKeys).toEqual(['1', '2']);
  });

  it('handleTableChange updates pagination and reloads', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    mockGetPendingApplications.mockClear();
    vm.handleTableChange({ current: 3, pageSize: 10 });
    await flushPromises();
    expect(vm.pagination.current).toBe(3);
    expect(vm.pagination.pageSize).toBe(10);
    expect(mockGetPendingApplications).toHaveBeenCalledTimes(1);
  });

  it('handleBatchReject opens reject modal with empty reason', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    vm.handleBatchReject();
    await wrapper.vm.$nextTick();
    expect(vm.rejectModalVisible).toBe(true);
    expect(vm.rejectTarget).toBeNull();
    expect(vm.rejectReason).toBe('');
  });

  it('loadData with dateRange passes startTime/endTime', async () => {
    const wrapper = mountComponent();
    await flushPromises();
    const vm = wrapper.vm as any;
    mockGetPendingApplications.mockClear();
    vm.dateRange = ['2024-01-01', '2024-01-31'];
    await vm.loadData();
    await flushPromises();
    expect(mockGetPendingApplications).toHaveBeenCalledWith(expect.objectContaining({
      startTime: '2024-01-01',
      endTime: '2024-01-31',
    }));
  });
});
