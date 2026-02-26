import { MockMethod } from 'vite-plugin-mock';
import { resultError, resultPageSuccess, resultSuccess, baseUrl } from '../_util';

const accountList = (() => {
    const result: any[] = [];
    for (let index = 0; index < 20; index++) {
        result.push({
            id: `${index}`,
            account: '@first',
            email: '@email',
            nickname: '@cname()',
            role: '@first',
            createTime: '@datetime',
            remark: '@cword(10,20)',
            'status|1': ['0', '1'],
        });
    }
    return result;
})();

const userList = (() => {
    const result: any[] = [];
    for (let index = 0; index < 20; index++) {
        result.push({
            id: `${index}`,
            username: '@first',
            email: '@email',
            realname: '@cname()',
            createTime: '@datetime',
            remark: '@cword(10,20)',
            avatar: 'https://q1.qlogo.cn/g?b=qq&nk=190848757&s=640'
        });
    }
    return result;
})();

const roleList = (() => {
    const result: any[] = [];
    for (let index = 0; index < 4; index++) {
        result.push({
            id: index + 1,
            orderNo: `${index + 1}`,
            roleName: ['super administrator', 'administrator', '文章administrator', 'Ordinary user'][index],
            roleValue: '@first',
            createTime: '@datetime',
            remark: '@cword(10,20)',
            menu: [['0', '1', '2'], ['0', '1'], ['0', '2'], ['2']][index],
            'status|1': ['0', '1'],
        });
    }
    return result;
})();

const newRoleList = (() => {
    const result: any[] = [];
    for (let index = 0; index < 4; index++) {
        result.push({
            id: index + 1,
            orderNo: `${index + 1}`,
            roleName: ['super administrator', 'administrator', '文章administrator', 'Ordinary user'][index],
            roleCode: '@first',
            createTime: '@datetime',
            remark: '@cword(10,20)'
        });
    }
    return result;
})();

const testList = (() => {
    const result: any[] = [];
    for (let index = 0; index < 4; index++) {
        result.push({
            id: index + 1,
            orderNo: `${index + 1}`,
            testName: ['data1', 'data2', 'data3', 'data4'][index],
            testValue: '@first',
            createTime: '@datetime'
        });
    }
    return result;
})();

const tableDemoList = (() => {
    const result: any[] = [];
    for (let index = 0; index < 4; index++) {
        result.push({
            id: index + 1,
            orderCode: '2008200' + `${index + 1}`,
            orderMoney: '@natural(1000,3000)',
            ctype: '@natural(1,2)',
            content: '@cword(10,20)',
            orderDate: '@datetime'
        });
    }
    return result;
})();

const deptList = (() => {
    const result: any[] = [];
    for (let index = 0; index < 3; index++) {
        result.push({
            id: `${index}`,
            deptName: ['East China Branch', 'South China Branch', 'Northwest Division'][index],
            orderNo: index + 1,
            createTime: '@datetime',
            remark: '@cword(10,20)',
            'status|1': ['0', '0', '1'],
            children: (() => {
                const children: any[] = [];
                for (let j = 0; j < 4; j++) {
                    children.push({
                        id: `${index}-${j}`,
                        deptName: ['R&D Department', 'Marketing Department', 'Ministry of Commerce', 'Finance Department'][j],
                        orderNo: j + 1,
                        createTime: '@datetime',
                        remark: '@cword(10,20)',
                        'status|1': ['0', '1'],
                        parentDept: `${index}`,
                        children: undefined,
                    });
                }
                return children;
            })(),
        });
    }
    return result;
})();

