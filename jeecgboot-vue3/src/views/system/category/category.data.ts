import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Category name',
    dataIndex: 'name',
    width: 350,
    align: 'left',
  },
  {
    title: 'Classification coding',
    dataIndex: 'code',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'name',
    field: 'name',
    component: 'JInput',
    colProps: { span: 6 },
  },
  {
    label: 'coding',
    field: 'code',
    component: 'JInput',
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'parent node',
    field: 'pid',
    component: 'TreeSelect',
    componentProps: {
      //update-begin---author:wangshuai ---date:20230829  for：replaceFieldsExpired，usefieldNamesreplace------------
      fieldNames: {
      //update-end---author:wangshuai ---date:20230829  for：replaceFieldsExpired，usefieldNamesreplace------------
        value: 'key',
      },
      dropdownStyle: {
        maxHeight: '50vh',
      },
      getPopupContainer: () => document.body,
    },
    show: ({ values }) => {
      return values.pid !== '0';
    },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    label: 'Category name',
    field: 'name',
    required: true,
    component: 'Input',
  },
];
