import { computed, ref, watch, defineComponent, h } from 'vue';
import { cloneDeep, debounce } from 'lodash-es';
import { defHttp } from '/@/utils/http/axios';
import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
import { ajaxGetDictItems, getDictItemsByCode } from '/@/utils/dict';
import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
import { dispatchEvent } from '/@/components/jeecg/JVxeTable/utils';
import { useResolveComponent as rc } from '/@/components/jeecg/JVxeTable/hooks';
import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
import { useMessage } from '/@/hooks/web/useMessage';

/** value - label map，Prevent duplicate queries（Refresh clear cache） */
const LabelMap = new Map<string, any>();
// askid
let requestId = 0;

/** display component，Comes with translation */
export const DictSearchSpanCell = defineComponent({
  name: 'JVxeSelectSearchSpanCell',
  props: useJVxeCompProps(),
  setup(props: JVxeComponent.Props) {
    const { innerOptions, innerSelectValue, innerValue } = useSelectDictSearch(props);
    return () => {
      return h('span', {}, [filterDictText(innerOptions.value, innerSelectValue.value || innerValue.value)]);
    };
  },
});

// Input selection component
export const DictSearchInputCell = defineComponent({
  name: 'JVxeSelectSearchInputCell',
  props: useJVxeCompProps(),
  setup(props: JVxeComponent.Props) {
    const { createMessage } = useMessage();
    const { dict, loading, isAsync, options, innerOptions, originColumn, cellProps, innerSelectValue, handleChangeCommon } =
      useSelectDictSearch(props);
    const hasRequest = ref(false);
    // Prompt message
    const tipsContent = computed(() => {
      return originColumn.value.tipsContent || 'Please enter search content';
    });
    // filter function
    const filterOption = computed(() => {
      if (isAsync.value) {
        //【jeecgboot-vue3/issues/I5QRT8】JVxeTypes.selectDictSearch syncquestion
        return ()=>true;
      }
      return (input, option) => option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    });

    /** Load data */
    const loadData = debounce((value) => {
      const currentRequestId = ++requestId;
      loading.value = true;
      innerOptions.value = [];
      if (value == null || value.trim() === '') {
        loading.value = false;
        hasRequest.value = false;
        return;
      }
      // dictionarycodeFormat：table,text,code
      hasRequest.value = true;
      loadDictByKeyword(dict.value, value)
        .then((res) => {
          if (currentRequestId !== requestId) {
            return;
          }
          let { success, result, message } = res;
          if (success) {
            innerOptions.value = result;
            result.forEach((item) => {
              LabelMap.set(item.value, [item]);
            });
          } else {
            createMessage.warning(message || 'Query failed');
          }
        })
        .finally(() => {
          loading.value = false;
        });
    }, 300);

    function handleChange(selectedValue) {
      innerSelectValue.value = selectedValue;
      handleChangeCommon(innerSelectValue.value);
    }

    function handleSearch(value) {
      if (isAsync.value) {
        // Loading should also be enabled while typing，becauseloadDataAdded anti-shake，So there will be800msusers’ subjective perception of lag time
        loading.value = true;
        if (innerOptions.value.length > 0) {
          innerOptions.value = [];
        }
        loadData(value);
      }
    }

    function renderOptionItem() {
      let optionItems: any[] = [];
      options.value.forEach(({ value, text, label, title, disabled }) => {
        optionItems.push(
          h(
            rc('a-select-option'),
            {
              key: value,
              value: value,
              disabled: disabled,
            },
            {
              default: () => text || label || title,
            }
          )
        );
      });
      return optionItems;
    }

    return () => {
      return h(
        rc('a-select'),
        {
          ...cellProps.value,
          value: innerSelectValue.value,
          filterOption: filterOption.value,
          showSearch: true,
          allowClear: true,
          autofocus: true,
          defaultOpen: true,
          style: 'width: 100%',
          onSearch: handleSearch,
          onChange: handleChange,
        },
        {
          default: () => renderOptionItem(),
          notFoundContent: () => {
            if (loading.value) {
              return h(rc('a-spin'), { size: 'small' });
            } else if (hasRequest.value) {
              return h('div', 'No data found');
            } else {
              return h('div', [tipsContent.value]);
            }
          },
        }
      );
    };
  },
  // 【Component enhancement】See notes for details：JVxeComponent.Enhanced
  enhanced: {
    aopEvents: {
      editActived({ $event }) {
        dispatchEvent({
          $event,
          props: this.props,
          className: '.ant-select .ant-select-selection-search-input',
          isClick: false,
          handler: (el) => el.focus(),
        });
      },
    },
  } as JVxeComponent.EnhancedPartial,
});

