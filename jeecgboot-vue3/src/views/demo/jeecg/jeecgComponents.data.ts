import { FormSchema, JCronValidator } from '/@/components/Form';
import { usePermission } from '/@/hooks/web/usePermission';

const { isDisabledAuth } = usePermission();
export const schemas: FormSchema[] = [
  {
    field: 'jdst',
    component: 'JDictSelectTag',
    label: 'Gender drop down',
    helpMessage: ['componentmodel'],
    componentProps: {
      dictCode: 'sex',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'jdst',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'jdst1',
    component: 'JDictSelectTag',
    label: 'sex selection',
    helpMessage: ['componentmodel'],
    componentProps: {
      dictCode: 'sex',
      type: 'radioButton',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'jdst1',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'jdst2',
    component: 'JDictSelectTag',
    label: 'Dictionary drop down',
    helpMessage: ['componentmodel'],
    componentProps: {
      dictCode: 'sys_user,realname,id',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'jdst2',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'jdst3',
    component: 'JDictSelectTag',
    label: 'Dictionary drop down(With conditions)',
    helpMessage: ['componentmodel'],
    componentProps: {
      dictCode: "sys_user,realname,id,username!='admin' order by create_time",
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'jdst3',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'jsst',
    component: 'JSearchSelect',
    label: 'dictionary search(synchronous)',
    colProps: { span: 12 },
    componentProps: {
      //dict: "sys_depart,depart_name,id",
      dictOptions: [
        {
          text: 'Option one',
          value: '1',
        },
        {
          text: 'Option two',
          value: '2',
        },
        {
          text: 'Option three',
          value: '3',
        },
      ],
    },
  },
  {
    field: 'jsst',
    component: 'JEllipsis',
    label: 'Select value',
    colProps: { span: 12 },
  },
  {
    field: 'jsst2',
    component: 'JSearchSelect',
    label: 'dictionary search(asynchronous)',
    colProps: { span: 12 },
    componentProps: {
      dict: 'sys_depart,depart_name,id',
      pageSize: 6,
      async: true,
    },
  },
  {
    field: 'jsst2',
    component: 'JEllipsis',
    label: 'Select value',
    colProps: { span: 12 },
  },
  {
    field: 'xldx',
    component: 'JDictSelectTag',
    label: 'Dictionary drop-down multiple selection',
    colProps: { span: 12 },
    componentProps: {
      dictCode: 'sex',
      mode: 'multiple',
    },
  },
  {
    field: 'xldx',
    component: 'JEllipsis',
    label: 'Select value',
    colProps: { span: 12 },
  },
  {
    field: 'xldx2',
    component: 'JSelectMultiple',
    label: 'Dictionary drop-down multiple selection2',
    colProps: { span: 12 },
    componentProps: {
      dictCode: 'sex',
    },
  },
  {
    field: 'xldx2',
    component: 'JEllipsis',
    label: 'Select value',
    colProps: { span: 12 },
  },
  {
    field: 'dxxlk',
    component: 'JDictSelectTag',
    label: 'Dictionary drop-down radio selection',
    colProps: { span: 12 },
    componentProps: {
      dictCode: 'sex',
    },
  },
  {
    field: 'dxxlk',
    component: 'JEllipsis',
    label: 'Select value',
    colProps: { span: 12 },
  },
  {
    label: 'Input drop-down',
    field: 'selectInput',
    component: 'JSelectInput',
    componentProps: {
      options: [
        { label: 'Option one', value: '1' },
        { label: 'Option two', value: '2' },
        { label: 'Option three', value: '3' },
      ],
    },
    colProps: { span: 12 },
  },
  {
    field: 'selectInput',
    component: 'JEllipsis',
    label: 'Select value',
    colProps: { span: 12 },
  },
  {
    field: 'depart3',
    component: 'JSelectDept',
    label: 'Select department—custom value',
    helpMessage: ['componentmodel'],
    componentProps: { showButton: false, rowKey: 'orgCode', primaryKey: 'orgCode' },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'depart3',
    component: 'JEllipsis',
    label: 'Select department',
    colProps: { span: 12 },
  },
  {
    field: 'depart2',
    component: 'JSelectDept',
    label: 'Select department',
    helpMessage: ['componentmodel'],
    componentProps: { showButton: false },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'depart2',
    component: 'JEllipsis',
    label: 'Select department',
    colProps: { span: 12 },
  },
  {
    field: 'depart4',
    component: 'JSelectDepartPost',
    label: 'Choose a position',
    helpMessage: ['componentmodel'],
    componentProps: { showButton: false },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'depart4',
    component: 'JEllipsis',
    label: 'Choose a position',
    colProps: { span: 12 },
  },
  {
    field: 'user2',
    component: 'JSelectUser',
    label: 'user selects component',
    helpMessage: ['componentmodel'],
    componentProps: {
      labelKey: 'realname',
      rowKey: 'id',
      showSelected: true,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'user2',
    component: 'JEllipsis',
    label: 'Select user',
    colProps: { span: 12 },
  },
  {
    field: 'user3',
    component: 'JSelectUserByDept',
    label: 'Department Select User',
    helpMessage: ['componentmodel'],
    componentProps: {
      labelKey: 'realname',
      rowKey: 'username',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'user3',
    component: 'JEllipsis',
    label: 'Select user',
    colProps: { span: 12 },
  },
  {
    field: 'userPost1',
    component: 'JSelectUserByDeptPost',
    label: 'Department position selection user',
    helpMessage: ['componentmodel'],
    componentProps: {
      labelKey: 'realname',
      rowKey: 'username',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'userPost1',
    component: 'JEllipsis',
    label: 'Select user',
    colProps: { span: 12 },
  },
  {
    field: 'user4',
    component: 'JSelectUserByDepartment',
    label: 'Department Select User',
    helpMessage: ['componentmodel'],
    defaultValue: '',
    componentProps: {
      labelKey: 'realname',
      rowKey: 'username',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'user4',
    component: 'JEllipsis',
    label: 'Select user',
    colProps: { span: 12 },
  },
  {
    field: 'role2',
    component: 'JSelectRole',
    label: 'Character selection component',
    helpMessage: ['componentmodel'],
    colProps: {
      span: 12,
    },
  },
  {
    field: 'role2',
    component: 'JEllipsis',
    label: 'Select character',
    colProps: { span: 12 },
  },
  {
    field: 'position2',
    component: 'JSelectPosition',
    label: 'job selection component',
    helpMessage: ['componentmodel'],
    colProps: { span: 12 },
    componentProps: { async: true, showSelectTable: true },
  },
  {
    field: 'position2',
    component: 'JEllipsis',
    label: 'Select position',
    colProps: { span: 12 },
  },
  {
    field: 'checkbox1',
    component: 'JCheckbox',
    label: 'JCheckboxcomponents1',
    helpMessage: ['componentmodel'],
    defaultValue: '1,2',
    componentProps: {
      options: [
        { label: 'male', value: '1' },
        { label: 'female', value: '2' },
      ],
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'checkbox1',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'checkbox2',
    component: 'Input',
    label: 'JCheckboxcomponents2',
    defaultValue: '1',
    helpMessage: ['插槽model'],
    slot: 'JCheckbox',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'checkbox2',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'data1',
    label: 'date selection',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'data1',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'data2',
    label: 'Year range selection',
    component: 'RangePicker',
    componentProps: {
      picker: 'year',
      valueFormat: 'YYYY',
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'data2',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'hk',
    component: 'Input',
    label: 'Slider verification code',
    helpMessage: ['插槽model'],
    slot: 'dargVerify',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'hk',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'JTreeDict',
    component: 'JTreeDict',
    label: 'tree dictionary',
    helpMessage: ['componentmodel'],
    colProps: { span: 12 },
  },
  {
    field: 'JTreeDict',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'ts',
    component: 'JTreeSelect',
    label: 'Drop down tree selection',
    helpMessage: ['componentmodel'],
    componentProps: {
      dict: 'sys_permission,name,id',
      pidField: 'parent_id',
      hasChildField: 'is_leaf',
      converIsLeafVal: 0,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'ts',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'ts1',
    component: 'JTreeSelect',
    label: 'Drop-down tree multiple selection',
    helpMessage: ['componentmodel'],
    componentProps: {
      dict: 'sys_permission,name,id',
      pidField: 'parent_id',
      hasChildField: 'is_leaf',
      converIsLeafVal: 0,
      multiple: true,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'ts1',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'category',
    component: 'JCategorySelect',
    label: 'Classification Dictionary Tree',
    helpMessage: ['componentmodel'],
    defaultValue: '',
    componentProps: {
      pcode: 'B01',
      multiple: true,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'category',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'JEasyCron',
    component: 'JEasyCron',
    label: 'JEasyCron',
    helpMessage: ['componentmodel'],
    colProps: { span: 12 },
    defaultValue: '* * * * * ? *',
    rules: [{ validator: JCronValidator }],
  },
  {
    field: 'JEasyCron',
    component: 'JEllipsis',
    label: 'Select value',
    colProps: { span: 12 },
  },
  {
    field: 'JInput',
    component: 'JInput',
    label: '特殊查询components',
    helpMessage: ['插槽model'],
    slot: 'JInput',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'jinputtype',
    component: 'Select',
    label: 'Query type',
    componentProps: {
      options: [
        { value: 'like', label: 'Vague（like）' },
        { value: 'ne', label: 'not equal to（ne）' },
        { value: 'ge', label: 'Greater than or equal to（ge）' },
        { value: 'le', label: 'less than or equal to（le)' },
      ],
    },
    colProps: {
      span: 6,
    },
  },
  {
    field: 'JInput',
    component: 'JEllipsis',
    label: 'Enter value',
    colProps: { span: 6 },
  },
  {
    field: 'field1',
    component: 'Select',
    label: 'Province and city selection',
    helpMessage: ['插槽model'],
    slot: 'jAreaLinkage',
    colProps: {
      span: 12,
    },
    defaultValue: ['130000', '130200'],
  },
  {
    field: 'field1',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'field0',
    component: 'Select',
    label: '禁用components(Method 1)',
    helpMessage: ['插槽model'],
    slot: 'jAreaLinkage1',
    colProps: {
      span: 12,
    },
    defaultValue: ['130000', '130200'],
  },

  {
    field: 'field0',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'field2',
    component: 'JAreaLinkage',
    label: '禁用components(Method 2)',
    helpMessage: ['componentmodel'],
    colProps: {
      span: 12,
    },
    dynamicDisabled: ({ values }) => {
      console.log(values);
      return isDisabledAuth(['demo.dbarray']);
    },
    defaultValue: ['140000', '140300', '140302'],
  },
  {
    field: 'field2',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pca1',
    component: 'JAreaSelect',
    label: 'Province and city cascade',
    helpMessage: ['componentmodel'],
    defaultValue: '140302',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pca1',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop1',
    component: 'Input',
    label: 'JPopupExample',
    helpMessage: ['插槽model'],
    slot: 'JPopup',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop1',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop2',
    component: 'Input',
    label: 'JPopup带参数Example',
    helpMessage: ['插槽model'],
    slot: 'JPopup2',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop2',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop3',
    component: 'Input',
    label: 'JPopup带查询条件参数Example',
    helpMessage: ['插槽model'],
    slot: 'JPopup3',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'pop3',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'JInputPop',
    component: 'JInputPop',
    label: 'JInputPop',
    helpMessage: ['componentmodel'],
    colProps: { span: 12 },
  },
  {
    field: 'JInputPop',
    component: 'JEllipsis',
    label: 'Enter value',
    colProps: { span: 12 },
  },
  {
    field: 'JTreeDictAsync',
    component: 'JTreeDict',
    label: 'asynchronousJTreeDict',
    helpMessage: ['componentmodel'],
    colProps: { span: 12 },
    componentProps: { async: true },
  },
  {
    field: 'JTreeDictAsync',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'JSwitch',
    component: 'JSwitch',
    label: 'JSwitch',
    helpMessage: ['componentmodel'],
    colProps: { span: 12 },
  },
  {
    field: 'JSwitch',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'JSwitchSelect',
    component: 'JSwitch',
    label: 'JSwitchSelect',
    helpMessage: ['componentmodel'],
    colProps: { span: 12 },
    componentProps: { query: true },
  },
  {
    field: 'JSwitchSelect',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  
  {
    field: 'userSelect2',
    component: 'UserSelect',
    label: 'Advanced user options',
    helpMessage: ['componentmodel'],
    colProps: { span: 12 },
  },
  {
    field: 'userSelect2',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  
  {
    field: 'superQuery',
    component: 'Input',
    label: 'Advanced query',
    helpMessage: ['插槽model'],
    slot: 'superQuery',
    colProps: { span: 12 },
  },
  {
    field: 'superQuery',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'superQuery1',
    component: 'Input',
    label: 'Advanced query',
    helpMessage: ['插槽model-Save query conditions yourself'],
    slot: 'superQuery1',
    colProps: { span: 12 },
  },
  {
    field: 'superQuery1',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'pop2',
    component: 'JPopupDict',
    label: 'JPopupDictExample',
    colProps: {
      span: 12,
    },
    componentProps:{
      placeholder: 'Please select',
      dictCode: 'report_user,username,id',
      multi: true,
    },
  },
  {
    field: 'pop2',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'sex',
    component: 'JDictSelectTag',
    label: 'gender(Control the course belowoptions)',
    helpMessage: ['componentmodel','gender不同，The course display options below are different'],
    componentProps: {
      dictCode: 'sex',
      type: 'radioButton',
      onChange: (value) => {
        console.log(value);
      },
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'sex',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'course',
    component: 'Select',
    label: 'course',
    dynamicPropskey: 'options',
    dynamicPropsVal: ({ model }) => {
      let options;
      if (model.sex == 1) {
        return [
          { value: '0', label: 'java - male' },
          { value: '1', label: 'vue - male' },
        ];
      } else if (model.sex == 2) {
        return [
          { value: '2', label: 'Yoga - female' },
          { value: '3', label: 'Manicure - female' },
        ];
      } else {
        return [];
      }
    },
    componentProps: {
      disabled: false,
    },
    colProps: {
      span: 12,
    },
  },
  {
    field: 'course',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'field100',
    component: 'JInputSelect',
    label: 'JInputSelect',
    helpMessage: ['componentmodel'],
    componentProps: {
      selectPlaceholder: 'Optional system variables',
      inputPlaceholder: 'Please enter',
      selectWidth:'200px',
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
    colProps: {
      span: 12,
    },
  },
  {
    field: 'field100',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  {
    field: 'JAreaLinkage',
    component: 'JAreaLinkage',
    label: 'Province and city selection',
    colProps: {
      span: 12,
    },
  },
  {
    field: 'JAreaLinkage',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },

  {
    field: 'orderAuth',
    component: 'Input',
    label: 'Command authority',
    helpMessage: ['The one with authority on the right"selected value"visible，否则不visible'],
    colProps: {
      span: 12,
    },
  },
  {
    field: 'orderAuth',
    auth: 'demo:order:auth',
    component: 'JEllipsis',
    label: 'selected value',
    colProps: { span: 12 },
  },
  
];
