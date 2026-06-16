import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import CircleCard from '../CircleCard.vue';
import type { CircleVO } from '/@/api/content/model/circleModel';

// Mock ant-design-vue Tag
vi.mock('ant-design-vue', async (importOriginal) => {
  const actual = await importOriginal<any>();
  return {
    ...actual,
    Tag: {
      name: 'Tag',
      template: '<span class="ant-tag" :data-color="color"><slot /></span>',
      props: ['color'],
    },
  };
});

const baseCircle: CircleVO = {
  id: 'circle-001',
  name: '测试圈子',
  description: '这是一个测试圈子',
  iconUrl: 'https://example.com/icon.png',
  coverUrl: 'https://example.com/cover.png',
  category: '技术',
  privacyType: 'PUBLIC',
  joinType: 'DIRECT',
  creatorId: 'user-001',
  memberCount: 42,
  maxMemberCount: 100,
  status: 'ACTIVE',
  joined: true,
  myRole: 'MEMBER',
  applyStatus: null,
  isInvited: false,
  createTime: '2024-01-01 00:00:00',
};

describe('CircleCard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  function createWrapper(circleOverrides: Partial<CircleVO> = {}) {
    return mount(CircleCard, {
      props: {
        circle: { ...baseCircle, ...circleOverrides },
      },
      global: {
        stubs: {
          'v-lazy': true,
          PrivacyBadge: {
            template: '<span class="privacy-badge-stub">公开</span>',
            props: ['type'],
          },
          JoinStatusButton: {
            template: '<button class="join-btn-stub">加入</button>',
            props: ['circle'],
          },
        },
      },
    });
  }

  // ======================== 治理角标测试 ========================

  it('CREATOR 角色显示治理角标', () => {
    const wrapper = createWrapper({ myRole: 'CREATOR' });
    const badge = wrapper.find('.governance-badge');
    expect(badge.exists()).toBe(true);
    expect(badge.text()).toBe('治理');
    expect(badge.attributes('data-color')).toBe('blue');
  });

  it('MODERATOR 角色显示治理角标', () => {
    const wrapper = createWrapper({ myRole: 'MODERATOR' });
    const badge = wrapper.find('.governance-badge');
    expect(badge.exists()).toBe(true);
    expect(badge.text()).toBe('治理');
  });

  it('MEMBER 角色不显示治理角标', () => {
    const wrapper = createWrapper({ myRole: 'MEMBER' });
    expect(wrapper.find('.governance-badge').exists()).toBe(false);
  });

  it('myRole 为 null 时不显示治理角标', () => {
    const wrapper = createWrapper({ myRole: null });
    expect(wrapper.find('.governance-badge').exists()).toBe(false);
  });

  it('点击治理角标触发 governance 事件，不触发 click 事件', async () => {
    const wrapper = createWrapper({ myRole: 'CREATOR' });
    const badge = wrapper.find('.governance-badge');
    await badge.trigger('click');
    expect(wrapper.emitted('governance')).toBeTruthy();
    expect(wrapper.emitted('governance')![0]).toEqual(['circle-001']);
    // click 事件不应触发（@click.stop）
    expect(wrapper.emitted('click')).toBeFalsy();
  });

  // ======================== 基础渲染测试 ========================

  it('渲染圈子名称', () => {
    const wrapper = createWrapper();
    expect(wrapper.text()).toContain('测试圈子');
  });

  it('渲染成员数量', () => {
    const wrapper = createWrapper();
    expect(wrapper.text()).toContain('42 成员');
  });

  it('渲染分类', () => {
    const wrapper = createWrapper();
    expect(wrapper.text()).toContain('技术');
  });

  it('渲染描述', () => {
    const wrapper = createWrapper();
    expect(wrapper.text()).toContain('这是一个测试圈子');
  });

  it('点击卡片触发 click 事件', async () => {
    const wrapper = createWrapper();
    await wrapper.find('.circle-card').trigger('click');
    expect(wrapper.emitted('click')).toBeTruthy();
  });
});
