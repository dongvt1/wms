import { getAllRoleList, isAccountExist } from '/@/api/demo/system';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'username',
    dataIndex: 'account',
    width: 120,
  },
  {
    title: 'Nick name',
    dataIndex: 'nickname',
    width: 120,
  },
  {
    title: 'Mail',
    dataIndex: 'email',
    width: 120,
  },
  {
    title: 'creation time',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: 'Role',
    dataIndex: 'role',
    width: 200,
  },
  {
    title: 'Remark',
    dataIndex: 'remark',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'account',
    label: 'username',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'nickname',
    label: 'Nick name',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const accountFormSchema: FormSchema[] = [
  {
    field: 'account',
    label: 'username',
    component: 'Input',
    helpMessage: ['This field demonstrates asynchronous verification', 'Cannot enter withadmin的username'],
    rules: [
      {
        required: true,
        message: '请输入username',
      },
      {
        validator(_, value) {
          return new Promise((resolve, reject) => {
            isAccountExist(value)
              .then(() => resolve())
              .catch((err) => {
                reject(err.message || 'Authentication failed');
              });
          });
        },
      },
    ],
  },
  {
    field: 'pwd',
    label: 'password',
    component: 'InputPassword',
    required: true,
    ifShow: false,
  },
  {
    label: 'Role',
    field: 'role',
    component: 'ApiSelect',
    componentProps: {
      api: getAllRoleList,
      labelField: 'roleName',
      valueField: 'roleValue',
    },
    required: true,
  },
  {
    field: 'dept',
    label: 'Department',
    component: 'TreeSelect',
    componentProps: {
      fieldNames: {
        label: 'deptName',
        key: 'id',
        value: 'id',
      },
      getPopupContainer: () => document.body,
    },
    required: true,
  },
  {
    field: 'nickname',
    label: 'Nick name',
    component: 'Input',
    required: true,
  },

  {
    label: 'Mail',
    field: 'email',
    component: 'Input',
    required: true,
  },

  {
    label: 'Remark',
    field: 'remark',
    component: 'InputTextArea',
  },
];
