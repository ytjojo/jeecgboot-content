import type { AppRouteRecordRaw } from '/@/router/types';

// Test route module structure without importing the full router
describe('contentSocial routes', () => {
  let routes: AppRouteRecordRaw[];

  beforeAll(async () => {
    const mod = await import('/@/router/routes/modules/contentSocial');
    routes = mod.default;
  });

  it('exports an array of routes', () => {
    expect(Array.isArray(routes)).toBe(true);
    expect(routes.length).toBe(4);
  });

  it('has mutual-follow route', () => {
    const route = routes.find((r) => r.path === '/content/mutual-follow');
    expect(route).toBeDefined();
    expect(route!.name).toBe('ContentMutualFollow');
    expect(route!.meta!.hideMenu).toBe(true);
  });

  it('has fan route', () => {
    const route = routes.find((r) => r.path === '/content/fan');
    expect(route).toBeDefined();
    expect(route!.name).toBe('ContentFan');
  });

  it('has invite route', () => {
    const route = routes.find((r) => r.path === '/content/invite');
    expect(route).toBeDefined();
    expect(route!.name).toBe('ContentInvite');
  });

  it('has invite landing route with ignoreAuth', () => {
    const route = routes.find((r) => r.path === '/invite/:inviteCode');
    expect(route).toBeDefined();
    expect(route!.name).toBe('InviteLanding');
    expect(route!.meta!.ignoreAuth).toBe(true);
  });
});
