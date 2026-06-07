import { mount, flushPromises } from '@vue/test-utils';
import GovernanceDetailDrawer from '/@/views/channel/governance/GovernanceDetailDrawer.vue';

const stubs = {
  'a-drawer': { template: '<div v-if="open"><slot /></div>', props: ['open', 'title', 'width'], emits: ['update:open'] },
  'a-descriptions': { template: '<div class="desc"><slot /></div>', props: ['column', 'bordered'] },
  'a-descriptions-item': { template: '<div class="desc-item"><span class="desc-label">{{ label }}</span><slot /></div>', props: ['label'] },
  'a-tag': { template: '<span class="tag"><slot /></span>', props: ['color'] },
};

describe('GovernanceDetailDrawer', () => {
  function mountComponent() {
    return mount(GovernanceDetailDrawer, { global: { stubs } });
  }

  it('should open and set record via open() method', async () => {
    const wrapper = mountComponent();
    wrapper.vm.open({
      action: 'REMOVE', operatorName: 'admin', targetUserName: 'badUser',
      createTime: '2024-01-01', reason: 'spam', beforeState: '正常', afterState: '已移除',
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('admin');
    expect(wrapper.text()).toContain('badUser');
  });

  it('should render action color map correctly', async () => {
    const wrapper = mountComponent();
    const cases = [
      { action: 'REMOVE', expectedText: '移除' },
      { action: 'MUTE', expectedText: '禁言' },
      { action: 'UNMUTE', expectedText: '解除禁言' },
      { action: 'BLACKLIST_ADD', expectedText: '加入黑名单' },
      { action: 'BLACKLIST_REMOVE', expectedText: '移出黑名单' },
    ];
    for (const tc of cases) {
      wrapper.vm.open({ action: tc.action, operatorName: 'op', targetUserName: 'target', createTime: '2024-01-01', reason: '', beforeState: '', afterState: '' });
      await wrapper.vm.$nextTick();
      expect(wrapper.text()).toContain(tc.expectedText);
    }
  });

  it('should fall back to raw action text for unknown action', async () => {
    const wrapper = mountComponent();
    wrapper.vm.open({ action: 'UNKNOWN_ACTION', operatorName: 'op', targetUserName: 'target', createTime: '2024-01-01', reason: '', beforeState: '', afterState: '' });
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('UNKNOWN_ACTION');
  });

  it('should show all description field labels', async () => {
    const wrapper = mountComponent();
    wrapper.vm.open({
      action: 'REMOVE', operatorName: 'operator1', targetUserName: 'target1',
      createTime: '2024-06-01', reason: 'bad behavior', beforeState: '正常', afterState: '已移除',
    });
    await wrapper.vm.$nextTick();

    const labels = wrapper.findAll('.desc-label');
    const labelTexts = labels.map((l) => l.text());
    expect(labelTexts).toContain('操作类型');
    expect(labelTexts).toContain('操作者');
    expect(labelTexts).toContain('目标用户');
    expect(labelTexts).toContain('操作时间');
    expect(labelTexts).toContain('原因');
    expect(labelTexts).toContain('操作前状态');
    expect(labelTexts).toContain('操作后状态');
  });

  it('should show "无" when reason is empty', async () => {
    const wrapper = mountComponent();
    wrapper.vm.open({
      action: 'MUTE', operatorName: 'op', targetUserName: 'target',
      createTime: '2024-01-01', reason: '', beforeState: '正常', afterState: '已禁言',
    });
    await wrapper.vm.$nextTick();
    expect(wrapper.text()).toContain('无');
  });
});
