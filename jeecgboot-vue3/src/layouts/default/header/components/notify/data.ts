export interface ListItem {
  id: string;
  avatar: string;
  // The title content of the notification
  title: string;
  // Whether to show strikethrough on the title
  titleDelete?: boolean;
  datetime: string;
  type: string;
  read?: boolean;
  description: string;
  clickClose?: boolean;
  extra?: string;
  color?: string;
  // priority
  priority?: string;
}

export enum PriorityTypes {
  // 低priority，general news
  L = 'L',
  // 中priority，important news
  M = 'M',
  // 高priority，emergency message
  H = 'H',
}

export interface TabItem {
  key: string;
  name: string;
  list: ListItem[];
  unreadlist?: ListItem[];
  count: number;
}

export const tabListData: TabItem[] = [
  {
    key: '1',
    name: 'notify',
    list: [
      {
        id: '000000001',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/ThXAXghbEsBCCSDihZxY.png',
        title: 'you received 14 new weekly newspaper',
        description: '',
        datetime: '2017-08-09',
        type: '1',
      },
      {
        id: '000000002',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/OKJXDXrmkNshAMvwtvhu.png',
        title: 'Recommended by you Qu Nini Passed the third round of interview',
        description: '',
        datetime: '2017-08-08',
        type: '1',
      },
      {
        id: '000000003',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/kISTdvpyTAhtGxpovNWd.png',
        title: '这种模板可以区分多种notify类型',
        description: '',
        datetime: '2017-08-07',
        // read: true,
        type: '1',
      },
      {
        id: '000000004',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'The icons on the left are used to distinguish different types',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000005',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'Titles can be set to automatically display ellipsis，In this example, the number of header rows has been set to1OK，If the content exceeds1OK将自动截断并支持tooltipShow full title。',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000006',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'The icons on the left are used to distinguish different types',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000007',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'The icons on the left are used to distinguish different types',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000008',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'The icons on the left are used to distinguish different types',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000009',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'The icons on the left are used to distinguish different types',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
      {
        id: '000000010',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/GvqBnKhFgObvnSGkDsje.png',
        title: 'The icons on the left are used to distinguish different types',
        description: '',
        datetime: '2017-08-07',
        type: '1',
      },
    ],
    count: 0,
  },
  {
    key: '2',
    name: 'System messages',
    list: [
      {
        id: '000000006',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/fcHMVNCjPOsbUGdEduuv.jpeg',
        title: 'Qu Lili commented on you',
        description: 'description information description information description information',
        datetime: '2017-08-07',
        type: '2',
        clickClose: true,
      },
      {
        id: '000000007',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/fcHMVNCjPOsbUGdEduuv.jpeg',
        title: 'Zhu Xianyou Replied you',
        description: 'This template is used to remind you who has interacted with you',
        datetime: '2017-08-07',
        type: '2',
        clickClose: true,
      },
      {
        id: '000000008',
        avatar: 'https://gw.alipayobjects.com/zos/rmsportal/fcHMVNCjPOsbUGdEduuv.jpeg',
        title: 'title',
        description:
          'Please move your mouse here，To test how long messages will be handled here。本例中设置的描述最大OK数为2，Exceed2OK的描述内容将被省略并且可以通过tooltipView full content',
        datetime: '2017-08-07',
        type: '2',
        clickClose: true,
      },
    ],
    count: 0,
  },
  // {
  //   key: '3',
  //   name: 'To-do',
  //   list: [
  //     {
  //       id: '000000009',
  //       avatar: '',
  //       title: 'Task name',
  //       description: 'The task needs to be in 2017-01-12 20:00 start before',
  //       datetime: '',
  //       extra: 'Not started',
  //       color: '',
  //       type: '3',
  //     },
  //     {
  //       id: '000000010',
  //       avatar: '',
  //       title: 'Third-party emergency code changes',
  //       description: 'Guanlin Need to be in 2017-01-07 Complete code change tasks before',
  //       datetime: '',
  //       extra: 'Expires soon',
  //       color: 'red',
  //       type: '3',
  //     },
  //     {
  //       id: '000000011',
  //       avatar: '',
  //       title: 'Information Security Exam',
  //       description: 'Assign Zhueryu 2017-01-09 Updated and published before',
  //       datetime: '',
  //       extra: 'Time spent 8 sky',
  //       color: 'gold',
  //       type: '3',
  //     },
  //     {
  //       id: '000000012',
  //       avatar: '',
  //       title: 'ABCD version release',
  //       description: 'Assign Zhueryu 2017-01-09 Updated and published before',
  //       datetime: '',
  //       extra: '进OK中',
  //       color: 'blue',
  //       type: '3',
  //     },
  //   ],
  // },
];