function useSelectDictSearch(props) {
  const setup = useJVxeComponent(props);
  const { innerValue, originColumn } = setup;

  // Loading status
  const loading = ref(false);
  // Internal selection value
  const innerSelectValue = ref(null);
  // internal options
  const innerOptions = ref<any[]>([]);

  const dict = computed(() => originColumn.value.dict);
  // Whether it is asynchronous mode
  const isAsync = computed(() => {
    let isAsync = originColumn.value.async;
    return isAsync != null && isAsync !== '' ? !!isAsync : true;
  });
  const options = computed(() => {
    if (isAsync.value) {
      return innerOptions.value;
    } else {
      return originColumn.value.options || [];
    }
  });

  /** Public property monitoring */
  watch(
    innerValue,
    (value: string) => {
      if (value == null || value === '') {
        innerSelectValue.value = null;
      } else {
        loadDataByValue(value);
      }
    },
    { immediate: true }
  );
  watch(dict, () => loadDataByDict());

  // according to value Query data，used to echo
  async function loadDataByValue(value) {
    if (isAsync.value) {
      if (innerSelectValue.value !== value) {
        if (LabelMap.has(value)) {
          innerOptions.value = cloneDeep(LabelMap.get(value));
        } else {
          let result = await loadDictItem(dict.value, value);
          if (result && result.length > 0) {
            innerOptions.value = [{ value: value, text: result[0] }];
            LabelMap.set(value, cloneDeep(innerOptions.value));
          }
        }
      }
    }
    innerSelectValue.value = (value || '').toString();
  }

  // 初始化dictionary
  async function loadDataByDict() {
    if (!isAsync.value) {
      // 如果dictionary项集合有数据
      if (!originColumn.value.options || originColumn.value.options.length === 0) {
        // according todictionaryCode, 初始化dictionary数组
        let dictStr = '';
        if (dict.value) {
          let arr = dict.value.split(',');
          if (arr[0].indexOf('where') > 0) {
            let tbInfo = arr[0].split('where');
            dictStr = tbInfo[0].trim() + ',' + arr[1] + ',' + arr[2] + ',' + encodeURIComponent(tbInfo[1]);
          } else {
            dictStr = dict.value;
          }
          if (dict.value.indexOf(',') === -1) {
            //优先从缓存中读取dictionary配置
            let cache = getDictItemsByCode(dict.value);
            if (cache) {
              innerOptions.value = cache;
              return;
            }
          }
          let { success, result } = await ajaxGetDictItems(dictStr, null);
          if (success) {
            innerOptions.value = result;
          }
        }
      }
    }
  }

  return {
    ...setup,
    loading,
    innerOptions,
    innerSelectValue,
    dict,
    isAsync,
    options,
  };
}

/** 获取dictionary项 */
function loadDictItem(dict: string, key: string) {
  return defHttp.get({
    url: `/sys/dict/loadDictItem/${dict}`,
    params: {
      key: key,
    },
  });
}

/** according to关键字获取dictionary项（search） */
function loadDictByKeyword(dict: string, keyword: string) {
  return defHttp.get(
    {
      url: `/sys/dict/loadDict/${dict}`,
      params: {
        keyword: keyword,
      },
    },
    {
      isTransformResponse: false,
    }
  );
}
