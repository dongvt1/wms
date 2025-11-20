<template>
  <a-row :class="['p-4', `${prefixCls}--box`]" :gutter="10">
    <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1">
      <a-card :bordered="false" style="height: 100%">
        <DepartTree @select="onTreeSelect" />
      </a-card>
    </a-col>
    <a-col :xl="18" :lg="16" :md="14" :sm="24" style="flex: 1">
      <a-card :bordered="false" style="height: 100%">
        <a-tabs defaultActiveKey="user-info">
          <a-tab-pane tab="Basic information" key="base-info" forceRender>
            <DepartBaseInfoTab :data="departData" />
          </a-tab-pane>
          <a-tab-pane tab="User information" key="user-info">
            <DepartUserInfoTab :key="reRender" :data="departData" />
          </a-tab-pane>
          <a-tab-pane tab="Department role" key="role-info">
            <DepartRoleInfoTab :key="reRender" :data="departData" />
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup name="system-depart-user">
  import { provide, ref } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';

  import DepartTree from './components/DepartTree.vue';
  import DepartBaseInfoTab from './components/DepartBaseInfoTab.vue';
  import DepartUserInfoTab from './components/DepartUserInfoTab.vue';
  import DepartRoleInfoTab from './components/DepartRoleInfoTab.vue';

  const { prefixCls } = useDesign('depart-user');
  provide('prefixCls', prefixCls);

  // Currently selected department information
  let departData = ref({});

  const reRender = ref(-1);

  // Triggered after the left tree is selected
  function onTreeSelect(data) {
    // update-begin--author:liaozhiyang---date:20250106---for：【issues/7658】When my department does not have department list data，Click Query or Reset to find out the data
    if (reRender.value == -1) {
      // Re-render the component
      reRender.value = Math.random();
    }
    // update-end--author:liaozhiyang---date:20250106---for：【issues/7658】When my department does not have department list data，Click Query or Reset to find out the data
    departData.value = data;
  }
</script>

<style lang="less">
  @import './index.less';
</style>
