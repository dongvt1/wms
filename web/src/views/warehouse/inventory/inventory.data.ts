import { BasicColumn, FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// Inventory status enum
export enum InventoryStatus {
  NORMAL = 'normal',
  LOW_STOCK = 'low_stock',
  OUT_OF_STOCK = 'out_of_stock',
}

// Inventory status color mapping
export const inventoryStatusColorMap = {
  [InventoryStatus.NORMAL]: 'green',
  [InventoryStatus.LOW_STOCK]: 'orange',
  [InventoryStatus.OUT_OF_STOCK]: 'red',
};

// Inventory status text mapping
export const inventoryStatusTextMap = {
  [InventoryStatus.NORMAL]: 'normal',
  [InventoryStatus.LOW_STOCK]: 'low inventory',
  [InventoryStatus.OUT_OF_STOCK]: 'out of stock',
};

// Inventory table column configuration
export const columns: BasicColumn[] = [
  {
    title: 'productID',
    dataIndex: 'productId',
    key: 'productId',
    width: 120,
    fixed: 'left',
  },
  {
    title: 'product编码',
    dataIndex: 'productCode',
    key: 'productCode',
    width: 120,
  },
  {
    title: 'product名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 180,
  },
  {
    title: 'total inventory',
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
    title: 'reserve inventory',
    dataIndex: 'reservedQuantity',
    key: 'reservedQuantity',
    width: 100,
    sorter: true,
  },
  {
    title: 'Available stock',
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
    title: 'Minimum inventory threshold',
    dataIndex: 'minStockThreshold',
    key: 'minStockThreshold',
    width: 120,
    sorter: true,
  },
  {
    title: 'Last updated',
    dataIndex: 'lastUpdated',
    key: 'lastUpdated',
    width: 150,
    sorter: true,
  },
  {
    title: 'Updater',
    dataIndex: 'updatedBy',
    key: 'updatedBy',
    width: 100,
  },
];

// Inventory adjustment form configuration
export const adjustmentFormSchema: FormSchema[] = [
  {
    field: 'productId',
    label: 'product',
    component: 'Select',
    required: true,
    componentProps: {
      options: [],
      placeholder: '请选择product',
      showSearch: true,
      filterOption: (input: string, option: any) => {
        return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
      },
    },
  },
  {
    field: 'currentQuantity',
    label: 'Current inventory',
    component: 'InputNumber',
    required: true,
    componentProps: {
      disabled: true,
      placeholder: 'Current inventoryquantity',
      style: { width: '100%' },
    },
  },
  {
    field: 'newQuantity',
    label: 'new stock',
    component: 'InputNumber',
    required: true,
    rules: [
      { required: true, message: '请输入new stockquantity' },
      { type: 'number', min: 0, message: 'The inventory quantity cannot be less than0' },
    ],
    componentProps: {
      placeholder: 'new stockquantity',
      style: { width: '100%' },
    },
  },
  {
    field: 'minStockThreshold',
    label: 'Minimum inventory threshold',
    component: 'InputNumber',
    required: true,
    rules: [
      { required: true, message: '请输入Minimum inventory threshold' },
      { type: 'number', min: 0, message: 'Minimum inventory threshold不能小于0' },
    ],
    componentProps: {
      placeholder: 'Minimum inventory threshold',
      style: { width: '100%' },
    },
  },
  {
    field: 'reason',
    label: 'Reason for adjustment',
    component: 'InputTextArea',
    required: true,
    rules: [{ required: true, message: '请输入Reason for adjustment' }],
    componentProps: {
      placeholder: '请输入Reason for adjustment',
      rows: 3,
      style: { width: '100%' },
    },
  },
];

// Inventory search form configuration
export const searchFormSchema: FormSchema[] = [
  {
    field: 'productCode',
    label: 'product编码',
    component: 'Input',
    componentProps: {
      placeholder: '请输入product编码',
      style: { width: '100%' },
    },
  },
  {
    field: 'productName',
    label: 'product名称',
    component: 'Input',
    componentProps: {
      placeholder: '请输入product名称',
      style: { width: '100%' },
    },
  },
  {
    field: 'status',
    label: 'Stock status',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'all', value: '' },
        { label: 'normal', value: 'normal' },
        { label: 'low inventory', value: 'low_stock' },
        { label: 'out of stock', value: 'out_of_stock' },
      ],
      placeholder: '请选择Stock status',
      style: { width: '100%' },
    },
  },
];

