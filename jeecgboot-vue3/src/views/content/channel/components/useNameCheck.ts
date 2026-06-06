import { ref } from 'vue';
import { checkNameUnique } from '/@/api/content/channel';

export function useNameCheck(excludeId?: string) {
  const checking = ref(false);
  const nameError = ref('');
  let debounceTimer: ReturnType<typeof setTimeout> | null = null;

  function checkName(name: string): Promise<boolean> {
    if (debounceTimer) clearTimeout(debounceTimer);
    nameError.value = '';

    if (!name || name.trim().length === 0) {
      nameError.value = '频道名称不能为空';
      return Promise.resolve(false);
    }

    return new Promise((resolve) => {
      debounceTimer = setTimeout(async () => {
        checking.value = true;
        try {
          const result = await checkNameUnique(name.trim(), excludeId);
          if (!result.available) {
            nameError.value = '该频道名称已被使用';
            resolve(false);
          } else {
            nameError.value = '';
            resolve(true);
          }
        } catch {
          nameError.value = '名称校验失败，请重试';
          resolve(false);
        } finally {
          checking.value = false;
        }
      }, 300);
    });
  }

  function clearError() {
    nameError.value = '';
  }

  return { checking, nameError, checkName, clearError };
}
