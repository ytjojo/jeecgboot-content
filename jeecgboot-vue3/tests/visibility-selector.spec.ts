import { mount } from '@vue/test-utils';

describe('VisibilitySelector.vue', () => {
  async function mountComponent(props: Record<string, any> = {}) {
    const Component = (await import('/@/views/content/components/VisibilitySelector.vue')).default;
    return mount(Component, {
      props,
      global: {
        stubs: {
          'a-select': {
            template: '<select :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"><slot /></select>',
            props: ['modelValue', 'size', 'style'],
            emits: ['update:modelValue', 'change'],
          },
          'a-select-option': {
            template: '<option :value="value"><slot /></option>',
            props: ['value'],
          },
        },
      },
    });
  }

  it('defaults to PUBLIC visibility', async () => {
    const wrapper = await mountComponent();
    expect(wrapper.find('select').element.value).toBe('PUBLIC');
  });

  it('accepts external modelValue prop', async () => {
    const wrapper = await mountComponent({ modelValue: 'MUTUAL_FOLLOW' });
    expect(wrapper.find('select').element.value).toBe('MUTUAL_FOLLOW');
  });

  it('renders both visibility options', async () => {
    const wrapper = await mountComponent();
    const options = wrapper.findAll('option');
    expect(options.length).toBe(2);
    expect(options[0].text()).toContain('公开');
    expect(options[1].text()).toContain('仅互关可见');
  });
});
