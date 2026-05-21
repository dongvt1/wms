<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" width="70%" @fullScreen="handleFullScreen">
    <a-form ref="formRef" :model="orderMainModel" :label-col="labelCol" :wrapper-col="wrapperCol" :rules="validatorRules">
      <!-- update-begin--author:liaozhiyang---date:20230803---for：【QQYUN-5866】When the mouse is placed, left and right scroll bars appear -->
      <div style="overflow-x: hidden">
        <a-row class="form-row" :gutter="16">
          <a-col :lg="8">
            <a-form-item label="Order number" name="orderCode">
              <a-input v-model:value="orderMainModel.orderCode" placeholder="请输入Order number" />
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="Order type" name="ctype">
              <a-select placeholder="请选择Order type" v-model:value="orderMainModel.ctype">
                <a-select-option value="1">Domestic orders</a-select-option>
                <a-select-option value="2">international orders</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="order date" name="orderDate">
              <a-date-picker showTime valueFormat="YYYY-MM-DD HH:mm:ss" v-model:value="orderMainModel.orderDate" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="16">
          <a-col :lg="8">
            <a-form-item label="Order amount" name="orderMoney">
              <a-input v-model:value="orderMainModel.orderMoney" placeholder="请输入Order amount" />
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="Order notes" name="content">
              <a-input v-model:value="orderMainModel.content" placeholder="请输入Order notes" />
            </a-form-item>
          </a-col>
        </a-row>
      </div>
      <!-- update-end--author:liaozhiyang---date:20230803---for：【QQYUN-5866】When the mouse is placed, left and right scroll bars appear -->
      <!-- subform area -->
      <a-tabs v-model:activeKey="activeKey" @change="handleChangeTabs">
        <a-tab-pane tab="Customer information" key="tableRef1">
          <JVxeTable
            ref="tableRef1"
            stripe
            toolbar
            rowNumber
            rowSelection
            resizable
            keepSource
            :height="tableH"
            :checkbox-config="{ range: true }"
            :loading="table1.loading"
            :columns="table1.columns"
            :dataSource="table1.dataSource"
          ></JVxeTable>
        </a-tab-pane>

        <a-tab-pane tab="Air ticket information" key="tableRef2" forceRender>
          <JVxeTable
            ref="tableRef2"
            stripe
            toolbar
            rowNumber
            rowSelection
            resizable
            keepSource
            :height="tableH"
            :checkbox-config="{ range: true }"
            :loading="table2.loading"
            :columns="table2.columns"
            :dataSource="table2.dataSource"
          ></JVxeTable>
        </a-tab-pane>
      </a-tabs>
    </a-form>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { JVxeTable } from '/src/components/jeecg/JVxeTable';
  import { columns, columns1 } from './jvxetable.data';
  import { orderCustomerList, orderTicketList, saveOrUpdate } from './jvxetable.api';
  import { useJvxeMethod } from '/@/hooks/system/useJvxeMethods.ts';
  export default defineComponent({
    name: 'JVexTableModal',
    components: { BasicModal, JVxeTable },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const tableH = ref(300);
      const isUpdate = ref(true);
      const tableRef1 = ref();
      const tableRef2 = ref();
      const refKeys = ref(['tableRef1', 'tableRef2']);
      const activeKey = ref('tableRef1');
      const tableRefs = { tableRef1, tableRef2 };
      const labelCol = reactive({
        xs: { span: 24 },
        sm: { span: 5 },
      });
      const wrapperCol = reactive({
        xs: { span: 24 },
        sm: { span: 16 },
      });
      // Customer information
      const table1 = reactive({
        loading: false,
        dataSource: [],
        columns,
      });
      // Air ticket information
      const table2 = reactive({
        loading: false,
        dataSource: [],
        columns: columns1,
      });
      const orderMainModel = reactive({
        id: null,
        orderCode: '',
        orderMoney: '',
        ctype: '',
        content: '',
        jeecgOrderCustomerList: [],
        jeecgOrderTicketList: [],
      });
      const [handleChangeTabs, handleSubmit, requestSubTableData, formRef] = useJvxeMethod(
        requestAddOrEdit,
        classifyIntoFormData,
        tableRefs,
        activeKey,
        refKeys
      );
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        reset();
        isUpdate.value = !!data?.isUpdate;
        if (unref(isUpdate)) {
          Object.assign(orderMainModel, data.record);
          //Load subtable data
          let params = { id: orderMainModel.id };
          requestSubTableData(orderCustomerList, params, table1);
          requestSubTableData(orderTicketList, params, table2);
        }
      });

      const validatorRules = { orderCode: [{ required: true, message: 'Order number不能为空', trigger: 'blur' }] };
      const getTitle = computed(() => (!unref(isUpdate) ? 'New' : 'edit'));

      function classifyIntoFormData(allValues) {
        let orderMain = Object.assign(orderMainModel, allValues.formValue);
        return {
          ...orderMain, // Expand
          jeecgOrderCustomerList: allValues.tablesValue[0].tableData,
          jeecgOrderTicketList: allValues.tablesValue[1].tableData,
        };
      }
      function reset() {
        orderMainModel.id = null;
        orderMainModel.orderCode = '';
        orderMainModel.orderMoney = '';
        orderMainModel.orderDate = null;
        orderMainModel.ctype = '';
        orderMainModel.content = '';
        orderMainModel.jeecgOrderCustomerList = [];
        orderMainModel.jeecgOrderTicketList = [];
        table1.dataSource = [];
        table2.dataSource = [];
      }
      async function requestAddOrEdit(values) {
        setModalProps({ confirmLoading: true });
        //Submit form
        await saveOrUpdate(values, unref(isUpdate));
        //Close pop-up window
        closeModal();
        //Refresh list
        emit('success');
      }
      // update-begin--author:liaozhiyang---date:20230804---for：【QQYUN-5866】Adaptable number of enlarged lines
      const handleFullScreen = (val) => {
        tableH.value=val ? document.documentElement.clientHeight - 387 :  300;
      };
      // update-end--author:liaozhiyang---date:20230804---for：【QQYUN-5866】Adaptable number of enlarged lines
      return {
        formRef,
        activeKey,
        table1,
        table2,
        tableRef1,
        tableRef2,
        getTitle,
        labelCol,
        wrapperCol,
        validatorRules,
        orderMainModel,
        registerModal,
        handleChangeTabs,
        handleSubmit,
        handleFullScreen,
        tableH,
      };
    },
  });
</script>
<style scoped></style>
