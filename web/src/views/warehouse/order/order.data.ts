import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

// Order status options
export const orderStatusOptions = [
  { label: 'Pending', value: 'PENDING', color: 'orange' },
  { label: 'Confirmed', value: 'CONFIRMED', color: 'blue' },
  { label: 'Shipping', value: 'SHIPPING', color: 'cyan' },
  { label: 'Completed', value: 'COMPLETED', color: 'green' },
  { label: 'Cancelled', value: 'CANCELLED', color: 'red' },
];

// Order columns
export const orderColumns: BasicColumn[] = [
  {
    title: 'Order Code',
    dataIndex: 'orderCode',
    width: 120,
    fixed: 'left',
  },
  {
    title: 'Customer Name',
    dataIndex: 'customerName',
    width: 150,
  },
  {
    title: 'Order Date',
    dataIndex: 'orderDate',
    width: 150,
    customRender: ({ record }) => {
      if (!record.orderDate) return '';
      const date = new Date(record.orderDate);
      return date.toLocaleDateString();
    },
  },
  {
    title: 'Status',
    dataIndex: 'status',
    width: 100,
    customRender: ({ record }) => {
      const status = orderStatusOptions.find(item => item.value === record.status);
      if (!status) return record.status;
      return h(Tag, { color: status.color }, status.label);
    },
  },
  {
    title: 'Total Amount',
    dataIndex: 'totalAmount',
    width: 120,
    customRender: ({ record }) => {
      if (!record.totalAmount) return '$0.00';
      return `$${parseFloat(record.totalAmount).toFixed(2)}`;
    },
  },
  {
    title: 'Discount Amount',
    dataIndex: 'discountAmount',
    width: 120,
    customRender: ({ record }) => {
      if (!record.discountAmount) return '$0.00';
      return `$${parseFloat(record.discountAmount).toFixed(2)}`;
    },
  },
  {
    title: 'Final Amount',
    dataIndex: 'finalAmount',
    width: 120,
    customRender: ({ record }) => {
      if (!record.finalAmount) return '$0.00';
      return `$${parseFloat(record.finalAmount).toFixed(2)}`;
    },
  },
  {
    title: 'Notes',
    dataIndex: 'notes',
    width: 200,
    ellipsis: true,
  },
  {
    title: 'Created By',
    dataIndex: 'createdBy',
    width: 100,
  },
];

// Order item columns
export const orderItemColumns: BasicColumn[] = [
  {
    title: 'Product Code',
    dataIndex: 'productCode',
    width: 120,
  },
  {
    title: 'Product Name',
    dataIndex: 'productName',
    width: 200,
  },
  {
    title: 'Quantity',
    dataIndex: 'quantity',
    width: 80,
  },
  {
    title: 'Unit Price',
    dataIndex: 'unitPrice',
    width: 100,
    customRender: ({ record }) => {
      if (!record.unitPrice) return '$0.00';
      return `$${parseFloat(record.unitPrice).toFixed(2)}`;
    },
  },
  {
    title: 'Total Price',
    dataIndex: 'totalPrice',
    width: 100,
    customRender: ({ record }) => {
      if (!record.totalPrice) return '$0.00';
      return `$${parseFloat(record.totalPrice).toFixed(2)}`;
    },
  },
  {
    title: 'Discount Amount',
    dataIndex: 'discountAmount',
    width: 120,
    customRender: ({ record }) => {
      if (!record.discountAmount) return '$0.00';
      return `$${parseFloat(record.discountAmount).toFixed(2)}`;
    },
  },
  {
    title: 'Final Amount',
    dataIndex: 'finalAmount',
    width: 100,
    customRender: ({ record }) => {
      if (!record.finalAmount) return '$0.00';
      return `$${parseFloat(record.finalAmount).toFixed(2)}`;
    },
  },
];

// Order status history columns
export const orderStatusHistoryColumns: BasicColumn[] = [
  {
    title: 'From Status',
    dataIndex: 'fromStatus',
    width: 100,
    customRender: ({ record }) => {
      if (!record.fromStatus) return '-';
      const status = orderStatusOptions.find(item => item.value === record.fromStatus);
      if (!status) return record.fromStatus;
      return h(Tag, { color: status.color }, status.label);
    },
  },
  {
    title: 'To Status',
    dataIndex: 'toStatus',
    width: 100,
    customRender: ({ record }) => {
      const status = orderStatusOptions.find(item => item.value === record.toStatus);
      if (!status) return record.toStatus;
      return h(Tag, { color: status.color }, status.label);
    },
  },
  {
    title: 'Reason',
    dataIndex: 'reason',
    width: 200,
    ellipsis: true,
  },
  {
    title: 'User',
    dataIndex: 'userId',
    width: 100,
  },
  {
    title: 'Created At',
    dataIndex: 'createdAt',
    width: 150,
    customRender: ({ record }) => {
      if (!record.createdAt) return '';
      const date = new Date(record.createdAt);
      return date.toLocaleString();
    },
  },
];

// Order search form schema
export const orderSearchFormSchema: FormSchema[] = [
  {
    field: 'orderCode',
    label: 'Order Code',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'customerName',
    label: 'Customer Name',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: 'Status',
    component: 'Select',
    componentProps: {
      options: orderStatusOptions,
      fieldNames: { label: 'label', value: 'value' },
    },
    colProps: { span: 8 },
  },
  {
    field: 'orderDateRange',
    label: 'Order Date',
    component: 'RangePicker',
    colProps: { span: 8 },
  },
];

// Order form schema
export const orderFormSchema: FormSchema[] = [
  {
    field: 'customerId',
    label: 'Customer',
    component: 'ApiSelect',
    required: true,
    componentProps: {
      api: () => import('/@/views/warehouse/customer/customer.api').then(m => m.customerApi.getActive),
      labelField: 'customerName',
      valueField: 'id',
      placeholder: 'Please select customer',
    },
    colProps: { span: 24 },
  },
  {
    field: 'notes',
    label: 'Notes',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
];

// Order item form schema
export const orderItemFormSchema: FormSchema[] = [
  {
    field: 'productId',
    label: 'Product',
    component: 'ApiSelect',
    required: true,
    componentProps: {
      api: () => import('/@/views/warehouse/product/product.api').then(m => m.productApi.list),
      labelField: 'name',
      valueField: 'id',
      placeholder: 'Please select product',
    },
    colProps: { span: 12 },
  },
  {
    field: 'quantity',
    label: 'Quantity',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 1,
      placeholder: 'Please enter quantity',
    },
    colProps: { span: 12 },
  },
  {
    field: 'unitPrice',
    label: 'Unit Price',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: 'Please enter unit price',
    },
    colProps: { span: 12 },
  },
  {
    field: 'discountAmount',
    label: 'Discount Amount',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: 'Please enter discount amount',
    },
    colProps: { span: 12 },
  },
];

// Order status update form schema
export const orderStatusFormSchema: FormSchema[] = [
  {
    field: 'newStatus',
    label: 'New Status',
    component: 'Select',
    required: true,
    componentProps: {
      options: orderStatusOptions,
      fieldNames: { label: 'label', value: 'value' },
    },
    colProps: { span: 24 },
  },
  {
    field: 'reason',
    label: 'Reason',
    component: 'InputTextArea',
    required: true,
    colProps: { span: 24 },
  },
];