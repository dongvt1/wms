import { BasicColumn, FormSchema } from '/@/components/Table';
import { usePermission } from '/@/hooks/web/usePermission';
import { JVxeColumn, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
const { isDisabledAuth, hasPermission, initBpmFormData} = usePermission();

export const columns: BasicColumn[] = [
  {
    title: 'Order number',
    dataIndex: 'orderCode',
    width: 260,
  },
  {
    title: 'Order type',
    dataIndex: 'ctype',
    slots: { customRender: 'ctype' },
  },
  {
    title: 'order date',
    dataIndex: 'orderDate',
    width: 300,
  },
  {
    title: 'Order amount',
    width: 200,
    dataIndex: 'orderMoney',
  },
  {
    title: 'Order notes',
    width: 200,
    dataIndex: 'content',
  },
  {
    title: 'process status',
    width: 200,
    dataIndex: 'bpmStatus',
    customRender: ({ text }) => {
      if (!text || text == '1') {
        return 'To be submitted';
      } else if (text == '2') {
        return 'Processing';
      } else if (text == '2') {
        return 'Completed';
      } else {
        return text;
      }
    },
  },
];

export function getBpmFormSchema(formData) {
  //Inject process node form permissions
  initBpmFormData(formData);
  
  const formSchema2: FormSchema[] = [
    {
      label: 'Order number',
      field: 'orderCode',
      component: 'Input',
      show: ({ values }) => {
        return hasPermission('order:orderCode');
      },
    },
    {
      label: 'Order type',
      field: 'ctype',
      component: 'Select',
      componentProps: {
        options: [
          { label: 'Domestic orders', value: '1', key: '1' },
          { label: 'international orders', value: '2', key: '2' },
        ],
      },
    },
    {
      label: 'order date',
      field: 'orderDate',
      component: 'DatePicker',
      componentProps: {
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        style: {
          width: '100%',
        },
      },
    },
    {
      label: 'Order amount',
      field: 'orderMoney',
      component: 'Input',
    },
    {
      label: 'Order notes',
      field: 'content',
      component: 'Input',
    },
  ];
  return formSchema2;
}

export function getOrderCustomerFormSchema(formData) {
  //Inject process node form permissions
  initBpmFormData(formData);
  
  const formSchema2: FormSchema[] = [
    {
      label: 'Customer name',
      field: 'name',
      component: 'Input',
      dynamicDisabled: ({ values }) => {
        return isDisabledAuth('order:name');
      },
    },
    {
      label: 'gender',
      field: 'sex',
      component: 'Select',
      componentProps: {
        options: [
          { label: 'male', value: '1', key: '1' },
          { label: 'female', value: '2', key: '2' },
        ],
      },
    },
    {
      label: 'ID number',
      field: 'idcard',
      component: 'Input',
    },
    {
      label: 'Phone number',
      field: 'telphone',
      component: 'Input',
    },
  ];
  return formSchema2;
}

export const jeecgOrderTicketColumns: JVxeColumn[] = [
  {
    title: 'flight number',
    key: 'ticketCode',
    width: 180,
    type: JVxeTypes.input,
    placeholder: 'Please enter${title}',
    defaultValue: '',
  },
  {
    title: 'Flight time',
    key: 'tickectDate',
    width: 180,
    type: JVxeTypes.date,
    placeholder: 'Please select${title}',
    defaultValue: '',
  },
];
