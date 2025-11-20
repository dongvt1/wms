import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Form';

// 仓库区域表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'areaCode',
    label: 'Area Code',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'areaName',
    label: 'Area Name',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: 'Status',
    component: 'Select',
    componentProps: {
      placeholder: 'Please select status',
      options: [
        { label: 'Inactive', value: 0 },
        { label: 'Active', value: 1 },
      ],
    },
    colProps: { span: 8 },
  },
];

// 表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'areaCode',
    label: 'Area Code',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: 'Please enter area code' }],
  },
  {
    field: 'areaName',
    label: 'Area Name',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: 'Please enter area name' }],
  },
  {
    field: 'status',
    label: 'Status',
    component: 'Select',
    defaultValue: 1,
    componentProps: {
      options: [
        { label: 'Inactive', value: 0 },
        { label: 'Active', value: 1 },
      ],
    },
  },
  {
    field: 'capacity',
    label: 'Capacity',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 0,
    },
    rules: [{ required: true, message: 'Please enter capacity' }],
  },
  {
    field: 'description',
    label: 'Description',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
    },
  },
];

// 仓库区域表格列配置
export const columns: BasicColumn[] = [
  {
    title: 'Area Code',
    dataIndex: 'areaCode',
    key: 'areaCode',
    width: 120,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Area Name',
    dataIndex: 'areaName',
    key: 'areaName',
    width: 180,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Description',
    dataIndex: 'description',
    key: 'description',
    width: 200,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      const status = Number(text);
      if (status === 0) {
        return 'Inactive';
      } else if (status === 1) {
        return 'Active';
      }
      return text;
    },
  } as BasicColumn,
  {
    title: 'Capacity',
    dataIndex: 'capacity',
    key: 'capacity',
    width: 100,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Used Capacity',
    dataIndex: 'usedCapacity',
    key: 'usedCapacity',
    width: 120,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Create Time',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
    align: 'center',
  } as BasicColumn,
];