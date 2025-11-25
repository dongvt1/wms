export interface GrowCardItem {
  icon: string;
  title: string;
  value?: number;
  total: number;
  color?: string;
  action?: string;
  footer?: string;
}

export const growCardList: GrowCardItem[] = [
  {
    title: 'Number of visits',
    icon: 'visit-count|svg',
    value: 2000,
    total: 120000,
    color: 'green',
    action: 'moon',
  },
  {
    title: 'Turnover',
    icon: 'total-sales|svg',
    value: 20000,
    total: 500000,
    color: 'blue',
    action: 'moon',
  },
  {
    title: 'Number of downloads',
    icon: 'download-count|svg',
    value: 8000,
    total: 120000,
    color: 'orange',
    action: 'week',
  },
  {
    title: 'Number of transactions',
    icon: 'transaction|svg',
    value: 5000,
    total: 50000,
    color: 'purple',
    action: 'Year',
  },
];

export const chartCardList: GrowCardItem[] = [
  {
    title: 'total sales',
    icon: 'visit-count|svg',
    total: 126560,
    value: 234.56,
    footer: 'average daily sales',
  },
  {
    title: 'Order quantity',
    icon: 'total-sales|svg',
    value: 1234,
    total: 8846,
    color: 'blue',
    footer: '日Order quantity',
  },
  {
    title: 'Number of payments',
    icon: 'download-count|svg',
    value: 60,
    total: 6560,
    color: 'orange',
    footer: 'conversion rate',
  },
  {
    title: 'Operational activity effects',
    icon: 'transaction|svg',
    total: 78,
  },
];
export const bdcCardList: GrowCardItem[] = [
  {
    title: 'Acceptance volume',
    icon: 'ant-design:info-circle-outlined',
    total: 100,
    value: 60,
    footer: '今日Acceptance volume',
  },
  {
    title: 'Volume of transactions',
    icon: 'ant-design:info-circle-outlined',
    value: 54,
    total: 87,
    color: 'blue',
    footer: '今日Volume of transactions',
  },
  {
    title: '用户Acceptance volume',
    icon: 'ant-design:info-circle-outlined',
    value: 13,
    total: 15,
    color: 'orange',
    footer: '用户今日Acceptance volume',
  },
  {
    title: '用户Volume of transactions',
    icon: 'ant-design:info-circle-outlined',
    total: 9,
    value: 7,
    footer: '用户今日Volume of transactions',
  },
];

