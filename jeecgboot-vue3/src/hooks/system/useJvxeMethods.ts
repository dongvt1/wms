import { defHttp } from '/@/utils/http/axios';
import { ref, unref } from 'vue';
import { VALIDATE_FAILED, validateFormModelAndTables } from '/@/utils/common/vxeUtils';

export function useJvxeMethod(requestAddOrEdit, classifyIntoFormData, tableRefs, activeKey, refKeys, validateSubForm?) {
  const formRef = ref();
  /** Query atabdata */
  function requestSubTableData(url, params, tab, success) {
    tab.loading = true;
    defHttp
      .get({ url, params }, { isTransformResponse: false })
      .then((res) => {
        let { result } = res;
        if (res.success && result) {
          if (Array.isArray(result)) {
            tab.dataSource = result;
          } else if (Array.isArray(result.records)) {
            tab.dataSource = result.records;
          }
        }
        typeof success === 'function' ? success(res) : '';
      })
      .finally(() => {
        tab.loading = false;
      });
  }

  /* --- handle event --- */

  /** ATab 选项卡切换event */
  function handleChangeTabs(key) {
    // automatic resetscrollTopstate，Prevent white screen from appearing
    tableRefs[key]?.value?.resetScrollTop(0);
  }

  /** get alleditableTableExample*/
  function getAllTable() {
    let values = Object.values(tableRefs);
    return Promise.all(values);
  }
  /** 确定按钮点击event */
  function handleSubmit() {
    /** Trigger form validation */
    getAllTable()
      .then((tables) => {
        let values = formRef.value.getFieldsValue();
        return validateFormModelAndTables(formRef.value.validate, values, tables, formRef.value.getProps, false);
      })
      .then((allValues) => {
        /** Verify all child tables one-to-one at once */
        return validateSubForm && typeof validateSubForm === 'function' ? validateSubForm(allValues) : validateAllSubOne(allValues);
      })
      .then((allValues) => {
        if (typeof classifyIntoFormData !== 'function') {
          throw throwNotFunction('classifyIntoFormData');
        }
        let formData = classifyIntoFormData(allValues);
        // Make a request
        return requestAddOrEdit(formData);
      })
      .catch((e) => {
        if (e.error === VALIDATE_FAILED) {
          // If there are subtables that fail form validation，will automatically jump to where it istab
          //update-begin-author:taoyan date:2022-11-22 for: VUEN-2866【code generation】Tabstyle When one-to-many subtable verification fails，Click submit and the form is blank，The process additional page also has this problem
          if(e.paneKey){
            activeKey.value = e.paneKey
          }else{
            //update-begin-author:liusq date:2024-06-12 for: TV360X-478 one to manytab，When the verification fails，tabNo jump
            activeKey.value = e.subIndex == null ? (e.index == null ? unref(activeKey) : refKeys.value[e.index]) : Object.keys(tableRefs)[e.subIndex];
            //update-end-author:liusq date:2024-06-12  for: TV360X-478 one to manytab，When the verification fails，tabNo jump
          }
          //update-end-author:taoyan date:2022-11-22 for: VUEN-2866【code generation】Tabstyle When one-to-many subtable verification fails，Click submit and the form is blank，The process additional page also has this problem
          //update-begin---author:wangshuai---date:2024-06-17---for:【TV360X-1064】Items that failed the rolling verification of the non-native submission form---
          if (e?.errorFields) {
            const firstField = e.errorFields[0];
            if (firstField) {
              formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'end' });
            }
          }
          return Promise.reject(e?.errorFields);
          //update-end---author:wangshuai---date:2024-06-17---for:【TV360X-1064】Items that failed the rolling verification of the non-native submission form---
        } else {
          console.error(e);
        }
      });
  }
  //Validate all subforms
  function validateAllSubOne(allValues) {
    return new Promise((resolve) => {
      resolve(allValues);
    });
  }
  /* --- throw --- */

  /** not a function */
  function throwNotFunction(name) {
    return `${name} undefined or not a function`;
  }

  /** not a array */
  function throwNotArray(name) {
    return `${name} undefined or not an array`;
  }
  return [handleChangeTabs, handleSubmit, requestSubTableData, formRef];
}

//update-begin-author:taoyan date:2022-6-16 for: code generation-For native forms
/**
 * Validate multiple forms and subformstable，for nativeantd-vueform
 * @param activeKey Subform/vxe-table locationtabsof activeKey
 * @param refMap Subform/vxe-table对应ofrefobject mapstructure
 * Example：
 * useValidateAntFormAndTable(activeKey, {
 *   'tableA': tableARef,
 *   'formB': formBRef
 * })
 */
export function useValidateAntFormAndTable(activeKey, refMap) {
  /**
   * Get all child table data
   */
  async function getSubFormAndTableData() {
    let formData = {};
    let all = Object.keys(refMap);
    let key = '';
    for (let i = 0; i < all.length; i++) {
      key = all[i];
      let instance = refMap[key].value;
      if (instance.isForm) {
        let subFormData = await validateFormAndGetData(instance, key);
        if (subFormData) {
          formData[key + 'List'] = [subFormData];
        }
      } else {
        let arr = await validateTableAndGetData(instance, key);
        if (arr && arr.length > 0) {
          formData[key + 'List'] = arr;
        }
      }
    }
    return formData;
  }

  /**
   * For converting data 如果有数组转成逗号分割of格式
   * @param data
   */
  function transformData(data) {
    if (data) {
      Object.keys(data).map((k) => {
        if (data[k] instanceof Array) {
          data[k] = data[k].join(',');
        }
      });
    }
    return data;
  }

  /**
   * Subtabletable
   * @param instance
   * @param key
   */
  async function validateTableAndGetData(instance, key) {
    const errors = await instance.validateTable();
    if (!errors) {
      return instance.getTableData();
    } else {
      activeKey.value = key;
      // automatic resetscrollTopstate，Prevent white screen from appearing
      instance.resetScrollTop(0);
      return Promise.reject(1);
    }
  }

  /**
   * Subform
   * @param instance
   * @param key
   */
  async function validateFormAndGetData(instance, key) {
    try {
      let data = await instance.getFormData();
      transformData(data);
      return data;
    } catch (e) {
      activeKey.value = key;
      return Promise.reject(e);
    }
  }

  return {
    getSubFormAndTableData,
    transformData,
  };
}
//update-end-author:taoyan date:2022-6-16 for: code generation-For native forms
