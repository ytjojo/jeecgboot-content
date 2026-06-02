export type PasswordStrength = 'weak' | 'medium' | 'strong';

export function evaluatePasswordStrength(pwd: string): PasswordStrength {
  if (!pwd) return 'weak';
  let score = 0;
  if (pwd.length >= 8) score++;
  if (pwd.length >= 12) score++;
  if (/\d/.test(pwd)) score++;
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++;
  if (/[^A-Za-z0-9]/.test(pwd)) score++;
  if (score <= 2) return 'weak';
  if (score <= 3) return 'medium';
  return 'strong';
}

export const STRENGTH_LABEL: Record<PasswordStrength, string> = {
  weak: '弱',
  medium: '中',
  strong: '强',
};

export const STRENGTH_COLOR: Record<PasswordStrength, string> = {
  weak: '#ff4d4f',
  medium: '#faad14',
  strong: '#52c41a',
};
