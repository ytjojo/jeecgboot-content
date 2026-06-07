import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import GovernanceActionMenu from '../components/GovernanceActionMenu.vue';

describe('GovernanceActionMenu', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('未置顶时应展示更多按钮', () => {
    const wrapper = mount(GovernanceActionMenu, { props: { isPinned: false, isFeatured: false } });
    // Ant Design Vue Button 中文会加空格: '更 多'
    expect(wrapper.text()).toContain('更');
    expect(wrapper.text()).toContain('多');
  });

  it('组件应正确接收 props 并渲染', () => {
    const wrapper = mount(GovernanceActionMenu, { props: { isPinned: true, isFeatured: false } });
    expect(wrapper.emitted()).not.toHaveProperty('action');
  });

  it('点击菜单项应触发 action 事件', async () => {
    const wrapper = mount(GovernanceActionMenu, { props: { isPinned: false, isFeatured: false } });
    const vm = wrapper.vm as any;
    vm.handleMenuClick({ key: 'pin' });
    expect(wrapper.emitted('action')).toBeTruthy();
    expect(wrapper.emitted('action')![0]).toEqual(['pin']);
  });
});
