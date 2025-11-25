import { BasicColumn, FormSchema } from '/@/components/Table';
import { getAutoScrollContainer } from '/@/utils/common/compUtils';
import { render } from "/@/utils/common/renderUtils";
import { rules } from "/@/utils/helper/validator";

export const columns: BasicColumn[] = [
  {
    title: 'Tenant name',
    dataIndex: 'name',
    width: 200,
    align: 'left',
  },
  {
    title: 'Tenant number(ID)',
    dataIndex: 'id',
    width: 180,
  },{
    title: 'organizeLOGO',
    dataIndex: 'companyLogo',
    width: 100,
    customRender: ({ text }) => {
      if(!text){
        return text;
      }
      return render.renderImage({text});
    },
  },
  {
    dataIndex: 'trade_dictText',
    title: 'Industry',
    width: 150
  },
  {
    dataIndex: 'companySize_dictText',
    title: 'Company size',
    width: 100
  },
  {
    dataIndex: 'houseNumber',
    title: 'house number',
    width: 100,
  },
  // {
  //   dataIndex: 'position_dictText',
  //   title: 'Rank',
  //   width: 150
  // },
  // {
  //   dataIndex: 'department_dictText',
  //   title: 'department',
  //   width: 150
  // },
  {
    dataIndex: 'createBy_dictText',
    title: 'Creator(have)',
    width: 150
  },
/*  {
    title: 'start time',
    dataIndex: 'beginDate',
    sorter: true,
    width: 180,
  },
  {
    title: 'end time',
    dataIndex: 'endDate',
    sorter: true,
    width: 180,
  },*/
  {
    title: 'state',
    dataIndex: 'status_dictText',
    width: 100,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Tenant name',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: 'state',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'normal', value: 1 },
        { label: 'freeze', value: 0 },
      ],
    },
    colProps: { span: 8 },
  },
  // {
  //   field: 'fieldTime',
  //   component: 'RangePicker',
  //   label: 'time field',
  //   componentProps: {
  //     valueType: 'Date',
  //   },
  //   colProps: {
  //     span: 8,
  //   },
  // },
];

export const formSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Tenant name',
    component: 'Input',
    required: true,
  },
  {
    field: 'id',
    label: 'Tenant number(ID)',
    component: 'InputNumber',
    required: true,
    ifShow: ({ values }) => {
      return values.id!=null;
    },
  },
  {
    field: 'companyLogo',
    label: 'organizeLOGO',
    component: 'JImageUpload',
    componentProps:{
      text:'logo'
    }
  },
  {
    field: 'trade',
    label: 'Industry',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode:'trade',
    }
  }, {
    field: 'companySize',
    label: 'Company size',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode:'company_size',
    }
  }, {
    field: 'companyAddress',
    label: 'Company address',
    component: 'JAreaSelect',
    componentProps: {
      placeholder: '请输入Company address',
      rows: 4,
    }
  },
  {
    field: 'workPlace',
    label: 'work place',
    component: 'InputTextArea',
    componentProps: {
      placeholder: '请输入work place',
      rows: 4,
    }
  },
/*  {
    field: 'beginDate',
    label: 'start time',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      getPopupContainer: getAutoScrollContainer,
    },
  },
  {
    field: 'endDate',
    label: 'end time',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      getPopupContainer: getAutoScrollContainer,
    },
  },*/
  {
    field: 'houseNumber',
    label: 'house number',
    component: 'Input',
    dynamicDisabled: true,
    ifShow: ({ values }) => {
      return values.id!=null;
    },
  },
  {
    field: 'position',
    label: 'Rank',
    component: 'JDictSelectTag',
    componentProps:{
      dictCode: 'company_rank'
    }
  },
  {
    field: 'department',
    label: 'department',
    component: 'JDictSelectTag',
    componentProps:{
      dictCode:'company_department'
    }
  },
  {
    field: 'status',
    label: 'state',
    component: 'RadioButtonGroup',
    defaultValue: 1,
    componentProps: {
      options: [
        { label: 'normal', value: 1 },
        { label: 'freeze', value: 0 },
      ],
    },
  },
];

