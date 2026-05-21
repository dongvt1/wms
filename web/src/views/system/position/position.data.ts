import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';

export const columns: BasicColumn[] = [
  // {
  //   title: 'Job code',
  //   dataIndex: 'code',
  //   width: 200,
  //   align: 'left',
  // },
  {
    title: 'Job level name',
    dataIndex: 'name',
    align: 'left'
    // width: 200,
  },
  {
    title: 'Job level(The smaller the level, the higher the level)',
    dataIndex: 'postLevel',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Job level name',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: 'primary key',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'name',
    label: 'Job level name',
    component: 'Input',
    required: true,
  },
  {
    label: 'Job level',
    field: 'postLevel',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 1,
      max: 99
    },
    dynamicRules: ({ model, schema }) => {
      return [{ required: true, message: '请输入Job level!' }];
    },
  },
  // {
  //   field: 'code',
  //   label: 'Job code',
  //   component: 'Input',
  //   required: true,
  //   dynamicDisabled: ({ values }) => {
  //     return !!values.id;
  //   },
  //   dynamicRules: ({ model, schema }) => {
  //     return rules.duplicateCheckRule('sys_position', 'code', model, schema, true);
  //   },
  // },
];
