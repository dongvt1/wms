import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { getAllRolesListNoByTenant, getAllTenantList } from './user.api';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
export const columns: BasicColumn[] = [
  {
    title: 'User account',
    dataIndex: 'username',
    width: 120,
  },
  {
    title: 'User name',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: 'avatar',
    dataIndex: 'avatar',
    width: 120,
    customRender: render.renderAvatar,
  },
  {
    title: 'gender',
    dataIndex: 'sex',
    width: 80,
    sorter: true,
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
  {
    title: 'Birthday',
    dataIndex: 'birthday',
    width: 100,
  },
  {
    title: 'Phone number',
    dataIndex: 'phone',
    width: 100,
  },
  {
    title: 'department',
    width: 150,
    dataIndex: 'orgCodeTxt',
  },
  {
    title: '负责department',
    width: 150,
    dataIndex: 'departIds_dictText',
  },
  {
    title: 'main post',
    width: 150,
    dataIndex: 'mainDepPostId_dictText',
  },
  {
    title: 'state',
    dataIndex: 'status_dictText',
    width: 80,
  },
];

export const recycleColumns: BasicColumn[] = [
  {
    title: 'User account',
    dataIndex: 'username',
    width: 100,
  },
  {
    title: 'User name',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: 'avatar',
    dataIndex: 'avatar',
    width: 80,
    customRender: render.renderAvatar,
  },
  {
    title: 'gender',
    dataIndex: 'sex',
    width: 80,
    sorter: true,
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'account',
    field: 'username',
    component: 'JInput',
    //colProps: { span: 6 },
  },
  {
    label: 'name',
    field: 'realname',
    component: 'JInput',
   //colProps: { span: 6 },
  },
  {
    label: 'gender',
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: '请选择gender',
      stringToNumber: true,
    },
    //colProps: { span: 6 },
  },
  {
    label: 'Phone number码',
    field: 'phone',
    component: 'Input',
    //colProps: { span: 6 },
  },
  {
    label: '用户state',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'user_status',
      placeholder: '请选择state',
      stringToNumber: true,
    },
   //colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'User account',
    field: 'username',
    component: 'Input',
    required: true,
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_user', 'username', model, schema, true),
  },
  {
    label: 'Login password',
    field: 'password',
    component: 'StrengthMeter',
    componentProps:{
      autocomplete: 'new-password',
    },
    rules: [
      {
        required: true,
        message: '请输入Login password',
      },
      {
        pattern: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/,
        message: 'The password is8digits、Composed of uppercase and lowercase letters and special symbols!',
      },
    ],
  },
  {
    label: 'Confirm Password',
    field: 'confirmPassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
  },
  {
    label: 'User name',
    field: 'realname',
    required: true,
    component: 'Input',
  },
  {
    label: 'Job number',
    field: 'workNo',
    required: false,
    component: 'Input',
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_user', 'work_no', model, schema, false),
  },