const menuList = (() => {
    const result: any[] = [];
    for (let index = 0; index < 3; index++) {
        result.push({
            id: `${index}`,
            icon: ['ion:layers-outline', 'ion:git-compare-outline', 'ion:tv-outline'][index],
            component: 'LAYOUT',
            type: '0',
            menuName: ['Dashboard', 'Permission management', 'Function'][index],
            permission: '',
            orderNo: index + 1,
            createTime: '@datetime',
            'status|1': ['0', '0', '1'],
            children: (() => {
                const children: any[] = [];
                for (let j = 0; j < 4; j++) {
                    children.push({
                        id: `${index}-${j}`,
                        type: '1',
                        menuName: ['menu1', 'menu2', 'menu3', 'menu4'][j],
                        icon: 'ion:document',
                        permission: ['menu1:view', 'menu2:add', 'menu3:update', 'menu4:del'][index],
                        component: [
                            '/dashboard/welcome/index',
                            '/dashboard/Analysis/index',
                            '/dashboard/workbench/index',
                            '/dashboard/test/index',
                        ][j],
                        orderNo: j + 1,
                        createTime: '@datetime',
                        'status|1': ['0', '1'],
                        parentMenu: `${index}`,
                        children: (() => {
                            const children: any[] = [];
                            for (let k = 0; k < 4; k++) {
                                children.push({
                                    id: `${index}-${j}-${k}`,
                                    type: '2',
                                    menuName: 'button' + (j + 1) + '-' + (k + 1),
                                    icon: '',
                                    permission:
                                        ['menu1:view', 'menu2:add', 'menu3:update', 'menu4:del'][index] +
                                        ':btn' +
                                        (k + 1),
                                    component: [
                                        '/dashboard/welcome/index',
                                        '/dashboard/Analysis/index',
                                        '/dashboard/workbench/index',
                                        '/dashboard/test/index',
                                    ][j],
                                    orderNo: j + 1,
                                    createTime: '@datetime',
                                    'status|1': ['0', '1'],
                                    parentMenu: `${index}-${j}`,
                                    children: undefined,
                                });
                            }
                            return children;
                        })(),
                    });
                }
                return children;
            })(),
        });
    }
    return result;
})();

export default [
  {
    url: `${baseUrl}/system/getAccountList`,
    timeout: 100,
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 20 } = query;
      return resultPageSuccess(page, pageSize, accountList);
    },
  },
  {
    url: `${baseUrl}/sys/user/list`,
    timeout: 100,
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 20 } = query;
      return resultPageSuccess(page, pageSize, userList);
    },
  },
  {
    url: `${baseUrl}/system/getRoleListByPage`,
    timeout: 100,
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 20 } = query;
      return resultPageSuccess(page, pageSize, roleList);
    },
  },
  {
    url: `${baseUrl}/sys/role/list`,
    timeout: 100,
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 20 } = query;
      return resultPageSuccess(page, pageSize, newRoleList);
    },
  },
  {
    url: `${baseUrl}/system/getTestListByPage`,
    timeout: 100,
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 20 } = query;
      return resultPageSuccess(page, pageSize, testList);
    },
  },
  {
    url: `${baseUrl}/system/getDemoTableListByPage`,
    timeout: 100,
    method: 'get',
    response: ({ query }) => {
      const { page = 1, pageSize = 20 } = query;
      return resultPageSuccess(page, pageSize, tableDemoList);
    },
  },
  {
    url: `${baseUrl}/system/setRoleStatus`,
    timeout: 500,
    method: 'post',
    response: ({ query }) => {
      const { id, status } = query;
      return resultSuccess({ id, status });
    },
  },
  {
    url: `${baseUrl}/system/getAllRoleList`,
    timeout: 100,
    method: 'get',
    response: () => {
      return resultSuccess(roleList);
    },
  },
  {
    url: `${baseUrl}/system/getDeptList`,
    timeout: 100,
    method: 'get',
    response: () => {
      return resultSuccess(deptList);
    },
  },
  {
    url: `${baseUrl}/system/getMenuList`,
    timeout: 100,
    method: 'get',
    response: () => {
      return resultSuccess(menuList);
    },
  },
  {
    url: `${baseUrl}/system/accountExist`,
    timeout: 500,
    method: 'post',
    response: ({ body }) => {
      const { account } = body || {};
      if (account && account.indexOf('admin') !== -1) {
        return resultError('This field cannot containadmin');
      } else {
        return resultSuccess(`${account} can use`);
      }
    },
  },
] as MockMethod[];
