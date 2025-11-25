import { FormSchema } from '/@/components/Table';

//column configuration
export const columns = [
  {
    title: 'Association type(user/Role)',
    dataIndex: 'relationType_dictText',
    width: 80,
    slots: { customRender: 'relationType' },
  },
  {
    title: 'user/Role编码',
    dataIndex: 'roleCode',
    width: 80,
    slots: { customRender: 'roleCode' },
  },
  {
    title: 'Home page routing',
    dataIndex: 'url',
    width: 100,
  },
  {
    title: 'Component address',
    dataIndex: 'component',
    width: 100,
  },
  {
    title: 'Whether to turn on',
    dataIndex: 'status',
    slots: { customRender: 'status' },
    width: 60,
  },
];
//Query configuration
export const searchFormSchema: FormSchema[] = [
  {
    field: 'relationType',
    label: 'Association type',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'relation_type',
    },
  },
  {
    field: 'route',
    label: 'Whether to route menu',
    helpMessage: 'Set the non-routing menu to the homepage，Need to be turned on',
    component: 'Switch',
    show: false,
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: '',
    component: 'Input',
    show: false,
  },
  {
    field: 'relationType',
    label: 'Association type',
    component: 'JDictSelectTag',
    required: true,
    defaultValue: 'ROLE',
    componentProps: {
      dictCode: 'relation_type',
      type: 'radioButton',
    },
  },
  {
    label: 'Role编码',
    field: 'roleCode',
    component: 'JSelectRole',
    required: true,
    componentProps: {
      rowKey: 'roleCode',
      isRadioSelection: true,
    },
    ifShow: ({ values }) => values.relationType == 'ROLE',
  },
  {
    label: 'user编码',
    field: 'userCode',
    component: 'JSelectUser',
    required: true,
    componentProps: {
      isRadioSelection: true,
    },
    ifShow: ({ values }) => values.relationType == 'USER',
  },
  {
    label: 'Home page routing',
    field: 'url',
    component: 'Input',
    required: true,
  },
  {
    label: 'Component address',
    field: 'component',
    component: 'Input',
    componentProps: {
      placeholder: 'Please enter the front-end component',
    },
    required: true,
  },
  {
    label: 'priority',
    field: 'priority',
    component: 'InputNumber',
  },
  {
    field: 'route',
    label: 'Whether to route menu',
    helpMessage: 'Set the non-routing menu to the homepage，Need to be turned on',
    component: 'Switch',
    defaultValue: true,
    show: false,
  },
  {
    label: 'Whether to turn on',
    field: 'status',
    component: 'JSwitch',
    defaultValue: '1',
    componentProps: {
      options: ['1', '0'],
    },
  },
];
