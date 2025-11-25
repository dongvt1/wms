import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';

export const columns: BasicColumn[] = [
  {
    title: 'Name',
    dataIndex: 'name',
    width: 170,
    align: 'left',
    resizable: true,
    sorter: {
      multiple:1
    }
  },
  {
    title: 'keywords',
    dataIndex: 'keyWord',
    width: 130,
    resizable: true,
  },
  {
    title: 'Check in time',
    dataIndex: 'punchTime',
    width: 140,
    resizable: true,
  },
  {
    title: 'salary',
    dataIndex: 'salaryMoney',
    width: 140,
    resizable: true,
    sorter: {
      multiple: 2
    }
  },
  {
    title: 'bonus',
    dataIndex: 'bonusMoney',
    width: 140,
    resizable: true,
  },
  {
    title: 'gender',
    dataIndex: 'sex',
    sorter: {
      multiple: 3
    },
    customRender: ({ record }) => {
      return render.renderDict(record.sex, 'sex');
      // let v = record.sex ? (record.sex == '1' ? 'male' : 'female') : '';
      // return h('span', v);
    },
    width: 120,
    resizable: true,
  },
  {
    title: 'Birthday',
    dataIndex: 'birthday',
    width: 120,
    resizable: true,
  },
  {
    title: 'Mail',
    dataIndex: 'email',
    width: 120,
    resizable: true,
  },
  {
    title: 'Profile',
    dataIndex: 'content',
    width: 120,
    resizable: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Name',
    component: 'Input',
    componentProps: {
      trim: true,
    },
    colProps: { span: 8 },
  },
  {
    field: 'birthday',
    label: 'Birthday',
    component: 'RangePicker',
    componentProps: {
      valueType: 'Date'
    },
    colProps: { span: 8 },
  },
  {
    field: 'age',
    label: 'age',
    component: 'Input',
    slot: 'age',
    colProps: { span: 8 },
  },
  {
    field: 'sex',
    label: 'gender',
    colProps: { span: 8 },
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: '请选择gender',
    },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'createBy',
    label: 'createBy',
    component: 'Input',
    show: false,
  },
  {
    field: 'createTime',
    label: 'createTime',
    component: 'Input',
    show: false,
  },
  {
    field: 'name',
    label: 'name',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: '请输入name',
    },
  },
  {
    field: 'keyWord',
    label: 'keywords',
    component: 'Input',
    componentProps: {
      placeholder: '请输入keywords',
    },
  },
  {
    field: 'punchTime',
    label: 'Check in time',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: '请选择Check in time',
    },
  },
  {
    field: 'salaryMoney',
    label: 'salary',
    component: 'Input',
    componentProps: {
      placeholder: '请输入salary',
    },
  },
  {
    field: 'sex',
    label: 'gender',
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      type: 'radio',
      dictCode: 'sex',
      placeholder: '请选择gender',
    },
  },
  {
    field: 'age',
    label: 'age',
    component: 'InputNumber',
    defaultValue: 1,
    componentProps: {
      placeholder: '请输入age',
    },
  },
  {
    field: 'birthday',
    label: 'Birthday',
    component: 'DatePicker',
    defaultValue: '',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      placeholder: '请选择Birthday',
    },
  },
  {
    field: 'email',
    label: 'Mail',
    component: 'Input',
    rules: [{ required: false, type: 'email', message: 'Mail格式不正确', trigger: 'blur' }],
    componentProps: {
      placeholder: '请输入Mail',
    },
  },
  {
    field: 'content',
    label: 'Profile - To introduce myself',
    component: 'InputTextArea',
    labelLength: 4,
    componentProps: {
      placeholder: '请输入Profile',
    },
  },
  {
    field: 'updateCount',
    label: 'optimistic locking',
    show: false,
    component: 'Input',
  },
];
