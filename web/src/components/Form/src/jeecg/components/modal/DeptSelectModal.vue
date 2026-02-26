<!--Department selection box-->
<template>
  <div>
    <BasicModal v-bind="$attrs" @register="register" :title="modalTitle" width="600px" :minHeight="300" :maxHeight="maxHeight" @ok="handleOk" destroyOnClose @visible-change="visibleChange">
      <a-input-search v-if="izOnlySelectDepartPost" placeholder="Search by job title…" style="margin-bottom: 10px" @search="onSearch" @change="handelSearchChange"/>
      <BasicTree
        ref="treeRef"
        :treeData="treeData"
        :load-data="sync == false ? null : onLoadData"
        v-bind="getBindValue"
        @select="onSelect"
        @check="onCheck"
        :fieldNames="fieldNames"
        :checkedKeys="checkedKeys"
        :expandedKeys="expandedKeys"
        :multiple="multiple"
        :checkStrictly="getCheckStrictly"
        :key="reloadKey"
      >
        <template #title="{ orgCategory, title }">
          <TreeIcon :orgCategory="orgCategory" :title="title"></TreeIcon>
        </template>
      </BasicTree>
      <!--Tree operation section-->
      <template #insertFooter>
        <a-dropdown placement="top">
          <template #overlay>
            <a-menu>
              <a-menu-item v-if="multiple" key="1" @click="checkALL(true)">Check all</a-menu-item>
              <a-menu-item v-if="multiple" key="2" @click="checkALL(false)">Deselect all</a-menu-item>
              <a-menu-item key="3" @click="expandAll(true)">Expand all</a-menu-item>
              <a-menu-item key="4" @click="expandAll(false)">Collapse all</a-menu-item>
            </a-menu>
          </template>
          <a-button style="float: left"> tree operations <Icon icon="ant-design:up-outlined" /> </a-button>
        </a-dropdown>
      </template>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { queryDepartTreeSync, queryTreeList, queryDepartAndPostTreeSync } from '/@/api/common/api';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { treeProps } from '/@/components/Form/src/jeecg/props/props';
  import { BasicTree, TreeActionType } from '/@/components/Tree';
  import { useTreeBiz } from '/@/components/Form/src/jeecg/hooks/useTreeBiz';
  import {propTypes} from "/@/utils/propTypes";
  import { omit } from 'lodash-es';
  import TreeIcon from '@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue';

  export default defineComponent({
    name: 'DeptSelectModal',
    components: {
      TreeIcon,
      BasicModal,
      BasicTree,
    },
    props: {
      ...treeProps,
      //Select box title
      modalTitle: {
        type: String,
        default: 'Department selection',
      },
      // update-begin--author:liaozhiyang---date:20231220---for：【QQYUN-7678】There is too much content in the department component and there is no scroll bar（Give a default maximum height）
      maxHeight: {
        type: Number,
        default: 500,
      },
      // update-end--author:liaozhiyang---date:20231220---for：【QQYUN-7678】There is too much content in the department component and there is no scroll bar（Give a default maximum height）
      value: propTypes.oneOfType([propTypes.string, propTypes.array]),
      //query parameters
      params: {
        type: Object,
        default: () => ({}),
      },
    },
    emits: ['register', 'getSelectResult', 'close'],
    setup(props, { emit }) {
      //Registration pop-up box
      const [register, { closeModal }] = useModalInner();
      const attrs = useAttrs();
      const treeRef = ref<Nullable<TreeActionType>>(null);
      //load treekey
      const reloadKey = ref<number>(Math.random());
      
      //update-begin-author:taoyan date:2022-10-28 for: Department selection警告类型不匹配
      let propValue = props.value === ''?[]:props.value;
      // Make sure to pass toBasicTreeofvalueis an array format
      if (propValue && typeof propValue === 'string') {
        propValue = propValue.split(',');
      }
      //update-begin-author:liusq date:2023-05-26 for:  [issues/538]JSelectDeptComponents are subject to dynamicDisabled Influence
      let temp = Object.assign({}, unref(props), unref(attrs), {value: propValue},{disabled: false});
      const getBindValue = omit(temp, 'multiple');
      //update-end-author:liusq date:2023-05-26 for:  [issues/538]JSelectDeptComponents are subject to dynamicDisabled Influence
     //update-end-author:taoyan date:2022-10-28 for: Department selection警告类型不匹配
      
      const queryUrl = getQueryUrl();
      const [{ visibleChange, checkedKeys, getCheckStrictly, getSelectTreeData, onCheck, onLoadData, treeData, checkALL, expandAll, onSelect, onSearch, expandedKeys }] =
        useTreeBiz(treeRef, queryUrl, getBindValue, props, emit);
      const searchInfo = ref(props.params || {});
      const tree = ref([]);
      //replacetreeNodemiddlekeyThe fields aretreeDatamiddle对应of字段
      const fieldNames = {
        key: props.rowKey,
      };
      // {children:'children', title:'title', key:'key' }
      /**
       * Confirm selection
       */
      function handleOk() {
        getSelectTreeData((options, values) => {
          //回传选项和已选择of值
          emit('getSelectResult', options, values);
          //Close pop-up window
          closeModal();
        });
      }

      /** Get query data method */
      function getQueryUrl() {
        let queryFn = props.izOnlySelectDepartPost ? queryDepartAndPostTreeSync :props.sync ? queryDepartTreeSync : queryTreeList;
        //update-begin-author:taoyan date:2022-7-4 for: issues/I5F3P4 online配置Department selection后编辑，Viewing the data should show the department name，Not a department code
        return (params) => queryFn(Object.assign({}, params, { primaryKey: props.rowKey }));
        //update-end-author:taoyan date:2022-7-4 for: issues/I5F3P4 online配置Department selection后编辑，Viewing the data should show the department name，Not a department code
      }

      /**
       * Search value change event
       * @param value
       */
      function handelSearchChange(value) {
        if(!value.target.value){
          reloadKey.value = Math.random();
        }
      }
      return {
        tree,
        handleOk,
        searchInfo,
        treeRef,
        treeData,
        onCheck,
        onSelect,
        checkALL,
        expandAll,
        fieldNames,
        checkedKeys,
        expandedKeys,
        register,
        getBindValue,
        getCheckStrictly,
        visibleChange,
        onLoadData,
        onSearch,
        reloadKey,
        handelSearchChange,
      };
    },
  });
</script>
<style>
  .svg-company {
    width: 18px;
    height: 18px;
    position: relative;
    top: 1px;
    right: 2px;
  }
  .svg-depart,.svg-post {
    width: 14px;
    height: 16px;
    position: relative;
    right: 2px;
  }
</style>
