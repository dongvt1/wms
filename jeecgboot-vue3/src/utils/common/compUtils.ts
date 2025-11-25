import { useGlobSetting } from '/@/hooks/setting';
import { merge, random } from 'lodash-es';
import { isArray } from '/@/utils/is';
import { FormSchema } from '/@/components/Form';
import { reactive } from "vue";
import { getTenantId, getToken, getAuthCache, setAuthCache } from "/@/utils/auth";
import { useUserStoreWithOut } from "/@/store/modules/user";
import dayjs from 'dayjs';
import Big from 'big.js';

import { Modal } from "ant-design-vue";
import { defHttp } from "@/utils/http/axios";
import { useI18n } from "@/hooks/web/useI18n";

const globSetting = useGlobSetting();
const baseApiUrl = globSetting.domainUrl;
/**
 *  Get file service access path
 * @param fileUrl file path
 * @param prefix(defaulthttp)  file path前缀 http/https
 */
export const getFileAccessHttpUrl = (fileUrl, prefix = 'http') => {
  let result = fileUrl;
  try {
    if (fileUrl && fileUrl.length > 0 && !fileUrl.startsWith(prefix)) {
      //Determine whether it is an array format
      let isArray = fileUrl.indexOf('[') != -1;
      if (!isArray) {
        let prefix = `${baseApiUrl}/sys/common/static/`;
        // Determine whether the prefix is ​​included
        if (!fileUrl.startsWith(prefix)) {
          result = `${prefix}${fileUrl}`;
        }
      }
    }
  } catch (err) {}
  return result;
};

/**
 * trigger window.resize
 */
export function triggerWindowResizeEvent() {
  let event: any = document.createEvent('HTMLEvents');
  event.initEvent('resize', true, true);
  event.eventType = 'message';
  window.dispatchEvent(event);
}

/**
 * Get random number
 *  @param length Number of digits
 */
export const getRandom = (length: number = 1) => {
  return '-' + parseInt(String(Math.random() * 10000 + 1), length);
};

/**
 * Randomly generate string
 * @param length length of string
 * @param chats Optional string interval（Only characters in the string passed in will be generated）
 * @return string generated string
 */
export function randomString(length: number, chats?: string) {
  if (!length) length = 1;
  if (!chats) {
    // noinspection SpellCheckingInspection
    chats = '0123456789qwertyuioplkjhgfdsazxcvbnm';
  }
  let str = '';
  for (let i = 0; i < length; i++) {
    let num = random(0, chats.length - 1);
    str += chats[num];
  }
  return str;
}

/**
 * Convert ordinary list data intotreestructure
 * @param array treedata
 * @param opt  Configuration parameters
 * @param startPid parent node
 */
export const listToTree = (array, opt, startPid) => {
  const obj = {
    primaryKey: opt.primaryKey || 'key',
    parentKey: opt.parentKey || 'parentId',
    titleKey: opt.titleKey || 'title',
    startPid: opt.startPid || '',
    currentDept: opt.currentDept || 0,
    maxDept: opt.maxDept || 100,
    childKey: opt.childKey || 'children',
  };
  if (startPid) {
    obj.startPid = startPid;
  }
  return toTree(array, obj.startPid, obj.currentDept, obj);
};
/**
 *  recursive buildtree
 * @param list
 * @param startPid
 * @param currentDept
 * @param opt
 * @returns {Array}
 */
export const toTree = (array, startPid, currentDept, opt) => {
  if (opt.maxDept < currentDept) {
    return [];
  }
  let child = [];
  if (array && array.length > 0) {
    child = array
      .map((item) => {
        // 筛查符合条件的data（primary key = startPid）
        if (typeof item[opt.parentKey] !== 'undefined' && item[opt.parentKey] === startPid) {
          // Recurse if conditions are met
          const nextChild = toTree(array, item[opt.primaryKey], currentDept + 1, opt);
          // Node information storage
          if (nextChild.length > 0) {
            item['isLeaf'] = false;
            item[opt.childKey] = nextChild;
          } else {
            item['isLeaf'] = true;
          }
          item['title'] = item[opt.titleKey];
          item['label'] = item[opt.titleKey];
          item['key'] = item[opt.primaryKey];
          item['value'] = item[opt.primaryKey];
          return item;
        }
      })
      .filter((item) => {
        return item !== undefined;
      });
  }
  return child;
};

/**
 * Total tool method at the bottom of the table
 * @param tableData 表格data
 * @param fieldKeys Column field to be totaled
 */
