import { watch } from 'vue';
import { isFunction, isPromise, isArray } from '/@/utils/is';
import { JVxeColumn, JVxeDataProps, JVxeTableProps, JVxeLinkageConfig } from '../types';

/**
 * Multi-level linkage
 */
export function useLinkage(props: JVxeTableProps, data: JVxeDataProps, methods) {
  // 整理Multi-level linkage配置
  watch(
    () => props.linkageConfig,
    (linkageConfig: JVxeLinkageConfig[]) => {
      data.innerLinkageConfig.clear();
      if (isArray(linkageConfig) && linkageConfig.length > 0) {
        linkageConfig.forEach((config) => {
          let keys = getLinkageKeys(config.key, []);
          // Multiplekeyshare a，Reference address
          let configItem = {
            ...config,
            keys,
            optionsMap: new Map(),
          };
          keys.forEach((k) => data.innerLinkageConfig.set(k, configItem));
        });
      }
    },
    { immediate: true }
  );

  // Get linkedkeyorder
  function getLinkageKeys(key: string, keys: string[]): string[] {
    let col = props.columns?.find((col: JVxeColumn) => col.key === key) as JVxeColumn;
    if (col) {
      keys.push(col.key);
      // Looking for subordinates
      if (col.linkageKey) {
        return getLinkageKeys(col.linkageKey, keys);
      }
    }
    return keys;
  }

  // Processing linkage echo data
  function handleLinkageBackData(row) {
    if (data.innerLinkageConfig.size > 0) {
      for (let configItem of data.innerLinkageConfig.values()) {
        autoSetLinkageOptionsByData(row, '', configItem, 0);
      }
    }
  }

  /** 【Multi-level linkage】Get sibling linkage drop-down options */
  function getLinkageOptionsSibling(row, col, config, request) {
    // If the current column is not a top-level column
    let key = '';
    if (col.key !== config.key) {
      // Just find the linkage parent column
      let idx = config.keys.findIndex((k) => col.key === k);
      let parentKey = config.keys[idx - 1];
      key = row[parentKey];
      // If no data is selected in the linked upper-level column，Just return an empty array directly
      if (key === '' || key == null) {
        return [];
      }
    } else {
      key = 'root';
    }
    let options = config.optionsMap.get(key);
    if (!Array.isArray(options)) {
      if (request) {
        let parent = key === 'root' ? '' : key;
        return getLinkageOptionsAsync(config, parent);
      } else {
        options = [];
      }
    }
    return options;
  }

  /** 【Multi-level linkage】Get linkage drop-down options（asynchronous） */
  function getLinkageOptionsAsync(config, parent) {
    return new Promise((resolve) => {
      let key = parent ? parent : 'root';
      let options;
      if (config.optionsMap.has(key)) {
        options = config.optionsMap.get(key);
        if (isPromise(options)) {
          options.then((opt) => {
            config.optionsMap.set(key, opt);
            resolve(opt);
          });
        } else {
          resolve(options);
        }
      } else if (isFunction(config.requestData)) {
        // callrequestDatamethod，By passing inparentto get children
        // noinspection JSVoidFunctionReturnValueUsed,TypeScriptValidateJSTypes
        let promise = config.requestData(parent);
        config.optionsMap.set(key, promise);
        promise.then((opt) => {
          config.optionsMap.set(key, opt);
          resolve(opt);
        });
      } else {
        resolve([]);
      }
    });
  }

  // 【Multi-level linkage】 Used to echo data，autofill optionsMap
  function autoSetLinkageOptionsByData(data, parent, config, level) {
    if (level === 0) {
      getLinkageOptionsAsync(config, '');
    } else {
      getLinkageOptionsAsync(config, parent);
    }
    if (config.keys.length - 1 > level) {
      let value = data[config.keys[level]];
      if (value) {
        autoSetLinkageOptionsByData(data, value, config, level + 1);
      }
    }
  }

  // 【Multi-level linkage】Linkage componentschangehour，Clear subordinate components
  function handleLinkageSelectChange(row, col, config, value) {
    if (col.linkageKey) {
      getLinkageOptionsAsync(config, value);
      let idx = config.keys.findIndex((k) => k === col.key);
      let values = {};
      for (let i = idx; i < config.keys.length; i++) {
        values[config.keys[i]] = '';
      }
      // Clear the data in the last few columns
      methods.setValues([{ rowKey: row.id, values }]);
    }
  }

  return {
    getLinkageOptionsAsync,
    getLinkageOptionsSibling,
    handleLinkageSelectChange,
    handleLinkageBackData,
  };
}
