import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { ref, type Ref } from 'vue';
import { useMention } from '../useMention';

const { mockGetMemberList } = vi.hoisted(() => ({
  mockGetMemberList: vi.fn(),
}));

vi.mock('/@/api/content/circle', () => ({
  getMemberList: mockGetMemberList,
}));

const mockMembers = [
  { id: 'm1', userId: 'u1', nickname: '张三', avatar: '', role: 'MEMBER', status: 'ACTIVE', muteEndTime: null, createTime: '2026-01-01' },
  { id: 'm2', userId: 'u2', nickname: '李四', avatar: '', role: 'MEMBER', status: 'ACTIVE', muteEndTime: null, createTime: '2026-01-02' },
  { id: 'm3', userId: 'u3', nickname: '王五', avatar: '', role: 'CREATOR', status: 'ACTIVE', muteEndTime: null, createTime: '2026-01-03' },
];

function createCircleId(): Ref<string> {
  return ref('circle-1');
}

describe('useMention', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    mockGetMemberList.mockResolvedValue({
      records: mockMembers,
      total: 3,
      pages: 1,
      current: 1,
      size: 100,
    });
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  // 1. @触发检测: 在光标位置前有 @ 时打开浮层
  it('应在检测到 @ 字符时打开浮层', () => {
    const circleId = createCircleId();
    const { mentionState, onInput } = useMention(circleId);

    onInput('hello @', 7);

    expect(mentionState.isOpen).toBe(true);
    expect(mentionState.searchKeyword).toBe('');
    expect(mentionState.selectedIndex).toBe(0);
  });

  // 2. @不触发场景: 普通文本输入不触发
  it('普通文本输入不触发浮层', () => {
    const circleId = createCircleId();
    const { mentionState, onInput } = useMention(circleId);

    onInput('hello world', 11);

    expect(mentionState.isOpen).toBe(false);
  });

  it('空输入不触发浮层', () => {
    const circleId = createCircleId();
    const { mentionState, onInput } = useMention(circleId);

    onInput('', 0);

    expect(mentionState.isOpen).toBe(false);
  });

  // 3. 防抖搜索: 快速连续调用 searchMembers 只触发一次过滤
  it('快速连续调用 searchMembers 应防抖 300ms', async () => {
    const circleId = createCircleId();
    const { mentionState, onInput, searchMembers } = useMention(circleId);

    // 先触发 @ 加载成员
    onInput('@', 1);
    await vi.runAllTimersAsync();
    expect(mentionState.members).toHaveLength(3);

    // 快速连续搜索
    searchMembers('张');
    searchMembers('张三');
    searchMembers('张');

    // 300ms 前，members 应仍是全部成员（未触发过滤）
    expect(mentionState.members).toHaveLength(3);

    // 推进 300ms
    await vi.advanceTimersByTimeAsync(300);

    // 最后一次搜索生效（keyword='张'）
    expect(mentionState.members).toHaveLength(1);
    expect(mentionState.members[0].nickname).toBe('张三');
  });

  // 4. 标记插入: selectMember 返回正确格式
  it('selectMember 应返回 @{userId:xxx}昵称 格式', () => {
    const circleId = createCircleId();
    const { mentionState, selectMember, onInput } = useMention(circleId);

    // 先打开浮层
    onInput('@', 1);

    const result = selectMember({
      id: 'm1',
      userId: 'u1',
      nickname: '张三',
      avatar: '',
      role: 'MEMBER',
    });

    expect(result).toBe('@{userId:u1}张三');
    expect(mentionState.isOpen).toBe(false);
  });

  // 5. 标记渲染: renderContent 正确解析纯文本标记
  // 说明: 正则 @{userId:(.+?)}(.+?)(?=@|$) 中昵称部分延伸到下一个 @ 或结尾
  it('renderContent 应正确解析 @{userId:xxx}昵称 纯文本标记', () => {
    const circleId = createCircleId();
    const { renderContent } = useMention(circleId);

    // 单个 mention，昵称延续到结尾
    const parts = renderContent('你好 @{userId:u1}张三');

    expect(parts).toHaveLength(2);
    expect(parts[0]).toEqual({ type: 'text', content: '你好 ' });
    expect(parts[1]).toEqual({ type: 'mention', content: '@张三', userId: 'u1' });
  });

  it('renderContent 应处理多个 @mention', () => {
    const circleId = createCircleId();
    const { renderContent } = useMention(circleId);

    // 多个 mention：第一个 mention 昵称延续到下一个 @
    const parts = renderContent('@{userId:u1}张三 @{userId:u2}李四');

    expect(parts).toHaveLength(2);
    expect(parts[0]).toEqual({ type: 'mention', content: '@张三 ', userId: 'u1' });
    expect(parts[1]).toEqual({ type: 'mention', content: '@李四', userId: 'u2' });
  });

  it('renderContent 纯文本无 mention 时返回全部为 text', () => {
    const circleId = createCircleId();
    const { renderContent } = useMention(circleId);

    const parts = renderContent('普通文本无提及');

    expect(parts).toHaveLength(1);
    expect(parts[0]).toEqual({ type: 'text', content: '普通文本无提及' });
  });

  // 6. 富文本标记渲染: renderContent 正确解析 <span class="mention"> 标记
  it('renderContent 应正确解析富文本 <span class="mention"> 标记', () => {
    const circleId = createCircleId();
    const { renderContent } = useMention(circleId);

    const parts = renderContent('你好 <span class="mention" data-user-id="u1">@张三</span>，欢迎');

    expect(parts).toHaveLength(3);
    expect(parts[0]).toEqual({ type: 'text', content: '你好 ' });
    expect(parts[1]).toEqual({ type: 'mention', content: '@张三', userId: 'u1' });
    expect(parts[2]).toEqual({ type: 'text', content: '，欢迎' });
  });

  // 7. 键盘导航 ArrowDown
  it('ArrowDown 应增加 selectedIndex', async () => {
    const circleId = createCircleId();
    const { mentionState, onInput, navigateKeyboard } = useMention(circleId);

    onInput('@', 1);
    await vi.runAllTimersAsync();

    expect(mentionState.selectedIndex).toBe(0);

    navigateKeyboard('ArrowDown');
    expect(mentionState.selectedIndex).toBe(1);

    navigateKeyboard('ArrowDown');
    expect(mentionState.selectedIndex).toBe(2);

    // 到达最后一个，不再增加
    navigateKeyboard('ArrowDown');
    expect(mentionState.selectedIndex).toBe(2);
  });

  // 8. 键盘导航 ArrowUp: selectedIndex 减少（不低于 0）
  it('ArrowUp 应减少 selectedIndex 且不低于 0', async () => {
    const circleId = createCircleId();
    const { mentionState, onInput, navigateKeyboard } = useMention(circleId);

    onInput('@', 1);
    await vi.runAllTimersAsync();

    // 先移动到位置 2
    mentionState.selectedIndex = 2;
    expect(mentionState.selectedIndex).toBe(2);

    navigateKeyboard('ArrowUp');
    expect(mentionState.selectedIndex).toBe(1);

    navigateKeyboard('ArrowUp');
    expect(mentionState.selectedIndex).toBe(0);

    // 不低于 0
    navigateKeyboard('ArrowUp');
    expect(mentionState.selectedIndex).toBe(0);
  });

  // 9. 键盘导航 Enter: 选择当前成员
  it('Enter 应关闭浮层（等同于选择当前成员）', async () => {
    const circleId = createCircleId();
    const { mentionState, onInput, navigateKeyboard } = useMention(circleId);

    onInput('@', 1);
    await vi.runAllTimersAsync();

    // 移动到第二个成员
    navigateKeyboard('ArrowDown');
    expect(mentionState.selectedIndex).toBe(1);
    expect(mentionState.isOpen).toBe(true);

    navigateKeyboard('Enter');
    expect(mentionState.isOpen).toBe(false);
  });

  // 10. 键盘导航 Escape: 关闭浮层
  it('Escape 应关闭浮层', async () => {
    const circleId = createCircleId();
    const { mentionState, onInput, navigateKeyboard } = useMention(circleId);

    onInput('@', 1);
    await vi.runAllTimersAsync();
    expect(mentionState.isOpen).toBe(true);

    navigateKeyboard('Escape');
    expect(mentionState.isOpen).toBe(false);
    expect(mentionState.searchKeyword).toBe('');
    expect(mentionState.selectedIndex).toBe(0);
  });

  // 11. 点击外部关闭: handleOutsideClick 关闭浮层
  it('handleOutsideClick 应关闭浮层', async () => {
    const circleId = createCircleId();
    const { mentionState, onInput, handleOutsideClick } = useMention(circleId);

    onInput('@', 1);
    await vi.runAllTimersAsync();
    expect(mentionState.isOpen).toBe(true);

    handleOutsideClick();
    expect(mentionState.isOpen).toBe(false);
  });

  // 12. 首次加载成员: getMemberList 被调用
  it('首次 @ 触发应调用 getMemberList 加载成员', async () => {
    const circleId = createCircleId();
    const { mentionState, onInput } = useMention(circleId);

    onInput('@', 1);

    expect(mentionState.loading).toBe(true);
    expect(mockGetMemberList).toHaveBeenCalledWith({
      circleId: 'circle-1',
      pageNum: 1,
      pageSize: 100,
    });

    await vi.runAllTimersAsync();

    expect(mentionState.loading).toBe(false);
    expect(mentionState.members).toHaveLength(3);
    expect(mentionState.members[0].nickname).toBe('张三');
  });

  // 13. 缓存重用: 第二次 @ 触发不重新调用 API
  it('缓存重用：第二次 @ 触发不应重新调用 API', async () => {
    const circleId = createCircleId();
    const { mentionState, onInput, closePicker } = useMention(circleId);

    // 第一次触发
    onInput('@', 1);
    await vi.runAllTimersAsync();
    expect(mockGetMemberList).toHaveBeenCalledTimes(1);
    expect(mentionState.members).toHaveLength(3);

    // 关闭
    closePicker();
    expect(mentionState.isOpen).toBe(false);

    // 第二次触发
    onInput('@', 1);
    expect(mockGetMemberList).toHaveBeenCalledTimes(1); // 不再调用
    expect(mentionState.isOpen).toBe(true);
    expect(mentionState.members).toHaveLength(3);
  });

  // 14. 加载失败: 错误状态设置
  it('getMemberList 失败时应设置 error', async () => {
    mockGetMemberList.mockRejectedValue(new Error('Network Error'));

    const circleId = createCircleId();
    const { mentionState, onInput } = useMention(circleId);

    onInput('@', 1);

    expect(mentionState.loading).toBe(true);

    await vi.runAllTimersAsync();

    expect(mentionState.loading).toBe(false);
    expect(mentionState.error).toBe('Network Error');
    expect(mentionState.members).toHaveLength(0);
  });

  // 15. 搜索无结果: members 为空
  it('搜索无结果时 members 应为空数组', async () => {
    const circleId = createCircleId();
    const { mentionState, onInput, searchMembers } = useMention(circleId);

    onInput('@', 1);
    await vi.runAllTimersAsync();
    expect(mentionState.members).toHaveLength(3);

    searchMembers('不存在的名字');
    await vi.advanceTimersByTimeAsync(300);

    expect(mentionState.members).toHaveLength(0);
  });

  // closePicker 应重置状态
  it('closePicker 应重置搜索关键词和选择索引', async () => {
    const circleId = createCircleId();
    const { mentionState, onInput, searchMembers, closePicker } = useMention(circleId);

    onInput('@', 1);
    await vi.runAllTimersAsync();

    searchMembers('张');
    await vi.advanceTimersByTimeAsync(300);
    mentionState.selectedIndex = 1;

    closePicker();

    expect(mentionState.isOpen).toBe(false);
    expect(mentionState.searchKeyword).toBe('');
    expect(mentionState.selectedIndex).toBe(0);
  });

  // 空成员列表时键盘导航不崩溃
  it('空成员列表时键盘导航不应崩溃', () => {
    const circleId = createCircleId();
    const { navigateKeyboard } = useMention(circleId);

    // 未加载任何成员，直接导航不应报错
    expect(() => navigateKeyboard('ArrowDown')).not.toThrow();
    expect(() => navigateKeyboard('ArrowUp')).not.toThrow();
    expect(() => navigateKeyboard('Enter')).not.toThrow();
  });

  // @ 在文本中间也能检测
  it('应在文本中间输入 @ 时检测到', () => {
    const circleId = createCircleId();
    const { mentionState, onInput } = useMention(circleId);

    onInput('hello @ world', 7);

    expect(mentionState.isOpen).toBe(true);
  });
});