// Inventory transaction record table column configuration
export const transactionColumns: BasicColumn[] = [
  {
    title: 'tradeID',
    dataIndex: 'id',
    key: 'id',
    width: 120,
  },
  {
    title: 'product编码',
    dataIndex: 'productCode',
    key: 'productCode',
    width: 120,
  },
  {
    title: 'product名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 180,
  },
  {
    title: 'trade类型',
    dataIndex: 'transactionType',
    key: 'transactionType',
    width: 100,
    customRender: ({ text }) => {
      const typeMap = {
        'IN': { color: 'green', text: 'Warehouse' },
        'OUT': { color: 'red', text: 'out of warehouse' },
        'ADJUST': { color: 'blue', text: 'Adjustment' },
      };
      const config = typeMap[text] || { color: 'default', text: text };
      return h(Tag, { color: config.color }, config.text);
    },
  },
  {
    title: 'quantity',
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
    title: 'refer toID',
    dataIndex: 'referenceId',
    key: 'referenceId',
    width: 120,
  },
  {
    title: 'reason',
    dataIndex: 'reason',
    key: 'reason',
    width: 200,
  },
  {
    title: 'operator',
    dataIndex: 'userName',
    key: 'userName',
    width: 100,
  },
  {
    title: 'trade时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150,
  },
];

// 库存Adjustment记录表格列配置
export const adjustmentColumns: BasicColumn[] = [
  {
    title: 'AdjustmentID',
    dataIndex: 'id',
    key: 'id',
    width: 120,
  },
  {
    title: 'product编码',
    dataIndex: 'productCode',
    key: 'productCode',
    width: 120,
  },
  {
    title: 'product名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 180,
  },
  {
    title: 'Adjustment前quantity',
    dataIndex: 'oldQuantity',
    key: 'oldQuantity',
    width: 120,
  },
  {
    title: 'Adjustment后quantity',
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
    title: 'Reason for adjustment',
    dataIndex: 'adjustmentReason',
    key: 'adjustmentReason',
    width: 200,
  },
  {
    title: 'operator',
    dataIndex: 'userName',
    key: 'userName',
    width: 100,
  },
  {
    title: 'Adjustment时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150,
  },
];

// Inventory warning table column configuration
export const alertColumns: BasicColumn[] = [
  {
    title: 'early warningID',
    dataIndex: 'id',
    key: 'id',
    width: 120,
  },
  {
    title: 'product编码',
    dataIndex: 'productCode',
    key: 'productCode',
    width: 120,
  },
  {
    title: 'product名称',
    dataIndex: 'productName',
    key: 'productName',
    width: 180,
  },
  {
    title: 'early warning类型',
    dataIndex: 'alertType',
    key: 'alertType',
    width: 100,
    customRender: ({ text }) => {
      const typeMap = {
        'LOW_STOCK': { color: 'orange', text: 'low inventory' },
        'OUT_OF_STOCK': { color: 'red', text: 'out of stock' },
      };
      const config = typeMap[text] || { color: 'default', text: text };
      return h(Tag, { color: config.color }, config.text);
    },
  },
  {
    title: '当前quantity',
    dataIndex: 'currentQuantity',
    key: 'currentQuantity',
    width: 100,
  },
  {
    title: 'threshold',
    dataIndex: 'thresholdValue',
    key: 'thresholdValue',
    width: 100,
  },
  {
    title: 'early warning状态',
    dataIndex: 'alertStatus',
    key: 'alertStatus',
    width: 100,
    customRender: ({ text }) => {
      const statusMap = {
        'ACTIVE': { color: 'red', text: 'active' },
        'RESOLVED': { color: 'green', text: 'Resolved' },
        'DISMISSED': { color: 'gray', text: 'Ignored' },
      };
      const config = statusMap[text] || { color: 'default', text: text };
      return h(Tag, { color: config.color }, config.text);
    },
  },
  {
    title: 'creation time',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150,
  },
  {
    title: 'Resolution time',
    dataIndex: 'resolvedAt',
    key: 'resolvedAt',
    width: 150,
  },
  {
    title: 'solve people',
    dataIndex: 'resolvedBy',
    key: 'resolvedBy',
    width: 100,
  },
];