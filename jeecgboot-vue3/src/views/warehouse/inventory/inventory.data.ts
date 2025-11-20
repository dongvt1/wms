import { BasicColumn, FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// 库存状态枚举
export enum InventoryStatus {
  NORMAL = 'normal',
  LOW_STOCK = 'low_stock',
  OUT_OF_STOCK = 'out_of_stock',
}

// 库存状态颜色映射
export const inventoryStatusColorMap = {
  [InventoryStatus.NORMAL]: 'green',
  [InventoryStatus.LOW_STOCK]: 'orange',
  [InventoryStatus.OUT_OF_STOCK]: 'red',
};

// 库存状态文本映射
export const inventoryStatusTextMap = {
  [InventoryStatus.NORMAL]: '正常',
  [InventoryStatus.LOW_STOCK]: '低库存',
  [InventoryStatus.OUT_OF_STOCK]: '缺货',
};

// 库存表格列配置
export const columns: BasicColumn[] = [
  {
    title: '产品ID',
    dataIndex: 'productId',
    key: 'productId',
    width: 120,
    fixed: 'left',
  },
  {
    title: '产品编码',
    dataIndex: 'productCode',
    key: 'productCode',
    width: 120,
  },
  {
    title: '产品名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 180,
  },
  {
    title: '总库存',
    dataIndex: 'quantity',
    key: 'quantity',
    width: 100,
    sorter: true,
    customRender: ({ record }) => {
      const quantity = record.quantity || 0;
      const minStockThreshold = record.minStockThreshold || 0;
      
      if (quantity === 0) {
        return h(Tag, { color: 'red' }, quantity);
      } else if (quantity <= minStockThreshold) {
        return h(Tag, { color: 'orange' }, quantity);
      } else {
        return h(Tag, { color: 'green' }, quantity);
      }
    },
  },
  {
    title: '预留库存',
    dataIndex: 'reservedQuantity',
    key: 'reservedQuantity',
    width: 100,
    sorter: true,
  },
  {
    title: '可用库存',
    dataIndex: 'availableQuantity',
    key: 'availableQuantity',
    width: 100,
    sorter: true,
    customRender: ({ record }) => {
      const quantity = record.availableQuantity || 0;
      const minStockThreshold = record.minStockThreshold || 0;
      
      if (quantity === 0) {
        return h(Tag, { color: 'red' }, quantity);
      } else if (quantity <= minStockThreshold) {
        return h(Tag, { color: 'orange' }, quantity);
      } else {
        return h(Tag, { color: 'green' }, quantity);
      }
    },
  },
  {
    title: '最小库存阈值',
    dataIndex: 'minStockThreshold',
    key: 'minStockThreshold',
    width: 120,
    sorter: true,
  },
  {
    title: '最后更新时间',
    dataIndex: 'lastUpdated',
    key: 'lastUpdated',
    width: 150,
    sorter: true,
  },
  {
    title: '更新人',
    dataIndex: 'updatedBy',
    key: 'updatedBy',
    width: 100,
  },
];

// 库存调整表单配置
export const adjustmentFormSchema: FormSchema[] = [
  {
    field: 'productId',
    label: '产品',
    component: 'Select',
    required: true,
    componentProps: {
      options: [],
      placeholder: '请选择产品',
      showSearch: true,
      filterOption: (input: string, option: any) => {
        return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
    },
  },
  {
    field: 'currentQuantity',
    label: '当前库存',
    component: 'InputNumber',
    required: true,
    componentProps: {
      disabled: true,
      placeholder: '当前库存数量',
      style: { width: '100%' },
    },
  },
  {
    field: 'newQuantity',
    label: '新库存',
    component: 'InputNumber',
    required: true,
    rules: [
      { required: true, message: '请输入新库存数量' },
      { type: 'number', min: 0, message: '库存数量不能小于0' },
    ],
    componentProps: {
      placeholder: '新库存数量',
      style: { width: '100%' },
    },
  },
  {
    field: 'minStockThreshold',
    label: '最小库存阈值',
    component: 'InputNumber',
    required: true,
    rules: [
      { required: true, message: '请输入最小库存阈值' },
      { type: 'number', min: 0, message: '最小库存阈值不能小于0' },
    ],
    componentProps: {
      placeholder: '最小库存阈值',
      style: { width: '100%' },
    },
  },
  {
    field: 'reason',
    label: '调整原因',
    component: 'InputTextArea',
    required: true,
    rules: [{ required: true, message: '请输入调整原因' }],
    componentProps: {
      placeholder: '请输入调整原因',
      rows: 3,
      style: { width: '100%' },
    },
  },
];

// 库存搜索表单配置
export const searchFormSchema: FormSchema[] = [
  {
    field: 'productCode',
    label: '产品编码',
    component: 'Input',
    componentProps: {
      placeholder: '请输入产品编码',
      style: { width: '100%' },
    },
  },
  {
    field: 'productName',
    label: '产品名称',
    component: 'Input',
    componentProps: {
      placeholder: '请输入产品名称',
      style: { width: '100%' },
    },
  },
  {
    field: 'status',
    label: '库存状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '全部', value: '' },
        { label: '正常', value: 'normal' },
        { label: '低库存', value: 'low_stock' },
        { label: '缺货', value: 'out_of_stock' },
      ],
      placeholder: '请选择库存状态',
      style: { width: '100%' },
    },
  },
];

