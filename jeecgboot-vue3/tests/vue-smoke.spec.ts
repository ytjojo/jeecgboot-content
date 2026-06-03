import { mount } from '@vue/test-utils';

const HelloWorld = {
  template: '<div class="hello">Hello {{ name }}</div>',
  props: ['name'],
};

describe('Vue component smoke test', () => {
  it('mounts a simple inline component', () => {
    const wrapper = mount(HelloWorld, { props: { name: 'World' } });
    expect(wrapper.html()).toContain('Hello World');
  });
});
