<template>
  <a-card :bordered="false">
    <a-row>
      <!-- File tree on the left -->
      <a-col :span="4" class="clName">
        <a-tree :treeData="treeData" :defaultExpandAll="true" @select="onSelect" style="height: 500px; overflow-y: auto"> </a-tree>
      </a-col>
      <!--Thumbnail on the right-->
      <a-col :span="18">
        <div v-for="(file, key) in dataSource" :key="key">
          <a-col :span="24">
            <a-divider orientation="left">{{ file.fileName }}</a-divider>
          </a-col>
          <!-- preview area -->
          <a-col :span="24">
            <template v-if="file.filePdfPath">
              <div @click="pdfPreview(file.title)">
                <img style="width: 80px; height: 80px" src="../../../assets/images/pdf4.jpg" />
              </div>
            </template>
            <template v-else> (No material yet，Click"Select file"or"Scan and upload"Upload files) </template>
          </a-col>
        </div>
      </a-col>
    </a-row>
    <div style="display: none">
      <iframe id="pdfPreviewIframe" :src="url" frameborder="0" width="100%" height="550px" scrolling="auto"></iframe>
    </div>
  </a-card>
</template>

<script lang="ts">
  import { defineComponent, ref, unref, onMounted } from 'vue';
  import { useGlobSetting } from '/@/hooks/setting';
  import { getToken } from '/@/utils/auth';

  const mockdata = [
    {
      id: '1',
      key: '1',
      title: 'Example.pdf',
      fileCode: 'shili',
      fileName: 'Example',
      filePdfPath: 'Example',
    },
  ];

  export default defineComponent({
    name: 'JeecgPdfView',
    setup() {
      const glob = useGlobSetting();
      const treeData = ref([
        {
          title: 'allPDFelectronic file',
          key: '0-0',
          children: mockdata,
        },
      ]);
      const dataSource = ref(mockdata);
      const allData = ref(mockdata);
      const url = ref(`${glob.domainUrl}/sys/common/pdf/pdfPreviewIframe`);

      /**
       * Openiframewindow
       * @param title
       */
      function pdfPreview(title) {
        let iframe = document.getElementById('pdfPreviewIframe');
        let json = { title: title, token: getToken() };
        iframe.contentWindow.postMessage(json, '*');
      }

      // choosePDFdocument
      function onSelect(selectedKeys, info) {
        dataSource.value = [];
        if (selectedKeys[0] === undefined || selectedKeys[0] === '0-0') {
          dataSource.value = unref(allData);
        } else {
          dataSource.value.push(info.node.dataRef);
        }
      }

      return {
        url,
        dataSource,
        treeData,
        allData,
        onSelect,
        pdfPreview,
      };
    },
  });
</script>
