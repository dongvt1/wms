import { FormSchema } from '/@/components/Table';
import { isRoleExist } from './role.api';
export const columns = [
  {
    title: 'Character name',
    dataIndex: 'roleName',
    width: 100,
  },
  {
    title: 'role coding',
    dataIndex: 'roleCode',
    width: 100,
  },
  {
    title: 'creation time',
    dataIndex: 'createTime',
    width: 100,
  },
];
/**
 * role userColumns
 */
export const userColumns = [
  {
    title: 'User account',
    dataIndex: 'username',
  },
  {
    title: 'User name',
    dataIndex: 'realname',
  },
  {
    title: 'state',
    dataIndex: 'status_dictText',
    width: 80,
  },
];
export const searchFormSchema: FormSchema[] = [
  {
    field: 'roleName',
    label: 'Character name',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'roleCode',
    label: 'role coding',
    component: 'Input',
    colProps: { span: 6 },
  },
];
/**
 * role user搜索form
 */
export const searchUserFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: 'User account',
    component: 'Input',
    colProps: { span: 8 },
    labelWidth: 74,
  },
  {
    field: 'realname',
    label: 'Username',
    component: 'Input',
    colProps: { span: 8 },
    labelWidth: 74,
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
    field: 'roleName',
    label: 'Character name',
    required: true,
    component: 'Input',
  },
  {
    field: 'roleCode',
    label: 'role coding',
    required: true,
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ values, model }) => {
      console.log('values:', values);
      return [
        {
          required: true,
          validator: (_, value) => {
            if (!value) {
              return Promise.reject('请输入role coding');
            }
            if (values) {
              return new Promise((resolve, reject) => {
                isRoleExist({ id: model.id, roleCode: value })
                  .then((res) => {
                    res.success ? resolve() : reject(res.message || 'Verification failed');
                  })
                  .catch((err) => {
                    reject(err.message || 'Authentication failed');
                  });
              });
            }
            return Promise.resolve();
          },
        },
      ];
    },
  },
  {
    label: 'Remark',
    field: 'description',
    component: 'InputTextArea',
  },
];

export const formDescSchema = [
  {
    field: 'roleName',
    label: 'Character name',
  },
  {
    field: 'roleCode',
    label: 'role coding',
  },
  {
    label: 'Remark',
    field: 'description',
  },
];

export const roleIndexFormSchema: FormSchema[] = [
  {
    field: 'id',
    label: '',
    component: 'Input',
    show: false,
  },
  {
    label: 'role coding',
    field: 'roleCode',
    component: 'Input',
    dynamicDisabled: true,
  },
  {
    label: 'Home page routing',
    field: 'url',
    component: 'Input',
    required: true,
    helpMessage: 'Home page routing的访问地址',
  },
  {
    label: 'Component address',
    field: 'component',
    component: 'Input',
    helpMessage: 'Home page routing的Component address',
    componentProps: {
      placeholder: 'Please enter the front-end component',
    },
    required: true,
  },
  {
    field: 'route',
    label: 'Whether to route menu',
    helpMessage: 'Set the non-routing menu to the homepage，Need to be turned on',
    component: 'Switch',
    defaultValue: true
  },
  {
    label: 'priority',
    field: 'priority',
    component: 'InputNumber',
  },
  {
    label: 'Whether to turn on',
    field: 'status',
    component: 'JSwitch',
    componentProps: {
      options: ['1', '0'],
    },
  },
];
