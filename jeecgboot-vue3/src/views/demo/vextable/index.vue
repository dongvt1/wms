<template>
  <div class="p-4">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-dropdown>
          <template #overlay>
            <a-menu @click="handleCreate">
              <a-menu-item :key="1">One to one example</a-menu-item>
              <a-menu-item :key="2">One-to-many example</a-menu-item>
              <a-menu-item :key="3">one to many(JVexTable)</a-menu-item>
            </a-menu>
          </template>
          <a-button type="primary">New <DownOutlined /></a-button>
        </a-dropdown>
      </template>
      <template #ctype="{ text }">
        {{ text === '1' ? 'Domestic orders' : text === '2' ? 'international orders' : text }}
      </template>
      <template #action="{ record }">
        <TableAction :actions="getAction(record)" :dropDownActions="getDropDownActions(record)" />
      </template>
    </BasicTable>
    <!--        <TableDrawer @register="registerDrawer" @success="handleSuccess" />-->
    <TableModal @register="registerModal" @success="handleSuccess" />
    <JVxeTableModal @register="registerVexTableModal" @success="handleSuccess"></JVxeTableModal>
    <OneToOneModal @register="registerOneToOneModal" @success="handleSuccess"></OneToOneModal>
  </div>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import TableDrawer from './drawer.vue';
  import TableModal from './modal.vue';
  import VexTableModal from './VexTableModal.vue';
  import JVxeTableModal from './jvxetable/JVxeTableModal.vue';
  import OneToOneModal from './OneToOneModal.vue';
  import { DownOutlined } from '@ant-design/icons-vue';
  import { useListPage } from '/@/hooks/system/useListPage';

  import { useModal } from '/@/components/Modal';
  import { columns } from './data';
  import { list, deleteOne } from './api';
  import { defHttp } from '/@/utils/http/axios';

  const [registerModal, { openModal }] = useModal();
  const [registerOneToOneModal, { openModal: openOneToOneModal }] = useModal();
  const [registerVexTableModal, { openModal: openVexTableModal }] = useModal();

  //Define table row operations
  const getAction = (record) => {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
      },
    ];
  };

  const getDropDownActions = (record) => {
    let arr = [
      {
        label: 'delete',
        popConfirm: {
          title: '是否delete？',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
    return arr;
  };

  // List page public parameters、method
  const { tableContext } = useListPage({
    tableProps: {
      api: list,
      columns: columns,
      useSearchForm: false,
      actionColumn: {
        width: 160,
        title: 'operate',
        dataIndex: 'action',
        slots: { customRender: 'action' },
      },
    },
  });

  //registertabledata
  const [registerTable, { reload }, { rowSelection }] = tableContext;
  //New类型
  //update-begin---author:wangshuai ---date:20220720  for：[VUEN-1661]One-to-many example，edit的时候，Sometimes one on one，有时候是one to many，默认one to many------------
  const addType = ref(3);
  //update-end---author:wangshuai ---date:20220720  for：[VUEN-1661]One-to-many example，edit的时候，Sometimes one on one，有时候是one to many，默认one to many--------------
  //Add event
  function handleCreate(e) {
    addType.value = e.key;
    let type = addType.value;
    if (type == 1) {
      openOneToOneModal(true, {
        isUpdate: false,
      });
    }
    if (type == 2) {
      openModal(true, {
        isUpdate: false,
      });
    }
    if (type == 3) {
      openVexTableModal(true, {
        isUpdate: false,
      });
    }
  }

  //edit事件
  function handleEdit(record: Recordable) {
    let type = addType.value;
    if (type == 1) {
      openOneToOneModal(true, {
        record,
        isUpdate: true,
      });
    }
    if (type == 2) {
      openModal(true, {
        record,
        isUpdate: true,
      });
    }
    if (type == 3) {
      openVexTableModal(true, {
        record,
        isUpdate: true,
      });
    }
  }

  async function handleDelete(record: Recordable) {
    await deleteOne({ id: record.id }, reload);
  }

  function handleSuccess() {
    reload();
  }
</script>
