import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { filterDictTextByCache } from '/@/utils/dict/JDictSelectUtil';

export const columns: BasicColumn[] = [
  {
    title: 'Template title',
    dataIndex: 'templateName',
    width: 80,
  },
  {
    title: 'template encoding',
    dataIndex: 'templateCode',
    width: 100,
  },
  {
    title: 'notification template',
    dataIndex: 'templateContent',
    width: 150,
  },
  {
    title: 'template type',
    dataIndex: 'templateType',
    width: 100,
    customRender: ({ text }) => filterDictTextByCache('msgType', text),
  },
  {
    title: 'Whether to apply',
    dataIndex: 'useStatus',
    width: 90,
    customRender: function ({ text }) {
      if (text == '1') {
        return 'yes';
      } else {
        return 'no';
      }
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Template title',
    field: 'templateName',
    component: 'Input',
  },
  {
    label: 'template encoding',
    field: 'templateCode',
    component: 'Input',
  },
  {
    label: 'template type',
    field: 'templateType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'msgType',
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
    label: 'Template title',
    field: 'templateName',
    component: 'Input',
    required: true,
  },
  {
    label: 'template encoding',
    field: 'templateCode',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [ ...rules.duplicateCheckRule('sys_sms_template', 'template_code', model, schema, true)];
    },
    // The encoding cannot be modified in edit mode
    dynamicDisabled: (params) => !!params.values.id,
  },
  {
    label: 'template type',
    field: 'templateType',
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      dictCode: 'msgType',
      type: 'radio',
      placeholder: '请选择template type',
    },
    required: true,
  },
  {
    label: 'Template classification',
    field: 'templateCategory',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'msgCategory',
      placeholder: '请选择Template classification',
    }
  },
  {
    label: 'Whether to apply',
    field: 'useStatus',
    component: 'JSwitch',
    componentProps: {
      options: ['1', '0'],
    },
  },
  {
    label: 'Template content',
    field: 'templateContent',
    component: 'InputTextArea',
    componentProps: {
      autoSize: {
        minRows: 8,
        maxRows: 8,
      },
    },
    ifShow: ({ values }) => {
      return !['2', '4', '5'].includes(values.templateType);
    },
  },

  {
    label: 'Template content',
    field: 'templateContent',
    component: 'JEditor',
    ifShow: ({ values }) => {
      return ['2', '4'].includes(values.templateType);
    },
  },
  {
    label: 'Template content',
    field: 'templateContent',
    component: 'JMarkdownEditor',
    ifShow: ({ values }) => {
      return ['5'].includes(values.templateType);
    },
  },
];

export const sendTestFormSchemas: FormSchema[] = [
  {
    label: 'template encoding',
    field: 'templateCode',
    component: 'Input',
    show: false,
  },
  {
    label: 'Template title',
    field: 'templateName',
    component: 'Input',
    componentProps: { disabled: true },
  },
  {
    label: 'Template content',
    field: 'templateContent',
    component: 'InputTextArea',
    componentProps: { disabled: true, rows: 5 },
  },
  {
    label: 'test data',
    field: 'testData',
    component: 'InputTextArea',
    required: true,
    helpMessage: 'JSONdata',
    defaultValue: '{}',
    componentProps: {
      placeholder: 'Please enterJSON格式test data',
      rows: 5,
    },
  },
  {
    label: 'Message type',
    field: 'msgType',
    component: 'JDictSelectTag',
    required: true,
    defaultValue:'system',
    componentProps: { dictCode: 'messageType',type:'radio' },
  },
  {
    label: 'message receiver',
    field: 'receiver',
    required: true,
    component: 'JSelectUser',
    componentProps: {
      labelKey: 'username',
      rowKey: 'username',
    },
  },
];
