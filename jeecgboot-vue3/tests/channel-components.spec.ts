import { vi, describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import ChannelTypeTag from '/@/components/jeecg/channel/ChannelTypeTag.vue';
import ChannelStatusTag from '/@/components/jeecg/channel/ChannelStatusTag.vue';

const stubs = { 'a-tag': { template: '<span class="ant-tag"><slot /></span>', props: ['color'] } };

describe('ChannelTypeTag', () => {
  it('renders system type with label', () => {
    const wrapper = mount(ChannelTypeTag, { props: { type: 'system' }, global: { stubs } });
    expect(wrapper.text()).toBe('系统频道');
  });

  it('renders personal type with label', () => {
    const wrapper = mount(ChannelTypeTag, { props: { type: 'personal' }, global: { stubs } });
    expect(wrapper.text()).toBe('个人频道');
  });

  it('renders organization type with label', () => {
    const wrapper = mount(ChannelTypeTag, { props: { type: 'organization' }, global: { stubs } });
    expect(wrapper.text()).toBe('组织频道');
  });
});

describe('ChannelStatusTag', () => {
  const statusCases = [
    { status: 'DRAFT', label: '草稿' },
    { status: 'PENDING_REVIEW', label: '待审核' },
    { status: 'ACTIVE', label: '已激活' },
    { status: 'REJECTED', label: '已拒绝' },
    { status: 'DELETE_COOLING', label: '删除冷静期' },
    { status: 'DELETED', label: '已删除' },
  ];

  statusCases.forEach(({ status, label }) => {
    it(`renders "${label}" for status ${status}`, () => {
      const wrapper = mount(ChannelStatusTag, { props: { status }, global: { stubs } });
      expect(wrapper.text()).toBe(label);
    });
  });
});
