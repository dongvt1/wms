<template>
  <!--Asynchronous dictionary drop-down search elements-->
  <a-select
    v-if="async"
    v-bind="attrs"
    v-model:value="selectedAsyncValue"
    showSearch
    labelInValue
    allowClear
    :getPopupContainer="getParentContainer"
    :placeholder="placeholder"
    :filterOption="isDictTable ? false : filterOption"
    :notFoundContent="loading ? undefined : null"
    @focus="handleAsyncFocus"
    @search="loadData"
    @change="handleAsyncChange"
    @popupScroll="handlePopupScroll"
    :mode="multiple?'multiple':''"
    @select="handleSelect"
    @deselect="handleDeSelect"
  >
    <template #notFoundContent>
      <a-spin size="small" />
    </template>
    <a-select-option v-for="d in options" :key="d?.value" :value="d?.value">{{ d?.text }}</a-select-option>
  </a-select>
  <!--Dictionary drop-down search-->
  <a-select
    v-else
    v-model:value="selectedValue"
    v-bind="attrs"
    showSearch
    :getPopupContainer="getParentContainer"
    :placeholder="placeholder"
    :filterOption="filterOption"
    :notFoundContent="loading ? undefined : null"
    :dropdownAlign="{overflow: {adjustY: adjustY }}"
    @change="handleChange"
    :mode="multiple?'multiple':''"
    @select="handleSelect"
    @deselect="handleDeSelect"
  >
    <template #notFoundContent>
      <a-spin v-if="loading" size="small" />
    </template>
    <a-select-option v-for="d in options" :key="d?.value" :value="d?.value">{{ d?.text }}</a-select-option>
  </a-select>
</template>

