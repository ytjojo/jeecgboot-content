import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { reactive } from 'vue';
import MentionMemberPicker from '../MentionMemberPicker.vue';
import type { MentionState } from '../../composables/useMention';

function createState(overrides?: Partial<MentionState>): MentionState {
  return reactive<MentionState>({
    isOpen: true,
    searchKeyword: '',
    members: [
      { id: 'm1', userId: 'u1', nickname: '张三', avatar: '', role: 'MEMBER' },
      { id: 'm2', userId: 'u2', nickname: '李四', avatar: '', role: 'CREATOR' },
      { id: 'm3', userId: 'u3', nickname: '王五', avatar: '', role: 'MODERATOR' },
    ],
    selectedIndex: 0,
    loading: false,
    error: null,
    ...overrides,
  });
}

function mountComponent(state: MentionState) {
  return mount(MentionMemberPicker, {
    props: { state },
    global: {
      stubs: {
        'a-input': {
          template: '<input class="input-stub" :value="value" @input="$emit(\'update:value\', $event.target.value)" />',
          props: ['value', 'placeholder', 'allowClear'],
          emits: ['update:value'],
        },
        'a-avatar': {
          template: '<span class="avatar-stub"><slot /></span>',
          props: ['src', 'size'],
        },
        'a-tag': {
          template: '<span class="tag-stub" :data-color="color"><slot /></span>',
          props: ['color', 'size'],
        },
      },
    },
  });
}

describe('MentionMemberPicker', () => {
  // 1. 显示成员列表
  it('应显示成员列表', () => {
    const state = createState();
    const wrapper = mountComponent(state);

    expect(wrapper.text()).toContain('张三');
    expect(wrapper.text()).toContain('李四');
    expect(wrapper.text()).toContain('王五');
  });

  // 2. 高亮当前选中项
  it('应高亮当前选中项 (selectedIndex)', () => {
    const state = createState({ selectedIndex: 1 });
    const wrapper = mountComponent(state);

    const items = wrapper.findAll('.mention-picker-item');
    expect(items).toHaveLength(3);
    expect(items[0].classes()).not.toContain('mention-picker-item--active');
    expect(items[1].classes()).toContain('mention-picker-item--active');
    expect(items[2].classes()).not.toContain('mention-picker-item--active');
  });

  // 3. 点击成员触发 select 事件
  it('点击成员应触发 select 事件', async () => {
    const state = createState();
    const wrapper = mountComponent(state);

    const items = wrapper.findAll('.mention-picker-item');
    await items[1].trigger('click');

    const emitted = wrapper.emitted('select');
    expect(emitted).toBeTruthy();
    expect(emitted![0]).toEqual([{
      id: 'm2',
      userId: 'u2',
      nickname: '李四',
      avatar: '',
      role: 'CREATOR',
    }]);
  });

  // 4. 搜索输入触发 search 事件
  it('搜索输入应触发 search 事件', async () => {
    const state = createState();
    const wrapper = mountComponent(state);

    const input = wrapper.find('.input-stub');
    await input.setValue('张');

    const emitted = wrapper.emitted('search');
    expect(emitted).toBeTruthy();
    expect(emitted![0]).toEqual(['张']);
  });

  // 5. 加载状态
  it('应显示加载状态', () => {
    const state = createState({ loading: true });
    const wrapper = mountComponent(state);

    expect(wrapper.text()).toContain('加载中');
    expect(wrapper.find('[data-testid="mention-loading"]').exists()).toBe(true);
  });

  // 6. 错误状态
  it('应显示错误状态', () => {
    const state = createState({ error: '网络错误，请重试', members: [] });
    const wrapper = mountComponent(state);

    expect(wrapper.text()).toContain('网络错误，请重试');
    expect(wrapper.find('[data-testid="mention-error"]').exists()).toBe(true);
  });

  // 7. 空状态（搜索无结果）
  it('搜索无结果时应显示空状态', () => {
    const state = createState({ members: [], searchKeyword: 'xxx' });
    const wrapper = mountComponent(state);

    expect(wrapper.text()).toContain('无匹配成员');
    expect(wrapper.find('[data-testid="mention-empty"]').exists()).toBe(true);
  });

  // 8. 空状态（无成员）
  it('圈子无成员时应显示空状态', () => {
    const state = createState({ members: [], searchKeyword: '' });
    const wrapper = mountComponent(state);

    expect(wrapper.text()).toContain('暂无成员');
    expect(wrapper.find('[data-testid="mention-empty"]').exists()).toBe(true);
  });

  // 9. 角色标签
  it('应显示创建者角色标签', () => {
    const state = createState();
    const wrapper = mountComponent(state);

    const tags = wrapper.findAll('.tag-stub');
    expect(tags).toHaveLength(2); // CREATOR + MODERATOR

    // 李四 (index 1) 是 CREATOR
    const items = wrapper.findAll('.mention-picker-item');
    const creatorItem = items[1];
    expect(creatorItem.find('.tag-stub').text()).toBe('创建者');
    expect(creatorItem.find('.tag-stub').attributes('data-color')).toBe('gold');
  });

  it('应显示版主角色标签', () => {
    const state = createState();
    const wrapper = mountComponent(state);

    // 王五 (index 2) 是 MODERATOR
    const items = wrapper.findAll('.mention-picker-item');
    const moderatorItem = items[2];
    const tags = moderatorItem.findAll('.tag-stub');
    expect(tags[0].text()).toBe('版主');
    expect(tags[0].attributes('data-color')).toBe('blue');
  });

  // 10. 关闭状态下隐藏
  it('isOpen 为 false 时应隐藏', () => {
    const state = createState({ isOpen: false });
    const wrapper = mountComponent(state);

    expect(wrapper.find('[data-testid="mention-picker"]').exists()).toBe(false);
  });

  // 11. 头像显示昵称首字符
  it('应显示昵称首字符作为头像占位', () => {
    const state = createState();
    const wrapper = mountComponent(state);

    const avatars = wrapper.findAll('.avatar-stub');
    expect(avatars).toHaveLength(3);
    expect(avatars[0].text()).toBe('张');
    expect(avatars[1].text()).toBe('李');
    expect(avatars[2].text()).toBe('王');
  });
});
