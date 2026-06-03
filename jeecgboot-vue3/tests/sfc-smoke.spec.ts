import { mount } from '@vue/test-utils';
import HelloSfc from './sfc-smoke.vue';

describe('SFC component smoke test', () => {
  it('mounts a SFC component', () => {
    const wrapper = mount(HelloSfc, { props: { name: 'SFC' } });
    expect(wrapper.html()).toContain('Hello SFC');
  });
});
