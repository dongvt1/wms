<template>
  <a-card :bordered="false">
    <a-tabs>
      <a-tab-pane tab="ERPlayout template" key="erp">
        <erp-template />
      </a-tab-pane>
      <a-tab-pane tab="layout template1" key="1">
        <template1 />
      </a-tab-pane>
      <a-tab-pane tab="layout template2" key="2">
        <template2 />
      </a-tab-pane>
      <a-tab-pane tab="layout template3" key="3">
        <template3 />
      </a-tab-pane>
      <a-tab-pane tab="layout template4" key="4">
        <template4 />
      </a-tab-pane>
      <a-tab-pane tab="layout template5" key="5">
        <template5 />
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script lang="ts" setup>
  import Template1 from './Template1.vue';
  import Template2 from './Template2.vue';
  import Template3 from './Template3.vue';
  import Template4 from './Template4.vue';
  import Template5 from './Template5.vue';
  import ErpTemplate from './ErpTemplate.vue';
</script>

<style scoped></style>
