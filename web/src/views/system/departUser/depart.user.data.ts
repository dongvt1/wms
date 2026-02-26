import { Ref } from 'vue';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { DescItem } from '/@/components/Description';
import { findTree } from '/@/utils/common/compUtils';

// User information columns
export const userInfoColumns: BasicColumn[] = [
  {
    title: 'User account',
    dataIndex: 'username',
    width: 150,
  },
  {
    title: 'Username',
    dataIndex: 'realname',
    width: 180,
  },
  {
    title: 'department',
    dataIndex: 'orgCode',
    width: 200,
  },
  {
    title: 'gender',
    dataIndex: 'sex_dictText',
    width: 80,
  },
  {
    title: 'Telephone',
    dataIndex: 'phone',
    width: 120,
  },
];

// User information查询条件form
export const userInfoSearchFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: 'User account',
    component: 'Input',
  },
];

// department角色 columns
export const departRoleColumns: BasicColumn[] = [
  {
    title: 'department角色名称',
    dataIndex: 'roleName',
    width: 100,
  },
  {
    title: 'department角色编码',
    dataIndex: 'roleCode',
    width: 100,
  },
  {
    title: 'department',
    dataIndex: 'departId_dictText',
    width: 100,
  },
  {
    title: 'Remark',
    dataIndex: 'description',
    width: 100,
  },
];

// department角色查询条件form
export const departRoleSearchFormSchema: FormSchema[] = [
  {
    field: 'roleName',
    label: 'department角色名称',
    component: 'Input',
  },
];

// department角色弹窗formform
export const departRoleModalFormSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'roleName',
    label: 'department角色名称',
    component: 'Input',
    rules: [
      { required: true, message: 'department角色名称不能为空！' },
      { min: 2, max: 30, message: 'The length is 2 arrive 30 characters', trigger: 'blur' },
    ],
  },
  {
    field: 'roleCode',
    label: 'department角色编码',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model }) => {
      return [
        { required: true, message: 'department角色编码不能为空！' },
        { min: 0, max: 64, message: 'The length cannot exceed 64 characters', trigger: 'blur' },
        {
          validator: (_, value) => {
            if (/[\u4E00-\u9FA5]/g.test(value)) {
              return Promise.reject('department角色编码不可输入汉字！');
            }
            return new Promise((resolve, reject) => {
              let params = {
                tableName: 'sys_depart_role',
                fieldName: 'role_code',
                fieldVal: value,
                dataId: model.id,
              };
              duplicateCheckDelay(params)
                .then((res) => {
                  res.success ? resolve() : reject(res.message || 'Verification failed');
                })
                .catch((err) => {
                  reject(err.message || 'Authentication failed');
                });
            });
          },
        },
      ];
    },
  },
  {
    field: 'description',
    label: 'describe',
    component: 'Input',
    rules: [{ min: 0, max: 126, message: 'The length cannot exceed 126 characters', trigger: 'blur' }],
  },
];

// Basic informationform
export function useBaseInfoForm(treeData: Ref<any[]>) {
  const descItems: DescItem[] = [
    {
      field: 'departName',
      label: 'Organization name',
    },
    {
      field: 'parentId',
      label: '上级department',
      render(val) {
        if (val) {
          let data = findTree(treeData.value, (item) => item.key == val);
          return data?.title ?? val;
        }
        return val;
      },
    },
    {
      field: 'orgCode',
      label: 'Institution code',
    },
    {
      field: 'orgCategory',
      label: 'Institution type',
      render(val) {
        if (val === '1') {
          return 'company';
        } else if (val === '2') {
          return 'department';
        } else if (val === '3') {
          return 'post';
        } else if(val === '4'){
          return '子company';
        }
        return val;
      },
    },
    {
      field: 'departOrder',
      label: 'sort',
    },

    {
      field: 'mobile',
      label: 'Phone number',
    },
    {
      field: 'address',
      label: 'address',
    },
    {
      field: 'memo',
      label: 'Remark',
    },
  ];

  return { descItems };
}
