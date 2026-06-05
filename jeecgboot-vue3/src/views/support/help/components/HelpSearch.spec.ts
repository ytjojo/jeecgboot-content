import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import HelpSearch from './HelpSearch.vue';

describe('HelpSearch', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  const stubs = {
    'a-input-search': {
      template: '<input :value="value" @input="$emit(\'update:value\', $event.target.value)" @keydown.enter="$emit(\'search\', value)" />',
      props: ['value', 'placeholder', 'size'],
      emits: ['update:value', 'search'],
    },
  };

  it('should render search input', () => {
    const wrapper = mount(HelpSearch, { global: { stubs } });
    expect(wrapper.find('input').exists()).toBe(true);
  });

  it('should emit search event on enter', async () => {
    const wrapper = mount(HelpSearch, { global: { stubs } });
    await wrapper.find('input').setValue('如何修改密码');
    await wrapper.find('input').trigger('keydown.enter');
    expect(wrapper.emitted('search')).toBeTruthy();
    expect(wrapper.emitted('search')![0]).toEqual(['如何修改密码']);
  });

  it('should debounce search input', async () => {
    vi.useFakeTimers();
    const wrapper = mount(HelpSearch, { global: { stubs } });
    await wrapper.find('input').setValue('a');
    await wrapper.find('input').setValue('ab');
    await wrapper.find('input').setValue('abc');
    vi.advanceTimersByTime(300);
    expect(wrapper.emitted('search')).toHaveLength(1);
    expect(wrapper.emitted('search')![0]).toEqual(['abc']);
    vi.useRealTimers();
  });
});
