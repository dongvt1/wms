import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Message title',
    dataIndex: 'esTitle',
    width: 140,
  },
  {
    title: 'Send content',
    dataIndex: 'esContent',
    width: 200,
    // slots: { customRender: 'esContent' },
  },
  {
    title: 'recipient',
    dataIndex: 'esReceiver',
    width: 140,
  },
  {
    title: 'Send times',
    dataIndex: 'esSendNum',
    width: 120,
  },
  {
    title: 'Send status',
    dataIndex: 'esSendStatus_dictText',
    width: 120,
  },
  {
    title: 'Send time',
    dataIndex: 'esSendTime',
    width: 140,
  },
  {
    title: 'Send method',
    dataIndex: 'esType_dictText',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Message title',
    field: 'esTitle',
    component: 'Input',
  },
  {
    label: 'Send status',
    field: 'esSendStatus',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'msgSendStatus',
    },
  },
  {
    label: 'Send method',
    field: 'esType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'messageType',
    },
  },
];

export const formSchemas: FormSchema[] = [
  {
    label: 'ID',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Message title',
    field: 'esTitle',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Send content',
    field: 'esContent',
    component: 'InputTextArea',
    componentProps: { readOnly: true },
  },
  {
    label: 'Send parameters',
    field: 'esParam',
    component: 'Input',
    componentProps: { readOnly: true },
  },

  {
    label: 'recipient',
    field: 'esReceiver',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Send method',
    field: 'esType',
    component: 'JDictSelectTag',
    componentProps: { disabled: true, dictCode: 'messageType' },
  },
  {
    label: 'Send time',
    field: 'esSendTime',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Send status',
    field: 'esSendStatus',
    component: 'JDictSelectTag',
    componentProps: { disabled: true, dictCode: 'msgSendStatus' },
  },
  {
    label: 'Send times',
    field: 'esSendNum',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Reason for sending failure',
    field: 'esResult',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Remark',
    field: 'remark',
    component: 'InputTextArea',
    componentProps: { readOnly: true },
  },
];
