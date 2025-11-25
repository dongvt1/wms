import type { RouteLocationNormalized, RouteRecordNormalized } from 'vue-router';
import type { App, Plugin } from 'vue';
import type { FormSchema, FormActionType } from "@/components/Form";

import { unref } from 'vue';
import { isObject, isFunction, isString } from '/@/utils/is';
import Big from 'big.js';
import dayjs from "dayjs";
// update-begin--author:sunjianlei---date:20220408---for: 【VUEN-656】Configure external URL cannot be opened，The reason is that I brought#Number，Need to replace
export const URL_HASH_TAB = `__AGWE4H__HASH__TAG__PWHRG__`;
// update-end--author:sunjianlei---date:20220408---for: 【VUEN-656】Configure external URL cannot be opened，The reason is that I brought#Number，Need to replace

export const noop = () => {};

/**
 * @description:  Set ui mount node
 */
export function getPopupContainer(node?: HTMLElement): HTMLElement {
  return (node?.parentNode as HTMLElement) ?? document.body;
}

/**
 * Add the object as a parameter to the URL
 * @param baseUrl url
 * @param obj
 * @returns {string}
 * eg:
 *  let obj = {a: '3', b: '4'}
 *  setObjToUrlParams('www.baidu.com', obj)
 *  ==>www.baidu.com?a=3&b=4
 */
export function setObjToUrlParams(baseUrl: string, obj: any): string {
  let parameters = '';
  for (const key in obj) {
    parameters += key + '=' + encodeURIComponent(obj[key]) + '&';
  }
  parameters = parameters.replace(/&$/, '');
  return /\?$/.test(baseUrl) ? baseUrl + parameters : baseUrl.replace(/\/?$/, '?') + parameters;
}

export function deepMerge<T = any>(src: any = {}, target: any = {}): T {
  let key: string;
  for (key in target) {
    // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineformlabelLong optimization
    if (isObject(src[key]) && isObject(target[key])) {
      src[key] = deepMerge(src[key], target[key]);
    } else {
      // update-begin--author:liaozhiyang---date:20250318---for：【issues/7940】componentPropsWhen written in functional form，updateSchemaWhen written as an object，Parameters are not merged
      try {
        if (isFunction(src[key]) && isObject(src[key]()) && isObject(target[key])) {
          // src[key]is a function and returns an object，andtarget[key]is an object
          src[key] = deepMerge(src[key](), target[key]);
        } else if (isObject(src[key]) && isFunction(target[key]) && isObject(target[key]())) {
          // target[key]is a function and returns an object，andsrc[key]is an object
          src[key] = deepMerge(src[key], target[key]());
        } else if (isFunction(src[key]) && isFunction(target[key]) && isObject(src[key]()) && isObject(target[key]())) {
          // src[key]is a function and returns an object，target[key]is a function and returns an object
          src[key] = deepMerge(src[key](), target[key]());
        } else {
          src[key] = target[key];
        }
      } catch (error) {
        src[key] = target[key];
      }
      // update-end--author:liaozhiyang---date:20250318---for：【issues/7940】componentPropsWhen written in functional form，updateSchemaWhen written as an object，Parameters are not merged
    }
    // update-end--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineformlabelLong optimization
  }
  return src;
}

export function openWindow(url: string, opt?: { target?: TargetContext | string; noopener?: boolean; noreferrer?: boolean }) {
  const { target = '__blank', noopener = true, noreferrer = true } = opt || {};
  const feature: string[] = [];

  noopener && feature.push('noopener=yes');
  noreferrer && feature.push('noreferrer=yes');

  window.open(url, target, feature.join(','));
}

// dynamic use hook props
export function getDynamicProps<T, U>(props: T): Partial<U> {
  const ret: Recordable = {};

  // @ts-ignore
  Object.keys(props).map((key) => {
    ret[key] = unref((props as Recordable)[key]);
  });

  return ret as Partial<U>;
}

/**
 * Getform字段值数据类型
 * @param props
 * @param field
 * @updateBy:zyf
 */