export function mapTableTotalSummary(tableData: Recordable[], fieldKeys: string[]) {
  let totals: any = { _row: 'total', _index: 'total' };
  fieldKeys.forEach((key) => {
    totals[key] = tableData.reduce((prev, next) => {
      // update-begin--author:liaozhiyang---date:20240118---for：【QQYUN-7891】PR total工具method，Convert toNuberType recalculation
      const value = Number(next[key]);
      if (!Number.isNaN(value)) {
        // update-begin--author:liaozhiyang---date:20250224---for：【issues/7830】total小数计算精度
        prev = Big(prev).plus(value).toString();
        // update-end--author:liaozhiyang---date:20250224---for：【issues/7830】total小数计算精度
      }
      // update-end--author:liaozhiyang---date:20240118---for：【issues/7830】PR total工具method，Convert toNuberType recalculation
      return prev;
    }, 0);
    // update-begin--author:liaozhiyang---date:20250224---for：【issues/7830】total小数计算精度
    totals[key] = +totals[key];
    // update-end--author:liaozhiyang---date:20250224---for：【issues/7830】total小数计算精度
  });
  return totals;
}

/**
 * Simple method to achieve anti-shake
 *
 * Anti-shake(debounce)函数在第一次trigger给定的函数时，Do not execute function immediately，Instead, it gives a deadline value(delay)，for example100ms。
 * if100msExecute the function again within，Just start timing again，The function is not actually executed until the timer expires.。
 * 这样做的好处是if短时间内大量trigger同一事件，The function will only be executed once。
 *
 * @param fn 要Anti-shake的函数
 * @param delay Anti-shake的毫Second数
 * @returns {Function}
 */
export function simpleDebounce(fn, delay = 100) {
  let timer: any | null = null;
  return function () {
    let args = arguments;
    if (timer) {
      clearTimeout(timer);
    }
    timer = setTimeout(() => {
      // @ts-ignore
      fn.apply(this, args);
    }, delay);
  };
}

/**
 * date formatting
 * @param date date
 * @param block Format string
 */
export function dateFormat(date, block) {
  if (!date) {
    return '';
  }
  let format = block || 'yyyy-MM-dd';
  date = new Date(date);
  const map = {
    M: date.getMonth() + 1, // month
    d: date.getDate(), // day
    h: date.getHours(), // Hour
    m: date.getMinutes(), // point
    s: date.getSeconds(), // Second
    q: Math.floor((date.getMonth() + 3) / 3), // quarter
    S: date.getMilliseconds(), // 毫Second
  };
  format = format.replace(/([yMdhmsqS])+/g, (all, t) => {
    let v = map[t];
    if (v !== undefined) {
      if (all.length > 1) {
        v = `0${v}`;
        v = v.substr(v.length - 2);
      }
      return v;
    } else if (t === 'y') {
      return date
        .getFullYear()
        .toString()
        .substr(4 - all.length);
    }
    return all;
  });
  return format;
}

/**
 * Get event bubbling path，compatible IE11，Edge，Chrome，Firefox，Safari
 * Current place of use：JVxeTable Spanmodel
 */
export function getEventPath(event) {
  let target = event.target;
  let path = (event.composedPath && event.composedPath()) || event.path;

  if (path != null) {
    return path.indexOf(window) < 0 ? path.concat(window) : path;
  }

  if (target === window) {
    return [window];
  }

  let getParents = (node, memo) => {
    const parentNode = node.parentNode;

    if (!parentNode) {
      return memo;
    } else {
      return getParents(parentNode, memo.concat(parentNode));
    }
  };
  return [target].concat(getParents(target, []), window);
}

/**
 * if值不存在就 push into array，Otherwise, don’t deal with it
 * @param array 要操作的data
 * @param value value to add
 * @param key available，if比较的是对象，There may be situations where the addresses are different but the values ​​are actually the same.，You can pass this field to determine the only field in the object，For example id。If not passed, the actual value will be compared directly.
 * @returns {boolean} success push return true，不处理return false
 */
export function pushIfNotExist(array, value, key?) {
  for (let item of array) {
    if (key && item[key] === value[key]) {
      return false;
    } else if (item === value) {
      return false;
    }
  }
  array.push(value);
  return true;
}
/**
 * Filter empty properties in objects
 * @param obj
 * @returns {*}
 */
export function filterObj(obj) {
  if (!(typeof obj == 'object')) {
    return;
  }

  for (let key in obj) {
    if (obj.hasOwnProperty(key) && (obj[key] == null || obj[key] == undefined || obj[key] === '')) {
      delete obj[key];
    }
  }
  return obj;
}

/**
 * Underscore to camel case
 * @param string
 */
export function underLine2CamelCase(string: string) {
  return string.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
}

/**
 * 查找树structure
 * @param treeList
 * @param fn Find method
 * @param childrenKey
 */
