import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//List data
export const columns: BasicColumn[] = [
  {
    title: 'Authorization name',
    align: "center",
    dataIndex: 'name'
  },
  {
    title: 'AK',
    align: "center",
    dataIndex: 'ak'
  },
  {
    title: 'SK',
    align: "center",
    dataIndex: 'sk'
  },
  {
    title: 'Creator',
    align: "center",
    dataIndex: 'createBy'
  },
  {
    title: 'creation time',
    align: "center",
    dataIndex: 'createTime'
  },
  // {
  //   title: 'Associated system user name',
  //   align: "center",
  //   dataIndex: 'createBy',
  // },
];

// Advanced query data
export const superQuerySchema = {
  name: {title: 'Authorization name',order: 0,view: 'text', type: 'string',},
  ak: {title: 'AK',order: 1,view: 'text', type: 'string',},
  sk: {title: 'SK',order: 2,view: 'text', type: 'string',},
  createBy: {title: 'Associated system user name',order: 3,view: 'text', type: 'string',},
  createTime: {title: 'creation time',order: 4,view: 'datetime', type: 'string',},
  // systemUserId: {title: 'Associated system user name',order: 5,view: 'text', type: 'string',},
};
