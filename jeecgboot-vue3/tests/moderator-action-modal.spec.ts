import { mount } from '@vue/test-utils';

describe('ModeratorActionModal.vue', () => {
  async function mountComponent(props = {}) {
    const Component = (await import('/@/views/content/components/ModeratorActionModal.vue')).default;
    return mount(Component, {
      props: { visible: true, actionType: 'deleteComment', targetId: 'c1', ...props },
      global: {
        stubs: {
          'a-modal': {
            template:
              '<div class="modal-stub" v-if="visible"><slot /><slot name="footer" /><button class="ok-btn" @click="$emit(\'ok\')">OK</button><button class="cancel-btn" @click="$emit(\'cancel\')">Cancel</button></div>',
            props: ['visible', 'title', 'confirmLoading', 'okText', 'cancelText'],
            emits: ['ok', 'cancel'],
          },
          'a-form': { template: '<form><slot /></form>', props: ['model'] },
          'a-form-item': { template: '<div class="form-item"><slot /></div>', props: ['label', 'name', 'rules'] },
          'a-textarea': {
            template: '<input class="reason-input" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue', 'placeholder', 'rows', 'maxlength', 'showCount'],
            emits: ['update:modelValue'],
          },
          'a-radio-group': { template: '<div><slot /></div>', props: ['modelValue'] },
          'a-radio': { template: '<label><input type="radio" /><slot /></label>', props: ['value'] },
        },
      },
    });
  }

  it('renders delete comment modal when visible', async () => {
    const wrapper = await mountComponent();
    expect(wrapper.find('.modal-stub').exists()).toBe(true);
    expect(wrapper.find('.reason-input').exists()).toBe(true);
  });

  it('does not render when visible is false', async () => {
    const wrapper = await mountComponent({ visible: false });
    expect(wrapper.find('.modal-stub').exists()).toBe(false);
  });

  it('renders warn user action type', async () => {
    const wrapper = await mountComponent({ actionType: 'warnUser', targetId: 'u1' });
    expect(wrapper.find('.modal-stub').exists()).toBe(true);
  });

  it('emits confirm with reason when form is submitted', async () => {
    const wrapper = await mountComponent();
    // Set reason directly on component's reactive state
    (wrapper.vm as any).formState.reason = '违规内容';
    await wrapper.vm.$nextTick();
    await wrapper.find('.ok-btn').trigger('click');
    expect(wrapper.emitted('confirm')).toBeTruthy();
  });

  it('emits cancel when modal is closed', async () => {
    const wrapper = await mountComponent();
    await wrapper.find('.cancel-btn').trigger('click');
    expect(wrapper.emitted('cancel')).toBeTruthy();
  });
});
