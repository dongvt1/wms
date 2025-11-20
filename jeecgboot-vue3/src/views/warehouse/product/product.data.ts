import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Form';
import { categoryApi } from './category.api';

// 产品表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'code',
    label: 'Product Code',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'name',
    label: 'Product Name',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'categoryId',
    label: 'Category',
    component: 'Select',
    componentProps: {
      placeholder: 'Please select category',
      options: [], // Need to get category list from API
    },
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

// 产品表格列配置
export const columns: BasicColumn[] = [
  {
    title: 'Product Code',
    dataIndex: 'code',
    key: 'code',
    width: 120,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Product Name',
    dataIndex: 'name',
    key: 'name',
    width: 180,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Category',
    dataIndex: 'categoryName',
    key: 'categoryName',
    width: 120,
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
    title: 'Price',
    dataIndex: 'price',
    key: 'price',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      return `$${Number(text).toFixed(2)}`;
    },
  } as BasicColumn,
  {
    title: 'Min Stock Level',
    dataIndex: 'minStockLevel',
    key: 'minStockLevel',
    width: 120,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Current Stock',
    dataIndex: 'currentStock',
    key: 'currentStock',
    width: 120,
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
    title: 'Image',
    dataIndex: 'image',
    key: 'image',
    width: 100,
    align: 'center',
    customRender: ({ text }) => {
      if (text) {
        return `<img src="${text}" alt="Product" style="width: 50px; height: 50px; object-fit: cover;" />`;
      }
      return 'No Image';
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

// 产品表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'code',
    label: 'Product Code',
    component: 'Input',
    required: true,
    rules: [
      { required: true, message: 'Please enter product code' },
      { pattern: /^[A-Z0-9-_]+$/, message: 'Product code can only contain uppercase letters, numbers, hyphens and underscores' }
    ],
  },
  {
    field: 'name',
    label: 'Product Name',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: 'Please enter product name' }],
  },
  {
    field: 'categoryId',
    label: 'Category',
    component: 'Select',
    required: true,
    componentProps: {
      placeholder: 'Please select category',
      options: [], // Need to get category list from API
    },
    rules: [{ required: true, message: 'Please select category' }],
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
    field: 'price',
    label: 'Price',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      formatter: (value) => `$ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ','),
      parser: (value) => value!.replace(/\$\s?|(,*)/g, ''),
    },
    rules: [
      { required: true, message: 'Please enter price' },
      { type: 'number', min: 0, message: 'Price must be a positive number' }
    ],
  },
  {
    field: 'minStockLevel',
    label: 'Min Stock Level',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 0,
    },
    rules: [
      { required: true, message: 'Please enter minimum stock level' },
      { type: 'number', min: 0, message: 'Minimum stock level must be a non-negative number' }
    ],
  },
  {
    field: 'image',
    label: 'Product Image',
    component: 'JImageUpload',
    componentProps: {
      accept: '.jpg,.jpeg,.png,.gif',
      maxSize: 5, // 5MB
      maxNumber: 1,
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

// 获取产品分类选项
export async function getCategoryOptions() {
  try {
    const result = await categoryApi.list({ pageSize: 999 });
    return result.records.map((item) => ({
      label: item.name,
      value: item.id,
    }));
  } catch (error) {
    console.error('Failed to fetch category list:', error);
    return [];
  }
}

// 产品历史记录表格列配置
export const historyColumns: BasicColumn[] = [
  {
    title: 'Action',
    dataIndex: 'action',
    key: 'action',
    width: 120,
    align: 'center',
  } as BasicColumn,
  {
    title: 'User',
    dataIndex: 'userName',
    key: 'userName',
    width: 120,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Old Data',
    dataIndex: 'oldData',
    key: 'oldData',
    width: 200,
    align: 'center',
    customRender: ({ text }) => {
      if (text) {
        try {
          return JSON.stringify(JSON.parse(text), null, 2);
        } catch (e) {
          return text;
        }
      }
      return '-';
    },
  } as BasicColumn,
  {
    title: 'New Data',
    dataIndex: 'newData',
    key: 'newData',
    width: 200,
    align: 'center',
    customRender: ({ text }) => {
      if (text) {
        try {
          return JSON.stringify(JSON.parse(text), null, 2);
        } catch (e) {
          return text;
        }
      }
      return '-';
    },
  } as BasicColumn,
  {
    title: 'Time',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
    align: 'center',
  } as BasicColumn,
];