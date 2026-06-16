import { reactive, type Ref } from 'vue';
import { getMemberList } from '/@/api/content/circle';
import type { CircleMemberVO } from '/@/api/content/model/circleModel';

export interface MentionMember {
  id: string;
  userId: string;
  nickname: string;
  avatar?: string;
  role: string;
}

export interface MentionState {
  isOpen: boolean;
  searchKeyword: string;
  members: MentionMember[];
  selectedIndex: number;
  loading: boolean;
  error: string | null;
}

export function useMention(circleId: Ref<string>) {
  const mentionState = reactive<MentionState>({
    isOpen: false,
    searchKeyword: '',
    members: [],
    selectedIndex: 0,
    loading: false,
    error: null,
  });

  // 缓存已加载的成员列表
  let cachedMembers: MentionMember[] = [];

  // 防抖定时器
  let debounceTimer: ReturnType<typeof setTimeout> | null = null;

  // 加载成员列表
  const loadMembers = async () => {
    if (cachedMembers.length > 0) return;
    mentionState.loading = true;
    mentionState.error = null;
    try {
      const res = await getMemberList({
        circleId: circleId.value,
        pageNum: 1,
        pageSize: 100,
      });
      cachedMembers = (res.records || []).map((m: CircleMemberVO) => ({
        id: m.id,
        userId: m.userId,
        nickname: m.nickname || '未知用户',
        avatar: m.avatar,
        role: m.role,
      }));
      mentionState.members = cachedMembers;
    } catch (e: any) {
      mentionState.error = e?.message || '加载成员失败';
      mentionState.members = [];
    } finally {
      mentionState.loading = false;
    }
  };

  // 检测 @ 触发
  const onInput = (value: string, cursorPos: number) => {
    if (cursorPos > 0 && value[cursorPos - 1] === '@') {
      mentionState.isOpen = true;
      mentionState.searchKeyword = '';
      mentionState.selectedIndex = 0;
      if (cachedMembers.length === 0) {
        loadMembers();
      } else {
        mentionState.members = cachedMembers;
      }
    }
  };

  // 防抖搜索
  const searchMembers = (keyword: string) => {
    if (debounceTimer) clearTimeout(debounceTimer);

    mentionState.searchKeyword = keyword;

    debounceTimer = setTimeout(() => {
      if (!keyword) {
        mentionState.members = cachedMembers;
      } else {
        const lower = keyword.toLowerCase();
        mentionState.members = cachedMembers.filter((m) =>
          m.nickname.toLowerCase().includes(lower)
        );
      }
      mentionState.selectedIndex = 0;
    }, 300);
  };

  // 选择成员
  const selectMember = (member: MentionMember): string => {
    mentionState.isOpen = false;
    mentionState.searchKeyword = '';
    mentionState.selectedIndex = 0;
    return `@{userId:${member.userId}}${member.nickname}`;
  };

  // 关闭浮层
  const closePicker = () => {
    mentionState.isOpen = false;
    mentionState.searchKeyword = '';
    mentionState.selectedIndex = 0;
  };

  // 键盘导航
  const navigateKeyboard = (key: 'ArrowUp' | 'ArrowDown' | 'Enter' | 'Escape') => {
    const len = mentionState.members.length;

    switch (key) {
      case 'ArrowUp':
        mentionState.selectedIndex = Math.max(0, mentionState.selectedIndex - 1);
        break;
      case 'ArrowDown':
        if (len > 0) {
          mentionState.selectedIndex = Math.min(len - 1, mentionState.selectedIndex + 1);
        }
        break;
      case 'Enter':
        closePicker();
        break;
      case 'Escape':
        closePicker();
        break;
    }
  };

  // 点击外部关闭
  const handleOutsideClick = () => {
    closePicker();
  };

  // 渲染内容：将 @提及 标记解析为片段数组
  const renderContent = (content: string) => {
    const parts: Array<{ type: 'text' | 'mention'; content: string; userId?: string }> = [];

    // 尝试匹配纯文本格式 @{userId:xxx}昵称
    const pureTextRegex = /@\{userId:(.+?)\}(.+?)(?=@|$)/g;
    // 尝试匹配富文本格式 <span class="mention" data-user-id="xxx">@昵称</span>
    const richTextRegex = /<span\s+class="mention"\s+data-user-id="(.+?)">(.+?)<\/span>/g;

    // 先处理富文本格式
    if (richTextRegex.test(content)) {
      richTextRegex.lastIndex = 0;
      let lastIndex = 0;
      let match: RegExpExecArray | null;

      while ((match = richTextRegex.exec(content)) !== null) {
        if (match.index > lastIndex) {
          parts.push({ type: 'text', content: content.slice(lastIndex, match.index) });
        }
        const userId = match[1];
        const displayContent = match[2];
        parts.push({ type: 'mention', content: displayContent, userId });
        lastIndex = match.index + match[0].length;
      }

      if (lastIndex < content.length) {
        parts.push({ type: 'text', content: content.slice(lastIndex) });
      }

      return parts;
    }

    // 处理纯文本格式
    pureTextRegex.lastIndex = 0;
    let lastIndex = 0;
    let match: RegExpExecArray | null;

    while ((match = pureTextRegex.exec(content)) !== null) {
      if (match.index > lastIndex) {
        parts.push({ type: 'text', content: content.slice(lastIndex, match.index) });
      }
      const userId = match[1];
      const nickname = match[2];
      parts.push({ type: 'mention', content: `@${nickname}`, userId });
      lastIndex = match.index + match[0].length;
    }

    if (lastIndex < content.length) {
      parts.push({ type: 'text', content: content.slice(lastIndex) });
    }

    // 如果没有匹配到任何 mention，返回整个文本
    if (parts.length === 0) {
      parts.push({ type: 'text', content });
    }

    return parts;
  };

  return {
    mentionState,
    onInput,
    searchMembers,
    selectMember,
    closePicker,
    renderContent,
    navigateKeyboard,
    handleOutsideClick,
  };
}
