import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import StatusTag from '../components/StatusTag.vue';

describe('StatusTag', () => {
  it('Active 状态应显示绿色"运营中"标签', () => {
    const wrapper = mount(StatusTag, { props: { status: 'Active' } });
    expect(wrapper.text()).toBe('运营中');
  });

  it('ReadonlyFrozen 状态应显示橙色"已冻结"标签', () => {
    const wrapper = mount(StatusTag, { props: { status: 'ReadonlyFrozen' } });
    expect(wrapper.text()).toBe('已冻结');
  });

  it('Hidden 状态应显示红色"已隐藏"标签', () => {
    const wrapper = mount(StatusTag, { props: { status: 'Hidden' } });
    expect(wrapper.text()).toBe('已隐藏');
  });

  it('Archived 状态应显示"已归档"标签', () => {
    const wrapper = mount(StatusTag, { props: { status: 'Archived' } });
    expect(wrapper.text()).toBe('已归档');
  });

  it('Merged 状态应显示蓝色"已合并"标签', () => {
    const wrapper = mount(StatusTag, { props: { status: 'Merged' } });
    expect(wrapper.text()).toBe('已合并');
  });

  it('Closed 状态应显示"已关闭"标签', () => {
    const wrapper = mount(StatusTag, { props: { status: 'Closed' } });
    expect(wrapper.text()).toBe('已关闭');
  });

  it('pending 审核状态应显示"待审核"', () => {
    const wrapper = mount(StatusTag, { props: { status: 'pending' } });
    expect(wrapper.text()).toBe('待审核');
  });

  it('approved 审核状态应显示"已通过"', () => {
    const wrapper = mount(StatusTag, { props: { status: 'approved' } });
    expect(wrapper.text()).toBe('已通过');
  });

  it('rejected 审核状态应显示"已拒绝"', () => {
    const wrapper = mount(StatusTag, { props: { status: 'rejected' } });
    expect(wrapper.text()).toBe('已拒绝');
  });

  it('submitted 申诉状态应显示"处理中"', () => {
    const wrapper = mount(StatusTag, { props: { status: 'submitted' } });
    expect(wrapper.text()).toBe('处理中');
  });

  it('未知状态应回退显示原始状态值', () => {
    const wrapper = mount(StatusTag, { props: { status: 'UnknownStatus' } });
    expect(wrapper.text()).toBe('UnknownStatus');
  });

  it('small 尺寸应正常工作', () => {
    const wrapper = mount(StatusTag, { props: { status: 'Active', size: 'small' } });
    expect(wrapper.classes()).toContain('small');
    expect(wrapper.text()).toBe('运营中');
  });
});
