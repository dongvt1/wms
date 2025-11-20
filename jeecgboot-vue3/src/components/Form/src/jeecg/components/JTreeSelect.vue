<template>
  <a-tree-select
    v-if="show"
    allowClear
    labelInValue
    style="width: 100%"
    :getPopupContainer="(node) => node?.parentNode"
    :dropdownStyle="{ maxHeight: '400px', overflow: 'auto' }"
    :placeholder="placeholder"
    :loadData="asyncLoadTreeData"
    :value="treeValue"
    :treeData="treeData"
    :multiple="multiple"
    v-bind="attrs"
    @change="onChange"
    @search="onSearch"
    :tree-checkable="treeCheckAble"
    :tagRender="tagRender"
  >
    <template #[name]="data" v-for="name in slotNamesGroup" :key="name">
      <slot :name="name" v-bind="data"></slot>
    </template>
  </a-tree-select>
</template>
<script lang="ts" setup>
  /*
   * Asynchronous tree loading component By passing in the table name Show fields storage field Load a tree control
   * <j-tree-select dict="aa_tree_test,aad,id" pid-field="pid" ></j-tree-select>
   * */
  import { ref, watch, unref, nextTick, computed } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { TreeSelect } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { isObject, isArray } from '/@/utils/is';
  import { useI18n } from '/@/hooks/web/useI18n';
  enum Api {
    url = '/sys/dict/loadTreeData',
    view = '/sys/dict/loadDictItem/',
  }

  const props = defineProps({
    value: propTypes.string.def(''),
    placeholder: propTypes.string.def('Please select'),
    dict: propTypes.string.def('id'),
    parentCode: propTypes.string.def(''),
    pidField: propTypes.string.def('pid'),
    //update-begin---author:wangshuai ---date:20220620  for：JTreeSelectcomponentspidValueRevert to empty，否则会影响自定义components树示例------------
    pidValue: propTypes.string.def(''),
    //update-end---author:wangshuai ---date:20220620  for：JTreeSelectcomponentspidValueRevert to empty，否则会影响自定义components树示例--------------
    hasChildField: propTypes.string.def(''),
    converIsLeafVal: propTypes.integer.def(1),
    condition: propTypes.string.def(''),
    multiple: propTypes.bool.def(false),
    loadTriggleChange: propTypes.bool.def(false),
    reload: propTypes.number.def(1),
    //update-begin-author:taoyan date:2022-11-8 for: issues/4173 Online JTreeSelectcontrolchangeOptionsThe method does not take effect
    url: propTypes.string.def(''),
    params: propTypes.object.def({}),
    //update-end-author:taoyan date:2022-11-8 for: issues/4173 Online JTreeSelectcontrolchangeOptionsThe method does not take effect
    //update-begin---author:wangshuai date: 20230202 for: Add whether there is a checkbox
    //There is no selection box by default
    treeCheckAble: propTypes.bool.def(false),
    //update-end---author:wangshuai date: 20230202 for: Add whether there is a checkbox
    hiddenNodeKey: propTypes.string.def(''),
    //update-begin---author:wangshuai---date:2025-09-06---for: Rendering when multiple selectiontagtext，Do not render if empty，Radio selection is not supported---
    //Rendering when multiple selectiontagtext
    tagRender: propTypes.func,
    //update-end---author:wangshuai---date:2025-09-06---for:Rendering when multiple selectiontagtext，Do not render if empty，Radio selection is not supported---
  });
  const attrs = useAttrs();
  const { t } = useI18n();
  const emit = defineEmits(['change', 'update:value']);
  const slots = defineSlots();
  const { createMessage } = useMessage();
  //Tree drop down data
  const treeData = ref<any[]>([]);
  //Select data
  const treeValue = ref<any>(null);
  const tableName = ref<any>('');
  const text = ref<any>('');
  const code = ref<any>('');
  const show = ref<boolean>(true);
  /**
   * monitorvaluedata and initialize
   */
  watch(
    () => props.value,
    () => loadItemByCode(),
    { deep: true, immediate: true }
  );
  /**
   * monitordictchange
   */
  // update-begin--author:liaozhiyang---date:20240612---for：【issues/1283】JtreeSelectcomponents初始调用了两次接口
  watch(
    () => props.dict,
    () => {
      initDictInfo();
      loadRoot();
    }
  );
  // update-end--author:liaozhiyang---date:20240612---for：【issues/1283】JtreeSelectcomponents初始调用了两次接口
  // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-87】When editing the tree table, you cannot select your own and descendant nodes as parent nodes.
  watch(
    () => props.hiddenNodeKey,
    () => {
      if (treeData.value?.length && props.hiddenNodeKey) {
        handleHiddenNode(treeData.value);
        treeData.value = [...treeData.value];
      }
    }
  );
  // update-end--author:liaozhiyang---date:20240529---for：【TV360X-87】When editing the tree table, you cannot select your own and descendant nodes as parent nodes.

  //update-begin-author:taoyan date:2022-5-25 for: VUEN-1056 15、serious——onlinetree form，When adding，The parent node is empty
  watch(
    () => props.reload,
    async () => {
      treeData.value = [];
      // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-88】onlineWhen the tree table is repeatedly added, the parent node data is not fully loaded and the opened child nodes are not reloaded.
      show.value = false;
      nextTick(() => {
        show.value = true;
      });
      // update-end--author:liaozhiyang---date:20240524---for：【TV360X-88】onlineWhen the tree table is repeatedly added, the parent node data is not fully loaded and the opened child nodes are not reloaded.
      await loadRoot();
    },
    {
      immediate: false,
    }
  );
  //update-end-author:taoyan date:2022-5-25 for: VUEN-1056 15、serious——onlinetree form，When adding，The parent node is empty

  /**
   * according tocodeGet drop-down data and echo it
   */
  async function loadItemByCode() {
    if (!props.value || props.value == '0') {
      if(props.multiple){
        treeValue.value = [];
      }else{
        treeValue.value = { label: null, value: null };
      }
    } else {
      //update-begin-author:taoyan date:2022-11-8 for: issues/4173 Online JTreeSelectcontrolchangeOptionsThe method does not take effect
      if(props.url){
        getItemFromTreeData();
      }else{
        // update-begin--author:liaozhiyang---date:20250423---for：【issues/8093】After selecting a node, it will first change to encoding and then display.labelWord
        if (props.value) {
          if (isArray(treeValue.value)) {
            let isNotRequestTransform = false;
            const value = isArray(props.value) ? props.value : props.value.split(',');
            isNotRequestTransform = value.every((value) => !!treeValue.value.find((item) => item.value === value));
            if (isNotRequestTransform) {
              return;
            }
          } else if (isObject(treeValue.value) && unref(treeValue).label != null) {
            if (props.value == unref(treeValue).value) {
              // No need to ask for translation again
              return;
            }
          }
        }
        // update-end--author:liaozhiyang---date:20250423---for：【issues/8093】After selecting a node, it will first change to encoding and then display.labelWord
        let params = { key: props.value };
        let result = await defHttp.get({ url: `${Api.view}${props.dict}`, params }, { isTransformResponse: false });
        if (result.success) {
          //update-start-author:liaozhiyang date:2023-7-17 for:【issues/5141】useJtreeSelect components Console error
          if(props.multiple){
            let values = props.value.split(',');
            treeValue.value = result.result.map((item, index) => ({
              key: values[index],
              value: values[index],
              label: translateTitle(item),
            }));
          }else{
            treeValue.value = { key: props.value, value: props.value, label: translateTitle(result.result[0]) };
          }
          //update-end-author:liaozhiyang date:2023-7-17 for:【issues/5141】useJtreeSelect components Console error
          onLoadTriggleChange(result.result[0]);
        }
      }
      //update-end-author:taoyan date:2022-11-8 for: issues/4173 Online JTreeSelectcontrolchangeOptionsThe method does not take effect
    }
  }

  function onLoadTriggleChange(text) {
    //Only single selection will trigger
    if (!props.multiple && props.loadTriggleChange) {
      emit('change', props.value, text);
    }
  }

  /**
   * initialization data
   */
  function initDictInfo() {
    let arr = props.dict?.split(',');
    tableName.value = arr[0];
    text.value = arr[1];
    code.value = arr[2];
  }

  /**
   * Load drop-down tree data
   */
  async function loadRoot() {
    let params = {
      pid: props.pidValue,
      pidField: props.pidField,
      hasChildField: props.hasChildField,
      converIsLeafVal: props.converIsLeafVal,
      condition: props.condition,
      tableName: unref(tableName),
      text: unref(text),
      code: unref(code),
    };
    let res = await defHttp.get({ url: Api.url, params }, { isTransformResponse: false });
    if (res.success && res.result) {
      for (let i of res.result) {
        i.title = translateTitle(i.title);
        i.value = i.key;
        i.isLeaf = !!i.leaf;
      }
      // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-87】When editing the tree table, you cannot select your own and descendant nodes as parent nodes.
      handleHiddenNode(res.result);
      // update-end--author:liaozhiyang---date:20240523---for：【TV360X-87】When editing the tree table, you cannot select your own and descendant nodes as parent nodes.
      treeData.value = [...res.result];
    } else {
      console.log('Abnormal query results for several root nodes', res);
    }
  }

  /**
   * translate
   * @param text
   */
  function translateTitle(text) {
    if (text.includes("t('") && t) {
      return new Function('t', `return ${text}`)(t);
    }
    return text;
  }
  /**
   * Load data asynchronously
   */
  async function asyncLoadTreeData(treeNode) {
    if (treeNode.dataRef.children) {
      return Promise.resolve();
    }
    if (props.url) {
      return Promise.resolve();
    }
    let pid = treeNode.dataRef.key;
    let params = {
      pid: pid,
      pidField: props.pidField,
      hasChildField: props.hasChildField,
      converIsLeafVal: props.converIsLeafVal,
      condition: props.condition,
      tableName: unref(tableName),
      text: unref(text),
      code: unref(code),
    };
    let res = await defHttp.get({ url: Api.url, params }, { isTransformResponse: false });
    if (res.success) {
      for (let i of res.result) {
        i.title = translateTitle(i.title);
        i.value = i.key;
        i.isLeaf = !!i.leaf;
      }
      // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-87】When editing the tree table, you cannot select your own and descendant nodes as parent nodes.
      handleHiddenNode(res.result);
      // update-end--author:liaozhiyang---date:20240523---for：【TV360X-87】When editing the tree table, you cannot select your own and descendant nodes as parent nodes.
      //Add child node
      addChildren(pid, res.result, treeData.value);
      treeData.value = [...treeData.value];
    }
    return Promise.resolve();
  }

  /**
   * Load child nodes
   */
  function addChildren(pid, children, treeArray) {
    if (treeArray && treeArray.length > 0) {
      for (let item of treeArray) {
        if (item.key == pid) {
          if (!children || children.length == 0) {
            item.isLeaf = true;
          } else {
            item.children = children;
          }
          break;
        } else {
          addChildren(pid, children, item.children);
        }
      }
    }
  }

  /**
   * Select tree node event
   */
  function onChange(value) {
    if (!value) {
      emitValue('');
    } else if (value instanceof Array) {
      emitValue(value.map((item) => item.value).join(','));
    } else {
      emitValue(value.value);
    }
    // update-begin--author:liaozhiyang---date:20250423---for：【issues/8093】After deletion, it will be changed to code first and then displayed.labelWord
    if (isArray(value)) {
      // There is a selected value when editing and deleting, which is asynchronous（Level 2 or above）Will it be displayed?label
      value.forEach((item) => {
        if (item.label === undefined && item.value != null) {
          const findItem = treeValue.value.find((o) => o.value === item.value);
          if (findItem) {
            item.label = findItem.label;
          }
        }
      });
      treeValue.value = value;
    } else {
      treeValue.value = value;
    }
    // update-end--author:liaozhiyang---date:20250423---for：【issues/8093】After deletion, it will be changed to code first and then displayed.labelWord
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }

  /**
   * text框值change
   */
  function onSearch(value) {
    console.log(value);
  }

  /**
   * Check whether the verification conditions are configured incorrectly
   */
  function validateProp() {
    let mycondition = props.condition;
    return new Promise((resolve, reject) => {
      if (!mycondition) {
        resolve();
      } else {
        try {
          let test = JSON.parse(mycondition);
          if (typeof test == 'object' && test) {
            resolve();
          } else {
            createMessage.error('componentsJTreeSelect-conditionWrong value passed，need onejsonstring!');
            reject();
          }
        } catch (e) {
          createMessage.error('componentsJTreeSelect-conditionWrong value passed，need onejsonstring!');
          reject();
        }
      }
    });
  }

  //update-begin-author:taoyan date:2022-11-8 for: issues/4173 Online JTreeSelectcontrolchangeOptionsThe method does not take effect
  watch(()=>props.url, async (val)=>{
    if(val){
      await loadRootByUrl();
    }
  });

  /**
   * according to自定义的请求地址加载数据
   */
  async function loadRootByUrl(){
    let url = props.url;
    let params = props.params;
    let res = await defHttp.get({ url, params }, { isTransformResponse: false });
    if (res.success && res.result) {
      for (let i of res.result) {
        i.title = translateTitle(i.title);
        i.key = i.value;
        i.isLeaf = !!i.leaf;
      }
      // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-87】When editing the tree table, you cannot select your own and descendant nodes as parent nodes.
      handleHiddenNode(res.result);
      // update-end--author:liaozhiyang---date:20240523---for：【TV360X-87】When editing the tree table, you cannot select your own and descendant nodes as parent nodes.
      treeData.value = [...res.result];
    } else {
      console.log('Abnormal query results for several root nodes', res);
    }
  }

  /**
   * according to已有的树数据 translate选项
   */
  function getItemFromTreeData(){
    let data = treeData.value;
    let arr = []
    findChildrenNode(data, arr);
    if(arr.length>0){
      treeValue.value = arr
      onLoadTriggleChange(arr[0]);
    }
  }

  /**
   * Find child nodes recursively
   * @param data
   * @param arr
   */
  function findChildrenNode(data, arr){
    let val = props.value;
    if(data && data.length){
      for(let item of data){
        if(val===item.value){
          arr.push({
            key: item.key,
            value: item.value,
            label: item.label||item.title
          })
        }else{
          findChildrenNode(item.children, arr)
        }
      }
    }
  }
  //update-end-author:taoyan date:2022-11-8 for: issues/4173 Online JTreeSelectcontrolchangeOptionsThe method does not take effect

  /**
   * 2024-05-23
   * liaozhiyang
   * Filter out specified nodes(Contains its descendant nodes)
   */
  function handleHiddenNode(data) {
    if (props.hiddenNodeKey && data?.length) {
      for (let i = 0, len = data.length; i < len; i++) {
        const item = data[i];
        if (item.key == props.hiddenNodeKey) {
          data.splice(i, 1);
          i--;
          len--;
          return;
        }
      }
    }
  }
  /**
   * 2024-07-30
   * liaozhiyang
   * 【issues/6953】JTreeSelect components能支持antdv Correspondinga-tree-select components的插槽
   */
  const slotNamesGroup = computed(() => {
    const native: string[] = [];
    if (isObject(slots)) {
      for (const name of Object.keys(slots)) {
        native.push(name);
      }
    }
    return native;
  });
  // onCreated
  validateProp().then(() => {
    initDictInfo();
    loadRoot();
    loadItemByCode();
  });
</script>

<style lang="less"></style>