export const table = {
  dataSource: [
    { reBizCode: '1', type: 'transfer registration', acceptBy: 'Zhang San', acceptDate: '2019-01-22', curNode: 'Task assignment', flowRate: 60 },
    { reBizCode: '2', type: 'mortgage registration', acceptBy: 'John Doe', acceptDate: '2019-01-23', curNode: 'Leadership review', flowRate: 30 },
    { reBizCode: '3', type: 'transfer registration', acceptBy: 'Wang Wu', acceptDate: '2019-01-25', curNode: 'Task processing', flowRate: 20 },
    { reBizCode: '4', type: 'transfer registration', acceptBy: 'Zhao Lou', acceptDate: '2019-11-22', curNode: 'Department review', flowRate: 80 },
    { reBizCode: '5', type: 'transfer registration', acceptBy: 'Money is', acceptDate: '2019-12-12', curNode: 'Task assignment', flowRate: 90 },
    { reBizCode: '6', type: 'transfer registration', acceptBy: 'Sunba', acceptDate: '2019-03-06', curNode: 'Task processing', flowRate: 10 },
    { reBizCode: '7', type: 'mortgage registration', acceptBy: 'week大', acceptDate: '2019-04-13', curNode: 'Task assignment', flowRate: 100 },
    { reBizCode: '8', type: 'mortgage registration', acceptBy: 'Wu Er', acceptDate: '2019-05-09', curNode: 'Task reporting', flowRate: 50 },
    { reBizCode: '9', type: 'mortgage registration', acceptBy: 'Zheng Shuang', acceptDate: '2019-07-12', curNode: 'Task processing', flowRate: 63 },
    { reBizCode: '20', type: 'mortgage registration', acceptBy: 'Lin You', acceptDate: '2019-12-12', curNode: 'Mission return', flowRate: 59 },
    { reBizCode: '11', type: 'transfer registration', acceptBy: 'code cloud', acceptDate: '2019-09-10', curNode: 'Task signing', flowRate: 87 },
  ],
  columns: [
    {
      title: 'Business number',
      align: 'center',
      dataIndex: 'reBizCode',
    },
    {
      title: 'Business type',
      align: 'center',
      dataIndex: 'type',
    },
    {
      title: 'Assignee',
      align: 'center',
      dataIndex: 'acceptBy',
    },
    {
      title: 'Acceptance time',
      align: 'center',
      dataIndex: 'acceptDate',
    },
    {
      title: 'current node',
      align: 'center',
      dataIndex: 'curNode',
    },
    {
      title: 'Processing time',
      align: 'center',
      dataIndex: 'flowRate',
    },
  ],
  ipagination: {
    current: 1,
    pageSize: 5,
    pageSizeOptions: ['10', '20', '30'],
    showTotal: (total, range) => {
      return range[0] + '-' + range[1] + ' common' + total + 'strip';
    },
    showQuickJumper: true,
    showSizeChanger: true,
    total: 0,
  },
};
export const table1 = {
  dataSource: [
    { reBizCode: 'A001', type: 'transfer registration', acceptBy: 'Zhang Si', acceptDate: '2019-01-22', curNode: 'Task assignment', flowRate: 12 },
    { reBizCode: 'A002', type: 'mortgage registration', acceptBy: 'Li Bar', acceptDate: '2019-01-23', curNode: 'Task signing', flowRate: 3 },
    { reBizCode: 'A003', type: 'transfer registration', acceptBy: 'Wang San', acceptDate: '2019-01-25', curNode: 'Task processing', flowRate: 24 },
    { reBizCode: 'A004', type: 'transfer registration', acceptBy: 'Zhao Er', acceptDate: '2019-11-22', curNode: 'Department review', flowRate: 10 },
    { reBizCode: 'A005', type: 'transfer registration', acceptBy: 'Money is big', acceptDate: '2019-12-12', curNode: 'Task signing', flowRate: 8 },
    { reBizCode: 'A006', type: 'transfer registration', acceptBy: 'Sun Jiu', acceptDate: '2019-03-06', curNode: 'Task processing', flowRate: 10 },
    { reBizCode: 'A007', type: 'mortgage registration', acceptBy: 'week晕', acceptDate: '2019-04-13', curNode: 'Department review', flowRate: 24 },
    { reBizCode: 'A008', type: 'mortgage registration', acceptBy: 'Wu You', acceptDate: '2019-05-09', curNode: 'Department review', flowRate: 30 },
    { reBizCode: 'A009', type: 'mortgage registration', acceptBy: 'Zheng Wu', acceptDate: '2019-07-12', curNode: 'Task assignment', flowRate: 1 },
    { reBizCode: 'A0010', type: 'mortgage registration', acceptBy: 'Lin Shuang', acceptDate: '2019-12-12', curNode: 'Department review', flowRate: 16 },
    { reBizCode: 'A0011', type: 'transfer registration', acceptBy: 'Ma Lou', acceptDate: '2019-09-10', curNode: 'Department review', flowRate: 7 },
  ],
  columns: [
    {
      title: 'Business number',
      align: 'center',
      dataIndex: 'reBizCode',
    },
    {
      title: 'Assignee',
      align: 'center',
      dataIndex: 'acceptBy',
    },
    {
      title: 'Launch time',
      align: 'center',
      dataIndex: 'acceptDate',
    },
    {
      title: 'current node',
      align: 'center',
      dataIndex: 'curNode',
    },
    {
      title: 'timeout',
      align: 'center',
      dataIndex: 'flowRate',
    },
  ],
  ipagination: {
    current: 1,
    pageSize: 5,
    pageSizeOptions: ['10', '20', '30'],
    showTotal: (total, range) => {
      return range[0] + '-' + range[1] + ' common' + total + 'strip';
    },
    showQuickJumper: true,
    showSizeChanger: true,
    total: 0,
  },
};