/*  {
    label: 'Position',
    field: 'post',
    required: false,
    component: 'JSelectPosition',
    componentProps: {
      labelKey: 'name',
    },
  },*/
  {
    label: 'Position',
    field: 'positionType',
    required: false,
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "user_position"
    },
  },
  {
    label: 'Role',
    field: 'selectedroles',
    component: 'ApiSelect',
    componentProps: {
      mode: 'multiple',
      api: getAllRolesListNoByTenant,
      labelField: 'roleName',
      valueField: 'id',
      immediate: false,
    },
  },
  {
    label: '所属department',
    field: 'selecteddeparts',
    component: 'JSelectDept',
    componentProps: ({ formActionType, formModel }) => {
      return {
        sync: false,
        checkStrictly: true,
        defaultExpandLevel: 2,

        onSelect: (options, values) => {
          const { updateSchema } = formActionType;
          //所属department修改后更新负责department下拉框数据
          updateSchema([
            {
              field: 'departIds',
              componentProps: { options },
            },
            //修改main post和part-time position的参数
            {
              field: 'mainDepPostId',
              componentProps: { params: { departIds: values?values.value.join(","): "" } },
            },
            {
              field: 'otherDepPostId',
              componentProps: { params: { departIds: values?values.value.join(","): "" } },
            }
          ]);
          //update-begin---author:wangshuai---date:2024-05-11---for:【issues/1222】User editing interface“所属department”and“负责department”Linkage error correction---
          if(!values){
            formModel.departIds = [];
            formModel.mainDepPostId = "";
            formModel.otherDepPostId = "";
            return;
          }
          //update-end---author:wangshuai---date:2024-05-11---for:【issues/1222】User editing interface“所属department”and“负责department”Linkage error correction---
          //所属department修改后更新负责department数据
          formModel.departIds && (formModel.departIds = formModel.departIds.filter((item) => values.value.indexOf(item) > -1));
        },
      };
    },
  },
  {
    label: 'main post',
    field: 'mainDepPostId',
    component: 'JSelectDepartPost',
    componentProps: {
      rowKey: 'id',
      multiple: false
    },
    ifShow:  ({ values }) => {
      if(!values.selecteddeparts){
        return false;
      }
      return !(values.selecteddeparts instanceof Array && values.selecteddeparts.length == 0);
    },
  },
  {
    label: 'part-time position',
    field: 'otherDepPostId',
    component: 'JSelectDepartPost',
    componentProps: {
      rowKey: 'id',
    },
    ifShow:  ({ values }) => {
      if(!values.selecteddeparts){
        return false;
      }
      return !(values.selecteddeparts instanceof Array && values.selecteddeparts.length == 0);
    },
  },
  {
    label: 'tenant',
    field: 'relTenantIds',
    component: 'JSearchSelect',
    componentProps: {
      dict:"sys_tenant,name,id",
      async: true,
      multiple: true
    },
  },
  {
    label: 'identity',
    field: 'userIdentity',
    component: 'RadioGroup',
    defaultValue: 1,
    componentProps: ({ formModel }) => {
      return {
        options: [
          { label: 'Ordinary user', value: 1, key: '1' },
          { label: 'Superior', value: 2, key: '2' },
        ],
        onChange: () => {
          formModel.userIdentity == 1 && (formModel.departIds = []);
        },
      };
    },
  },
  {
    label: '负责department',
    field: 'departIds',
    component: 'Select',
    componentProps: {
      mode: 'multiple',
    },
    ifShow: ({ values }) => values.userIdentity == 2,
  },
  {
    label: 'avatar',
    field: 'avatar',
    component: 'JImageUpload',
    componentProps: {
      fileMax: 1,
    },
  },
  {
    label: 'Birthday',
    field: 'birthday',
    component: 'DatePicker',
  },
  {
    label: 'gender',
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: '请选择gender',
      stringToNumber: true,
    },
  },
  {
    label: 'Mail',
    field: 'email',
    component: 'Input',
    required: false,
    dynamicRules: ({ model, schema }) => {
      return [
        { ...rules.duplicateCheckRule('sys_user', 'email', model, schema, false)[0], trigger: 'blur' },
        { ...rules.rule('email', false)[0], trigger: 'blur' },
      ];
    },
  },
  {
    label: 'Phone number码',
    field: 'phone',
    component: 'Input',
    required: true,
    dynamicRules: ({ model, schema }) => {
      return [
        { ...rules.duplicateCheckRule('sys_user', 'phone', model, schema, true)[0], trigger: 'blur' },
        { pattern: /^1[3456789]\d{9}$/, message: 'Phone number码格式有误', trigger: 'blur' },
      ];
    },
  },
  {
    label: 'landline',
    field: 'telephone',
    component: 'Input',
    rules: [{ pattern: /^0\d{2,3}-[1-9]\d{6,7}$/, message: '请输入正确的landline号码' }],
  },
  {
    label: 'workflow engine',
    field: 'activitiSync',
    defaultValue: 1,
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'activiti_sync',
      type: 'radio',
      stringToNumber: true,
    },
  },
];

export const formPasswordSchema: FormSchema[] = [
  {
    label: 'User account',
    field: 'username',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Login password',
    field: 'password',
    component: 'StrengthMeter',
    componentProps: {
      placeholder: '请输入Login password',
    },
    rules: [
      {
        required: true,
        message: '请输入Login password',
      },
      {
        pattern: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/,
        message: 'The password is8digits、Composed of uppercase and lowercase letters and special symbols!',
      },
    ],
  },
  {
    label: 'Confirm Password',
    field: 'confirmPassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
  },
];



//tenant用户列表
export const userTenantColumns: BasicColumn[] = [
  {
    title: 'User account',
    dataIndex: 'username',
    width: 120,
  },
  {
    title: 'User name',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: 'avatar',
    dataIndex: 'avatar',
    width: 120,
    customRender: render.renderAvatar,
  },
  {
    title: 'Phone number',
    dataIndex: 'phone',
    width: 100,
  },
  {
    title: 'department',
    width: 150,
    dataIndex: 'orgCodeTxt',
  },
  {
    title: 'state',
    dataIndex: 'status',
    width: 80,
    customRender: ({ text }) => {
      if (text === '1') {
        return 'normal';
      } else if (text === '3') {
        return 'Under review';
      } else {
        return 'Rejected';
      }
    },
  },
];

//用户tenant搜索表单
export const userTenantFormSchema: FormSchema[] = [
  {
    label: 'account',
    field: 'username',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: 'name',
    field: 'realname',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: 'gender',
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: '请选择gender',
      stringToNumber: true,
    },
    colProps: { span: 6 },
  },
];
