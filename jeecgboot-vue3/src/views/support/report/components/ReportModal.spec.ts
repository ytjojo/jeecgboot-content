import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import ReportModal from './ReportModal.vue';

// Mock API
vi.mock('/@/api/support/report', () => ({
  createReport: vi.fn().mockResolvedValue({ code: 200, result: { reportNo: 'R20260601001' } }),
}));

vi.mock('/@/api/sys/upload', () => ({
  uploadFile: vi.fn().mockResolvedValue({ result: { url: 'https://example.com/file1.jpg' } }),
}));

// Mock message
vi.mock('ant-design-vue', async (importOriginal) => {
  const actual = await importOriginal<any>();
  return {
    ...actual,
    message: {
      success: vi.fn(),
      warning: vi.fn(),
      error: vi.fn(),
    },
  };
});

describe('ReportModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  const defaultProps = {
    visible: true,
    targetType: 'article',
    targetId: '123',
    targetSummary: '测试文章标题...',
  };

  it('should render modal when visible is true', () => {
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: {
        stubs: {
          'a-modal': {
            template: '<div class="ant-modal"><slot /><slot name="footer" /></div>',
            props: ['open', 'title', 'width', 'confirmLoading', 'maskClosable'],
          },
          'a-form': { template: '<form><slot /></form>' },
          'a-form-item': { template: '<div><slot /></div>' },
          'a-radio-group': { template: '<div><slot /></div>' },
          'a-radio': { template: '<div><slot /></div>', props: ['value'] },
          'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
          'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType'] },
          'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'disabled'] },
        },
      },
    });
    expect(wrapper.find('.ant-modal').exists()).toBe(true);
  });

  it('should display target summary', () => {
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: {
        stubs: {
          'a-modal': { template: '<div><slot /><slot name="footer" /></div>' },
          'a-form': { template: '<form><slot /></form>' },
          'a-form-item': { template: '<div><slot /></div>' },
          'a-radio-group': { template: '<div><slot /></div>' },
          'a-radio': { template: '<div><slot /></div>', props: ['value'] },
          'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
          'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType'] },
          'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'disabled'] },
        },
      },
    });
    expect(wrapper.text()).toContain('测试文章标题...');
  });

  it('should show report type options', () => {
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: {
        stubs: {
          'a-modal': { template: '<div><slot /><slot name="footer" /></div>' },
          'a-form': { template: '<form><slot /></form>' },
          'a-form-item': { template: '<div><slot /></div>' },
          'a-radio-group': { template: '<div><slot /></div>' },
          'a-radio': { template: '<div><slot /></div>', props: ['value'] },
          'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
          'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType'] },
          'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'disabled'] },
        },
      },
    });
    expect(wrapper.text()).toContain('色情内容');
    expect(wrapper.text()).toContain('暴力内容');
    expect(wrapper.text()).toContain('诈骗信息');
    expect(wrapper.text()).toContain('骚扰行为');
    expect(wrapper.text()).toContain('其他');
  });

  it('should disable submit when uploading', async () => {
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: {
        stubs: {
          'a-modal': { template: '<div><slot /><slot name="footer" /></div>' },
          'a-form': { template: '<form><slot /></form>' },
          'a-form-item': { template: '<div><slot /></div>' },
          'a-radio-group': { template: '<div><slot /></div>' },
          'a-radio': { template: '<div><slot /></div>', props: ['value'] },
          'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
          'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType'] },
          'a-button': {
            template: '<button :disabled="disabled"><slot /></button>',
            props: ['type', 'loading', 'disabled'],
          },
        },
      },
    });
    // Set uploading via component internals
    (wrapper.vm as any).uploading = true;
    await nextTick();
    const submitBtn = wrapper.find('[data-testid="submit-btn"]');
    expect(submitBtn.attributes('disabled')).toBeDefined();
  });

  it('should emit close when cancel is triggered', async () => {
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: {
        stubs: {
          'a-modal': {
            template: '<div><slot /><slot name="footer" /></div>',
            emits: ['cancel'],
          },
          'a-form': { template: '<form><slot /></form>' },
          'a-form-item': { template: '<div><slot /></div>' },
          'a-radio-group': { template: '<div><slot /></div>' },
          'a-radio': { template: '<div><slot /></div>', props: ['value'] },
          'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
          'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType'] },
          'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'disabled'] },
        },
      },
    });
    // Trigger handleClose directly
    (wrapper.vm as any).handleClose();
    await nextTick();
    expect(wrapper.emitted('close')).toBeTruthy();
  });

  it('should show warning when submitting without reportType', async () => {
    const { message } = await import('ant-design-vue');
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: {
        stubs: {
          'a-modal': { template: '<div><slot /><slot name="footer" /></div>' },
          'a-form': { template: '<form><slot /></form>' },
          'a-form-item': { template: '<div><slot /></div>' },
          'a-radio-group': { template: '<div><slot /></div>' },
          'a-radio': { template: '<div><slot /></div>', props: ['value'] },
          'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
          'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType'] },
          'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'disabled'] },
        },
      },
    });
    await (wrapper.vm as any).handleSubmit();
    expect(message.warning).toHaveBeenCalledWith('请选择举报类型');
  });

  it('should emit success after successful submission', async () => {
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: {
        stubs: {
          'a-modal': { template: '<div><slot /><slot name="footer" /></div>' },
          'a-form': { template: '<form><slot /></form>' },
          'a-form-item': { template: '<div><slot /></div>' },
          'a-radio-group': { template: '<div><slot /></div>' },
          'a-radio': { template: '<div><slot /></div>', props: ['value'] },
          'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
          'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType'] },
          'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'disabled'] },
        },
      },
    });
    // Select a report type first
    (wrapper.vm as any).formData.reportType = 'porn';
    (wrapper.vm as any).formData.description = '违规内容描述';
    await nextTick();
    await (wrapper.vm as any).handleSubmit();
    await flushPromises();
    expect(wrapper.emitted('success')).toBeTruthy();
    expect(wrapper.emitted('success')![0]).toEqual(['R20260601001']);
  });

  it('should handle duplicate report error', async () => {
    const { createReport } = await import('/@/api/support/report');
    const { message } = await import('ant-design-vue');
    vi.mocked(createReport).mockRejectedValueOnce({ code: 'DUPLICATE_REPORT' });

    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: {
        stubs: {
          'a-modal': { template: '<div><slot /><slot name="footer" /></div>' },
          'a-form': { template: '<form><slot /></form>' },
          'a-form-item': { template: '<div><slot /></div>' },
          'a-radio-group': { template: '<div><slot /></div>' },
          'a-radio': { template: '<div><slot /></div>', props: ['value'] },
          'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
          'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType'] },
          'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'disabled'] },
        },
      },
    });
    (wrapper.vm as any).formData.reportType = 'spam';
    await (wrapper.vm as any).handleSubmit();
    await flushPromises();
    expect(message.warning).toHaveBeenCalledWith('您已举报过该内容，请勿重复举报');
    expect(wrapper.emitted('success')).toBeFalsy();
  });

  it('should reset form when visible changes to true', async () => {
    const wrapper = mount(ReportModal, {
      props: { ...defaultProps, visible: false },
      global: {
        stubs: {
          'a-modal': { template: '<div><slot /><slot name="footer" /></div>' },
          'a-form': { template: '<form><slot /></form>' },
          'a-form-item': { template: '<div><slot /></div>' },
          'a-radio-group': { template: '<div><slot /></div>' },
          'a-radio': { template: '<div><slot /></div>', props: ['value'] },
          'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
          'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType'] },
          'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'disabled'] },
        },
      },
    });
    // Set some data
    (wrapper.vm as any).formData.reportType = 'porn';
    (wrapper.vm as any).formData.description = 'some text';
    await nextTick();

    // Trigger visible change
    await wrapper.setProps({ visible: true });
    await nextTick();
    expect((wrapper.vm as any).formData.reportType).toBe('');
    expect((wrapper.vm as any).formData.description).toBe('');
  });
});
