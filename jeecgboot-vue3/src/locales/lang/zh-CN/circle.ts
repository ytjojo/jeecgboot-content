/** 圈子模块国际化文案（中文） */
export const circleLocale = {
  // 通用
  circle: '圈子',
  createCircle: '创建圈子',
  joinCircle: '加入圈子',
  circleName: '圈子名称',
  circleDescription: '圈子简介',
  circleCategory: '分类标签',
  circleIcon: '圈子图标',
  circleCover: '封面图',

  // 列表页
  myCircles: '已加入',
  discoverCircles: '发现',
  searchPlaceholder: '搜索圈子...',
  noCirclesYet: '还没有加入任何圈子',
  goDiscover: '发现圈子',
  loadFailed: '加载失败，请重试',
  retry: '重试',

  // 创建流程
  createStep1: '基础信息',
  createStep2: '隐私设置',
  createStep3: '创建成功',
  nextStep: '下一步',
  prevStep: '上一步',
  submitCreate: '提交创建',
  createSuccess: '创建成功',
  enterCircle: '进入圈子',
  backToList: '返回圈子列表',
  nameRequired: '请输入圈子名称',
  nameLength: '名称长度需在2-30字符之间',
  descRequired: '请输入圈子简介',
  descLength: '简介长度需在10-500字之间',
  nameAvailable: '该名称可用',
  nameExists: '该圈子名称已存在，请修改',
  nameChecking: '校验中...',

  // 隐私与加入方式
  privacyType: '隐私类型',
  privacyPublic: '公开',
  privacyPrivate: '私有',
  privacyPassword: '密码保护',
  joinType: '加入方式',
  joinDirect: '直接加入',
  joinApproval: '申请审核',
  joinInvite: '邀请加入',
  joinPassword: '密码加入',
  password: '密码',
  passwordPlaceholder: '请输入6-20位密码',
  passwordStrengthWeak: '弱',
  passwordStrengthMedium: '中',
  passwordStrengthStrong: '强',
  passwordOnlyDigits: '密码不能为纯数字，请包含字母',
  passwordNoLetter: '密码需包含至少一个字母',
  passwordTooShort: '密码长度需在6-20位之间',
  passwordTooLong: '密码长度需在6-20位之间',

  // 图片上传
  iconRequired: '建议尺寸 200x200，JPG/PNG/WebP，≤2MB',
  coverRequired: '建议尺寸 750x422，JPG/PNG/WebP，≤5MB',
  iconFormatError: '图标格式不支持',
  coverFormatError: '封面图格式不支持',
  iconSizeError: '图标大小不能超过2MB',
  coverSizeError: '封面图大小不能超过5MB',

  // 敏感词
  sensitiveWordHit: '创建成功，内容将在审核后公开',
  sensitiveWordUnavailable: '敏感词服务暂不可用，已降级放行',

  // 详情页
  circleDetail: '圈子详情',
  edit: '编辑',
  circleMembers: '成员列表',
  manageMembers: '成员管理',
  governanceLog: '治理日志',
  leave: '退出',
  leaveConfirm: '确定要退出该圈子吗？退出后需重新申请加入',
  leaveSuccess: '已退出圈子',
  cannotLeave: '创建者不可退出',
  join: '加入',
  joined: '已加入',
  applyJoin: '申请加入',
  applying: '申请中',
  applicationSubmitted: '申请已提交，请等待审核',
  passwordJoin: '密码加入',
  inviteOnly: '仅限邀请加入',
  inviteOnlyTip: '该圈子仅限受邀用户加入',
  circleFull: '圈子已满员，无法加入',
  membersCount: '成员数',
  memberLimit: '上限',
  descriptionExpand: '展开',
  descriptionCollapse: '收起',

  // 加入成功
  joinSuccess: '加入成功',

  // 编辑页面
  editCircle: '编辑圈子',
  nameReadonly: '名称不可修改',
  editSuccess: '编辑成功',

  // 密码弹窗
  enterPassword: '请输入圈子密码',
  passwordError: '密码错误，请重新输入',
  passwordTooManyAttempts: '密码错误次数过多，请稍后再试',
  confirm: '确认',
  cancel: '取消',

  // 成员管理
  memberRole: '角色',
  memberStatus: '状态',
  memberJoinTime: '加入时间',
  memberActions: '操作',
  allRoles: '全部角色',
  allStatuses: '全部状态',
  roleCreator: '创建者',
  roleModerator: '版主',
  roleMember: '成员',
  statusActive: '正常',
  statusMuted: '禁言中',
  statusRemoved: '已移除',
  searchMember: '搜索成员昵称...',
  noMembers: '暂无成员',

  // 角色管理
  setAsModerator: '设为版主',
  unsetModerator: '取消版主',
  setAsModeratorSuccess: '已设置为版主',
  unsetModeratorSuccess: '已取消版主',
  setAsModeratorConfirm: '确定要将 {nickname} 设置为版主吗？',
  unsetModeratorConfirm: '确定要取消 {nickname} 的版主身份吗？',

  // 禁言
  mute: '禁言',
  unmute: '解除禁言',
  muteSuccess: '已禁言',
  unmuteSuccess: '已解除禁言',
  muteDuration: '禁言时长',
  muteReason: '禁言原因（可选）',
  mute1h: '1小时',
  mute24h: '24小时',
  mute7d: '7天',
  muteForever: '永久',
  mutedBanner: '您已被禁言，解除时间：{time}',

  // 移除
  remove: '移除',
  removeSuccess: '已移除',
  removeConfirm: '确定要移除 {nickname} 吗？移除后可重新申请加入',

  // 权限
  permissionDenied: '权限不足',
  operationFailed: '操作失败，该成员状态已变更',

  // 搜索
  searchResults: '搜索结果',
  searchNoResults: '未找到相关圈子',
  searchUnavailable: '搜索暂时不可用',
  browsePublic: '浏览公开圈子',
  searchResultCount: '共 {count} 个结果',

  // 治理日志
  governanceLogTitle: '治理日志',
  operationType: '操作类型',
  targetUser: '操作对象',
  operationDetail: '详情',
  operationTime: '操作时间',
  opTypeAll: '全部',
  opTypeMute: '禁言',
  opTypeUnmute: '解除禁言',
  opTypeRemove: '移除',
  opTypeRoleChange: '角色变更',
  dateRange: '日期范围',
  last30Days: '近30天',
  noGovernanceLog: '暂无治理记录',
  muteDurationLabel: '禁言时长',
  roleChangeDetail: '{from} → {to}',

  // 404/403
  notFound: '圈子不存在或已被删除',
  forbidden: '您没有权限访问此页面',

  // 创建者不可退出
  creatorCannotLeave: '创建者不可退出圈子',
};

export default circleLocale;
