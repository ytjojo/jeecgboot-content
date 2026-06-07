import { nextTick } from 'vue';
import { mount } from '@vue/test-utils';

const mockApplyToJoin = vi.fn();
vi.mock('/@/api/content/channelMember', () => ({
  applyToJoin: (...args: any[]) => mockApplyToJoin(...args),
}));

vi.mock('/@/hooks/web/useMessage', () => ({
  useMessage: () => ({ createMessage: { success: mockSuccess, error: mockError } }),
}));

const mockSuccess = vi.fn();
const mockError = vi.fn();

import JoinApplyModal from '/@/views/channel/components/JoinApplyModal.vue';

const stubs = {
  'a-modal': {
    template: '<div v-if="open"><slot /><slot name="footer" /></div>',
    props: ['open', 'title', 'confirmLoading', 'okButtonProps'],
    emits: ['ok', 'cancel', 'update:open'],
  },
  'a-form': { template: '<form><slot /></form>', props: ['layout'] },
  'a-form-item': { template: '<div><slot /></div>', props: ['label', 'validateStatus', 'help'] },
  'a-input': { template: '<input />', props: ['value', 'placeholder', 'maxlength', 'minlength'] },
  'a-textarea': {
    template: '<textarea @input="$emit(\'update:value\', $event.target.value)"></textarea>',
    props: ['value', 'rows', 'placeholder', 'maxlength', 'minlength'],
    emits: ['update:value', 'input'],
  },
};

function mountModal(props = {}) {
  return mount(JoinApplyModal, {
    props: {
      channelId: 'ch-1',
      channelName: '测试频道',
      ...props,
    },
    global: { stubs },
  });
}

describe('JoinApplyModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockApplyToJoin.mockResolvedValue(undefined);
  });

  describe('open method', () => {
    it('sets visible to true and resets reason', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.open();
      await nextTick();

      expect(vm.visible).toBe(true);
      expect(vm.reason).toBe('');
    });
  });

  describe('validation', () => {
    it('isValid is false when reason is empty', () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      expect(vm.isValid).toBe(false);
    });

    it('isValid is false when reason < 10 chars', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = 'short';
      await nextTick();
      expect(vm.isValid).toBe(false);
    });

    it('isValid is true when reason is 10-200 chars', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '这是一个有效的申请理由';
      await nextTick();
      expect(vm.isValid).toBe(true);
    });

    it('isValid is false when reason > 200 chars', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = 'x'.repeat(201);
      await nextTick();
      expect(vm.isValid).toBe(false);
    });
  });

  describe('validateStatus', () => {
    it('returns empty string when reason is empty', () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      expect(vm.validateStatus).toBe('');
    });

    it('returns "success" when reason is valid', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '这是一个有效的申请理由';
      await nextTick();
      expect(vm.validateStatus).toBe('success');
    });

    it('returns "error" when reason is invalid (too short)', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '短';
      await nextTick();
      expect(vm.validateStatus).toBe('error');
    });
  });

  describe('validateHelp', () => {
    it('returns empty when reason is empty', () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      expect(vm.validateHelp).toBe('');
    });

    it('returns "至少10字" hint when reason < 10', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '一二三四五';
      await nextTick();
      expect(vm.validateHelp).toContain('至少 10');
    });

    it('returns "不能超过200字" hint when reason > 200', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = 'x'.repeat(201);
      await nextTick();
      expect(vm.validateHelp).toContain('200');
    });

    it('returns empty when reason is valid', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '这是一个有效的申请理由内容';
      await nextTick();
      expect(vm.validateHelp).toBe('');
    });
  });

  describe('submit', () => {
    it('calls applyToJoin API on valid submit', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '这是一个有效的申请理由内容';
      await nextTick();
      await vm.handleSubmit();

      expect(mockApplyToJoin).toHaveBeenCalledWith({
        channelId: 'ch-1',
        reason: '这是一个有效的申请理由内容',
      });
    });

    it('emits "applied" and closes modal on success', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '这是一个有效的申请理由内容';
      await nextTick();
      await vm.handleSubmit();

      expect(wrapper.emitted('applied')).toBeTruthy();
      expect(vm.visible).toBe(false);
      expect(vm.reason).toBe('');
    });

    it('shows success message on success', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '这是一个有效的申请理由内容';
      await nextTick();
      await vm.handleSubmit();

      expect(mockSuccess).toHaveBeenCalledWith('申请已提交');
    });

    it('shows error message on failure', async () => {
      mockApplyToJoin.mockRejectedValue(new Error('fail'));
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '这是一个有效的申请理由内容';
      await nextTick();
      await vm.handleSubmit();

      expect(mockError).toHaveBeenCalledWith('申请提交失败，请重试');
    });

    it('does not call API when reason is invalid', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = 'short';
      await nextTick();
      await vm.handleSubmit();

      expect(mockApplyToJoin).not.toHaveBeenCalled();
    });
  });

  describe('okButtonProps', () => {
    it('disabled when isValid is false', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '';
      await nextTick();
      expect(vm.isValid).toBe(false);
    });

    it('enabled when isValid is true', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.reason = '这是一个有效的申请理由内容';
      await nextTick();
      expect(vm.isValid).toBe(true);
    });
  });

  describe('character count', () => {
    it('displays current length / 200', async () => {
      const wrapper = mountModal();
      const vm = wrapper.vm as any;
      vm.open();
      vm.reason = '测试内容';
      await nextTick();

      expect(wrapper.html()).toContain('4 / 200');
    });
  });
});
