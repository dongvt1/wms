/**
 * Dictionary util
 * author: scott
 * date: 20190109
 */

import { ajaxGetDictItems, getDictItemsByCode } from './index';

/**
 * Get dictionary array
 * 【Currently only used by form designer page】
 * @param dictCode Dictionary Code
 * @param isTransformResponse Whether to transform return result
 * @return List<Map>
 */
export async function initDictOptions(dictCode, isTransformResponse = true) {
  if (!dictCode) {
    return 'Dictionary Code cannot be empty!';
  }
  // Priority read dictionary configuration from cache
  if (getDictItemsByCode(dictCode)) {
    let res = {};
    res.result = getDictItemsByCode(dictCode);
    res.success = true;
    if (isTransformResponse) {
      return res.result;
    } else {
      return res;
    }
  }
  // Get dictionary array
  return await ajaxGetDictItems(dictCode, {}, { isTransformResponse });
}

/**
 * Dictionary value replace text common method
 * @param dictOptions Dictionary array
 * @param text Dictionary value
 * @return String
 */
export function filterDictText(dictOptions, text) {
  // --update-begin----author:sunjianlei---date:20200323------for: Dictionary translation text allows comma separation ---
  if (text != null && Array.isArray(dictOptions)) {
    let result = [];
    // Allow multiple comma separators, allow passing array objects
    let splitText;
    if (Array.isArray(text)) {
      splitText = text;
    } else {
      splitText = text.toString().trim().split(',');
    }
    for (let txt of splitText) {
      let dictText = txt;
      for (let dictItem of dictOptions) {
        // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-469】Compatible with data null value to prevent errors
        if (dictItem == null) continue;
        if (dictItem.value == null) continue;
        // update-end--author:liaozhiyang---date:20240524---for：【TV360X-469】Compatible with data null value to prevent errors
        if (txt.toString() === dictItem.value.toString()) {
          dictText = dictItem.text || dictItem.title || dictItem.label;
          break;
        }
      }
      result.push(dictText);
    }
    return result.join(',');
  }
  return text;
  // --update-end----author:sunjianlei---date:20200323------for: Dictionary translation text allows comma separation ---
}

/**
 * Dictionary value replace text common method (multi-select)
 * @param dictOptions Dictionary array
 * @param text Dictionary value
 * @return String
 */
export function filterMultiDictText(dictOptions, text) {
  //js "!text" considers 0 as empty, so do preprocessing
  if (text === 0 || text === '0') {
    if (dictOptions) {
      for (let dictItem of dictOptions) {
        if (text == dictItem.value) {
          return dictItem.text;
        }
      }
    }
  }

  if (!text || text == 'undefined' || text == 'null' || !dictOptions || dictOptions.length == 0) {
    return '';
  }
  let re = '';
  text = text.toString();
  let arr = text.split(',');
  dictOptions.forEach(function (option) {
    if (option) {
      for (let i = 0; i < arr.length; i++) {
        if (arr[i] === option.value) {
          re += option.text + ',';
          break;
        }
      }
    }
  });
  if (re == '') {
    return text;
  }
  return re.substring(0, re.length - 1);
}

/**
 * Translate field value corresponding text
 * @param children
 * @returns string
 */
export function filterDictTextByCache(dictCode, key) {
  if (key == null || key.length == 0) {
    return;
  }
  if (!dictCode) {
    return 'Dictionary Code cannot be empty!';
  }
  // Priority read dictionary configuration from cache
  if (getDictItemsByCode(dictCode)) {
    let item = getDictItemsByCode(dictCode).filter((t) => t['value'] == key);
    if (item && item.length > 0) {
      return item[0]['text'];
    }
  }
}

/** passcodeGet dictionary array */
export async function getDictItems(dictCode, params) {
  // update-begin--author:liaozhiyang---date:20230809---for：【issues/668】JDictSelectUtil dictionary tool class getDictItems method error
  // Priority read dictionary configuration from cache
  if (getDictItemsByCode(dictCode)) {
    let desformDictItems = getDictItemsByCode(dictCode).map((item) => ({
      ...item,
      label: item.text,
    }));
    return Promise.resolve(desformDictItems);
  }

  // If not in cache, request backend
  return await ajaxGetDictItems(dictCode, params)
    .then((result) => {
      if (result.length) {
        let res = result.map((item) => ({ ...item, label: item.text }));
        console.log('------- Got dictionary from DB-------dictCode : ', dictCode, res);
        return Promise.resolve(res);
      } else {
        console.error('getDictItems error: : ', res);
        return Promise.resolve([]);
      }
    })
    .catch((res) => {
      console.error('getDictItems error: ', res);
      return Promise.resolve([]);
    });
  // update-end--author:liaozhiyang---date:20230809---for：【issues/668】JDictSelectUtil dictionary tool class getDictItems method error
}
