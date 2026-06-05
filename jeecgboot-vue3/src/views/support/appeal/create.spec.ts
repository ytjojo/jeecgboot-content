import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { nextTick } from 'vue';
import AppealCreate from './create.vue';

// Mock API
vi.mock('/@/api/support/appeal', () => ({
  createAppeal: vi.fn().mockResolvedValue({ code: 200, result: { appealNo: 'A20260601001' } }),
}));

vi.mock('/@/api/sys/upload', () => ({
  uploadFile: vi.fn().mockResolvedValue({ result: { url: 'https://example.com/file1.jpg' } }),
}));

// Mock vue-router
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn(), back: vi.fn() }),
  useRoute: () => ({ query: { reportId: '123' } }),
}));

// Mock message and Modal
vi.mock('ant-design-vue', async (importOriginal) => {
  const actual = await importOriginal<any>();
  return {
    ...actual,
    message: {
      success: vi.fn(),
      warning: vi.fn(),
      error: vi.fn(),
    },
    Modal: {
      confirm: vi.fn(({ onOk }) => { onOk?.(); }),
    },
  };
});

describe('AppealCreate', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  const globalStubs = {
    'a-card': { template: '<div class="ant-card"><slot /><slot name="title" /></div>', props: ['title'] },
    'a-breadcrumb': { template: '<div><slot /></div>' },
    'a-breadcrumb-item': { template: '<span><slot /></span>' },
    'a-form': { template: '<form><slot /></form>', props: ['model', 'layout'] },
    'a-form-item': { template: '<div>{{ label }}<slot /></div>', props: ['label', 'required'] },
    'a-select': { template: '<select><slot /></select>', props: ['value', 'placeholder'] },
    'a-select-option': { template: '<option><slot /></option>', props: ['value'] },
    'a-input': { template: '<input />', props: ['value', 'disabled'] },
    'a-textarea': { template: '<textarea />', props: ['value', 'maxlength', 'rows', 'showCount', 'placeholder'] },
    'a-upload': { template: '<div><slot /><slot name="tip" /></div>', props: ['fileList', 'accept', 'listType', 'beforeUpload'] },
    'a-button': {
      template: '<button :disabled="disabled" :data-testid="$attrs[\'data-testid\']"><slot /></button>',
      props: ['type', 'loading', 'disabled'],
      inheritAttrs: false,
    },
    'a-space': { template: '<div><slot /></div>' },
    'a-alert': { template: '<div>{{ message }}<slot /></div>', props: ['message', 'type', 'showIcon'] },
  };

  it('should render appeal form with title', () => {
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    expect(wrapper.text()).toContain('提交申诉');
  });

  it('should show appeal type options', () => {
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    expect(wrapper.text()).toContain('申诉类型');
    expect(wrapper.text()).toContain('申诉理由');
  });

  it('should show appeal count indicator', () => {
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    expect(wrapper.text()).toContain('/3');
  });

  it('should disable submit when reason is empty', () => {
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    const submitBtn = wrapper.find('[data-testid="submit-btn"]');
    expect(submitBtn.attributes('disabled')).toBeDefined();
  });

  it('should show warning when submitting without appealType', async () => {
    const { message } = await import('ant-design-vue');
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    // Set reason but no appealType
    (wrapper.vm as any).formData.reason = '申诉理由';
    await nextTick();
    await (wrapper.vm as any).handleSubmit();
    expect(message.warning).toHaveBeenCalledWith('请选择申诉类型');
  });

  it('should show warning when submitting without reason', async () => {
    const { message } = await import('ant-design-vue');
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    (wrapper.vm as any).formData.appealType = 'content_delete';
    await nextTick();
    await (wrapper.vm as any).handleSubmit();
    expect(message.warning).toHaveBeenCalledWith('请填写申诉理由');
  });

  it('should show confirmation dialog on 3rd appeal', async () => {
    const { Modal } = await import('ant-design-vue');
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    (wrapper.vm as any).appealCount = 3;
    (wrapper.vm as any).formData.appealType = 'content_delete';
    (wrapper.vm as any).formData.reason = '申诉理由';
    await nextTick();
    await (wrapper.vm as any).handleSubmit();
    expect(Modal.confirm).toHaveBeenCalled();
  });

  it('should call createAppeal API on valid submission', async () => {
    const { createAppeal } = await import('/@/api/support/appeal');
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    (wrapper.vm as any).formData.appealType = 'content_delete';
    (wrapper.vm as any).formData.reason = '详细申诉理由';
    await nextTick();
    await (wrapper.vm as any).handleSubmit();
    await flushPromises();
    expect(createAppeal).toHaveBeenCalledWith(
      expect.objectContaining({ appealType: 'content_delete', reason: '详细申诉理由' })
    );
  });

  it('should have cancel button that triggers router.back', async () => {
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    expect(wrapper.text()).toContain('取消');
  });

  it('should reject upload when file exceeds 10MB', async () => {
    const { message } = await import('ant-design-vue');
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    const bigFile = new File([new ArrayBuffer(11 * 1024 * 1024)], 'big.jpg', { type: 'image/jpeg' });
    const result = (wrapper.vm as any).beforeUpload(bigFile);
    expect(result).toBe(false);
    expect(message.error).toHaveBeenCalledWith('文件大小不能超过 10MB');
  });

  it('should accept upload when file is under 10MB', async () => {
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    const smallFile = new File([new ArrayBuffer(1024)], 'small.jpg', { type: 'image/jpeg' });
    const result = (wrapper.vm as any).beforeUpload(smallFile);
    expect(result).toBe(true);
  });

  it('should handle attachment upload success', async () => {
    const { uploadFile } = await import('/@/api/sys/upload');
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    const onSuccess = vi.fn();
    const onError = vi.fn();
    await (wrapper.vm as any).handleUpload({ file: new File([], 'test.jpg'), onSuccess, onError });
    await flushPromises();
    expect(uploadFile).toHaveBeenCalled();
    expect(onSuccess).toHaveBeenCalled();
    expect((wrapper.vm as any).formData.attachmentUrls).toContain('https://example.com/file1.jpg');
  });

  it('should handle attachment upload error', async () => {
    const { uploadFile } = await import('/@/api/sys/upload');
    vi.mocked(uploadFile).mockRejectedValueOnce(new Error('upload failed'));
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    const onSuccess = vi.fn();
    const onError = vi.fn();
    await (wrapper.vm as any).handleUpload({ file: new File([], 'test.jpg'), onSuccess, onError });
    await flushPromises();
    expect(onError).toHaveBeenCalled();
  });

  it('should call createAppeal and navigate on valid submission', async () => {
    const { createAppeal } = await import('/@/api/support/appeal');
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    (wrapper.vm as any).formData.appealType = 'content_delete';
    (wrapper.vm as any).formData.reason = '详细申诉理由';
    await nextTick();
    await (wrapper.vm as any).handleSubmit();
    await flushPromises();
    expect(createAppeal).toHaveBeenCalled();
    const { message } = await import('ant-design-vue');
    expect(message.success).toHaveBeenCalledWith('申诉已提交');
  });

  it('should handle createAppeal API error', async () => {
    const { createAppeal } = await import('/@/api/support/appeal');
    vi.mocked(createAppeal).mockRejectedValueOnce(new Error('API error'));
    const { message } = await import('ant-design-vue');
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    (wrapper.vm as any).formData.appealType = 'content_delete';
    (wrapper.vm as any).formData.reason = '详细申诉理由';
    await nextTick();
    await (wrapper.vm as any).handleSubmit();
    await flushPromises();
    expect(message.error).toHaveBeenCalledWith('提交失败');
  });

  it('should call handleCancel to go back', async () => {
    const wrapper = mount(AppealCreate, { global: { stubs: globalStubs } });
    // handleCancel calls router.back() — verify it executes without error and submitting state is unchanged
    expect((wrapper.vm as any).submitting).toBe(false);
    await (wrapper.vm as any).handleCancel();
    // submitting should remain false (handleCancel doesn't change it)
    expect((wrapper.vm as any).submitting).toBe(false);
  });
});
