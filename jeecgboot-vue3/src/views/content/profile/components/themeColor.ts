/**
 * 主题色与 WCAG AA 对比度计算工具（纯函数，无 Vue/DOM 依赖）。
 *
 * - 提供 12 个品牌预设色板
 * - 提供 hex 解析、相对亮度、对比度计算（WCAG 2.x）
 * - 提供 `meetsWcagAA` 与 `getContrastingTextColor`（自动取白/黑）
 * - 提供 `validateModulesForSave` 用于主页模块保存前校验
 */
import type { ContentUserHomepageModuleVO } from '/@/api/content/profile/types';

/** 品牌预设主题色（Ant Design 色板 + 中性色），共 12 个。 */
export const PRESET_THEME_COLORS: string[] = [
  '#1677ff', // 默认蓝
  '#52c41a', // 极光绿
  '#faad14', // 日暮黄
  '#f5222d', // 烈焰红
  '#722ed1', // 酱紫
  '#13c2c2', // 青色
  '#eb2f96', // 桃粉
  '#fa541c', // 火山橙
  '#a0d911', // 青柠
  '#2f54eb', // 玄青蓝
  '#bfbfbf', // 中性灰
  '#000000', // 纯黑
];

/** 匹配 #RRGGBB 大小写。 */
const HEX_RE = /^#[0-9A-Fa-f]{6}$/;

export function isValidHex(s: string): boolean {
  if (typeof s !== 'string') return false;
  return HEX_RE.test(s);
}

export interface Rgb {
  r: number;
  g: number;
  b: number;
  hex: string;
}

export function parseThemeColor(hex: string): Rgb {
  const safe = (hex || '').toLowerCase();
  if (!isValidHex(safe)) {
    throw new Error(`Invalid #RRGGBB color: ${hex}`);
  }
  return {
    r: parseInt(safe.slice(1, 3), 16),
    g: parseInt(safe.slice(3, 5), 16),
    b: parseInt(safe.slice(5, 7), 16),
    hex: safe,
  };
}

/** sRGB → linear（WCAG 2.x）。 */
function linearize(c8: number): number {
  const c = c8 / 255;
  return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
}

export function getRelativeLuminance(r: number, g: number, b: number): number {
  return 0.2126 * linearize(r) + 0.7152 * linearize(g) + 0.0722 * linearize(b);
}

/** 两色对比度（WCAG 2.x）。顺序无关。 */
export function getContrastRatio(fg: string, bg: string): number {
  const a = parseThemeColor(fg);
  const b = parseThemeColor(bg);
  const la = getRelativeLuminance(a.r, a.g, a.b);
  const lb = getRelativeLuminance(b.r, b.g, b.b);
  const [hi, lo] = la >= lb ? [la, lb] : [lb, la];
  return (hi + 0.05) / (lo + 0.05);
}

/** WCAG AA 文本对比度阈值 4.5:1。 */
export const WCAG_AA_RATIO = 4.5;

export function meetsWcagAA(fg: string, bg: string): boolean {
  return getContrastRatio(fg, bg) >= WCAG_AA_RATIO;
}

/** 给定背景色，返回满足 4.5:1 的前景色（'#ffffff' 或 '#000000'）。 */
export function getContrastingTextColor(bg: string): '#ffffff' | '#000000' {
  if (meetsWcagAA('#ffffff', bg)) return '#ffffff';
  if (meetsWcagAA('#000000', bg)) return '#000000';
  // 极端情况：背景亮度恰在中间（理论上不会发生），选黑色作保守兜底。
  return '#000000';
}

/**
 * 主页模块保存前校验：至少保留一个可见模块。
 * 返回错误描述字符串；通过返回 null。
 */
export function validateModulesForSave(
  modules: ContentUserHomepageModuleVO[] | undefined | null
): string | null {
  if (!modules || modules.length === 0) {
    return '至少需要保留一个模块';
  }
  if (!modules.some((m) => m && m.visible)) {
    return '至少需要保留一个模块';
  }
  return null;
}
