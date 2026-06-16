import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';

// ---- Mock useCircleStoreWithOut ----
let _isAdmin = false;
vi.mock('/@/store/modules/circle', () => ({
  useCircleStoreWithOut: vi.fn(() => ({
    isCreator: _isAdmin,
    isModerator: _isAdmin,
    isMember: !_isAdmin,
  })),
}));

// ---- Mock ant-design-vue ----
vi.mock('ant-design-vue', async (importOriginal) => {
  const actual = await importOriginal<any>();
  return {
    ...actual,
    Dropdown: {
      name: 'ADropdown',
      template: '<div class="ant-dropdown"><slot /><slot name="overlay" /></div>',
      props: ['trigger', 'disabled'],
    },
    Button: {
      name: 'AButton',
      template: '<button class="ant-btn" :disabled="disabled || loading"><slot /></button>',
      props: ['size', 'loading', 'disabled'],
    },
    Menu: {
      name: 'AMenu',
      template: '<div class="ant-menu"><slot /></div>',
      Item: {
        name: 'AMenuItem',
        template: '<div class="ant-menu-item"><slot /></div>',
        props: ['key', 'danger'],
      },
      Divider: {
        name: 'AMenuDivider',
        template: '<div class="ant-menu-divider" />',
      },
    },
  };
});

import CircleContentActionMenu from '../CircleContentActionMenu.vue';

function mountMenu(props: Record<string, any> = {}) {
  return mount(CircleContentActionMenu, {
    props: { isPinned: false, isFeatured: false, ...props },
  });
}

describe('CircleContentActionMenu', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    _isAdmin = false;
  });

  it('管理员显示置顶/精华/删除菜单项', () => {
    _isAdmin = true;
    const wrapper = mountMenu();
    expect(wrapper.text()).toContain('置顶');
    expect(wrapper.text()).toContain('标记精华');
    expect(wrapper.text()).toContain('删除');
  });

  it('已置顶内容显示"取消置顶"', () => {
    _isAdmin = true;
    const wrapper = mountMenu({ isPinned: true });
    const items = wrapper.findAll('.ant-menu-item');
    const texts = items.map((item) => item.text());
    expect(texts).toContain('取消置顶');
    expect(texts).not.toContain('置顶');
  });

  it('已精华内容显示"取消精华"', () => {
    _isAdmin = true;
    const wrapper = mountMenu({ isFeatured: true });
    expect(wrapper.text()).toContain('取消精华');
    expect(wrapper.text()).not.toContain('标记精华');
  });

  it('普通成员仅显示"举报"选项', () => {
    const wrapper = mountMenu();
    const items = wrapper.findAll('.ant-menu-item');
    const texts = items.map((item) => item.text());
    expect(texts).toContain('举报');
    expect(texts).not.toContain('置顶');
    expect(texts).not.toContain('标记精华');
    expect(texts).not.toContain('删除');
  });

  it('loading 时按钮禁用', () => {
    _isAdmin = true;
    const wrapper = mountMenu({ loading: true });
    const btn = wrapper.find('.ant-btn');
    expect(btn.attributes('disabled')).toBeDefined();
  });
});
