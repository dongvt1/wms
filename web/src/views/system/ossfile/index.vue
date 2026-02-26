<template>
  <div>
    <!--Reference table-->
    <BasicTable @register="registerTable">
      <!--slot:tabletitle-->
      <template #tableTitle>
        <a-upload name="file" :showUploadList="false" :action="ossAction" :headers="tokenHeader" :beforeUpload="beforeUpload" @change="handleChange">
          <a-button type="primary" preIcon="ant-design:upload-outlined">OSSFile upload</a-button>
        </a-upload>
        <a-upload
          name="file"
          :showUploadList="false"
          :action="minioAction"
          :headers="tokenHeader"
          :beforeUpload="beforeUpload"
          @change="handleChange"
        >
          <a-button type="primary" preIcon="ant-design:upload-outlined">MINIOFile upload</a-button>
        </a-upload>
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="system-ossfile" setup>
  //tsgrammar
  import { ref, computed, unref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './ossfile.data';
  import { list, deleteFile, getOssUrl, getMinioUrl } from './ossfile.api';
  import { useGlobSetting } from '/@/hooks/setting';
  import { getToken } from '/@/utils/auth';
  import {encryptByBase64} from "@/utils/cipher";

  const { createMessage } = useMessage();
  const glob = useGlobSetting();
  const tokenHeader = { 'X-Access-Token': getToken() };
  //registertabledata
  const [registerTable, { reload }] = useTable({
    api: list,
    rowKey: 'id',
    columns,
    formConfig: {
      labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    showTableSetting: true,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    tableSetting: { fullScreen: true },
    beforeFetch: (params) => {
      return Object.assign({ column: 'createTime', order: 'desc' }, params);
    },
    actionColumn: {
      width: 80,
      title: 'operate',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });
  /**
   * uploadurl
   */
  const ossAction = computed(() => `${glob.uploadUrl}${getOssUrl}`);
  const minioAction = computed(() => `${glob.uploadUrl}${getMinioUrl}`);

  /**
   * Preview
   */
  function handleView(record) {
    if (record && record.url) {
      console.log('glob.onlineUrl', glob.viewUrl);
      //update-begin---author:scott ---date:2024-06-03  for：【TV360X-952】upgrade tokkfileview4.1.0---
      // let filePath = encodeURIComponent(record.url);
      let url = encodeURIComponent(encryptByBase64(record.url));
      // //Document adoptionpdfPreview高级模式
      // if(filePath.endsWith(".pdf") || filePath.endsWith(".doc") || filePath.endsWith(".docx")){
      //   filePath = filePath
      // }
      let previewUrl = `${glob.viewUrl}?url=` + url;
      //update-end---author:scott ---date:2024-06-03  for：【TV360X-952】upgrade tokkfileview4.1.0---
      
      window.open(previewUrl, '_blank');
    }
  }

  /**
   * delete event
   */
  async function handleDelete(record) {
    await deleteFile({ id: record.id }, reload);
  }

  /**
   * upload前事件
   */
  function beforeUpload(file) {
    var fileType = file.type;
    if (fileType === 'image') {
      if (fileType.indexOf('image') < 0) {
        createMessage.warning('请upload图片');
        return false;
      }
    } else if (fileType === 'file') {
      if (fileType.indexOf('image') >= 0) {
        createMessage.warning('请upload文件');
        return false;
      }
    }
    return true;
  }

  /**
   * File upload事件
   */
  function handleChange(info) {
    if (info.file.status === 'done') {
      if (info.file.response.success) {
        reload();
        createMessage.success(`${info.file.name} upload成功!`);
      } else {
        createMessage.error(`${info.file.response.message}`);
      }
    } else if (info.file.status === 'error') {
      createMessage.error(`${info.file.response.message}`);
    }
  }

  /**
   * Action bar
   */
  function getTableAction(record) {
    return [
      {
        label: 'Preview',
        onClick: handleView.bind(null, record),
      },
      {
        label: 'delete',
        popConfirm: {
          title: '是否确认delete',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped></style>