export function getValueType(props, field) {
  let formSchema = unref(unref(props)?.schemas);
  let valueType = 'string';
  if (formSchema) {
    let schema = formSchema.filter((item) => item.field === field)[0];
    valueType = schema && schema.componentProps && schema.componentProps.valueType ? schema.componentProps.valueType : valueType;
  }
  return valueType;
}

/**
 * Getform字段值数据类型
 * @param schema
 * @param formAction
 */
export function getValueTypeBySchema(schema: FormSchema, formAction: FormActionType) {
  let valueType = 'string';
  if (schema) {
    const componentProps = formAction.getSchemaComponentProps(schema);
    // update-begin--author:liaozhiyang---date:20250825---for：【issues/8738】componentPropsCannot be obtained when it is a functionvalueType
    if (isFunction(componentProps)) {
      const result = componentProps(schema);
      valueType = result?.valueType ?? valueType;
    } else {
      valueType = componentProps?.valueType ? componentProps?.valueType : valueType;
    }
    // update-end--author:liaozhiyang---date:20250825---for：【issues/8738】componentPropsCannot be obtained when it is a functionvalueType
  }
  return valueType;
}

/**
 * passpickerProperty gets date data
 * @param data
 * @param picker
 */
export function getDateByPicker(data, picker) {
  if (!data || !picker) {
    return data;
  }
  /**
   * need to put years、years、Set to the first day of this period（[year quarter]No processing requiredantdWhat is returned is the first day of the quarter，[year week]Not processed either）
   * For example, the date format is year，The time passed to the database must be20240101
   * 例如date格式是years（selected202502），The time passed to the database must be20250201
   */
  if (picker === 'year') {
    return dayjs(data).set('month', 0).set('date', 1).format('YYYY-MM-DD');
  } else if (picker === 'month') {
    return dayjs(data).set('date', 1).format('YYYY-MM-DD');
  } else if (picker === 'week') {
    return dayjs(data).startOf('week').format('YYYY-MM-DD');
  }
  return data;
}

export function getRawRoute(route: RouteLocationNormalized): RouteLocationNormalized {
  if (!route) return route;
  const { matched, ...opt } = route;
  return {
    ...opt,
    matched: (matched
      ? matched.map((item) => ({
          meta: item.meta,
          name: item.name,
          path: item.path,
        }))
      : undefined) as RouteRecordNormalized[],
  };
}
/**
 * Deep clone object、array
 * @param obj cloned object
 * @return cloned object
 */
export function cloneObject(obj) {
  return JSON.parse(JSON.stringify(obj));
}

export const withInstall = <T>(component: T, alias?: string) => {
  //console.log("---initialization---", component)

  const comp = component as any;
  comp.install = (app: App) => {
    // @ts-ignore
    app.component(comp.name || comp.displayName, component);
    if (alias) {
      app.config.globalProperties[alias] = component;
    }
  };
  return component as T & Plugin;
};

/**
 * GeturlAddress parameters
 * @param paraName
 */
export function getUrlParam(paraName) {
  let url = document.location.toString();
  let arrObj = url.split('?');

  if (arrObj.length > 1) {
    let arrPara = arrObj[1].split('&');
    let arr;

    for (let i = 0; i < arrPara.length; i++) {
      arr = arrPara[i].split('=');

      if (arr != null && arr[0] == paraName) {
        return arr[1];
      }
    }
    return '';
  } else {
    return '';
  }
}

/**
 * hibernate（setTimeoutofpromiseversion）
 * @param ms 要hibernateof时间，unit：millisecond
 * @param fn callback，available
 * @return Promise
 */
export function sleep(ms: number, fn?: Fn) {
  return new Promise<void>((resolve) =>
    setTimeout(() => {
      fn && fn();
      resolve();
    }, ms)
  );
}

/**
 * 不use正则of方式replace所有值
 * @param text 被replaceof字符串
 * @param checker  replace前of内容
 * @param replacer replace后of内容
 * @returns {String} replace后of字符串
 */
export function replaceAll(text, checker, replacer) {
  let lastText = text;
  text = text.replace(checker, replacer);
  if (lastText !== text) {
    return replaceAll(text, checker, replacer);
  }
  return text;
}

/**
 * GetURLupper parameters
 * @param url
 */
