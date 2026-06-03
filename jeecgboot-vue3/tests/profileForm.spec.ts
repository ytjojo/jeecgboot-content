import { validateProfileForm } from '/@/views/content/profile/validators/profileForm';

describe('validateProfileForm', () => {
  it('returns error when nickname is empty', () => {
    expect(validateProfileForm({ nickname: '', avatar: 'a' })).toBe('昵称不能为空');
    expect(validateProfileForm({ nickname: '   ', avatar: 'a' })).toBe('昵称不能为空');
    expect(validateProfileForm({ avatar: 'a' })).toBe('昵称不能为空');
  });

  it('returns error when nickname is too long', () => {
    expect(validateProfileForm({ nickname: 'a'.repeat(31), avatar: 'a' })).toBe('昵称不能超过 30 字符');
  });

  it('returns error when avatar is missing', () => {
    expect(validateProfileForm({ nickname: 'Nick' })).toBe('请上传头像');
  });

  it('returns error when avatar is too long', () => {
    expect(validateProfileForm({ nickname: 'Nick', avatar: 'a'.repeat(513) })).toBe(
      '头像 URL 不能超过 512 字符'
    );
  });

  it('returns error when bio is too long', () => {
    expect(validateProfileForm({ nickname: 'Nick', avatar: 'a', bio: 'b'.repeat(501) })).toBe(
      '个人简介不能超过 500 字符'
    );
  });

  it('returns error when personalLink is not http(s)', () => {
    expect(
      validateProfileForm({ nickname: 'Nick', avatar: 'a', personalLink: 'ftp://x' })
    ).toBe('个人链接必须以 http:// 或 https:// 开头');
    expect(
      validateProfileForm({ nickname: 'Nick', avatar: 'a', personalLink: 'example.com' })
    ).toBe('个人链接必须以 http:// 或 https:// 开头');
  });

  it('accepts http and https personal links', () => {
    expect(
      validateProfileForm({ nickname: 'Nick', avatar: 'a', personalLink: 'http://x.com' })
    ).toBeNull();
    expect(
      validateProfileForm({ nickname: 'Nick', avatar: 'a', personalLink: 'https://x.com/path?q=1' })
    ).toBeNull();
  });

  it('returns error when personalLink is too long', () => {
    expect(
      validateProfileForm({ nickname: 'Nick', avatar: 'a', personalLink: 'https://' + 'a'.repeat(260) })
    ).toBe('个人链接不能超过 256 字符');
  });

  it('returns error when themeColor is not #RRGGBB', () => {
    expect(validateProfileForm({ nickname: 'Nick', avatar: 'a', themeColor: 'red' })).toBe(
      '主题色必须为 #RRGGBB 格式'
    );
    expect(validateProfileForm({ nickname: 'Nick', avatar: 'a', themeColor: '#fff' })).toBe(
      '主题色必须为 #RRGGBB 格式'
    );
  });

  it('accepts valid #RRGGBB theme colors (upper and lower case)', () => {
    expect(validateProfileForm({ nickname: 'Nick', avatar: 'a', themeColor: '#1677ff' })).toBeNull();
    expect(validateProfileForm({ nickname: 'Nick', avatar: 'a', themeColor: '#AABBCC' })).toBeNull();
  });

  it('returns error when region/profession is too long', () => {
    expect(validateProfileForm({ nickname: 'Nick', avatar: 'a', region: 'r'.repeat(65) })).toBe(
      '地区不能超过 64 字符'
    );
    expect(
      validateProfileForm({ nickname: 'Nick', avatar: 'a', profession: 'p'.repeat(65) })
    ).toBe('职业不能超过 64 字符');
  });

  it('returns error when certification fields are too long', () => {
    expect(
      validateProfileForm({
        nickname: 'Nick',
        avatar: 'a',
        certificationType: 't'.repeat(33),
      })
    ).toBe('认证类型不能超过 32 字符');
    expect(
      validateProfileForm({
        nickname: 'Nick',
        avatar: 'a',
        certificationLabel: 'l'.repeat(65),
      })
    ).toBe('认证标签不能超过 64 字符');
    expect(
      validateProfileForm({
        nickname: 'Nick',
        avatar: 'a',
        certificationDescription: 'd'.repeat(513),
      })
    ).toBe('认证说明不能超过 512 字符');
  });

  it('returns null on a fully valid form', () => {
    expect(
      validateProfileForm({
        nickname: 'N',
        avatar: 'https://cdn/a.png',
        bio: 'hi',
        region: 'BJ',
        profession: 'dev',
        personalLink: 'https://example.com',
        homepageBackground: 'https://cdn/bg.png',
        themeColor: '#1677ff',
        certificationType: 'GOLD',
        certificationLabel: '高级会员',
        certificationDescription: 'desc',
      })
    ).toBeNull();
  });
});
