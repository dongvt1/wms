import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import { Icon } from '/@/components/Icon';

export const columns: BasicColumn[] = [
  {
    title: 'Menu name',
    dataIndex: 'menuName',
    width: 200,
    align: 'left',
  },
  {
    title: 'icon',
    dataIndex: 'icon',
    width: 50,
    customRender: ({ record }) => {
      return h(Icon, { icon: record.icon });
    },
  },
  {
    title: 'Permission ID',
    dataIndex: 'permission',
    width: 180,
  },
  {
    title: 'components',
    dataIndex: 'component',
  },
  {
    title: 'sort',
    dataIndex: 'orderNo',
    width: 50,
  },
  {
    title: 'state',
    dataIndex: 'status',
    width: 80,
    customRender: ({ record }) => {
      const status = record.status;
      const enable = ~~status === 0;
      const color = enable ? 'green' : 'red';
      const text = enable ? 'enable' : 'deactivate';
      return h(Tag, { color: color }, () => text);
    },
  },
  {
    title: 'creation time',
    dataIndex: 'createTime',
    width: 180,
  },
];

const isDir = (type: string) => type === '0';
const isMenu = (type: string) => type === '1';
const isButton = (type: string) => type === '2';

export const searchFormSchema: FormSchema[] = [
  {
    field: 'menuName',
    label: 'Menu name',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: 'state',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'enable', value: '0' },
        { label: 'deactivate', value: '1' },
      ],
    },
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'type',
    label: 'Menu type',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'Table of contents', value: '0' },
        { label: 'menu', value: '1' },
        { label: 'button', value: '2' },
      ],
    },
    colProps: { lg: 24, md: 24 },
  },
  {
    field: 'menuName',
    label: 'Menu name',
    component: 'Input',
    required: true,
  },

  {
    field: 'parentMenu',
    label: '上级menu',
    component: 'TreeSelect',
    componentProps: {
      replaceFields: {
        title: 'menuName',
        key: 'id',
        value: 'id',
      },
      getPopupContainer: () => document.body,
    },
  },

  {
    field: 'orderNo',
    label: 'sort',
    component: 'InputNumber',
    required: true,
  },
  {
    field: 'icon',
    label: 'icon',
    component: 'IconPicker',
    required: true,
    ifShow: ({ values }) => !isButton(values.type),
  },

  {
    field: 'routePath',
    label: 'routing address',
    component: 'Input',
    required: true,
    ifShow: ({ values }) => !isButton(values.type),
  },
  {
    field: 'component',
    label: 'components路径',
    component: 'Input',
    ifShow: ({ values }) => isMenu(values.type),
  },
  {
    field: 'permission',
    label: 'Permission ID',
    component: 'Input',
    ifShow: ({ values }) => !isDir(values.type),
  },
  {
    field: 'status',
    label: 'state',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'enable', value: '0' },
        { label: 'Disable', value: '1' },
      ],
    },
  },
  {
    field: 'isExt',
    label: 'Whether to external link',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'no', value: '0' },
        { label: 'yes', value: '1' },
      ],
    },
    ifShow: ({ values }) => !isButton(values.type),
  },

  {
    field: 'keepalive',
    label: 'yesno缓存',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'no', value: '0' },
        { label: 'yes', value: '1' },
      ],
    },
    ifShow: ({ values }) => isMenu(values.type),
  },

  {
    field: 'show',
    label: 'yesno显示',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'yes', value: '0' },
        { label: 'no', value: '1' },
      ],
    },
    ifShow: ({ values }) => !isButton(values.type),
  },
];
