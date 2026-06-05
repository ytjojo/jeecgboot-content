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

  const defaultStubs = {
    'a-modal': { template: '<div><slot /><slot name="footer" /></div>' },
    'a-form': { template: '<form><slot /></form>' },
    'a-form-item': { template: '<div><slot /></div>' },
    'a-radio-group': { template: '<div><slot /></div>' },
    'a-radio': { template: '<div><slot /></div>', props: ['value'] },
    'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
    'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType'] },
    'a-button': { template: '<button><slot /></button>', props: ['type', 'loading', 'disabled'] },
  };

  it('should reject upload when file exceeds 10MB', async () => {
    const { message } = await import('ant-design-vue');
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: { stubs: defaultStubs },
    });
    const bigFile = new File([new ArrayBuffer(11 * 1024 * 1024)], 'big.jpg', { type: 'image/jpeg' });
    const result = (wrapper.vm as any).beforeUpload(bigFile);
    expect(result).toBe(false);
    expect(message.error).toHaveBeenCalledWith('文件大小不能超过 10MB');
  });

  it('should reject upload when file count exceeds 5', async () => {
    const { message } = await import('ant-design-vue');
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: { stubs: defaultStubs },
    });
    (wrapper.vm as any).fileList = [{}, {}, {}, {}, {}]; // 5 files already
    await nextTick();
    const smallFile = new File([new ArrayBuffer(1024)], 'small.jpg', { type: 'image/jpeg' });
    const result = (wrapper.vm as any).beforeUpload(smallFile);
    expect(result).toBe(false);
    expect(message.error).toHaveBeenCalledWith('最多上传 5 个文件');
  });

  it('should accept upload for valid file', async () => {
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: { stubs: defaultStubs },
    });
    const smallFile = new File([new ArrayBuffer(1024)], 'small.jpg', { type: 'image/jpeg' });
    const result = (wrapper.vm as any).beforeUpload(smallFile);
    expect(result).toBe(true);
  });

  it('should handle evidence upload success', async () => {
    const { uploadFile } = await import('/@/api/sys/upload');
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: { stubs: defaultStubs },
    });
    const onSuccess = vi.fn();
    const onError = vi.fn();
    await (wrapper.vm as any).handleUpload({ file: new File([], 'evidence.jpg'), onSuccess, onError });
    await flushPromises();
    expect(uploadFile).toHaveBeenCalled();
    expect(onSuccess).toHaveBeenCalled();
    expect((wrapper.vm as any).formData.evidenceUrls).toContain('https://example.com/file1.jpg');
    // uploading should be false after completion
    expect((wrapper.vm as any).uploading).toBe(false);
  });

  it('should handle evidence upload error', async () => {
    const { uploadFile } = await import('/@/api/sys/upload');
    vi.mocked(uploadFile).mockRejectedValueOnce(new Error('upload failed'));
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: { stubs: defaultStubs },
    });
    const onSuccess = vi.fn();
    const onError = vi.fn();
    await (wrapper.vm as any).handleUpload({ file: new File([], 'evidence.jpg'), onSuccess, onError });
    await flushPromises();
    expect(onError).toHaveBeenCalled();
    expect((wrapper.vm as any).uploading).toBe(false);
  });

  it('should call createReport with all form fields', async () => {
    const { createReport } = await import('/@/api/support/report');
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: { stubs: defaultStubs },
    });
    (wrapper.vm as any).formData.reportType = 'harassment';
    (wrapper.vm as any).formData.description = '包含辱骂内容';
    (wrapper.vm as any).formData.evidenceUrls = ['https://example.com/evidence1.jpg'];
    await nextTick();
    await (wrapper.vm as any).handleSubmit();
    await flushPromises();
    expect(createReport).toHaveBeenCalledWith(
      expect.objectContaining({
        targetType: 'article',
        targetId: '123',
        reportType: 'harassment',
        description: '包含辱骂内容',
        evidenceUrls: ['https://example.com/evidence1.jpg'],
      })
    );
  });

  it('should handle generic API error on submit', async () => {
    const { createReport } = await import('/@/api/support/report');
    vi.mocked(createReport).mockRejectedValueOnce({ code: 'SERVER_ERROR' });
    const { message } = await import('ant-design-vue');
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: { stubs: defaultStubs },
    });
    (wrapper.vm as any).formData.reportType = 'spam';
    await (wrapper.vm as any).handleSubmit();
    await flushPromises();
    expect(message.error).toHaveBeenCalledWith('提交失败，请重试');
    expect((wrapper.vm as any).submitting).toBe(false);
  });

  it('should call handleTypeChange without error', () => {
    const wrapper = mount(ReportModal, {
      props: defaultProps,
      global: { stubs: defaultStubs },
    });
    // handleTypeChange is a no-op, verify it executes
    expect(() => (wrapper.vm as any).handleTypeChange()).not.toThrow();
  });
});