export function getQueryVariable(url) {
  if (!url) return;

  var t,
    n,
    r,
    i = url.split('?')[1],
    s = {};
  (t = i.split('&')), (r = null), (n = null);
  for (var o in t) {
    var u = t[o].indexOf('=');
    u !== -1 && ((r = t[o].substr(0, u)), (n = t[o].substr(u + 1)), (s[r] = n));
  }
  return s;
}
/**
 * Determine whether to display the processing button
 * @param bpmStatus
 * @returns {*}
 */
export function showDealBtn(bpmStatus) {
  if (bpmStatus != '1' && bpmStatus != '3' && bpmStatus != '4') {
    return true;
  }
  return false;
}
/**
 * Convert numbers to uppercase
 * @param value
 * @returns {*}
 */
export function numToUpper(value) {
  if (value != '') {
    let unit = new Array('thousand', 'Hundred', 'pickup', '', 'thousand', 'Hundred', 'pickup', '', 'horn', 'point');
    const toDx = (n) => {
      switch (n) {
        case '0':
          return 'zero';
        case '1':
          return 'one';
        case '2':
          return 'two';
        case '3':
          return 'three';
        case '4':
          return 'Four';
        case '5':
          return 'Wu';
        case '6':
          return 'land';
        case '7':
          return 'seven';
        case '8':
          return 'eight';
        case '9':
          return 'Jiu';
      }
    };
    let lth = value.toString().length;
    // update-begin--author:liaozhiyang---date:20241202---for：【issues/7493】numToUpperMethod returns to resolve error
    value = new Big(value).times(100);
    // update-end--author:liaozhiyang---date:20241202---for：【issues/7493】numToUpperMethod returns to resolve error
    value += '';
    let length = value.length;
    if (lth <= 8) {
      let result = '';
      for (let i = 0; i < length; i++) {
        if (i == 2) {
          result = 'Yuan' + result;
        } else if (i == 6) {
          result = 'Ten thousand' + result;
        }
        if (value.charAt(length - i - 1) == 0) {
          if (i != 0 && i != 1) {
            if (result.charAt(0) != 'zero' && result.charAt(0) != 'Yuan' && result.charAt(0) != 'Ten thousand') {
              result = 'zero' + result;
            }
          }
          continue;
        }
        result = toDx(value.charAt(length - i - 1)) + unit[unit.length - i - 1] + result;
      }
      result += result.charAt(result.length - 1) == 'Yuan' ? 'all' : '';
      return result;
    } else {
      return null;
    }
  }
  return null;
}

//update-begin-author:taoyan date:2022-6-8 for:solve老ofvue2Dynamic import file syntax vite不supportof问题
const allModules = import.meta.glob('../views/**/*.vue');
export function importViewsFile(path): Promise<any> {
  if (path.startsWith('/')) {
    path = path.substring(1);
  }
  let page = '';
  if (path.endsWith('.vue')) {
    page = `../views/${path}`;
  } else {
    page = `../views/${path}.vue`;
  }
  return new Promise((resolve, reject) => {
    let flag = true;
    for (const path in allModules) {
      if (path == page) {
        flag = false;
        allModules[path]().then((mod) => {
          console.log(path, mod);
          resolve(mod);
        });
      }
    }
    if (flag) {
      reject('The file does not exist:' + page);
    }
  });
}
//update-end-author:taoyan date:2022-6-8 for:solve老ofvue2Dynamic import file syntax vite不supportof问题


/**
 * 跳转至积木报表of Preview page
 * @param url
 * @param id
 * @param token
 */
export function goJmReportViewPage(url, id, token) {
  // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalReplace withnew Function，solvebuildwarn
  // URLsupport{{ window.xxx }}placeholder variable
  url = url.replace(/{{([^}]+)?}}/g, (_s1, s2) => _eval(s2))
  // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalReplace withnew Function，solvebuildwarn
  if (url.includes('?')) {
    url += '&'
  } else {
    url += '?'
  }
  url += `id=${id}`
  url += `&token=${token}`
  window.open(url)
}

/**
 * Get随机颜色
 */
