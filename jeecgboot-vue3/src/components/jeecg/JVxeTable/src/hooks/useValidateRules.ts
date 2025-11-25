import { VxeTablePropTypes } from 'vxe-table';
import { isArray } from '/@/utils/is';
import { HandleArgs } from './useColumns';
import { replaceProps } from '../utils/enhancedUtils';

export function useValidateRules(args: HandleArgs) {
  const { data } = args;
  const col = args.col!;
  let rules: VxeTablePropTypes.EditRules[] = [];
  if (isArray(col.validateRules)) {
    for (let rule of col.validateRules) {
      let replace = {
        message: replaceProps(col, rule.message),
      };
      if (rule.unique || rule.pattern === 'only') {
        // unique validator
        rule.validator = uniqueValidator(args);
      } else if (rule.pattern) {
        // Not empty
        if (rule.pattern === fooPatterns[0].value) {
          rule.required = true;
          delete rule.pattern;
        } else {
          // compatibleOnlineSpecial rules for forms
          for (let foo of fooPatterns) {
            if (foo.value === rule.pattern) {
              rule.pattern = foo.pattern;
              break;
            }
          }
        }
      } else if (typeof rule.handler === 'function') {
        // Custom function verification
        rule.validator = handlerConvertToValidator;
      }
      rules.push(Object.assign({}, rule, replace));
    }
  }
  data.innerEditRules[col.key] = rules;
}

/** unique validator */
function uniqueValidator({ methods }: HandleArgs) {
  return function (event) {
    const { cellValue, column, rule } = event;
    // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-299】JVxetableThe only check in the component filters out empty strings
    if (cellValue == '') return Promise.resolve();
    // update-end--author:liaozhiyang---date:20240522---for：【TV360X-299】JVxetableThe only check in the component filters out empty strings
    let tableData = methods.getTableData();
    let findCount = 0;
    for (let rowData of tableData) {
      if (rowData[column.params.key] === cellValue) {
        if (++findCount >= 2) {
          return Promise.reject(new Error(rule.message));
        }
      }
    }
    return Promise.resolve();
  };
}

/** Old versionhandlerConvert to new versionValidator */
function handlerConvertToValidator(event) {
  const { column, rule } = event;
  return new Promise((resolve, reject) => {
    rule.handler(event, (flag, msg) => {
      let message = rule.message;
      if (typeof msg === 'string') {
        message = replaceProps(column.params, msg);
      }
      if (flag == null) {
        resolve(message);
      } else if (!!flag) {
        resolve(message);
      } else {
        reject(new Error(message));
      }
    });
  });
}

// compatible online rules
const fooPatterns = [
  { title: 'Not empty', value: '*', pattern: /^.+$/ },
  { title: '6arrive16digits', value: 'n6-16', pattern: /^\d{6,16}$/ },
  { title: '6arrive16any character', value: '*6-16', pattern: /^.{6,16}$/ },
  { title: '6arrive18letters', value: 's6-18', pattern: /^[a-z|A-Z]{6,18}$/ },
  //update-begin-author:taoyan date:2022-6-1 for: VUEN-1160 to many subtables，URL verification is incorrect
  {
    title: 'URL',
    value: 'url',
    pattern: /^((ht|f)tps?):\/\/[\w\-]+(\.[\w\-]+)+([\w\-.,@?^=%&:\/~+#]*[\w\-@?^=%&\/~+#])?$/,
  },
  //update-end-author:taoyan date:2022-6-1 for: VUEN-1160 to many subtables，URL verification is incorrect
  // update-begin--author:liaozhiyang---date:20240527---for：【TV360X-466】The email address is consistent with the first pair of verification rules
  { title: 'e-mail', value: 'e', pattern: /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/ },
  // update-end--author:liaozhiyang---date:20240527---for：【TV360X-466】The email address is consistent with the first pair of verification rules
  { title: 'phone number', value: 'm', pattern: /^1[3456789]\d{9}$/ },
  { title: 'postal code', value: 'p', pattern: /^\d{6}$/ },
  { title: 'letter', value: 's', pattern: /^[A-Z|a-z]+$/ },
  { title: 'number', value: 'n', pattern: /^-?\d+(\.?\d+|\d?)$/ },
  { title: 'integer', value: 'z', pattern: /^-?\d+$/ },
  {
    title: 'Amount',
    value: 'money',
    pattern: /^(([1-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,5}))$/,
  },
];
