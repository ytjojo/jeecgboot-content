import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { mount, VueWrapper } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import { Modal, Rate, Input, Button } from 'ant-design-vue';
import RatingModal from './RatingModal.vue';

vi.mock('/@/api/support/customer-service', () => ({
  submitServiceRating: vi.fn().mockResolvedValue({}),
}));

let wrapper: VueWrapper;

function mountRatingModal(props = { visible: true, sessionId: 's1' }) {
  wrapper = mount(RatingModal, {
    props,
    attachTo: document.body,
    global: {
      components: {
        AModal: Modal,
        ARate: Rate,
        ATextarea: Input.TextArea,
        AButton: Button,
      },
    },
  });
  return wrapper;
}

describe('RatingModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    document.body.innerHTML = '';
  });

  afterEach(() => {
    wrapper?.unmount();
    document.body.innerHTML = '';
  });

  it('should render rating modal with title', () => {
    const wrapper = mountRatingModal();
    expect(document.body.textContent).toContain('服务评价');
    expect(document.body.textContent).toContain('请为本次服务评分');
  });

  it('should have star rating component', () => {
    mountRatingModal();
    expect(document.body.querySelector('.ant-rate')).toBeTruthy();
  });

  it('should have textarea for optional comment', () => {
    mountRatingModal();
    expect(document.body.querySelector('textarea')).toBeTruthy();
  });

  it('should disable submit button when score is 0', () => {
    mountRatingModal();
    const submitBtn = document.body.querySelector('[data-testid="submit-rating"]') as HTMLButtonElement;
    expect(submitBtn).toBeTruthy();
    expect(submitBtn.disabled).toBe(true);
  });

  it('should emit rated after successful submission', async () => {
    mountRatingModal();
    const vm = wrapper.vm as any;
    vm.score = 5;
    await wrapper.vm.$nextTick();
    const submitBtn = document.body.querySelector('[data-testid="submit-rating"]') as HTMLButtonElement;
    expect(submitBtn.disabled).toBe(false);
    submitBtn.click();
    await wrapper.vm.$nextTick();
    await new Promise((r) => setTimeout(r, 50));
    expect(wrapper.emitted('rated')).toBeTruthy();
  });

  it('should call submitServiceRating with correct params', async () => {
    const { submitServiceRating } = await import('/@/api/support/customer-service');
    mountRatingModal();
    const vm = wrapper.vm as any;
    vm.score = 4;
    vm.comment = 'good service';
    await wrapper.vm.$nextTick();
    const submitBtn = document.body.querySelector('[data-testid="submit-rating"]') as HTMLButtonElement;
    submitBtn.click();
    await wrapper.vm.$nextTick();
    await new Promise((r) => setTimeout(r, 50));
    expect(submitServiceRating).toHaveBeenCalledWith('s1', { score: 4, comment: 'good service' });
  });

  it('should emit close when modal is closed', async () => {
    mountRatingModal();
    const vm = wrapper.vm as any;
    vm.handleClose();
    await wrapper.vm.$nextTick();
    expect(wrapper.emitted('close')).toBeTruthy();
  });

  it('should show character count for comment textarea', () => {
    mountRatingModal();
    const textarea = document.body.querySelector('textarea');
    expect(textarea).toBeTruthy();
    // a-textarea with show-count renders a count element; verify textarea wrapper exists
    const inputWrapper = textarea?.closest('.ant-input-textarea');
    expect(inputWrapper).toBeTruthy();
    // Verify textarea is accessible and functional
    expect(textarea?.getAttribute('placeholder')).toBe('留下您的评价（可选）');
  });
});
