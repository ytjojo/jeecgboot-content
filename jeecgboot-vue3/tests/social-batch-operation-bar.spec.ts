import { mount } from '@vue/test-utils';

describe('BatchOperationBar.vue', () => {
  async function mountComponent(props: {
    selectedCount?: number;
    loading?: boolean;
  } = {}) {
    const Component = (await import('/@/components/social/BatchOperationBar.vue')).default;
    return mount(Component, {
      props: {
        selectedCount: 3,
        ...props,
      },
      global: {
        stubs: {
          Popconfirm: { template: '<div class="popconfirm-stub"><slot /></div>', props: ['title', 'okText', 'cancelText'] },
          Button: { template: '<button class="btn-stub" :loading="loading"><slot /></button>', props: ['type', 'loading'] },
        },
      },
    });
  }

  describe('visibility', () => {
    it('renders when selectedCount > 0', async () => {
      const wrapper = await mountComponent({ selectedCount: 3 });
      expect(wrapper.find('.batch-operation-bar').exists()).toBe(true);
    });

    it('does not render when selectedCount is 0', async () => {
      const wrapper = await mountComponent({ selectedCount: 0 });
      expect(wrapper.find('.batch-operation-bar').exists()).toBe(false);
    });
  });

  describe('selected count display', () => {
    it('shows selected count', async () => {
      const wrapper = await mountComponent({ selectedCount: 5 });
      expect(wrapper.find('.batch-operation-bar__info').text()).toContain('5');
    });
  });

  describe('action buttons', () => {
    it('renders all action buttons', async () => {
      const wrapper = await mountComponent();
      const buttons = wrapper.findAll('.btn-stub');
      // 4 popconfirm buttons + 1 cancel link button
      expect(buttons.length).toBeGreaterThanOrEqual(4);
    });

    it('renders cancel selection button', async () => {
      const wrapper = await mountComponent();
      const buttons = wrapper.findAll('.btn-stub');
      const cancelButton = buttons.find((b) => b.text() === '取消选择');
      expect(cancelButton).toBeTruthy();
    });
  });

  describe('events', () => {
    it('emits cancel when cancel selection is clicked', async () => {
      const wrapper = await mountComponent();
      const vm = wrapper.vm as any;
      vm.handleCancel();
      expect(wrapper.emitted('cancel')).toBeTruthy();
    });

    it('emits batchUnfollow', async () => {
      const wrapper = await mountComponent();
      const vm = wrapper.vm as any;
      vm.handleBatchUnfollow();
      expect(wrapper.emitted('batchUnfollow')).toBeTruthy();
    });

    it('emits batchPause', async () => {
      const wrapper = await mountComponent();
      const vm = wrapper.vm as any;
      vm.handleBatchPause();
      expect(wrapper.emitted('batchPause')).toBeTruthy();
    });

    it('emits batchResume', async () => {
      const wrapper = await mountComponent();
      const vm = wrapper.vm as any;
      vm.handleBatchResume();
      expect(wrapper.emitted('batchResume')).toBeTruthy();
    });

    it('emits batchCancel', async () => {
      const wrapper = await mountComponent();
      const vm = wrapper.vm as any;
      vm.handleBatchCancel();
      expect(wrapper.emitted('batchCancel')).toBeTruthy();
    });
  });
});
