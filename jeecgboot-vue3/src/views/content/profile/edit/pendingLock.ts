/**
 * 判断编辑表单是否应处于禁用状态
 * 当审核状态为 PENDING 时，表单字段不可编辑
 */
export function isFormDisabled(reviewStatus: string | null | undefined): boolean {
  return reviewStatus === 'PENDING';
}

/**
 * 判断保存按钮是否应处于禁用状态
 * 当审核状态为 PENDING 时，保存按钮禁用
 */
export function isSaveDisabled(reviewStatus: string | null | undefined): boolean {
  return reviewStatus === 'PENDING';
}
