import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Log content',
    dataIndex: 'logContent',
    width: 100,
    align: 'left',
  },
  {
    title: 'operatorID',
    dataIndex: 'userid',
    width: 80,
  },
  {
    title: 'operator',
    dataIndex: 'username',
    width: 80,
  },
  {
    title: 'IP',
    dataIndex: 'ip',
    width: 80,
  },
  {
    title: 'time consuming(millisecond)',
    dataIndex: 'costTime',
    width: 80,
  },
  {
    title: 'creation time',
    dataIndex: 'createTime',
    sorter: true,
    width: 80,
  },
  {
    title: 'client type',
    dataIndex: 'clientType_dictText',
    width: 60,
  },
];

/**
 * Operation log requires operation type
 */
export const operationLogColumn: BasicColumn[] = [
  ...columns,
  {
    title: 'Operation type',
    dataIndex: 'operateType_dictText',
    width: 40,
  },
];

export const exceptionColumns: BasicColumn[] = [
  {
    title: 'Exception title',
    dataIndex: 'logContent',
    width: 100,
    align: 'left',
  },
  {
    title: 'Request address',
    dataIndex: 'requestUrl',
    width: 100,
  },
  {
    title: 'Request parameters',
    dataIndex: 'method',
    width: 60,
  },
  {
    title: 'operator',
    dataIndex: 'username',
    width: 60,
    customRender: ({ record }) => {
      let pname = record.username;
      let pid = record.userid;
      if(!pname && !pid){
        return "";
      }
      return pname + " (account: "+ pid + " )";
    },
  },
  {
    title: 'IP',
    dataIndex: 'ip',
    width: 60,
  },
  {
    title: 'creation time',
    dataIndex: 'createTime',
    sorter: true,
    width: 60,
  },
  {
    title: 'client type',
    dataIndex: 'clientType_dictText',
    width: 60,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyWord',
    label: 'Search log',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'fieldTime',
    component: 'RangePicker',
    label: 'creation time',
    componentProps: {
      valueType: 'Date',
    },
    colProps: {
      span: 6,
    },
  },
];

export const operationSearchFormSchema: FormSchema[] = [
  ...searchFormSchema,
  {
    field: 'operateType',
    label: 'Operation type',
    component: 'JDictSelectTag',
    colProps: { span: 4 },
    componentProps: {
      dictCode: 'operate_type',
    },
  },
];
