import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '操作时间',
    dataIndex: 'createTime',
    width: 180,
    sorter: true,
  },
  {
    title: '操作人',
    dataIndex: 'operatorUserId_dictText',
    width: 120,
  },
  {
    title: '操作类型',
    dataIndex: 'eventType',
    width: 120,
    customRender: ({ text }) => {
      const map: Record<string, string> = {
        DELETE_COMMENT: '删除评论',
        WARN_USER: '警告用户',
        MUTE_USER: '禁言用户',
        BAN_USER: '封禁用户',
      };
      return map[text] || text;
    },
  },
  {
    title: '目标ID',
    dataIndex: 'targetId',
    width: 150,
  },
  {
    title: '操作原因',
    dataIndex: 'reason',
    ellipsis: true,
  },
  {
    title: '详情',
    dataIndex: 'detail',
    ellipsis: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '操作人',
    field: 'operatorUserId',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '操作类型',
    field: 'eventType',
    component: 'Select',
    componentProps: {
      options: [
        { label: '删除评论', value: 'DELETE_COMMENT' },
        { label: '警告用户', value: 'WARN_USER' },
        { label: '禁言用户', value: 'MUTE_USER' },
        { label: '封禁用户', value: 'BAN_USER' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    label: '开始时间',
    field: 'startTime',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
    },
    colProps: { span: 6 },
  },
  {
    label: '结束时间',
    field: 'endTime',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
    },
    colProps: { span: 6 },
  },
];
