import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import ArticleFeedback from './ArticleFeedback.vue';

vi.mock('/@/api/support/help', () => ({
  submitArticleFeedback: vi.fn().mockResolvedValue({}),
}));

describe('ArticleFeedback', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('should render helpful/unhelpful buttons', () => {
    const wrapper = mount(ArticleFeedback, { props: { articleId: '1' } });
    expect(wrapper.text()).toContain('有用');
    expect(wrapper.text()).toContain('无用');
  });

  it('should show selected state after clicking helpful', async () => {
    const wrapper = mount(ArticleFeedback, { props: { articleId: '1' } });
    await wrapper.find('[data-testid="helpful-btn"]').trigger('click');
    expect(wrapper.find('[data-testid="helpful-btn"]').classes()).toContain('selected');
  });

  it('should show contact CS prompt after clicking unhelpful', async () => {
    const wrapper = mount(ArticleFeedback, { props: { articleId: '1' } });
    await wrapper.find('[data-testid="unhelpful-btn"]').trigger('click');
    expect(wrapper.text()).toContain('联系客服');
  });

  it('should not allow duplicate feedback', async () => {
    const wrapper = mount(ArticleFeedback, { props: { articleId: '1' } });
    await wrapper.find('[data-testid="helpful-btn"]').trigger('click');
    await wrapper.find('[data-testid="helpful-btn"]').trigger('click');
    expect(wrapper.emitted('feedback')).toHaveLength(1);
  });
});
