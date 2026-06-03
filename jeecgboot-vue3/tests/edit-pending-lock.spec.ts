import { isFormDisabled, isSaveDisabled } from '/@/views/content/profile/edit/pendingLock';

describe('isFormDisabled', () => {
  it('returns true when reviewStatus is PENDING', () => {
    expect(isFormDisabled('PENDING')).toBe(true);
  });

  it('returns false when reviewStatus is APPROVED', () => {
    expect(isFormDisabled('APPROVED')).toBe(false);
  });

  it('returns false when reviewStatus is REJECTED', () => {
    expect(isFormDisabled('REJECTED')).toBe(false);
  });

  it('returns false when reviewStatus is empty string', () => {
    expect(isFormDisabled('')).toBe(false);
  });

  it('returns false when reviewStatus is null', () => {
    expect(isFormDisabled(null)).toBe(false);
  });
});

describe('isSaveDisabled', () => {
  it('returns true when reviewStatus is PENDING', () => {
    expect(isSaveDisabled('PENDING')).toBe(true);
  });

  it('returns false when reviewStatus is APPROVED', () => {
    expect(isSaveDisabled('APPROVED')).toBe(false);
  });

  it('returns false when reviewStatus is REJECTED', () => {
    expect(isSaveDisabled('REJECTED')).toBe(false);
  });

  it('returns false when reviewStatus is empty string', () => {
    expect(isSaveDisabled('')).toBe(false);
  });

  it('returns false when reviewStatus is null', () => {
    expect(isSaveDisabled(null)).toBe(false);
  });
});
