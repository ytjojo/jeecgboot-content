import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import GovernanceActionMenu from '../src/views/channel/components/GovernanceActionMenu.vue';

vi.mock('ant-design-vue', async () => {
  const { defineComponent, h, getCurrentInstance } = await import('vue');
  return {
    Dropdown: defineComponent({
      name: 'Dropdown',
      props: { trigger: Array },
      setup(_p: any, { slots }: any) {
        return () => h('div', { class: 'dropdown' }, [
          slots.default?.(),
          slots.overlay?.(),
        ]);
      },
    }),
    Button: defineComponent({
      name: 'Button',
      props: { size: String, type: String, danger: Boolean },
      emits: ['click'],
      setup(_p: any, { slots, emit }: any) {
        return () => h('button', { class: 'btn', onClick: () => emit('click') }, slots.default?.());
      },
    }),
    Menu: Object.assign(
      defineComponent({
        name: 'Menu',
        emits: ['click'],
        setup(_p: any, { slots, emit }: any) {
          return () => h('div', {
            class: 'menu',
            onClick: (e: MouseEvent) => {
              const target = e.target as HTMLElement;
              const menuItem = target?.closest?.('.menu-item');
              const key = menuItem?.getAttribute?.('data-key');
              emit('click', { key });
            },
          }, slots.default?.());
        },
      }),
      {
        Item: defineComponent({
          name: 'MenuItem',
          props: { danger: Boolean },
          setup(_p: any, { slots }: any) {
            const instance = getCurrentInstance();
            const key = instance?.vnode?.key;
            return () => h('div', { class: 'menu-item', 'data-key': key }, slots.default?.());
          },
        }),
      }
    ),
  };
});

vi.mock('@ant-design/icons-vue', () => ({}));

describe('GovernanceActionMenu', () => {
  it('应渲染更多按钮', () => {
    const wrapper = mount(GovernanceActionMenu, {
      props: { isPinned: false, isFeatured: false },
    });
    expect(wrapper.find('button').text()).toBe('更多');
  });

  it('点击应触发action事件', async () => {
    const wrapper = mount(GovernanceActionMenu, {
      props: { isPinned: false, isFeatured: false },
    });
    const menuItems = wrapper.findAll('.menu-item');
    const pinItem = menuItems.find(item => item.attributes('data-key') === 'pin');
    expect(pinItem).toBeTruthy();
    await pinItem!.trigger('click');
    expect(wrapper.emitted('action')).toBeTruthy();
    expect(wrapper.emitted('action')![0]).toEqual(['pin']);
  });

  it('置顶状态应显示取消置顶', () => {
    const wrapper = mount(GovernanceActionMenu, {
      props: { isPinned: true, isFeatured: false },
    });
    const menuItems = wrapper.findAll('.menu-item');
    const keys = menuItems.map(item => item.attributes('data-key'));
    expect(keys).toContain('unpin');
    expect(keys).not.toContain('pin');
    const unpinItem = menuItems.find(item => item.attributes('data-key') === 'unpin');
    expect(unpinItem!.text()).toBe('取消置顶');
  });

  it('精华状态应显示取消精华', () => {
    const wrapper = mount(GovernanceActionMenu, {
      props: { isPinned: false, isFeatured: true },
    });
    const menuItems = wrapper.findAll('.menu-item');
    const keys = menuItems.map(item => item.attributes('data-key'));
    expect(keys).toContain('unfeature');
    expect(keys).not.toContain('feature');
    const unfeatureItem = menuItems.find(item => item.attributes('data-key') === 'unfeature');
    expect(unfeatureItem!.text()).toBe('取消精华');
  });
});
