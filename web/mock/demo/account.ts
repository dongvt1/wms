import { MockMethod } from 'vite-plugin-mock';
import { resultSuccess, resultError, baseUrl } from '../_util';
import { ResultEnum } from '../../src/enums/httpEnum';
const userInfo = {
  name: 'Jeecg',
  userid: '00000001',
  email: 'test@gmail.com',
  signature: 'Tolerant of all rivers，Tolerance is great',
  introduction: 'smiling，Trying hard，admiring',
  title: 'Interaction expert',
  group: 'A certain business group－XX platform department－XX technical department－UED',
  tags: [
    {
      key: '0',
      label: 'Very thoughtful',
    },
    {
      key: '1',
      label: 'Focus on design',
    },
    {
      key: '2',
      label: 'hot~',
    },
    {
      key: '3',
      label: 'long legs',
    },
    {
      key: '4',
      label: 'Sichuan girl',
    },
    {
      key: '5',
      label: 'Tolerant of all rivers',
    },
  ],
  notifyCount: 12,
  unreadCount: 11,
  country: 'China',
  address: 'Xiamen City 77',
  phone: '0592-268888888',
};

export default [
  {
    url: `${baseUrl}/account/getAccountInfo`,
    timeout: 1000,
    method: 'get',
    response: () => {
      return resultSuccess(userInfo);
    },
  },
  {
    url: `${baseUrl}/user/sessionTimeout`,
    method: 'post',
    statusCode: 401,
    response: () => {
      return resultError();
    },
  },
  {
    url: '/basic-api/user/tokenExpired',
    method: 'post',
    statusCode: 200,
    response: () => {
      return resultError('Token Expired!', { code: ResultEnum.TIMEOUT as number });
    },
  },
] as MockMethod[];
