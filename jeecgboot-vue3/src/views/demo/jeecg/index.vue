<template>
  <div class="p-4">
    <a-card :bordered="false" style="height: 100%">
      <a-tabs v-model:activeKey="activeKey" @change="tabChange">
        <a-tab-pane key="JeecgComponents" tab="Drop down to select components"></a-tab-pane>
        <a-tab-pane key="JCodeEditDemo" tab="code editor" force-render></a-tab-pane>
        <a-tab-pane key="JEditorDemo" tab="rich text&MakeDown"></a-tab-pane>
        <a-tab-pane key="ImgDragSort" tab="Picture drag"></a-tab-pane>
        <a-tab-pane key="ImgTurnPage" tab="Picture page turning"></a-tab-pane>
        <a-tab-pane key="JeecgPdfView" tab="PDFPreview"></a-tab-pane>
        <a-tab-pane key="JUploadDemo" tab="File upload"></a-tab-pane>
      </a-tabs>
      <component :is="currentComponent"></component>
    </a-card>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import JeecgComponents from './JeecgComponents.vue';
  import JEditorDemo from './JEditorDemo.vue';
  import JCodeEditDemo from './JCodeEditDemo.vue';
  import ImgDragSort from './ImgDragSort.vue';
  import ImgTurnPage from './ImgTurnPage.vue';
  import JeecgPdfView from './JeecgPdfView.vue';
  import JUploadDemo from './JUploadDemo.vue';
  export default defineComponent({
    name: 'comp-jeecg-basic',
    setup() {
      const activeKey = ref('JeecgComponents');
      const currentComponent = computed(() => {
        const componentType = {
          JeecgComponents: JeecgComponents,
          JEditorDemo: JEditorDemo,
          JCodeEditDemo: JCodeEditDemo,
          ImgDragSort: ImgDragSort,
          ImgTurnPage: ImgTurnPage,
          JeecgPdfView: JeecgPdfView,
          JUploadDemo: JUploadDemo,
        };
        return componentType[activeKey.value];
      });

      //usecomponentdynamic switchingtab
      function tabChange(key) {
        activeKey.value = key;
      }
      return {
        activeKey,
        currentComponent,
        tabChange,
      };
    },
  });
</script>
