import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import MoveChannelDialog from '../src/views/channel/components/MoveChannelDialog.vue';

jest.mock('/@/api/content/channel/publish', () => ({
  getAvailableChannels: jest.fn().mockResolvedValue([]),
}));

jest.mock('/@/store/modules/channelGovernance', () => ({
  useChannelGovernanceStore: jest.fn(() => ({
    moveContent: jest.fn().mockResolvedValue({}),
  })),
}));

jest.mock('ant-design-vue', () => {
  const { defineComponent, h } = require('vue');
  return {
    Modal: defineComponent({
      name: 'Modal',
      props: { visible: Boolean, title: String, width: Number },
      emits: ['cancel', 'update:visible'],
      setup(_p: any, { slots }: any) {
        return () => h('div', { class: 'modal' }, [
          _p.title ? h('div', { class: 'modal-title' }, _p.title) : null,
          slots.default?.(),
          slots.footer?.(),
        ]);
      },
    }),
    Select: defineComponent({
      name: 'Select',
      props: { value: String, placeholder: String, options: { type: Array, default: () => [] } },
      emits: ['update:value'],
      setup(_p: any, { emit }: any) {
        return () =>
          h('select', {
            class: 'select-mock',
            value: _p.value || '',
            onChange: (e: any) => emit('update:value', e.target.value),
          },
            (_p.options || []).map((opt: any) =>
              h('option', { value: opt.value }, opt.label)
            )
          );
      },
    }),
    Button: defineComponent({
      name: 'Button',
      props: { type: String, disabled: Boolean, loading: Boolean },
      emits: ['click'],
      setup(_p: any, { slots, emit }: any) {
        return () =>
          h('button', {
            class: ['btn', _p.type === 'primary' ? 'btn-primary' : ''].join(' '),
            disabled: _p.disabled,
            onClick: () => emit('click'),
          }, slots.default?.());
      },
    }),
    message: { success: jest.fn(), error: jest.fn(), info: jest.fn(), warning: jest.fn() },
  };
});

jest.mock('@ant-design/icons-vue', () => ({
  InfoCircleOutlined: { name: 'InfoCircleOutlined', template: '<span />' },
}));

describe('MoveChannelDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('应展示弹窗标题', () => {
    const wrapper = mount(MoveChannelDialog, {
      props: { visible: true, contentId: '1', sourceChannelId: 'ch1' },
    });
    expect(wrapper.text()).toContain('移出频道');
  });

  it('应展示频道选择器', () => {
    const wrapper = mount(MoveChannelDialog, {
      props: { visible: true, contentId: '1', sourceChannelId: 'ch1' },
    });
    expect(wrapper.find('.select-mock').exists()).toBe(true);
  });

  it('选择直接展示频道应显示预期结果', async () => {
    const wrapper = mount(MoveChannelDialog, {
      props: { visible: true, contentId: '1', sourceChannelId: 'ch1' },
    });
    const vm = wrapper.vm as any;
    vm.channelOptions = [
      { label: '公开频道', value: 'ch2', publishResult: 'direct' },
      { label: '审核频道', value: 'ch3', publishResult: 'review' },
    ];
    await wrapper.vm.$nextTick();
    const select = wrapper.find('.select-mock');
    await select.setValue('ch2');
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('将直接展示');
  });

  it('选择审核频道应显示预期结果', async () => {
    const wrapper = mount(MoveChannelDialog, {
      props: { visible: true, contentId: '1', sourceChannelId: 'ch1' },
    });
    const vm = wrapper.vm as any;
    vm.channelOptions = [
      { label: '公开频道', value: 'ch2', publishResult: 'direct' },
      { label: '审核频道', value: 'ch3', publishResult: 'review' },
    ];
    await wrapper.vm.$nextTick();
    const select = wrapper.find('.select-mock');
    await select.setValue('ch3');
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('将进入目标频道待审区');
  });

  it('点击取消应关闭弹窗', async () => {
    const wrapper = mount(MoveChannelDialog, {
      props: { visible: true, contentId: '1', sourceChannelId: 'ch1' },
    });
    const buttons = wrapper.findAll('button');
    const cancelBtn = buttons.find(b => b.text() === '取消');
    expect(cancelBtn).toBeTruthy();
    await cancelBtn!.trigger('click');
    expect(wrapper.emitted('update:visible')).toBeTruthy();
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });
});
