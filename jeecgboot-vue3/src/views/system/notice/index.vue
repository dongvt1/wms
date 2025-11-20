<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">New</a-button>
<!--        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Export</a-button>-->
<!--        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">import</j-upload-button>-->
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                delete
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Batch operations
            <Icon style="fontsize: 12px" icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <NoticeModal @register="registerModal" @success="reload" />
    <DetailModal @register="register" :frameSrc="iframeUrl" />
  </div>
</template>
<script lang="ts" name="system-notice" setup>
  import { ref, onMounted } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import NoticeModal from './NoticeModal.vue';
  import DetailModal from './DetailModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useGlobSetting } from '/@/hooks/setting';
  import { getToken } from '/@/utils/auth';
  import { columns, searchFormSchema } from './notice.data';
  import { getList, deleteNotice, batchDeleteNotice,editIzTop, getExportUrl, getImportUrl, doReleaseData, doReovkeData } from './notice.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useAppStore } from '/@/store/modules/app';

  const appStore = useAppStore();
  const glob = useGlobSetting();
  const [registerModal, { openModal }] = useModal();
  const [register, { openModal: openDetail }] = useModal();
  const iframeUrl = ref('');
  const { createMessage, createConfirm } = useMessage();
  // List page public parameters、method
  const { prefixCls, onExportXls, onImportXls, tableContext, doRequest } = useListPage({
    designScope: 'notice-template',
    tableProps: {
      title: 'Message notification',
      api: getList,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      name: 'Message notification列表',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  //process coding
  const flowCode = 'dev_sys_announcement_001';
  /**
   * Add event
   */
  function handleAdd(record = {}) {
    openModal(true, {
      isUpdate: false,
      record,
    });
  }

  /**
   * Edit event
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * delete事件
   */
  async function handleDelete(record) {
    await deleteNotice({ id: record.id }, reload);
  }
  /**
   * Pin operation
   */
  async function handleTop(record, izTop) {
     await editIzTop({ id: record.id, izTop }, reload);
  }

  /**
   * 批量delete事件
   */
  async function batchHandleDelete() {
    doRequest(() => batchDeleteNotice({ ids: selectedRowKeys.value }));
  }
  /**
   * release
   */
  async function handleRelease(id) {
    await doReleaseData({ id });
    reload();
  }
  /**
   * Cancel
   */
  async function handleReovke(id) {
    await doReovkeData({ id });
    reload();
  }
  /**
   * Check
   */
  function handleDetail(record) {
    iframeUrl.value = `${glob.uploadUrl}/sys/annountCement/show/${record.id}?token=${getToken()}`;
    openDetail(true, { record });
  }

  /**
   * Operation column definition
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
        ifShow: record.sendStatus == 0 || record.sendStatus == '2',
      },
      {
        label: 'Check',
        onClick: handleDetail.bind(null, record),
        ifShow: record.sendStatus == 1,
      },
    ];
  }
  /**
   * Drop down action bar
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'delete',
        ifShow: record.sendStatus != 1,
        popConfirm: {
          title: '是否确认delete',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: 'release',
        ifShow: (!record?.izApproval || record.izApproval == '0') && record.sendStatus == 0,
        onClick: handleRelease.bind(null, record.id),
      },
      {
        label: 'Cancel',
        ifShow: record.sendStatus == 1,
        popConfirm: {
          title: '确定要Cancel吗？',
          confirm: handleReovke.bind(null, record.id),
        },
      },
      {
        label: 'release',
        ifShow: record.sendStatus == '2',
        popConfirm: {
          title: '确定要再次release吗？',
          confirm: handleRelease.bind(null, record.id),
        },
      },
      {
        label: 'pin to top',
        onClick: handleTop.bind(null, record, 1),
        ifShow: record.sendStatus == 1 && record.izTop == 0,
      },
      {
        label: '取消pin to top',
        onClick: handleTop.bind(null, record, 0),
        ifShow: record.sendStatus == 1 && record.izTop == 1,
      },
    ];
  }

  onMounted(() => {
    // update-begin--author:liaozhiyang---date:20250807---for：【JHHB-128】forward announcement
    const params = appStore.getMessageHrefParams;
    if (params?.add) {
      delete params.add;
      handleAdd(params);
      appStore.setMessageHrefParams('');
    }
    // update-begin--author:liaozhiyang---date:20250807---for：【JHHB-128】forward announcement
  });
</script>