export function findTree(treeList: any[], fn: Fn, childrenKey = 'children') {
  for (let i = 0; i < treeList.length; i++) {
    let item = treeList[i];
    if (fn(item, i, treeList)) {
      return item;
    }
    let children = item[childrenKey];
    if (isArray(children)) {
      let findResult = findTree(children, fn, childrenKey);
      if (findResult) {
        return findResult;
      }
    }
  }
  return null;
}

/** Get mapFormSchema method */
export function bindMapFormSchema<T>(spanMap, spanTypeDef: T) {
  return function (s: FormSchema, spanType: T = spanTypeDef) {
    return merge(
      {
        disabledLabelWidth: true,
      } as FormSchema,
      spanMap[spanType],
      s
    );
  };
}

/**
 * Is the stringnullornullstring
 * @param str
 * @return {boolean}
 */
export function stringIsNull(str) {
  // two == Can be judged simultaneously null and undefined
  return str == null || str === 'null' || str === 'undefined';
}

/**
 * 【There may be performance issues if there are too many components.】Get弹窗div，drop down box、date等组件mount tomodalsuperior，Solve the problem of pop-up window covering
 * @param node
 */
export function getAutoScrollContainer(node: HTMLElement) {
  let element: Nullable<HTMLElement> = node
  while (element != null) {
    if (element.classList.contains('scrollbar__view')) {
      // Determine whether there is a scroll bar
      if (element.clientHeight < element.scrollHeight) {
        // When there is a scroll bar，Mount to parent，Fix scrolling issues
        return node.parentElement
      } else {
        // When there is no scroll bar，mount tobodysuperior，Solve the problem of drop-down box covering
        return document.body
      }
    } else {
      element = element.parentElement
    }
  }
  // Not in the pop-up window，走default逻辑
  return node.parentElement
}

/**
 * Determine whether all submenus are hidden
 * @param menuTreeItem
 */
export  function checkChildrenHidden(menuTreeItem){
  //Is it an aggregate route?
  let alwaysShow=menuTreeItem.alwaysShow;
  if(alwaysShow){
    return false;
  }
  if(!menuTreeItem.children){
    return false
  }
  return menuTreeItem.children?.find((item) => item.hideMenu == false) != null;
}

/**
 * Calculate file size
 * @param fileSize
 * @param unit
 * @return return大小及后缀
 */
export function calculateFileSize(fileSize, unit?) {
  let unitArr = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  if (unit && unit.length > 0) {
    unitArr = unit;
  }
  let size = fileSize;
  let unitIndex = 0;
  while (size >= 1024 && unitIndex < unitArr.length - 1) {
    size /= 1024;
    unitIndex++;
  }
  //Keep to two decimal places，rounding
  size = Math.round(size * 100) / 100;
  return size + unitArr[unitIndex];
}

/**
 * Getsuperior传header
 */
export function getHeaders() {
  let tenantId = getTenantId();
  return reactive({
    'X-Access-Token': getToken(),
    'X-Tenant-Id': tenantId ? tenantId : '0',
  });
}

/** 根据表达式Get相应的用户信息 */
export function getUserInfoByExpression(expression) {
  if (!expression) {
    return expression;
  }
  // 当前date
  if (expression === 'sys_date' || expression === 'sysDate') {
    return dayjs().format('YYYY-MM-DD');
  }
  // current time
  if (expression === 'sys_time' || expression === 'sysTime') {
    return dayjs().format('HH:mm:ss');
  }
  const userStore = useUserStoreWithOut();
  let userInfo = userStore.getUserInfo;
  if (userInfo) {
    switch (expression) {
      case 'sysUserId':
        return userInfo.id;
      // Current logged in user login account
      case 'sysUserCode':
      case 'sys_user_code':
        return userInfo.username;
      // Real name of currently logged in user
      case 'sysUserName':
        return userInfo.realname;
      // Current login user department number
      case 'sysOrgCode':
      case 'sys_org_code':
        return userInfo.orgCode;
    }
  }
  return expression;
}

/**
 * replacement expression（#{xxx}）for user information
 * @param expression
 */
export function replaceUserInfoByExpression(expression: string | any[]) {
  if (!expression) {
    return expression;
  }
  const isString = typeof expression === 'string';
  const isArray = Array.isArray(expression)
  if (!isString && !isArray) {
    return expression;
  }
  const reg = /#{(.*?)}/g;
  const replace = (str) => {
    if (typeof str !== 'string') {
      return str;
    }
    let result = str.match(reg);
    if (result && result.length > 0) {
      result.forEach((item) => {
        let userInfo = getUserInfoByExpression(item.substring(2, item.length - 1));
        str = str.replace(item, userInfo);
      });
    }
    return str;
  };
  // @ts-ignore
  return isString ? replace(expression) : expression.map(replace);
}

