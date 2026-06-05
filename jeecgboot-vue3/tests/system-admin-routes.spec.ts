describe('System admin routes', () => {
  let routes: any[];

  beforeAll(async () => {
    const mod = await import('/@/router/routes/modules/systemAdmin');
    routes = mod.default;
  });

  it('exports a route array', () => {
    expect(Array.isArray(routes)).toBe(true);
    expect(routes.length).toBeGreaterThan(0);
  });

  it('defines audit-log route under /system path', () => {
    const systemRoute = routes[0];
    expect(systemRoute.path).toBe('/system');
    expect(systemRoute.children).toBeDefined();
    const auditLog = systemRoute.children!.find((r: any) => r.path === 'audit-log');
    expect(auditLog).toBeDefined();
    expect(auditLog!.name).toBe('SystemAuditLog');
  });

  it('audit-log route has correct meta', () => {
    const systemRoute = routes[0];
    const auditLog = systemRoute.children!.find((r: any) => r.path === 'audit-log');
    expect(auditLog!.meta!.title).toBe('审计日志');
    expect(auditLog!.meta!.hideMenu).toBe(true);
  });

  it('system route uses LAYOUT component', () => {
    const systemRoute = routes[0];
    expect(systemRoute.component).toBeDefined();
  });
});
