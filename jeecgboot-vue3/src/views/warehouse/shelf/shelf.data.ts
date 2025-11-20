import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Form';
import { warehouseAreaApi } from '../area/area.api';

// 仓库货架表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'shelfCode',
    label: 'Shelf Code',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'shelfName',
    label: 'Shelf Name',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'areaId',
    label: 'Warehouse Area',
    component: 'Select',
    componentProps: {
      placeholder: 'Please select warehouse area',
      options: [], // Need to get area list from API
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

// 仓库货架表格列配置
export const columns: BasicColumn[] = [
  {
    title: 'Shelf Code',
    dataIndex: 'shelfCode',
    key: 'shelfCode',
    width: 120,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Shelf Name',
    dataIndex: 'shelfName',
    key: 'shelfName',
    width: 180,
    align: 'center',
  } as BasicColumn,
  {
    title: 'Warehouse Area',
    dataIndex: 'areaName',
    key: 'areaName',
    width: 150,
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

// 表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'shelfCode',
    label: 'Shelf Code',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: 'Please enter shelf code' }],
  },
  {
    field: 'shelfName',
    label: 'Shelf Name',
    component: 'Input',
    required: true,
    rules: [{ required: true, message: 'Please enter shelf name' }],
  },
  {
    field: 'areaId',
    label: 'Warehouse Area',
    component: 'Select',
    required: true,
    componentProps: {
      placeholder: 'Please select warehouse area',
      options: [], // Need to get area list from API
    },
    rules: [{ required: true, message: 'Please select warehouse area' }],
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

// 获取仓库区域选项
export async function getWarehouseAreaOptions() {
  try {
    const result = await warehouseAreaApi.list({ pageSize: 999 });
    return result.records.map((item) => ({
      label: item.areaName,
      value: item.id,
    }));
  } catch (error) {
    console.error('获取仓库区域列表失败:', error);
    return [];
  }
}