export function getRandomColor(index?) {

  const colors = [
    'rgb(100, 181, 246)',
    'rgb(77, 182, 172)',
    'rgb(255, 183, 77)',
    'rgb(229, 115, 115)',
    'rgb(149, 117, 205)',
    'rgb(161, 136, 127)',
    'rgb(144, 164, 174)',
    'rgb(77, 208, 225)',
    'rgb(129, 199, 132)',
    'rgb(255, 138, 101)',
    'rgb(133, 202, 205)',
    'rgb(167, 214, 118)',
    'rgb(254, 225, 89)',
    'rgb(251, 199, 142)',
    'rgb(239, 145, 139)',
    'rgb(169, 181, 255)',
    'rgb(231, 218, 202)',
    'rgb(252, 128, 58)',
    'rgb(254, 161, 172)',
    'rgb(194, 163, 205)',
  ];
  return index && index < 19 ? colors[index] : colors[Math.floor((Math.random()*(colors.length-1)))];
}

export function getRefPromise(componentRef) {
  return new Promise((resolve) => {
    (function next() {
      const ref = componentRef.value;
      if (ref) {
        resolve(ref);
      } else {
        setTimeout(() => {
          next();
        }, 100);
      }
    })();
  });
}

/**
 * 2023-09-04
 * liaozhiyang
 * usenew Functionreplaceeval
 */
export function _eval(str: string) {
 return new Function(`return ${str}`)();
}

/**
 * 2024-04-30
 * liaozhiyang
 * pass时间或者时间戳Get对应antdof年、moon、week、quarter。
 */
export function getWeekMonthQuarterYear(date) {
  // Get ISO week数of函数
  const getISOWeek = (date) => {
    const jan4 = new Date(date.getFullYear(), 0, 4);
    const oneDay = 86400000; // 一天ofmillisecond数
    return Math.ceil(((date - jan4.getTime()) / oneDay + jan4.getDay() + 1) / 7);
  };
  // Convert timestamp to date object
  const dateObj = new Date(date);
  // 计算week
  const week = getISOWeek(dateObj);
  // 计算moon
  const month = dateObj.getMonth() + 1; // moon份是从0startof，So add1
  // 计算quarter
  const quarter = Math.floor(dateObj.getMonth() / 3) + 1;
  // Calculation year
  const year = dateObj.getFullYear();
  return {
    year: `${year}`,
    month: `${year}-${month.toString().padStart(2, '0')}`,
    week: `${year}-${week}week`,
    quarter: `${year}-Q${quarter}`,
  };
}

/**
 * 2024-05-17
 * liaozhiyang
 * 设置挂载ofmodalYuan素有可能会有多个，需要找到对应of。
 */
export const setPopContainer = (node, selector) => {
  if (typeof selector === 'string') {
    const targetEles = Array.from(document.querySelectorAll(selector));
    if (targetEles.length > 1) {
      const retrospect = (node, elems) => {
        let ele = node.parentNode;
        while (ele) {
          const findParentNode = elems.find(item => item === ele);
          if (findParentNode) {
            ele = null;
            return findParentNode;
          } else {
            ele = ele.parentNode;
          }
        }
        return null;
      };
      const elem = retrospect(node, targetEles);
      if (elem) {
        return elem;
      } else {
        return document.querySelector(selector);
      }
    } else {
      return document.querySelector(selector);
    }
  } else {
    return selector;
  }
};

/**
 * 2024-06-14
 * liaozhiyang
 * According to control display conditions
 * label、value通use，title、val给权限管理useof
 */
