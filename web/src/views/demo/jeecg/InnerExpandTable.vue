<template>
  <a-card :bordered="false">
    <BasicTable @register="registerTable" :expandedRowKeys="expandedRowKeys" :rowSelection="rowSelection" @expand="handleExpand">
      <template #tableTitle>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"> New</a-button>
      </template>
      <template #expandedRowRender>
        <BasicTable bordered size="middle" rowKey="id" :canResize="false" :columns="innerColumns" :dataSource="innerData" :pagination="false">
        </BasicTable>
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <JVxeTableModal @register="registerModal" @success="reload()"></JVxeTableModal>
  </a-card>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import JVxeTableModal from '/@/views/demo/vextable/jvxetable/JVxeTableModal.vue';
  //interfaceurl
  const url = {
    list: '/test/order/orderList',
    delete: '/test/order/delete',
    deleteBatch: '/test/order/deleteBatch',
    customerListByMainId: '/test/order/listOrderCustomerByMainId',
  };
  // Expandkey
  const expandedRowKeys = ref<any[]>([]);
  // choosekey
  const checkedKeys = ref<any[]>([]);
  // Subtable data
  const innerData = ref<any[]>([]);
  // Main table header
  const columns = [
    {
      title: 'Order number',
      align: 'center',
      dataIndex: 'orderCode',
      width: 100,
    },
    {
      title: 'Order type',
      align: 'center',
      dataIndex: 'ctype',
      width: 100,
      customRender: ({ text }) => {
        let re = '';
        if (text === '1') {
          re = 'Domestic orders';
        } else if (text === '2') {
          re = 'international orders';
        }
        return re;
      },
    },
    {
      title: 'order date',
      align: 'center',
      width: 100,
      dataIndex: 'orderDate',
    },
    {
      title: 'Order amount',
      align: 'center',
      dataIndex: 'orderMoney',
      width: 100,
    },
    {
      title: 'Order notes',
      align: 'center',
      dataIndex: 'content',
      width: 100,
    },
  ];
  // Subtable header
  const innerColumns = [
    {
      title: 'Customer name',
      align: 'center',
      width: 100,
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'gender',
      align: 'center',
      dataIndex: 'sex',
      customRender: function (text) {
        //console.log(typeof  text )
        //console.log(text)
        if (text.value == '1') {
          return 'male';
        } else if (text.value == '2') {
          return 'female';
        } else {
          return text;
        }
      },
    },
    {
      title: 'ID number',
      align: 'center',
      dataIndex: 'idcard',
    },
    {
      title: 'Telephone',
      dataIndex: 'telphone',
      align: 'center',
    },
  ];
  const list = (params) => defHttp.get({ url: url.list, params });
  const [registerModal, { openModal }] = useModal();
  const [registerTable, { reload }] = useTable({
    columns,
    api: list,
    rowKey: 'id',
    striped: true,
    useSearchForm: false,
    showTableSetting: true,
    clickToRowSelect: false,
    bordered: true,
    actionColumn: {
      width: 110,
      title: 'operate',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });

  /**
   * choose列配置
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 30,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  /**
   * choose事件
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }
  /**
   * Expand事件
   * */
  function handleExpand(expanded, record) {
    expandedRowKeys.value = [];
    innerData.value = [];
    if (expanded === true) {
      expandedRowKeys.value.push(record.id);
      defHttp.get({ url: url.customerListByMainId, params: { orderId: record.id } }, { isTransformResponse: false }).then((res) => {
        if (res.success) {
          innerData.value = res.result.records;
        }
      });
    }
  }
  /**
   * New事件
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
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
   * delete event
   */
  function handleDelete(record) {
    defHttp.delete({ url: url.delete, data: { id: record.id } }, { joinParamsToUrl: true }).then(() => {
      reload();
    });
  }
  /**
   * Action bar
   */
  function getTableAction(record) {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
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
<style scoped>
  .ant-card-body .table-operator {
    margin-bottom: 18px;
  }

  .ant-table-tbody .ant-table-row td {
    padding-top: 15px;
    padding-bottom: 15px;
  }

  .anty-row-operator button {
    margin: 0 5px;
  }

  .ant-btn-danger {
    background-color: #ffffff;
  }

  .ant-modal-cust-warp {
    height: 100%;
  }

  .ant-modal-cust-warp .ant-modal-body {
    height: calc(100% - 110px) !important;
    overflow-y: auto;
  }

  .ant-modal-cust-warp .ant-modal-content {
    height: 90% !important;
    overflow-y: hidden;
  }
</style>
