<template>
  <div class="p-4">
    <a-card :bordered="false" style="height: 100%">
      <a-tabs v-model:activeKey="activeKey" @change="tabChange">
        <a-tab-pane :key="item.key" :tab="item.label" v-for="item in compList" />
      </a-tabs>
      <component :is="currentComponent" />
    </a-card>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import {
    BasicFunctionForm,
    BasicFormConAttribute,
    BasicFormFieldShow,
    BasicFormFieldTip,
    BasicFormRules,
    BasicFormDynamicsRules,
    BasicFormSlots,
    BasicFormCustomSlots,
    BasicFormRander,
    BasicFixedWidthForm,
    BasicFiledsLayotForm,
    BasicFormLayout,
    BasicFormBtn,
    BasicFormCompact,
    BasicFormCleanRule,
    BasicFormValue,
    BasicFormSchemas,
    BasicFormAdd,
    BasicFormFooter,
    BasicFormModal,
    BasicFormCustom,
    BasicFormSearch,
    BasicFormComponent,
    BasicFormCustomComponent,
  } from './index';
  export default defineComponent({
    name: 'document-table-demo',
    components: {
      BasicFunctionForm,
      BasicFormConAttribute,
      BasicFormFieldShow,
      BasicFormFieldTip,
      BasicFormRules,
      BasicFormDynamicsRules,
      BasicFormSlots,
      BasicFormCustomSlots,
      BasicFormRander,
      BasicFixedWidthForm,
      BasicFiledsLayotForm,
      BasicFormLayout,
      BasicFormBtn,
      BasicFormCompact,
      BasicFormCleanRule,
      BasicFormValue,
      BasicFormSchemas,
      BasicFormAdd,
      BasicFormFooter,
      BasicFormModal,
      BasicFormCustom,
      BasicFormSearch,
      BasicFormComponent,
      BasicFormCustomComponent,
    },
    setup() {
      //Currently selectedkey
      const activeKey = ref('BasicFunctionForm');
      //Component collection
      const compList = ref([
        { key: 'BasicFunctionForm', label: 'basic form' },
        { key: 'BasicFormConAttribute', label: 'Field control properties' },
        { key: 'BasicFormComponent', label: 'Ant Design VueComes with controls' },
        { key: 'BasicFormCustomComponent', label: 'JEECGEncapsulated control' },
        { key: 'BasicFormFieldShow', label: 'Show and hide fields' },
        { key: 'BasicFormFieldTip', label: 'Field title hints' },
        { key: 'BasicFormRules', label: 'form validation' },
        { key: 'BasicFormDynamicsRules', label: 'Custom dynamic inspection' },
        { key: 'BasicFormSlots', label: 'field slot' },
        { key: 'BasicFormCustomSlots', label: 'Custom component(slot)' },
        { key: 'BasicFormCustom', label: 'Custom component(component)' },
        { key: 'BasicFormRander', label: 'custom rendering' },
        { key: 'BasicFixedWidthForm', label: 'fixedlabelwidth' },
        { key: 'BasicFiledsLayotForm', label: 'Title and field layout' },
        { key: 'BasicFormLayout', label: 'form layout' },
        { key: 'BasicFormBtn', label: 'Action button example' },
        { key: 'BasicFormCompact', label: 'Compact form' },
        { key: 'BasicFormCleanRule', label: 'form validation配置' },
        { key: 'BasicFormValue', label: 'Getvaluevalue' },
        { key: 'BasicFormSchemas', label: 'renewschemasForm configuration' },
        { key: 'BasicFormAdd', label: 'Dynamic increase and decrease forms' },
        { key: 'BasicFormFooter', label: 'Custom footer' },
        { key: 'BasicFormModal', label: 'popup form' },
        { key: 'BasicFormSearch', label: 'Query area' },
      ]);
      //Currently selected组件
      const currentComponent = computed(() => {
        return activeKey.value;
      });

      //usecomponentdynamic switchingtab
      function tabChange(key) {
        activeKey.value = key;
      }
      return {
        activeKey,
        currentComponent,
        tabChange,
        compList,
      };
    },
  });
</script>
