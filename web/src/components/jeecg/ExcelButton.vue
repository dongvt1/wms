<template>
  <a-button type="primary" v-if="hasExportAuth() && config.export" preIcon="ant-design:export-outlined" @click="onExportXls()"> Export</a-button>
  <a-upload name="file" :showUploadList="false" v-if="hasImportAuth() && config.import" :customRequest="(file) => onImportXls(file)">
    <a-button type="primary" preIcon="ant-design:import-outlined">import</a-button>
  </a-upload>
</template>

<script lang="ts" setup name="ExcelButton">
  import { PropType } from 'vue';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { useMessage } from '/@/hooks/web/useMessage';

  // definition excel Parameters required by the method
  interface ExcelConfig {
    // Export配置
    exportConfig: {
      url: string;
      // Export文件名
      name?: string | (() => string);
      //Button permissions
      auth?: string | string[];
    };
    // import配置
    importConfig: {
      url: string;
      // Export成功后的回调
      success?: (fileInfo?: any) => void;
      //Button permissions
      auth?: string | string[];
    };
  }
  /**
   * definition组件parameter
   */
  const props = defineProps({
    config: {
      type: Object as PropType<ExcelConfig>,
      default: null,
    },
  });
  //Button permissions问题
  const { hasPermission } = usePermission();
  //importExport方法
  const { handleExportXls, handleImportXls } = useMethods();

  const $message = useMessage();
  // Export excel
  function onExportXls() {
    let { url, name } = props.config?.export ?? {};
    if (url) {
      let title = typeof name === 'function' ? name() : name;
      return handleExportXls(title as string, url);
    } else {
      $message.createMessage.warn('no delivery export.url parameter');
      return Promise.reject();
    }
  }

  // import excel
  function onImportXls(file) {
    let { url, success } = props.config?.import ?? {};
    if (url) {
      return handleImportXls(file, url, success);
    } else {
      $message.createMessage.warn('no delivery import.url parameter');
      return Promise.reject();
    }
  }

  // importButton permissions
  function hasImportAuth() {
    let auth = props.config?.import?.auth;
    return auth && auth.length > 0 ? hasPermission(auth) : true;
  }

  // ExportButton permissions
  function hasExportAuth() {
    let auth = props.config?.export?.auth;
    return auth && auth.length > 0 ? hasPermission(auth) : true;
  }
</script>

<style scoped lang="less"></style>
