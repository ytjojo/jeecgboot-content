/**
 * 主页 16:9 背景裁剪 — 纯几何工具。
 *
 * - `ASPECT_16_9` — 16:9 宽高比常量
 * - `calculateTargetCropArea(width, height)` — 给定原图像素尺寸，返回在原图内
 *   能容纳的最大 16:9 区域（像素宽高）。
 */
export const ASPECT_16_9 = 16 / 9;

export interface CropArea {
  width: number;
  height: number;
}

/**
 * 已知原图 width × height，返回在原图内能容纳的最大 16:9 区域像素尺寸。
 * 不允许放大（任一维度不会超过原图）。
 */
export function calculateTargetCropArea(width: number, height: number): CropArea {
  if (!Number.isFinite(width) || !Number.isFinite(height) || width <= 0 || height <= 0) {
    throw new Error(`Invalid image dimensions: ${width}x${height}`);
  }
  const ratio = width / height;
  if (ratio > ASPECT_16_9) {
    // 太宽：以高度为基准反推宽度
    return { width: height * ASPECT_16_9, height };
  }
  // 太窄或正好：以宽度为基准反推高度
  return { width, height: width / ASPECT_16_9 };
}
