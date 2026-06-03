/**
 * 个人资料表单字段校验函数（纯函数，提取自页面，便于单测）。
 * 与后端 DTO 注解保持一致：
 *  - nickname: 必填，≤30 字符
 *  - avatar: 必填，≤512 字符
 *  - bio: ≤500 字符
 *  - region/profession: ≤64 字符
 *  - personalLink: ≤256 字符，匹配 `^https?://.*$`
 *  - homepageBackground: ≤512 字符
 *  - themeColor: ≤16 字符，匹配 `^#[0-9A-Fa-f]{6}$`
 *  - certificationType: ≤32 字符
 *  - certificationLabel: ≤64 字符
 *  - certificationDescription: ≤512 字符
 */
export interface ProfileForm {
  nickname?: string;
  avatar?: string;
  bio?: string;
  region?: string;
  profession?: string;
  personalLink?: string;
  homepageBackground?: string;
  themeColor?: string;
  certificationType?: string;
  certificationLabel?: string;
  certificationDescription?: string;
}

export function validateProfileForm(form: ProfileForm): string | null {
  if (!form.nickname || !form.nickname.trim()) {
    return '昵称不能为空';
  }
  if (form.nickname.length > 30) {
    return '昵称不能超过 30 字符';
  }
  if (!form.avatar) {
    return '请上传头像';
  }
  if (form.avatar.length > 512) {
    return '头像 URL 不能超过 512 字符';
  }
  if (form.bio && form.bio.length > 500) {
    return '个人简介不能超过 500 字符';
  }
  if (form.region && form.region.length > 64) {
    return '地区不能超过 64 字符';
  }
  if (form.profession && form.profession.length > 64) {
    return '职业不能超过 64 字符';
  }
  if (form.personalLink) {
    if (form.personalLink.length > 256) {
      return '个人链接不能超过 256 字符';
    }
    if (!/^https?:\/\/.*$/.test(form.personalLink)) {
      return '个人链接必须以 http:// 或 https:// 开头';
    }
  }
  if (form.homepageBackground && form.homepageBackground.length > 512) {
    return '主页背景 URL 不能超过 512 字符';
  }
  if (form.themeColor) {
    if (form.themeColor.length > 16) {
      return '主题色不能超过 16 字符';
    }
    if (!/^#[0-9A-Fa-f]{6}$/.test(form.themeColor)) {
      return '主题色必须为 #RRGGBB 格式';
    }
  }
  if (form.certificationType && form.certificationType.length > 32) {
    return '认证类型不能超过 32 字符';
  }
  if (form.certificationLabel && form.certificationLabel.length > 64) {
    return '认证标签不能超过 64 字符';
  }
  if (form.certificationDescription && form.certificationDescription.length > 512) {
    return '认证说明不能超过 512 字符';
  }
  return null;
}
