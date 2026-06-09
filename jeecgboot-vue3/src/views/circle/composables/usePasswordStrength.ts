import { computed, type ComputedRef } from 'vue';

export type StrengthLevel = '' | 'weak' | 'medium' | 'strong';

export interface PasswordStrengthResult {
  strengthLevel: ComputedRef<StrengthLevel>;
  strengthPercent: ComputedRef<number>;
  strengthLabel: ComputedRef<string>;
}

/**
 * 密码强度计算 composable
 * 规则：纯数字=弱，字母+数字=中，字母+数字+特殊字符或长度>=12=强
 */
export function usePasswordStrength(password: ComputedRef<string>): PasswordStrengthResult {
  const strengthLevel = computed<StrengthLevel>(() => {
    const pwd = password.value;
    if (!pwd) return '';
    const hasDigit = /\d/.test(pwd);
    const hasLetter = /[a-zA-Z]/.test(pwd);
    const hasSpecial = /[^a-zA-Z\d]/.test(pwd);
    const isLong = pwd.length >= 12;

    if (isLong || (hasDigit && hasLetter && hasSpecial)) return 'strong';
    if (hasLetter && hasDigit) return 'medium';
    return 'weak';
  });

  const strengthPercent = computed(() => {
    switch (strengthLevel.value) {
      case 'strong':
        return 100;
      case 'medium':
        return 66;
      case 'weak':
        return 33;
      default:
        return 0;
    }
  });

  const strengthLabel = computed(() => {
    switch (strengthLevel.value) {
      case 'strong':
        return '强';
      case 'medium':
        return '中';
      case 'weak':
        return '弱';
      default:
        return '';
    }
  });

  return { strengthLevel, strengthPercent, strengthLabel };
}
