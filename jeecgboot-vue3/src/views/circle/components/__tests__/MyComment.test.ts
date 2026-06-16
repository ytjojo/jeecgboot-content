import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { ref, reactive } from 'vue';
import MyComment from '../MyComment.vue';
import type { MentionState, MentionMember } from '../../composables/useMention';

// Mock useMention composable
const mockMentionState = reactive<MentionState>({
  isOpen: false,
  searchKeyword: '',
  members: [
    { id: 'm1', userId: 'u1', nickname: '张三', avatar: '', role: 'MEMBER' },
    { id: 'm2', userId: 'u2', nickname: '李四', avatar: '', role: 'CREATOR' },
  ],
  selectedIndex: 0,
  loading: false,
  error: null,
});

const mockSelectMember = vi.fn();
const mockOnInput = vi.fn();
const mockSearchMembers = vi.fn();
const mockClosePicker = vi.fn();
const mockRenderContent = vi.fn();
const mockNavigateKeyboard = vi.fn();
const mockHandleOutsideClick = vi.fn();

vi.mock('../../composables/useMention', () => ({
  useMention: () => ({
    mentionState: mockMentionState,
    onInput: mockOnInput,
    searchMembers: mockSearchMembers,
    selectMember: mockSelectMember,
    closePicker: mockClosePicker,
    renderContent: mockRenderContent,
    navigateKeyboard: mockNavigateKeyboard,
    handleOutsideClick: mockHandleOutsideClick,
  }),
}));

function mountComponent(props?: { circleId?: string; placeholder?: string; submitLabel?: string }) {
  return mount(MyComment, {
    props: {
      circleId: props?.circleId ?? 'circle-1',
      placeholder: props?.placeholder ?? '输入评论...',
      submitLabel: props?.submitLabel ?? '发送',
    },
    global: {
      stubs: {
        MentionMemberPicker: {
          template: '<div class="picker-stub" v-if="state.isOpen"><div class="member-item" v-for="m in state.members" :key="m.id" @click="$emit(\'select\', m)">{{ m.nickname }}</div></div>',
          props: ['state'],
          emits: ['select', 'search'],
        },
        'a-button': {
          template: '<button class="btn-stub" @click="$emit(\'click\')"><slot /></button>',
          props: ['type'],
          emits: ['click'],
        },
      },
    },
  });
}

describe('MyComment', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    Object.assign(mockMentionState, {
      isOpen: false,
      searchKeyword: '',
      members: [
        { id: 'm1', userId: 'u1', nickname: '张三', avatar: '', role: 'MEMBER' },
        { id: 'm2', userId: 'u2', nickname: '李四', avatar: '', role: 'CREATOR' },
      ],
      selectedIndex: 0,
      loading: false,
      error: null,
    });
    mockSelectMember.mockReturnValue('@{userId:u1}张三');
  });

  // 1. 渲染 textarea
  it('应渲染 textarea 输入框', () => {
    const wrapper = mountComponent();
    expect(wrapper.find('textarea').exists()).toBe(true);
    expect(wrapper.find('textarea').attributes('placeholder')).toBe('输入评论...');
  });

  // 2. 渲染提交按钮
  it('应渲染提交按钮', () => {
    const wrapper = mountComponent();
    expect(wrapper.find('.btn-stub').exists()).toBe(true);
    expect(wrapper.find('.btn-stub').text()).toBe('发送');
  });

  // 3. 输入时调用 onInput
  it('输入 @ 时应调用 onInput 检测', async () => {
    const wrapper = mountComponent();
    const textarea = wrapper.find('textarea');

    await textarea.setValue('hello @');
    expect(mockOnInput).toHaveBeenCalled();
  });

  // 4. @ 触发后显示 MentionMemberPicker
  it('@ 触发后 mentionState.isOpen=true 时应显示 MentionMemberPicker', async () => {
    const wrapper = mountComponent();

    // 模拟 mentionState 变为 open
    mockMentionState.isOpen = true;

    await wrapper.vm.$nextTick();

    expect(wrapper.find('.picker-stub').exists()).toBe(true);
  });

  // 5. 选择成员后插入文本
  it('选择成员后应插入提及文本到 textarea', async () => {
    const wrapper = mountComponent();

    mockMentionState.isOpen = true;
    await wrapper.vm.$nextTick();

    // 模拟输入了 @
    const textarea = wrapper.find('textarea');
    await textarea.setValue('你好 @');

    // 点击 picker 中的成员
    const memberItems = wrapper.findAll('.member-item');
    expect(memberItems).toHaveLength(2);

    await memberItems[0].trigger('click');

    expect(mockSelectMember).toHaveBeenCalledWith({
      id: 'm1',
      userId: 'u1',
      nickname: '张三',
      avatar: '',
      role: 'MEMBER',
    });
  });

  // 6. 键盘导航在 textarea 中触发
  it('textarea 中按 ArrowDown 应调用 navigateKeyboard', async () => {
    const wrapper = mountComponent();
    mockMentionState.isOpen = true;
    await wrapper.vm.$nextTick();

    const textarea = wrapper.find('textarea');
    await textarea.trigger('keydown', { key: 'ArrowDown' });

    expect(mockNavigateKeyboard).toHaveBeenCalledWith('ArrowDown');
  });

  it('textarea 中按 Enter 且 picker 未打开时应提交', async () => {
    const wrapper = mountComponent();
    mockMentionState.isOpen = false;
    await wrapper.vm.$nextTick();

    const textarea = wrapper.find('textarea');
    await textarea.setValue('测试评论');
    await textarea.trigger('keydown', { key: 'Enter', shiftKey: false });

    // 当 picker 未打开时，Enter 应提交
    const emitted = wrapper.emitted('submit');
    expect(emitted).toBeTruthy();
    expect(emitted![0]).toEqual(['测试评论']);
  });

  // 7. 提交按钮点击
  it('点击提交按钮应 emit submit', async () => {
    const wrapper = mountComponent();
    const textarea = wrapper.find('textarea');
    await textarea.setValue('我的评论');

    await wrapper.find('.btn-stub').trigger('click');

    const emitted = wrapper.emitted('submit');
    expect(emitted).toBeTruthy();
    expect(emitted![0]).toEqual(['我的评论']);
  });

  // 8. Shift+Enter 不提交
  it('Shift+Enter 不应提交评论', async () => {
    const wrapper = mountComponent();
    mockMentionState.isOpen = false;
    await wrapper.vm.$nextTick();

    const textarea = wrapper.find('textarea');
    await textarea.setValue('测试换行');
    await textarea.trigger('keydown', { key: 'Enter', shiftKey: true });

    const emitted = wrapper.emitted('submit');
    expect(emitted).toBeFalsy();
  });

  // 9. 空内容不提交
  it('空内容或纯空白时不应提交', async () => {
    const wrapper = mountComponent();
    const textarea = wrapper.find('textarea');

    await textarea.setValue('   ');
    await wrapper.find('.btn-stub').trigger('click');

    const emitted = wrapper.emitted('submit');
    expect(emitted).toBeFalsy();
  });

  // 10. 提交后清空输入框
  it('提交后应清空 textarea', async () => {
    const wrapper = mountComponent();
    const textarea = wrapper.find('textarea');

    await textarea.setValue('评论内容');
    await wrapper.find('.btn-stub').trigger('click');

    // 提交后 textarea 应清空
    expect((textarea.element as HTMLTextAreaElement).value).toBe('');
  });
});