export function useConditionFilter() {

  // 通use条件
  const commonConditionOptions = [
    {label: 'is empty', value: 'empty', val: 'EMPTY'},
    {label: '不is empty', value: 'not_empty', val: 'NOT_EMPTY'},
  ]

  // numerical value、date
  const numberConditionOptions = [
    { label: 'equal', value: 'eq', val: '=' },
    { label: 'exist...middle', value: 'in', val: 'IN', title: 'Include' },
    { label: '不equal', value: 'ne', val: '!=' },
    { label: 'greater than', value: 'gt', val: '>' },
    { label: 'greater thanequal', value: 'ge', val: '>=' },
    { label: 'less than', value: 'lt', val: '<' },
    { label: 'less thanequal', value: 'le', val: '<=' },
    ...commonConditionOptions,
  ];

  // text、password、多行text、富text、markdown
  const inputConditionOptions = [
    { label: 'equal', value: 'eq', val: '=' },
    { label: 'Vague', value: 'like', val: 'LIKE' },
    { label: 'by..start', value: 'right_like', title: '右Vague', val: 'RIGHT_LIKE' },
    { label: 'by..ending', value: 'left_like', title: '左Vague', val: 'LEFT_LIKE' },
    { label: 'exist...middle', value: 'in', val: 'IN', title: 'Include' },
    { label: '不equal', value: 'ne', val: '!=' },
    ...commonConditionOptions,
  ];

  // drop down、Single choice、Multiple choice、switch、use户、department、associated records、Provinces and municipalities、popup、popupDict、drop downMultiple choice、drop down搜索、point类字典、Custom tree
  const selectConditionOptions = [
    { label: 'equal', value: 'eq', val: '=' },
    { label: 'exist...middle', value: 'in', val: 'IN', title: 'Include' },
    { label: '不equal', value: 'ne', val: '!=' },
    ...commonConditionOptions,
  ];

  const def = [
    { label: 'equal', value: 'eq', val: '=' },
    { label: 'Vague', value: 'like', val: 'LIKE' },
    { label: 'by..start', value: 'right_like', title: '右Vague', val: 'RIGHT_LIKE' },
    { label: 'by..ending', value: 'left_like', title: '左Vague', val: 'LEFT_LIKE' },
    { label: 'exist...middle', value: 'in', val: 'IN', title: 'Include' },
    { label: '不equal', value: 'ne', val: '!=' },
    { label: 'greater than', value: 'gt', val: '>' },
    { label: 'greater thanequal', value: 'ge', val: '>=' },
    { label: 'less than', value: 'lt', val: '<' },
    { label: 'less thanequal', value: 'le', val: '<=' },
    ...commonConditionOptions,
  ];

  const filterCondition = (data) => {
    if (data.view == 'text' && data.fieldType == 'number') {
      data.view = 'number';
    }
    switch (data.view) {
      case 'file':
      case 'image':
      case 'password':
        return commonConditionOptions;
      case 'text':
      case 'textarea':
      case 'umeditor':
      case 'markdown':
      case 'pca':
      case 'popup':
        return inputConditionOptions;
      case 'list':
      case 'radio':
      case 'checkbox':
      case 'switch':
      case 'sel_user':
      case 'sel_depart':
      case 'link_table':
      case 'popup_dict':
      case 'list_multi':
      case 'sel_search':
      case 'cat_tree':
      case 'sel_tree':
        return selectConditionOptions;
      case 'date':
      // number是虚拟of
      case 'number':
        return numberConditionOptions;
      default:
        return def;
    }
  };
  return { filterCondition };
}
// Geturlmiddleof参数
export const getUrlParams = (url) => {
  const result = {
    url: '',
    params: {},
  };
  const list = url.split('?');
  result.url = list[0];
  const params = list[1];
  if (params) {
    const list = params.split('&');
    list.forEach((ele) => {
      const dic = ele.split('=');
      const label = dic[0];
      result.params[label] = dic[1];
    });
  }
  return result;
};

/* 20250325
 * liaozhiyang
 * point割url字符成array
 * 【issues/7990】图片参数middleInclude逗Number会错误of识别成多张图
 * */
export const split = (str) => {
  if (isString(str)) {
    const text = str.trim();
    if (text.startsWith('http')) {
      const parts = str.split(',');
      const urls: any = [];
      let currentUrl = '';
      for (const part of parts) {
        if (part.startsWith('http://') || part.startsWith('https://')) {
          // 如果遇到新ofURLbeginning，save currentURL并start新ofURL
          if (currentUrl) {
            urls.push(currentUrl);
          }
          currentUrl = part;
        } else {
          // otherwise，is currentURLof一部point（Such as parameters）
          currentUrl += ',' + part;
        }
      }
      // add lastURL
      if (currentUrl) {
        urls.push(currentUrl);
      }
      return urls;
    } else {
      return str.split(',');
    }
  }
  return str;
};