//Define user table columns
export const userColumns: BasicColumn[] =[
  {
    title: 'User account',
    dataIndex: 'username',
    width: 100,
    align: 'left',
  },
  {
    title: 'User name',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: 'gender',
    dataIndex: 'sex_dictText',
    width: 100,
  },
  {
    title: 'phone number',
    dataIndex: 'phone',
    width: 100,
  },
];

//Invite users to search form
export const userSearchFormSchema: FormSchema[] = [
  {
    field: 'username',
    label: 'account',
    component: 'Input',
  },
  {
    field: 'realname',
    label: 'Name',
    component: 'Input',
  },
];

//Package list
export const packColumns: BasicColumn[] = [
  {
    title: 'Package name',
    dataIndex: 'packName',
    width: 100,
    customRender: ( { record, text }) => {
      if(record.packCode && record.packCode.indexOf('default') != -1) {
        return text + '(Default product package)';
      } else {
        return text;
      }
    }
  },
  {
    title: 'Whether to automatically assign users',
    dataIndex: 'izSysn',
    width: 100,
    customRender: ( { text }) => {
      if(text === '1') {
        return 'yes';
      } else {
        return 'no';
      }
    }
  },
  {
    title: 'state',
    dataIndex: 'status',
    width: 100,
    customRender: ({ text }) => {
      if (text === '1') {
        return 'turn on';
      } else {
        return 'closure';
      }
    },
  },
  {
    title: 'Remarks',
    dataIndex: 'remarks',
    width: 150,
  },
];

//Package list
export const tenantPackColumns: BasicColumn[] = [
  {
    title: 'Package name',
    dataIndex: 'packName',
    width: 100,
    customRender: ( { record, text }) => {
      if(record.packCode && record.packCode.indexOf('default') != -1) {
        return text + '(Default product package)';
      } else {
        return text;
      }
    }
  },
  {
    title: 'Whether to automatically assign users',
    dataIndex: 'izSysn',
    width: 100,
    customRender: ( { text }) => {
      if(text === '1') {
        return 'yes';
      } else {
        return 'no';
      }
    }
  },
  {
    title: 'Remarks',
    dataIndex: 'remarks',
    width: 150,
  },
];

//Package list
export const defalutPackColumns: BasicColumn[] = [
  {
    title: 'Default package name',
    dataIndex: 'packName',
    width: 100,
  },
  {
    title: 'state',
    dataIndex: 'status',
    width: 100,
    customRender: ({ text }) => {
      if (text === '1') {
        return 'turn on';
      } else {
        return 'closure';
      }
    },
  },
  {
    title: 'Remarks',
    dataIndex: 'remarks',
    width: 150,
  },
];

//Package search form
export const packFormSchema: FormSchema[] = [
  {
    field: 'packName',
    label: 'Package name',
    component: 'JInput',
    colProps: { xxl: 8 },
  },
];

//Package search form
export const defaultPackFormSchema: FormSchema[] = [
  {
    field: 'packName',
    label: 'Default package name',
    component: 'JInput',
    colProps: { xxl: 8 },
  },
];

