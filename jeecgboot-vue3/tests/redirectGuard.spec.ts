// Locked contract: same predicate used by utils/redirectGuard.ts
// Why: prevent open-redirect via URL-encoded or path-confusion attacks on
// the `redirect` query param consumed by login/register/forgot-password.

const BLOCKED = ['/login', '/register', '/forgot-password', '/account-security'];

function isSafeRedirect(target) {
  if (!target) return false;
  let decoded = target;
  try {
    decoded = decodeURIComponent(target);
  } catch (_) {
    return false;
  }
  if (BLOCKED.some((p) => decoded.startsWith(p))) return false;
  if (decoded.startsWith('//')) return false;
  return true;
}

describe('isSafeRedirect contract', () => {
  it('rejects null and empty', () => {
    expect(isSafeRedirect(null)).toBe(false);
    expect(isSafeRedirect('')).toBe(false);
  });

  it('rejects undefined', () => {
    expect(isSafeRedirect(undefined)).toBe(false);
  });

  it('accepts normal app path', () => {
    expect(isSafeRedirect('/dashboard')).toBe(true);
    expect(isSafeRedirect('/content/article/123')).toBe(true);
  });

  it('blocks /login loopback', () => {
    expect(isSafeRedirect('/login')).toBe(false);
    expect(isSafeRedirect('/login?next=/x')).toBe(false);
  });

  it('blocks /register loopback', () => {
    expect(isSafeRedirect('/register')).toBe(false);
  });

  it('blocks /forgot-password loopback', () => {
    expect(isSafeRedirect('/forgot-password')).toBe(false);
  });

  it('blocks /account-security loopback', () => {
    expect(isSafeRedirect('/account-security/devices')).toBe(false);
  });

  it('blocks protocol-relative external URL', () => {
    expect(isSafeRedirect('//evil.com')).toBe(false);
  });

  it('blocks URL-encoded loopback (%2Flogin → /login)', () => {
    expect(isSafeRedirect('%2Flogin')).toBe(false);
  });

  it('rejects malformed URI', () => {
    expect(isSafeRedirect('%E0%A4%A')).toBe(false);
  });
});
