<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="User selection list" width="1000px" @ok="handleSubmit" destroyOnClose @openChange="handleOpenChange">
    <BasicTable @register="registerTable" :rowSelection="rowSelection" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, unref, toRaw } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { userColumns, searchUserFormSchema } from '../role.data';
  import { list } from '../../user/user.api';
  // statementEmits
  const emit = defineEmits(['select', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal, { setModalProps, closeModal }] = useModalInner();
  //registertabledata
  const [registerTable, { reload }] = useTable({
    api: list,
    rowKey: 'id',
    columns: userColumns,
    formConfig: {
      labelWidth: 60,
      schemas: searchUserFormSchema,
      baseRowStyle: { maxHeight: '20px' },
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    showTableSetting: false,
    bordered: true,
    showIndexColumn: false,
    canResize: false,
  });
  /**
   * Select column configuration
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 50,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };
  /**
   * Select event
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }

  const handleOpenChange = (visible) => {
    // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1679】system role-The role user opens the pop-up window again to reset the previously selected state.
    if (visible) {
      checkedKeys.value = [];
    }
    // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1679】system role-The role user opens the pop-up window again to reset the previously selected state.
  };

  //Submit event
  function handleSubmit() {
    setModalProps({ confirmLoading: true });
    //Close pop-up window
    closeModal();
    //Refresh list
    emit('select', toRaw(unref(checkedKeys)));
    setModalProps({ confirmLoading: false });
  }
</script>
