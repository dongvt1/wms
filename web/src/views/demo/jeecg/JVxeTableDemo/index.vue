<template>
  <PageWrapper>
    <a-card :bordered="false">
      <template #title>
        <span> 
          JVXETableis specialized for big data and variousERPHigh-performance table component developed for complex operations in style，The bottom layer adoptsvxe-tablecomponents，Can make up for it perfectlyantddefaulttablePerformance issues。 
          <a href="https://help.jeecg.com/component/JVxeTable.html">APIdocument</a>
        </span>
      </template>
    
      <a-tabs defaultActiveKey="1">
        <a-tab-pane tab="Basic example" key="1">
          <JVxeDemo1 />
        </a-tab-pane>
        <a-tab-pane tab="Advanced examples" key="2">
          <JVxeDemo2 />
        </a-tab-pane>
        <a-tab-pane tab="Sorting example" key="3">
          <JVxeDemo3 />
        </a-tab-pane>
        <a-tab-pane tab="Linkage example" key="4">
          <JVxeDemo4 />
        </a-tab-pane>
        <a-tab-pane tab="Keyboard operation" key="5">
          <JVxeDemo5 />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </PageWrapper>
</template>

<script lang="ts" setup>
  // noinspection ES6UnusedImports
  import { PageWrapper } from '/@/components/Page';
  import JVxeDemo1 from './JVxeDemo1.vue';
  import JVxeDemo2 from './JVxeDemo2.vue';
  import JVxeDemo3 from './JVxeDemo3.vue';
  import JVxeDemo4 from './JVxeDemo4.vue';
  import JVxeDemo5 from './JVxeDemo5.vue';
</script>
