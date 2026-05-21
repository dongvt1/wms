import { getValueType } from '/@/utils';

export const VALIDATE_FAILED = Symbol();
/**
 * Validate the main form and all subforms at once(new version)
 * @param form main form form object
 * @param cases receive an array，Each item is oneJEditableTableExample
 * @returns {Promise<any>}
 */
export async function validateFormModelAndTables(validate, formData, cases, props, autoJumpTab?) {
  if (!(validate && typeof validate === 'function')) {
    throw `validate The parameter requires a method，But what comes in is${typeof validate}`;
  }
  let dataMap = {};
  let values = await new Promise((resolve, reject) => {
    // Validate main form
    validate()
      .then(() => {
        //update-begin---author:wangshuai ---date:20220507  for：[VUEN-912]One-to-many user component（all styles，Single table and tree are no problem）Save error------------
        for (let data in formData) {
          //If the data is an array
          if (formData[data] instanceof Array) {
            let valueType = getValueType(props, data);
            //If it is a string type, it needs to be converted into a comma-separated string.
            if (valueType === 'string') {
              formData[data] = formData[data].join(',');
            }
          }
        }
        //update-end---author:wangshuai ---date:20220507  for：[VUEN-912]One-to-many user component（all styles，Single table and tree are no problem）Save error--------------
        resolve(formData);
      })
      //update-begin---author:wangshuai---date:2024-06-17---for:【TV360X-1064】Items that failed the rolling verification of the non-native submission form---
      .catch(({ errorFields }) => {
        reject({ error: VALIDATE_FAILED, index: 0, errorFields: errorFields });
      //update-end---author:wangshuai---date:2024-06-17---for:【TV360X-1064】Items that failed the rolling verification of the non-native submission form---
      });
  });
  Object.assign(dataMap, { formValue: values });
  // Form that validates all child tables
  let subData = await validateTables(cases, autoJumpTab);
  // Merge final data
  dataMap = Object.assign(dataMap, { tablesValue: subData });
  return dataMap;
}
/**
 * Validate and get all values ​​of one or more tables
 * @param cases receive an array，Each item is oneJEditableTableExample
 * @param autoJumpTab Whether to automatically jump to the error reportingtab
 */
export function validateTables(cases, autoJumpTab = true) {
  if (!(cases instanceof Array)) {
    throw `'validateTables'Functional'cases'The parameter requires an array，But what comes in is${typeof cases}`;
  }
  return new Promise((resolve, reject) => {
    let tablesData: any = [];
    let index = 0;
    if (!cases || cases.length === 0) {
      resolve(tablesData);
    }
    (function next() {
      let vm = cases[index];
      vm.value.validateTable().then((errMap) => {
        // Verification passed
        if (!errMap) {
          tablesData[index] = { tableData: vm.value.getTableData() };
          // Determine whether the verification is completed，Complete return success，Otherwise, continue to the next step of verification
          if (++index === cases.length) {
            resolve(tablesData);
          } else next();
        } else {
          // try to gettabKey，if inATabAvailable within the component
          let paneKey;
          let tabPane = getVmParentByName(vm.value, 'ATabPane');
          if (tabPane) {
            paneKey = tabPane.$.vnode.key;
            // Automatically jump to this form
            if (autoJumpTab) {
              let tabs = getVmParentByName(tabPane, 'Tabs');
              tabs && tabs.setActiveKey && tabs.setActiveKey(paneKey);
            }
          }
          // An unverified form appears，No further verification will be performed，Directly return failure
          //update-begin-author:liusq date:2024-06-12 for: TV360X-478 one to manytab，When the verification fails，tabNo jump
          reject({ error: VALIDATE_FAILED, index, paneKey, errMap, subIndex: index });
          //update-end-author:liusq date:2024-06-12 for: TV360X-478 one to manytab，When the verification fails，tabNo jump
        }
      });
    })();
  });
}

export function getVmParentByName(vm, name) {
  let parent = vm.$parent;
  if (parent && parent.$options) {
    if (parent.$options.name === name) {
      return parent;
    } else {
      let res = getVmParentByName(parent, name);
      if (res) {
        return res;
      }
    }
  }
  return null;
}
