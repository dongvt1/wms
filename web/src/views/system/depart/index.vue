<template>
  <a-row :class="['p-4', `${prefixCls}--box`]" type="flex" :gutter="10">
    <a-col :xl="10" :lg="24" :md="24" style="margin-bottom: 10px">
      <DepartLeftTree ref="leftTree" @select="onTreeSelect" @rootTreeData="onRootTreeData" />
    </a-col>
    <a-col :xl="14" :lg="24" :md="24" style="margin-bottom: 10px">
      <div style="height: 100%;" :class="[`${prefixCls}`]">
        <a-tabs v-show="departData != null" defaultActiveKey="base-info">
          <a-tab-pane tab="Basic information" key="base-info" forceRender style="position: relative">
            <div style="padding: 20px">
              <DepartFormTab :data="departData" :rootTreeData="rootTreeData" @success="onSuccess" />
            </div>
          </a-tab-pane>
          <a-tab-pane tab="Department authority" key="role-info">
            <div style="padding: 0 20px 20px">
              <DepartRuleTab :data="departData" />
            </div>
          </a-tab-pane>
          <a-tab-pane tab="Reporting relationships" key="rank">
            <div style="padding: 0 20px 20px">
              <DepartRankRelation :data="departData" />
            </div>
          </a-tab-pane>
          <a-tab-pane tab="User list" key="user">
            <div style="padding: 0 20px 20px">
              <DepartUserList :data="departData" :key="reRender"></DepartUserList>
            </div>
          </a-tab-pane>
        </a-tabs>
        <div v-show="departData == null" style="padding-top: 40px">
          <a-empty description="No department selected yet" />
        </div>
      </div>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup name="system-depart">
  import { provide, ref } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import DepartLeftTree from './components/DepartLeftTree.vue';
  import DepartFormTab from './components/DepartFormTab.vue';
  import DepartRuleTab from './components/DepartRuleTab.vue';
  import DepartRankRelation from './components/DepartRankRelation.vue';
  import DepartUserList from './components/DepartUserList.vue';

  const { prefixCls } = useDesign('depart-manage');
  provide('prefixCls', prefixCls);

  // Define a subcomponentrefvariable
  const leftTree = ref();

  // Currently selected department information
  const departData = ref({});
  const rootTreeData = ref<any[]>([]);
  const reRender = ref(-1);

  // Triggered after the left tree is selected
  function onTreeSelect(data) {
    console.log('onTreeSelect: ', data);
    if (reRender.value == -1) {
      // Re-render the component
      reRender.value = Math.random();
    }
    departData.value = data;
  }

  // tree on leftrootTreeDatatrigger
  function onRootTreeData(data) {
    rootTreeData.value = data;
  }

  function onSuccess() {
    leftTree.value.loadRootTreeData();
  }
</script>

<style lang="less">
  @import './index.less';
</style>
