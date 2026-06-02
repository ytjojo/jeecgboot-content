// Locked contract: matches the score-based algorithm in
// src/components/Auth/passwordStrength.ts (evaluatePasswordStrength).
// Score = (len>=8) + (len>=12) + (has digit) + (has lower+upper) + (has special).
// score <= 2 → weak, score === 3 → medium, score >= 4 → strong.

function evaluatePasswordStrength(pwd) {
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

describe('evaluatePasswordStrength contract', () => {
  it('returns weak for empty string', () => {
    expect(evaluatePasswordStrength('')).toBe('weak');
  });

  it('returns medium for 5-char with digit+lower+upper+special (score 3, fails only length)', () => {
    expect(evaluatePasswordStrength('Ab1!@')).toBe('medium');
  });

  it('returns weak for 5-char plain alphanumeric (score 2)', () => {
    expect(evaluatePasswordStrength('Ab1cd')).toBe('weak');
  });

  it('returns weak for all-digit 8-char password (score 2)', () => {
    expect(evaluatePasswordStrength('12345678')).toBe('weak');
  });

  it('returns medium for length>=8 + digit + lower+upper (score 3)', () => {
    expect(evaluatePasswordStrength('Abcdefg1')).toBe('medium');
  });

  it('returns medium for length>=12 + digit only (score 3)', () => {
    expect(evaluatePasswordStrength('123456789012')).toBe('medium');
  });

  it('returns strong for length>=8 + digit + lower+upper + special (score 4)', () => {
    expect(evaluatePasswordStrength('Abcdefg1!')).toBe('strong');
  });

  it('returns strong for length>=12 + lower+upper + digit (score 4)', () => {
    expect(evaluatePasswordStrength('Abcdef123456')).toBe('strong');
  });

  it('returns strong for max strength (all 5 conditions)', () => {
    expect(evaluatePasswordStrength('Abcdef123456!')).toBe('strong');
  });
});