<script lang="ts">
  import { useDebounceFn } from '@vueuse/core';
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { initDictOptions } from '/@/utils/dict/index';
  import { defHttp } from '/@/utils/http/axios';
  import { debounce } from 'lodash-es';
  import { setPopContainer } from '/@/utils';
  import { isObject } from '/@/utils/is';

  export default defineComponent({
    name: 'JSearchSelect',
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.string, propTypes.number]),
      dict: propTypes.string,
      dictOptions: {
        type: Array,
        default: () => [],
      },
      async: propTypes.bool.def(false),
      placeholder: propTypes.string,
      popContainer: propTypes.string,
      pageSize: propTypes.number.def(10),
      getPopupContainer: {
        type: Function,
        default: (node) => node?.parentNode,
      },
      //Enabled by defaultYAxis overflow position adjustment，Therefore, the position of the drop-down box will automatically move upward when there is insufficient visual space.，lead toSelectThe input box is blocked。It should be noted that，The default is visual space，rather than the space you own
      //update-begin-author:liusq date:2023-04-04 for:[issue/286]Drop-down search box occlusion problem
      adjustY:propTypes.bool.def(true),
      //update-end-author:liusq date:2023-04-04 for:[issue/286]Drop-down search box occlusion problem
      //Whether to trigger immediately after having a valuechange
      immediateChange: propTypes.bool.def(false),
      //update-begin-author:taoyan date:2022-8-15 for: VUEN-1971 【online special test】Associate records with other table fields 1
      //Supports passing in query parameters，Such as sorting information
      params:{
        type: Object,
        default: ()=>{}
      },
      //update-end-author:taoyan date:2022-8-15 for: VUEN-1971 【online special test】Associate records with other table fields 1
      //update-begin---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
      //Whether it is multiple selection
      multiple:{
        type: Boolean,
        default: false
      },
      //update-end---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, refs }) {
      const options = ref<any[]>([]);
      const loading = ref(false);
      // update-begin--author:liaozhiyang---date:20231205---for：【issues/897】JSearchSelectComponent additionclass/styleStyle does not take effect
      const attrs = useAttrs({'excludeDefaultKeys': false});
      // update-end--author:liaozhiyang---date:20231205---for：【issues/897】JSearchSelectComponent additionclass/styleStyle does not take effect
      const selectedValue = ref([]);
      const selectedAsyncValue = ref([]);
      const lastLoad = ref(0);
      // whether based onvalueloadtext
      const loadSelectText = ref(true);
      // asynchronous(Dictionary) - 滚动load时会用到
      let isHasData = true;
      let scrollLoading = false;
      let pageNo = 1;
      let searchKeyword = '';

      // 是否是Dictionary
      const isDictTable = computed(() => {
        if (props.dict) {
          return props.dict.split(',').length >= 2
        }
        return false;
      })

      /**
       * listening dictionarycode
       */
      watch(() => props.dict, () => {
        if (!props.dict) {
          return
        }
        if (isDictTable.value) {
          initDictTableData();
        } else {
          initDictCodeData();
        }
      }, {immediate: true});

      /**
       * monitorvalue
       */
      watch(
        () => props.value,
        (val) => {
          if (val || val === 0) {
            initSelectValue();
          } else {
            selectedValue.value = [];
            selectedAsyncValue.value = [];
          }
        },
        { immediate: true }
      );
      /**
       * monitordictOptions
       */
      watch(
        () => props.dictOptions,
        (val) => {
          if (val && val.length >= 0) {
            options.value = [...val];
          }
        },
        { immediate: true }
      );
      /**
       * asynchronous查询数据
       */
      const loadData = debounce(async function loadData(value) {
        if (!isDictTable.value) {
          return;
        }
        // update-begin--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
        pageNo = 1;
        isHasData = true;
        searchKeyword = value;
        // update-end--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
 
        lastLoad.value += 1;
        const currentLoad = unref(lastLoad);
        options.value = [];
        loading.value = true;
        let keywordInfo = getKeywordParam(value);
        //update-begin---author:chenrui ---date:2024/4/7  for：[QQYUN-8800]JSearchSelectofsearchThe event will be triggered when the Chinese input has not been successfully spelled.，lead to后端SQLinjection #6049------------
        keywordInfo = keywordInfo.replaceAll("'", '');
        //update-end---author:chenrui ---date:2024/4/7  for：[QQYUN-8800]JSearchSelectofsearchThe event will be triggered when the Chinese input has not been successfully spelled.，lead to后端SQLinjection #6049------------
        // dictionarycodeFormat：table,text,code
        defHttp
          .get({
            url: `/sys/dict/loadDict/${props.dict}`,
            params: { keyword: keywordInfo, pageSize: props.pageSize, pageNo },
          })
          .then((res) => {
            loading.value = false;
            if (res && res.length > 0) {
              if (currentLoad != unref(lastLoad)) {
                return;
              }
              options.value = res;
              // update-begin--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
              pageNo++;
              // update-end--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
            } else {
              // update-begin--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
              pageNo == 1 && (isHasData = false);
              // update-end--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
            }
          });
      }, 300);
      /**
       * initializationvalue
       */
      function initSelectValue() {
        //update-begin-author:taoyan date:2022-4-24 for: The drop-down search component will be triggered every time a value is selected.valueofmonitorevent，trigger this method，But it is not actually needed
        if (loadSelectText.value === false) {
          loadSelectText.value = true;
          return;
        }
        //update-end-author:taoyan date:2022-4-24 for: The drop-down search component will be triggered every time a value is selected.valueofmonitorevent，trigger this method，But it is not actually needed
        let { async, value, dict } = props;
        if (async) {
          if (!selectedAsyncValue || !selectedAsyncValue.key || selectedAsyncValue.key !== value) {
            defHttp.get({ url: `/sys/dict/loadDictItem/${dict}`, params: { key: value } }).then((res) => {
              if (res && res.length > 0) {
                //update-begin---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
                //判断组件Whether it is multiple selection
                if(props.multiple){
                  if(value){
                    let arr: any = [];
                    //多选返回of是以逗号拼接of方式
                    let values = value.toString().split(',');
                    for (let i = 0; i < res.length; i++) {
                      let obj = {
                        key: values[i],
                        label: res[i],
                      };
                      arr.push(obj);
                      selectedValue.value.push(obj.key);
                    }
                    selectedAsyncValue.value = arr;
                  }
                } else {
                  let obj = {
                    key: value,
                    label: res,
                  };
                  if (props.value == value) {
                    selectedAsyncValue.value = { ...obj };
                  }
                  //update-begin-author:taoyan date:2022-8-11 for: value change triggerchangeevent--used foronlineAssociated record configuration page
                  if(props.immediateChange == true){
                    emit('change', props.value);
                  }
                  //update-end-author:taoyan date:2022-8-11 for: value change triggerchangeevent--used foronlineAssociated record configuration page
                  //update-end---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
                }
              }
            });
          }
        } else {
          //update-begin---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
          if(!props.multiple){
            selectedValue.value = value.toString();
            //update-begin-author:taoyan date:2022-8-11 for: value change triggerchangeevent--used foronlineOther table field configuration interface
            if(props.immediateChange == true){
              emit('change', value.toString());
            }
            //update-end-author:taoyan date:2022-8-11 for: value change triggerchangeevent--used foronlineOther table field configuration interface
          }else{
            //多选of情况下需要转成数组
            selectedValue.value = value.toString().split(',');
          }
          //update-begin---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
        }
      }

      /**
       * initializationdictionary下拉数据
       */
      async function initDictTableData() {
        let { dict, async, dictOptions, pageSize } = props;
        if (!async) {
          //如果dictionary项集合有数据
          if (dictOptions && dictOptions.length > 0) {
            options.value = dictOptions;
          } else {
            //根据dictionaryCode, initializationdictionary数组
            let dictStr = '';
            if (dict) {
              let arr = dict.split(',');
              if (arr[0].indexOf('where') > 0) {
                let tbInfo = arr[0].split('where');
                dictStr = tbInfo[0].trim() + ',' + arr[1] + ',' + arr[2] + ',' + encodeURIComponent(tbInfo[1]);
              } else {
                dictStr = dict;
              }
              //根据dictionaryCode, initializationdictionary数组
              const dictData = await initDictOptions(dictStr);
              options.value = dictData;
            }
          }
        } else {
          if (!dict) {
            console.error('搜索组件未配置dictionary项');
          } else {
            // update-begin--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
            pageNo = 1;
            isHasData = true;
            searchKeyword = '';
            // update-end--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load

            //asynchronous一开始也load一point数据
            loading.value = true;
            let keywordInfo = getKeywordParam('');
            defHttp
              .get({
                url: `/sys/dict/loadDict/${dict}`,
                params: { pageSize: pageSize, keyword: keywordInfo, pageNo },
              })
              .then((res) => {
                loading.value = false;
                if (res && res.length > 0) {
                  options.value = res;
                  // update-begin--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
                  pageNo++;
                  // update-end--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
                } else {
                  // update-begin--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
                  pageNo == 1 && (isHasData = false);
                  // update-end--author:liaozhiyang---date:20240731---for：【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
                }
              });
          }
        }
      }

      /**
       * 查询数据dictionary
       */
      async function initDictCodeData() {
        options.value = await initDictOptions(props.dict);
      }

      /**
       * 同步改变event
       * */
      function handleChange(value) {
        //update-begin---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
        //Multiple selection will also triggerchangeevent，When you need to judge if, multiple selections do not require assignment.
        if(!props.multiple){
          selectedValue.value = value;
          callback();
        }
        //update-end---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
      }
      /**
       * asynchronous改变event
       * */
      function handleAsyncChange(selectedObj) {
          //update-begin---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
          // Used in single selection situationschangeevent
          if(!props.multiple){
            if (selectedObj) {
              selectedAsyncValue.value = selectedObj;
              selectedValue.value = selectedObj.key;
            } else {
              selectedAsyncValue.value = null;
              selectedValue.value = null;
              options.value = null;
              loadData('');
            }
            callback();
            // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-426】Dropdown search set default value，Delete the query conditions，Click Reset again，No value attached
            // pointxWhen clearing, you need toloadSelectTextset uptrue
            selectedObj ?? (loadSelectText.value = true);
            // update-end--author:liaozhiyang---date:20240524---for：【TV360X-426】Dropdown search set default value，Delete the query conditions，Click Reset again，No value attached
          }
          //update-end---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
      }

      //update-begin---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
      /**
       * asynchronous值选中event
       * @param selectedObj
       */
      function handleSelect(selectedObj){
        let key = selectedObj;
        if(props.async){
          key = selectedObj.key;
        }
        //Used in multiple selection situationsselectevent
        if(props.multiple && key){
          //asynchronousof时候才需要在selectedValueAdd value operation to array，同步of情况下直接走更新值操作
          if(props.async){
            selectedValue.value.push(key);
          }
          selectedObj ?? (loadSelectText.value = true);
          callback();
        }
      }
      
      /**
       * asynchronous值取消选中event
       * @param selectedObj
       */
      function handleDeSelect(selectedObj){
        let key = selectedObj;
        if(props.async){
          key = selectedObj.key;
        }
        //Used in multiple selection situationsselectevent
        if(props.multiple){
          //asynchronousof时候才需要在selectedValueDelete value operation from array，同步of情况下直接走更新值操作
          if(props.async){
            let findIndex = selectedValue.value.findIndex(item => item === key);
            if(findIndex != -1){
              selectedValue.value.splice(findIndex,1);
            }
          }
          selectedObj ?? (loadSelectText.value = true);
          callback();
        }
      }
      //update-end---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
      
      /**
       *callback method
       * */
      function callback() {
        loadSelectText.value = false;
        //update-begin---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
        //Single-select and go directly to the update value operation
        if(!props.multiple){
          emit('change', unref(selectedValue));
          emit('update:value', unref(selectedValue));
        } else {
          //Multiple selection requires converting arrays into strings
          emit('change', unref(selectedValue).join(","));
          emit('update:value', unref(selectedValue).join(","));
        }
        //update-end---author:wangshuai---date:2025-04-17---for:【issues/8101】front enddict组件lead to内存溢出问题：Search component supports multiple selections---
      }
      /**
       * filter selectedoption
       */
      function filterOption(input, option) {
        //update-begin-author:taoyan date:2022-11-8 for: issues/218 所有功能表单of下拉搜索框搜索无效
        let value = '', label = '';
        try {
          value = option.value;
          label = option.children()[0].children;
        }catch (e) {
          console.log('Failed to get drop-down items', e)
        }
        let str = input.toLowerCase();
        return value.toLowerCase().indexOf(str) >= 0 || label.toLowerCase().indexOf(str) >= 0;
        //update-end-author:taoyan date:2022-11-8 for: issues/218 所有功能表单of下拉搜索框搜索无效
      }

      function getParentContainer(node) {
        // update-begin-author:taoyan date:20220407 for: getPopupContainerAlways valuable lead topopContainerof逻辑永远走不进去，Move it to the front and judge
        if (props.popContainer) {
          // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9339】There are multiplemodal弹窗内都有下拉dictionary多选和下拉搜索组件时，open anothermodal时组件ofoptionsDo not display
          return setPopContainer(node, props.popContainer);
          // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9339】There are multiplemodal弹窗内都有下拉dictionary多选和下拉搜索组件时，open anothermodal时组件ofoptionsDo not display
        } else {
          if (typeof props.getPopupContainer === 'function') {
            return props.getPopupContainer(node);
          } else {
            return node?.parentNode;
          }
        }
        // update-end-author:taoyan date:20220407 for: getPopupContainerAlways valuable lead topopContainerof逻辑永远走不进去，Move it to the front and judge
      }

      //update-begin-author:taoyan date:2022-8-15 for: VUEN-1971 【online special test】Associate records with other table fields 1
      //Get keyword parameters 支持set up排序信息
      function getKeywordParam(text){
        // If sort information is set，Sorting information needs to be written，Add after the keyword [orderby:create_time,desc]
        if(props.params && props.params.column && props.params.order){
          let temp = text||''
          
          //update-begin-author:taoyan date:2023-5-22 for: /issues/4905 When configuring form builder fields，Select related fields，When doing advanced configuration，无法load数据库列表，hint SginSignature verification error！ #4905
          temp = temp+'[orderby:'+props.params.column+','+props.params.order+']'
          return encodeURI(temp);
          //update-end-author:taoyan date:2023-5-22 for: /issues/4905 When configuring form builder fields，Select related fields，When doing advanced configuration，无法load数据库列表，hint SginSignature verification error！ #4905
          
        }else{
          return text;
        }
      }
      //update-end-author:taoyan date:2022-8-15 for: VUEN-1971 【online special test】Associate records with other table fields 1
      // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-26】下拉搜索控件选中选项后再次point击下拉应该显示初始of下拉选项，Instead of only showing the selected results
      const handleAsyncFocus = () => {
        // update-begin--author:liaozhiyang---date:20240709---for：【issues/6681】asynchronous查询不生效
        if ((isObject(selectedAsyncValue.value) || selectedAsyncValue.value?.length) && isDictTable.value && props.async) {
          // update-begin--author:liaozhiyang---date:20240809---for：【TV360X-2062】After selecting the second page of data in the drop-down search，第一次point击时(得到焦point)滚动条没复原到初始位置且数据会load第二页数据(应该只load第一页数据)
          options.value = [];
          // update-end--author:liaozhiyang---date:20240809---for：【TV360X-2062】After selecting the second page of data in the drop-down search，第一次point击时(得到焦point)滚动条没复原到初始位置且数据会load第二页数据(应该只load第一页数据)
          initDictTableData();
        }
        // update-begin--author:liaozhiyang---date:20240919---for：【TV360X-2348】得到焦point时optionsOption to show first page content（Solve the problem of displaying non-first page content when adding new content）
        if (Array.isArray(selectedAsyncValue.value) && selectedAsyncValue.value.length === 0 && isDictTable.value && props.async) {
          if (pageNo > 2) {
            options.value = [];
            initDictTableData();
          }
        }
        // update-end--author:liaozhiyang---date:20240919---for：【TV360X-2348】得到焦point时optionsOption to show first page content（Solve the problem of displaying non-first page content when adding new content）
        attrs.onFocus?.();
      };
      // update-end--author:liaozhiyang---date:20240523---for：【TV360X-26】下拉搜索控件选中选项后再次point击下拉应该显示初始of下拉选项，Instead of only showing the selected results

      /**
       * 2024-07-30
       * liaozhiyang
       * 【TV360X-1898】JsearchSelect组件传入DictionaryFormat则支持滚动load
       * */
      const handlePopupScroll = async (e) => {
        // Dictionary才才支持滚动load
        if (isDictTable.value) {
          const { target } = e;
          const { scrollTop, scrollHeight, clientHeight } = target;
          if (!scrollLoading && isHasData && scrollTop + clientHeight >= scrollHeight - 10) {
            scrollLoading = true;
            let keywordInfo = getKeywordParam(searchKeyword);

            defHttp
              .get({ url: `/sys/dict/loadDict/${props.dict}`, params: { pageSize: props.pageSize, keyword: keywordInfo, pageNo } })
              .then((res) => {
                loading.value = false;
                if (res?.length > 0) {
                  // 防止开源只更新了front end代码没更新后端代码（第一页和第二页面of第一条数据相同则是后端代码没更新，No pagination）
                  if (JSON.stringify(res[0]) === JSON.stringify(options.value[0])) {
                    isHasData =  false;
                    return;
                  }
                  options.value.push(...res);
                  pageNo++;
                } else {
                  isHasData = false;
                }
              })
              .finally(() => {
                scrollLoading = false;
              })
              .catch(() => {
                pageNo != 1 && pageNo--;
              });
          }
        }
      };

      return {
        attrs,
        options,
        loading,
        isDictTable,
        selectedValue,
        selectedAsyncValue,
        loadData: useDebounceFn(loadData, 800),
        getParentContainer,
        filterOption,
        handleChange,
        handleAsyncChange,
        handleAsyncFocus,
        handlePopupScroll,
        handleSelect,
        handleDeSelect,
      };
    },
  });
</script>

<style scoped></style>
