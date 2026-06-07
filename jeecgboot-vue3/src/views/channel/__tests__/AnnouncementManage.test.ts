import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import AnnouncementManage from '../governance/AnnouncementManage.vue';

vi.mock('/@/api/content/channel/announcement', () => ({
  getAnnouncement: vi.fn().mockResolvedValue({ id: '1', title: '测试公告', content: '<p>公告内容</p>', status: 'published' }),
  saveAnnouncement: vi.fn().mockResolvedValue({}),
  deleteAnnouncement: vi.fn().mockResolvedValue({}),
  previewAnnouncement: vi.fn().mockResolvedValue('<p>预览内容</p>'),
  getAnnouncementHistory: vi.fn().mockResolvedValue([
    { id: '1', version: 1, modifier: '管理员', modifyTime: '2026-06-01' },
  ]),
  restoreAnnouncementVersion: vi.fn().mockResolvedValue({}),
}));

describe('AnnouncementManage', () => {
  beforeEach(() => { setActivePinia(createPinia()); });

  it('应加载当前公告', async () => {
    const wrapper = mount(AnnouncementManage, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('频道公告');
    expect(wrapper.text()).toContain('已发布');
  });

  it('应展示公告历史列表', async () => {
    const wrapper = mount(AnnouncementManage, { props: { channelId: '1' } });
    await vi.dynamicImportSettled();
    expect(wrapper.text()).toContain('公告历史');
  });
});
