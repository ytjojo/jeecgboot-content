export function isSafeRedirect(target: string | null | undefined, fallback = '/'): string {
  if (!target) return fallback;
  if (!target.startsWith('/')) return fallback;
  if (target.startsWith('//')) return fallback;
  const lower = target.toLowerCase();
  const blocked = ['/login', '/register', '/forgot-password', '/account-security'];
  if (blocked.some((p) => lower.startsWith(p))) return fallback;
  return target;
}
