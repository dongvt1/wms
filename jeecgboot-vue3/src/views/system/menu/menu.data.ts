import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Icon } from '/@/components/Icon';
import { duplicateCheck } from '../user/user.api';
import { ajaxGetDictItems ,checkPermDuplication } from './menu.api';
import { render } from '/@/utils/common/renderUtils';

const isDir = (type) => type === 0;
const isMenu = (type) => type === 1;
const isButton = (type) => type === 2;

// Define optional component types
export enum ComponentTypes {
  Default = 'layouts/default/index',
  IFrame = 'sys/iframe/FrameBlank',
}

export const columns: BasicColumn[] = [
  {
    title: 'Menu name',
    dataIndex: 'name',
    width: 200,
    align: 'left',
  },
  {
    title: 'Menu type',
    dataIndex: 'menuType',
    width: 150,
    customRender: ({ text }) => {
      return render.renderDict(text, 'menu_type');
    },
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
    title: 'components',
    dataIndex: 'component',
    align: 'left',
    width: 150,
  },
  {
    title: 'path',
    dataIndex: 'url',
    align: 'left',
    width: 150,
  },
  {
    title: 'sort',
    dataIndex: 'sortNo',
    width: 50,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Menu name',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'menuType',
    label: 'Menu type',
    component: 'RadioButtonGroup',
    defaultValue: 0,
    componentProps: ({ formActionType, formModel }) => {
      return {
        options: [
          { label: 'First level menu', value: 0 },
          { label: 'submenu', value: 1 },
          { label: 'button/Permissions', value: 2 },
        ],
        onChange: (e) => {
          const { updateSchema, clearValidate } = formActionType;
          const label = isButton(e) ? 'button/Permissions' : 'Menu name';
          //clear checksum
          clearValidate();
          updateSchema([
            {
              field: 'name',
              label: label,
            },
            {
              field: 'url',
              required: !isButton(e),
            },
          ]);
          //update-begin---author:wangshuai ---date:20220729  for：[VUEN-1834]只有First level menu，Only the default value，submenu的时候，Clear------------
          if (isMenu(e) && !formModel.id && (formModel.component=='layouts/default/index' || formModel.component=='layouts/RouteView')) {
            formModel.component = '';
          }
          //update-end---author:wangshuai ---date:20220729  for：[VUEN-1834]只有First level menu，Only the default value，submenu的时候，Clear------------
        },
      };
    },
  },
  {
    field: 'name',
    label: 'Menu name',
    component: 'Input',
    required: true,
  },
  {
    field: 'parentId',
    label: 'Previous menu',
    component: 'TreeSelect',
    required: true,
    componentProps: {
      //update-begin---author:wangshuai ---date:20230829  for：replaceFieldsExpired，usefieldNamesreplace------------
      fieldNames: {
        label: 'name',
        key: 'id',
        value: 'id',
      },
      //update-end---author:wangshuai ---date:20230829  for：replaceFieldsExpired，usefieldNamesreplace------------
      dropdownStyle: {
        maxHeight: '50vh',
      },
      getPopupContainer: (node) => node?.parentNode,
    },
    ifShow: ({ values }) => !isDir(values.menuType),
  },
  {
    field: 'url',
    label: '访问path',
    component: 'Input',
    required: true,
    //update-begin-author:liusq date:2023-06-06 for: [issues/5008]子表数据Permissions设置不生效
    ifShow: ({ values }) => !(values.component === ComponentTypes.IFrame && values.internalOrExternal),
    //update-begin-author:zyf date:2022-11-02 for: aggregate route允许path重复
     dynamicRules: ({ model, schema,values }) => {
       return checkPermDuplication(model, schema,  values.menuType !== 2?true:false);
    },
    //update-end-author:zyf date:2022-11-02 for: aggregate route允许path重复
    //update-end-author:liusq date:2022-06-06 for:  [issues/5008]子表数据Permissions设置不生效
  },
  {
    field: 'component',
    label: '前端components',
    component: 'Input',
    componentProps: {
      placeholder: 'Please enter前端components',
    },
    defaultValue:'layouts/default/index',
    required: true,
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'componentName',
    label: 'components名称',
    component: 'Input',
    componentProps: {
      placeholder: 'Please entercomponents名称',
    },
    helpMessage: [
      'The name here should be the same asvuecomponents的nameProperties remain consistent。',
      'components名称不能重复，Mainly used for route caching function。',
      '如果components名称和vuecomponents的nameAttributes are inconsistent，This will cause the route cache to become invalid.。',
      'Optional，留空则会根据访问path自动生成。',
    ],
    defaultValue: '',
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'frameSrc',
    label: 'Iframeaddress',
    component: 'Input',
    rules: [
      { required: true, message: 'Please enterIframeaddress' },
      { type: 'url', message: 'Please enter正确的urladdress' },
    ],
    ifShow: ({ values }) => !isButton(values.menuType) && values.component === ComponentTypes.IFrame,
  },
  {
    field: 'redirect',
    label: '默认跳转address',
    component: 'Input',
    ifShow: ({ values }) => isDir(values.menuType),
  },
  {
    field: 'perms',
    label: 'Authorization ID',
    component: 'Input',
    ifShow: ({ values }) => isButton(values.menuType),
    // dynamicRules: ({ model }) => {
    //   return [
    //     {
    //       required: false,
    //       validator: (_, value) => {
    //         return new Promise((resolve, reject) => {
    //           let params = {
    //             tableName: 'sys_permission',
    //             fieldName: 'perms',
    //             fieldVal: value,
    //             dataId: model.id,
    //           };
    //           duplicateCheck(params)
    //             .then((res) => {
    //               res.success ? resolve() : reject(res.message || 'Verification failed');
    //             })
    //             .catch((err) => {
    //               reject(err.message || 'Verification failed');
    //             });
    //         });
    //       },
    //     },
    //   ];
    // },
  },
  {
    field: 'permsType',
    label: 'Authorization strategy',
    component: 'RadioGroup',
    defaultValue: '1',
    helpMessage: ['visible/accessible(授权后visible/accessible)', 'Editable(Disabled without authorization)'],
    componentProps: {
      options: [
        { label: 'visible/accessible', value: '1' },
        { label: 'Editable', value: '2' },
      ],
    },
    ifShow: ({ values }) => isButton(values.menuType),
  },
  {
    field: 'status',
    label: 'state',
    component: 'RadioGroup',
    defaultValue: '1',
    componentProps: {
      options: [
        { label: 'efficient', value: '1' },
        { label: 'invalid', value: '0' },
      ],
    },
    ifShow: ({ values }) => isButton(values.menuType),
  },
  {
    field: 'icon',
    label: '菜单icon',
    component: 'IconPicker',
    ifShow: ({ values }) => !isButton(values.menuType),
    componentProps: {
      allowClear: true
    },
  },
  {
    field: 'sortNo',
    label: 'sort',
    component: 'InputNumber',
    defaultValue: 1,
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'route',
    label: 'Whether to route menu',
    component: 'Switch',
    defaultValue: true,
    componentProps: {
      checkedChildren: 'yes',
      unCheckedChildren: 'no',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'hidden',
    label: 'Hidden route',
    component: 'Switch',
    defaultValue: 0,
    componentProps: {
      checkedChildren: 'yes',
      unCheckedChildren: 'no',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'hideTab',
    label: 'hideTab',
    component: 'Switch',
    defaultValue: 0,
    componentProps: {
      checkedChildren: 'yes',
      unCheckedChildren: 'no',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'keepAlive',
    label: 'yesno缓存路由',
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: 'yes',
      unCheckedChildren: 'no',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'alwaysShow',
    label: 'aggregate route',
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: 'yes',
      unCheckedChildren: 'no',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
  {
    field: 'internalOrExternal',
    label: 'Open method',
    component: 'Switch',
    defaultValue: false,
    componentProps: {
      checkedChildren: 'external',
      unCheckedChildren: 'internal',
    },
    ifShow: ({ values }) => !isButton(values.menuType),
  },
];

export const dataRuleColumns: BasicColumn[] = [
  {
    title: 'Rule name',
    dataIndex: 'ruleName',
    width: 150,
  },
  {
    title: 'Rule field',
    dataIndex: 'ruleColumn',
    width: 100,
  },
  {
    title: 'rule value',
    dataIndex: 'ruleValue',
    width: 100,
  },
];

export const dataRuleSearchFormSchema: FormSchema[] = [
  {
    field: 'ruleName',
    label: 'Rule name',
    component: 'Input',
    // colProps: { span: 6 },
  },
  {
    field: 'ruleValue',
    label: 'rule value',
    component: 'Input',
    // colProps: { span: 6 },
  },
];

export const dataRuleFormSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'ruleName',
    label: 'Rule name',
    component: 'Input',
    required: true,
  },
  {
    field: 'ruleColumn',
    label: 'Rule field',
    component: 'Input',
    ifShow: ({ values }) => {
      const ruleConditions = Array.isArray(values.ruleConditions) ? values.ruleConditions[0] : values.ruleConditions;
      return ruleConditions !== 'USE_SQL_RULES';
    },
  },
  {
    field: 'ruleConditions',
    label: 'conditional rules',
    required: true,
    component: 'ApiSelect',
    componentProps: {
      api: ajaxGetDictItems,
      params: { code: 'rule_conditions' },
      labelField: 'text',
      valueField: 'value',
      getPopupContainer: (node) => document.body,
    },
  },
  // update-begin--author:liaozhiyang---date:20240724---for：【TV360X-1864】Add system variables
  {
    field: 'ruleValue',
    component: 'JInputSelect',
    label: 'rule value',
    required: true,
    componentProps: {
      selectPlaceholder: 'Optional system variables',
      inputPlaceholder: 'Please enter',
      getPopupContainer: () => document.body,
      selectWidth: '200px',
      options: [
        {
          label: 'Login user account',
          value: '#{sys_user_code}',
        },
        {
          label: 'Login user name',
          value: '#{sys_user_name}',
        },
        {
          label: 'current date',
          value: '#{sys_date}',
        },
        {
          label: 'current time',
          value: '#{sys_time}',
        },
        {
          label: 'Login user department',
          value: '#{sys_org_code}',
        },
        {
          label: 'User owns department',
          value: '#{sys_multi_org_code}',
        },
        {
          label: 'Login user tenant',
          value: '#{tenant_id}',
        },
      ],
    },
  },
  // update-end--author:liaozhiyang---date:20240724---for：【TV360X-1864】Add system variables
  {
    field: 'status',
    label: 'state',
    component: 'RadioButtonGroup',
    defaultValue: '1',
    componentProps: {
      options: [
        { label: 'invalid', value: '0' },
        { label: 'efficient', value: '1' },
      ],
    },
  },
];