/**
 * Set up tenant cache，When a tenant exits
 * 
 * @param tenantId
 */
export async function userExitChangeLoginTenantId(tenantId){
  const userStore = useUserStoreWithOut();
  //step 1 Get用户租户
  const url = '/sys/tenant/getCurrentUserTenant'
  let currentTenantId = null;
  const data = await defHttp.get({ url });
  if(data && data.list){
    let arr = data.list;
    if(arr.length>0){
      //step 2.Determine currentidDoes it exist in the user tenant?
      let filterTenantId = arr.filter((item) => item.id == tenantId);
      //The existence description is not exiting and is not the current tenant.，Just use the tenant you are using
      if(filterTenantId && filterTenantId.length>0){
        currentTenantId = tenantId;
      }else{
        //不存在default第一个
        currentTenantId = arr[0].id
      }
    }
  }
  let loginTenantId = getTenantId();
  userStore.setTenant(currentTenantId);

  //update-begin---author:wangshuai---date:2023-11-07---for:【QQYUN-7005】Quit the tenant，Determine exiting tenantsIDwith current tenantIDconsistent，Refresh again---
  //Tenant is empty，It means there are no tenants anymore，Need to refresh page。or者当前租户and退出的租户consistent则需要刷新浏览器
  if(!currentTenantId || tenantId == loginTenantId){
    window.location.reload();
  }
  //update-end---author:wangshuai---date:2023-11-07---for:【QQYUN-7005】Quit the tenant，Determine exiting tenantsIDwith current tenantIDconsistent，Refresh again---
}

/**
 * My tenant module needs to enable multi-tenant prompts
 * 
 * @param title title
 */
export function tenantSaasMessage(title){
  let tenantId = getTenantId();
  if(!tenantId){
    Modal.confirm({
      title:title,
      content: '此菜单需要在多租户model下使用，否则data会出现混乱',
      okText: 'confirm',
      okType: 'danger',
      // @ts-ignore
      cancelButtonProps: { style: { display: 'none' } },
    })
  }
}

/**
 * 判断dateandcurrent time是否为同一天
 * @param dateStr
 */
export function sameDay(dateStr) {
  if (!dateStr) {
    return false;
  }
  // Get当前date
  let currentDate = new Date();
  let currentDay = currentDate.getDate();
  let currentMonth = currentDate.getMonth();
  let currentYear = currentDate.getFullYear();

  //创建另一个date进行比较
  let otherDate = new Date(dateStr);
  let otherDay = otherDate.getDate();
  let otherMonth = otherDate.getMonth();
  let otherYear = otherDate.getFullYear();

  //比较date
  if (currentDay === otherDay && currentMonth === otherMonth && currentYear === otherYear) {
    return true;
  } else {
    return false;
  }
}


/**
 * Translate menu names
 * 2024-02-28
 * liaozhiyang
 * @param data
 */
export function translateTitle(data) {
  if (data?.length) {
    const { t } = useI18n();
    data.forEach((item) => {
      if (item.slotTitle) {
        if (item.slotTitle.includes("t('") && t) {
          item.slotTitle = new Function('t', `return ${item.slotTitle}`)(t);
        }
      }
      if (item.children?.length) {
        translateTitle(item.children);
      }
    });
  }
  return data;
}

/**
 *
 * deep freeze object
 * @param obj Object or Array
 */
export function freezeDeep(obj: Recordable | Recordable[]) {
  if (obj != null) {
    if (Array.isArray(obj)) {
      obj.forEach(item => freezeDeep(item))
    } else if (typeof obj === 'object') {
      Object.values(obj).forEach(value => {
        freezeDeep(value)
      })
    }
    Object.freeze(obj)
  }
  return obj
}

/**
 * Get父级名称
 * 
 * @param orgCode current departmentcode
 * @param label 当前default显示的值
 * @param depId depId
 * @return Department name
 */
export async function getDepartPathNameByOrgCode(orgCode, label, depId){
  let key:any = "DEPARTNAME" + depId + orgCode;
  let authCache = getAuthCache(key);
  if (authCache) {
    return authCache;
  }
  if (orgCode) {
    depId = "";
  }
  let result = await defHttp.get({ url: "/sys/sysDepart/getDepartPathNameByOrgCode", params:{ orgCode: orgCode, depId: depId } }, { isTransformResponse: false });
  if (result.success) {
    setAuthCache(key,result.result);
    return result.result;
  }
  return label;
}