// 库存交易记录表格列配置
export const transactionColumns: BasicColumn[] = [
  {
    title: '交易ID',
    dataIndex: 'id',
    key: 'id',
    width: 120,
  },
  {
    title: '产品编码',
    dataIndex: 'productCode',
    key: 'productCode',
    width: 120,
  },
  {
    title: '产品名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 180,
  },
  {
    title: '交易类型',
    dataIndex: 'transactionType',
    key: 'transactionType',
    width: 100,
    customRender: ({ text }) => {
      const typeMap = {
        'IN': { color: 'green', text: '入库' },
        'OUT': { color: 'red', text: '出库' },
        'ADJUST': { color: 'blue', text: '调整' },
      };
      const config = typeMap[text] || { color: 'default', text: text };
      return h(Tag, { color: config.color }, config.text);
    },
  },
  {
    title: '数量',
    dataIndex: 'quantity',
    key: 'quantity',
    width: 100,
    customRender: ({ record }) => {
      const { transactionType, quantity } = record;
      if (transactionType === 'OUT') {
        return `-${quantity}`;
      }
      return `+${quantity}`;
    },
  },
  {
    title: '参考ID',
    dataIndex: 'referenceId',
    key: 'referenceId',
    width: 120,
  },
  {
    title: '原因',
    dataIndex: 'reason',
    key: 'reason',
    width: 200,
  },
  {
    title: '操作人',
    dataIndex: 'userName',
    key: 'userName',
    width: 100,
  },
  {
    title: '交易时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150,
  },
];

// 库存调整记录表格列配置
export const adjustmentColumns: BasicColumn[] = [
  {
    title: '调整ID',
    dataIndex: 'id',
    key: 'id',
    width: 120,
  },
  {
    title: '产品编码',
    dataIndex: 'productCode',
    key: 'productCode',
    width: 120,
  },
  {
    title: '产品名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 180,
  },
  {
    title: '调整前数量',
    dataIndex: 'oldQuantity',
    key: 'oldQuantity',
    width: 120,
  },
  {
    title: '调整后数量',
    dataIndex: 'newQuantity',
    key: 'newQuantity',
    width: 120,
    customRender: ({ record }) => {
      const { oldQuantity, newQuantity } = record;
      const diff = newQuantity - oldQuantity;
      if (diff > 0) {
        return h('span', { style: { color: 'green' } }, `${newQuantity} (+${diff})`);
      } else if (diff < 0) {
        return h('span', { style: { color: 'red' } }, `${newQuantity} (${diff})`);
      }
      return newQuantity;
    },
  },
  {
    title: '调整原因',
    dataIndex: 'adjustmentReason',
    key: 'adjustmentReason',
    width: 200,
  },
  {
    title: '操作人',
    dataIndex: 'userName',
    key: 'userName',
    width: 100,
  },
  {
    title: '调整时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150,
  },
];

// 库存预警表格列配置
export const alertColumns: BasicColumn[] = [
  {
    title: '预警ID',
    dataIndex: 'id',
    key: 'id',
    width: 120,
  },
  {
    title: '产品编码',
    dataIndex: 'productCode',
    key: 'productCode',
    width: 120,
  },
  {
    title: '产品名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 180,
  },
  {
    title: '预警类型',
    dataIndex: 'alertType',
    key: 'alertType',
    width: 100,
    customRender: ({ text }) => {
      const typeMap = {
        'LOW_STOCK': { color: 'orange', text: '低库存' },
        'OUT_OF_STOCK': { color: 'red', text: '缺货' },
      };
      const config = typeMap[text] || { color: 'default', text: text };
      return h(Tag, { color: config.color }, config.text);
    },
  },
  {
    title: '当前数量',
    dataIndex: 'currentQuantity',
    key: 'currentQuantity',
    width: 100,
  },
  {
    title: '阈值',
    dataIndex: 'thresholdValue',
    key: 'thresholdValue',
    width: 100,
  },
  {
    title: '预警状态',
    dataIndex: 'alertStatus',
    key: 'alertStatus',
    width: 100,
    customRender: ({ text }) => {
      const statusMap = {
        'ACTIVE': { color: 'red', text: '活跃' },
        'RESOLVED': { color: 'green', text: '已解决' },
        'DISMISSED': { color: 'gray', text: '已忽略' },
      };
      const config = statusMap[text] || { color: 'default', text: text };
      return h(Tag, { color: config.color }, config.text);
    },
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150,
  },
  {
    title: '解决时间',
    dataIndex: 'resolvedAt',
    key: 'resolvedAt',
    width: 150,
  },
  {
    title: '解决人',
    dataIndex: 'resolvedBy',
    key: 'resolvedBy',
    width: 100,
  },
];