import { vi } from 'vitest';
vi.mock('/@/api/content/governance', () => ({
  listAuditLog: vi.fn().mockResolvedValue({ items: [], total: 0 }),
}));

vi.mock('/@/components/Table', () => ({
  BasicTable: {
    name: 'BasicTable',
    template: '<div class="basic-table-stub"><slot name="tableTitle" /></div>',
    props: ['onRegister'],
  },
}));

vi.mock('/@/hooks/system/useListPage', () => ({
  useListPage: vi.fn().mockReturnValue({
    tableContext: [vi.fn(), vi.fn(), {}],
  }),
}));

import { mount } from '@vue/test-utils';

describe('AuditLog page', () => {
  async function mountPage() {
    const Component = (await import('/@/views/system/audit-log/index.vue')).default;
    return mount(Component, {
      global: {
        stubs: {
          BasicTable: { template: '<div class="basic-table-stub"><slot name="tableTitle" /></div>', props: ['onRegister'] },
        },
      },
    });
  }

  it('renders the page with title', async () => {
    const wrapper = await mountPage();
    expect(wrapper.find('.basic-table-stub').exists()).toBe(true);
    expect(wrapper.text()).toContain('审计日志');
  });

  it('exports columns with correct field definitions', async () => {
    const { columns } = await import('/@/views/system/audit-log/audit-log.data');
    const fields = columns.map((c: any) => c.dataIndex);
    expect(fields).toContain('createTime');
    expect(fields).toContain('operatorUserId_dictText');
    expect(fields).toContain('eventType');
    expect(fields).toContain('reason');
  });

  it('exports search form schemas with filter fields', async () => {
    const { searchFormSchema } = await import('/@/views/system/audit-log/audit-log.data');
    const fields = searchFormSchema.map((s: any) => s.field);
    expect(fields).toContain('operatorUserId');
    expect(fields).toContain('eventType');
    expect(fields).toContain('startTime');
    expect(fields).toContain('endTime');
  });

  it('maps event types to Chinese labels in column render', async () => {
    const { columns } = await import('/@/views/system/audit-log/audit-log.data');
    const eventCol = columns.find((c: any) => c.dataIndex === 'eventType') as any;
    expect(eventCol).toBeDefined();
    expect(eventCol.customRender({ text: 'DELETE_COMMENT' })).toBe('删除评论');
    expect(eventCol.customRender({ text: 'WARN_USER' })).toBe('警告用户');
  });
});
