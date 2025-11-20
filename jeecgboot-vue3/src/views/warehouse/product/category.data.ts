import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Form';

// 产品分类表单配置
export const categorySearchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Category Name',
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

// 产品分类表格列配置
export const categoryColumns: BasicColumn[] = [
  {
    title: 'Category Name',
    dataIndex: 'name',
    key: 'name',
    width: 180,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Parent Category',
    dataIndex: 'parentName',
    key: 'parentName',
    width: 150,
    align: 'center',
    customRender: ({ text }) => {
      return text || 'Root Category';
    },
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
    title: 'Create Time',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
    align: 'center',
  } as BasicColumn,
];

// 产品分类表单配置
export const categoryFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Category Name',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: 'Please enter category name' }],
  },
  {
    field: 'parentId',
    label: 'Parent Category',
    component: 'TreeSelect',
    componentProps: {
      placeholder: 'Please select parent category',
      treeData: [], // Need to get category tree from API
    },
  },
  {
    field: 'description',
    label: 'Description',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
    },
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
];