//Package form
export const packMenuFormSchema: FormSchema[] = [
  {
    field: 'packName',
    label: 'Package name',
    component: 'Input',
  },
  {
    field: 'permissionIds',
    label: 'Authorization menu',
    component: 'JTreeSelect',
    componentProps: {
      dict: 'sys_permission,name,id',
      pidField: 'parent_id',
      hasChildField:'is_leaf',
      multiple: true,
      treeCheckAble:true,
      treeCheckStrictly: true,
      converIsLeafVal: 0,
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'remarks',
    label: 'Remarks',
    component: 'InputTextArea',
  },
  {
    field: 'izSysn',
    label: 'Automatically assign users',
    component: 'Switch',
    componentProps: {
      checkedValue: "1",
      checkedChildren: 'yes',
      unCheckedValue: "0",
      unCheckedChildren: 'no',
    },
    defaultValue: "1",
    helpMessage: "Automatically assigned to users by default，Personalized premium package，Tenant administrators are required to manually allocate personnel(have更灵活性权限控制)"
  },  
  {
    field: 'status',
    label: 'turn onstate',
    component: 'Switch',
    componentProps: {
      checkedValue: '1',
      checkedChildren: 'turn on',
      unCheckedValue: '0',
      unCheckedChildren: 'closure',
    },
    defaultValue: '1',
  },
  {
    field: 'id',
    label: 'turn onstate',
    component: 'Input',
    show: false
  },
  {
    field: 'packCode',
    label: 'Product package code',
    component: 'Input',
    show: false
  },  
  {
    field: 'packType',
    label: 'Product package type',
    component: 'Input',
    show: false
  },
];

//Recycle bin list
export const recycleColumns : BasicColumn[] = [
  {
    title: 'Tenant name',
    dataIndex: 'name',
    width: 100,
    align: 'left',
  },
  {
    title: 'Tenant number(ID)',
    dataIndex: 'id',
    width: 100,
  },
  {
    title: 'organizeLOGO',
    dataIndex: 'companyLogo',
    width: 100,
    customRender: ({ text }) => {
      if(!text){
        return text;
      }
      return render.renderImage({text});
    },
  },
  {
    dataIndex: 'houseNumber',
    title: 'house number',
    width: 100,
  }
]

//Tenant recycle bin search form
export const searchRecycleFormSchema : FormSchema[] = [
  {
    field: 'name',
    label: 'Tenant name',
    component: 'Input',
  },
  {
    field: 'houseNumber',
    label: 'house number',
    component: 'Input',
  },
]

//Package user list
export const tenantPackUserColumns: BasicColumn[] = [
  {
    title: 'user',
    dataIndex: 'realname',
    width: 200,
  },
  {
    title: 'department',
    dataIndex: 'departNames',
    width: 200,
    ellipsis: true,
    slots: { customRender: 'departNames' }
  },
  {
    title: 'Position',
    dataIndex: 'positionNames',
    ellipsis: true,
    width: 200,
    slots: { customRender: 'positionNames' }
  }
]

/**
 * usertenant新增编辑表单
 */
export const tenantUserSchema: FormSchema[] = [
  { field: 'id', label: 'id', component: 'Input', show: false },
  { field: 'username', label: 'username', component: 'Input', show: false },
  {
    field: 'realname',
    label: 'Name',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    field: 'phone',
    label: 'cell phone',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      if (model.id) {
        return [];
      }
      return [{ ...rules.phone(true)[0] }, { ...rules.duplicateCheckRule('sys_user', 'phone', model, schema, false)[0] }];
    },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    field: 'email',
    label: 'Mail',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      if (model.id) {
        return [];
      }
      return [{ ...rules.email(true)[0] }, { ...rules.duplicateCheckRule('sys_user', 'email', model, schema, false)[0] }];
    },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  { field: 'selecteddeparts', label: 'department', component: 'JSelectDept', componentProps: { checkStrictly: true } },
 /* {
    field: 'post',
    label: 'Position',
    component: 'JSelectPosition',
  },
  {
    field: 'workNo',
    label: 'Job number',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [{ required: false, message: '请输入Job number' }, { ...rules.duplicateCheckRule('sys_user', 'work_no', model, schema, false)[0] }];
    },
  },*/
  { field: 'relTenantIds', label: 'tenant', component: 'Input',show:false },
  { field: 'selectedroles', label: 'Role', component: 'Input',show:false },
];

// 分配usercombo
export const packUserAllotSchemas: FormSchema[] = [
  { 
    field: 'userId', 
    label: 'userid', 
    component: 'Input',
    show: false
  },
  {
    field: 'realname',
    label: 'User name',
    component: 'Input',
    componentProps:{
      readonly : true
    },
  },
  {
    field: 'packId',
    label: 'combo',
    component: 'Select',
    slot: 'packId'
  }
];