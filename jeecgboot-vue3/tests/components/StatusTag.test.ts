import { mount } from '@vue/test-utils';
import StatusTag from '/@/components/jeecg/UserStatus/StatusTag.vue';

// Mock ant-design-vue components
const mockComponents = {
  'a-tag': {
    template: '<span class="mock-tag" :class="$attrs"><slot /></span>',
    props: ['color'],
  },
  'a-tooltip': {
    template: '<span class="mock-tooltip"><slot /><span v-if="title" class="tooltip-title">{{ title }}</span></span>',
    props: ['title'],
  },
};

describe('StatusTag', () => {
  const mountTag = (props: Record<string, any> = {}) => {
    return mount(StatusTag, {
      props: { status: 'NORMAL', ...props },
      global: {
        components: mockComponents,
      },
    });
  };

  it('should render status label for known status', () => {
    const wrapper = mountTag({ status: 'NORMAL' });
    expect(wrapper.text()).toContain('正常');
  });

  it('should render raw status for unknown status', () => {
    const wrapper = mountTag({ status: 'UNKNOWN_STATUS' });
    expect(wrapper.text()).toContain('UNKNOWN_STATUS');
  });

  it('should render all 9 status labels', () => {
    const labels: Record<string, string> = {
      GUEST: '游客',
      REGISTERED_INCOMPLETE: '注册未完善',
      NORMAL: '正常',
      MUTED: '禁言',
      RESTRICTED_RECOMMEND: '限制推荐',
      FROZEN: '冻结',
      BANNED: '封禁',
      DEACTIVATING: '注销中',
      DEACTIVATED: '已注销',
    };

    for (const [status, label] of Object.entries(labels)) {
      const wrapper = mountTag({ status });
      expect(wrapper.text()).toContain(label);
    }
  });

  it('should render without tooltip by default', () => {
    const wrapper = mountTag({ status: 'NORMAL' });
    expect(wrapper.find('.mock-tooltip').exists()).toBe(false);
  });

  it('should render with tooltip when tooltipText is provided', () => {
    const wrapper = mountTag({ status: 'NORMAL', tooltipText: '用户状态正常' });
    expect(wrapper.find('.mock-tooltip').exists()).toBe(true);
    expect(wrapper.find('.tooltip-title').text()).toBe('用户状态正常');
  });

  it('should not render tooltip when tooltipText is empty', () => {
    const wrapper = mountTag({ status: 'NORMAL', tooltipText: '' });
    expect(wrapper.find('.mock-tooltip').exists()).toBe(false);
  });
});
