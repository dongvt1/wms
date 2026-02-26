<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" width="70%">
    <a-form ref="formRef" :model="orderMainModel" :label-col="labelCol" :wrapper-col="wrapperCol" :rules="validatorRules">
      <a-row class="form-row" :gutter="16">
        <a-col :lg="8">
          <a-form-item label="Order number" name="orderCode">
            <a-input v-model:value="orderMainModel.orderCode" placeholder="请输入Order number" />
          </a-form-item>
        </a-col>
        <a-col :lg="8">
          <a-form-item label="Order type">
            <a-select placeholder="请选择Order type" v-model:value="orderMainModel.ctype">
              <a-select-option value="1">Domestic orders</a-select-option>
              <a-select-option value="2">international orders</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :lg="8">
          <a-form-item label="order date">
            <a-date-picker showTime valueFormat="YYYY-MM-DD HH:mm:ss" v-model:value="orderMainModel.orderDate" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row class="form-row" :gutter="16">
        <a-col :lg="8">
          <a-form-item label="Order amount">
            <a-input v-model:value="orderMainModel.orderMoney" placeholder="请输入Order amount" />
          </a-form-item>
        </a-col>
        <a-col :lg="8">
          <a-form-item label="Order notes">
            <a-input v-model:value="orderMainModel.content" placeholder="请输入Order notes" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-tabs defaultActiveKey="1">
        <a-tab-pane tab="Customer information" key="1">
          <a-row class="form-row" :gutter="16">
            <a-col :lg="8">
              <a-form-item label="Customer name">
                <a-input v-model:value="orderMainModel.jeecgOrderCustomerList.name" placeholder="请输入Customer name" />
              </a-form-item>
            </a-col>
            <a-col :lg="8">
              <a-form-item label="Phone number">
                <a-input v-model:value="orderMainModel.jeecgOrderCustomerList.telphone" placeholder="请输入Phone number" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-tab-pane>

        <a-tab-pane tab="Air ticket information" key="2" forceRender>
          <a-row class="form-row" :gutter="16">
            <a-col :lg="8">
              <a-form-item label="flight number">
                <a-input v-model:value="orderMainModel.jeecgOrderTicketList.ticketCode" placeholder="请输入flight number" />
              </a-form-item>
            </a-col>
            <a-col :lg="8">
              <a-form-item label="Departure time">
                <a-date-picker showTime valueFormat="YYYY-MM-DD HH:mm:ss" v-model:value="orderMainModel.jeecgOrderTicketList.tickectDate" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-tab-pane>
      </a-tabs>
    </a-form>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { ValidateErrorEntity } from 'ant-design-vue/es/form/interface';
  import { saveOrUpdate } from './jvxetable/jvxetable.api';
  import { orderCustomerList, orderTicketList } from './api';

  export default defineComponent({
    name: 'OneToOneModal',
    components: { BasicModal },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const isUpdate = ref(true);
      const rowId = ref('');
      const formRef = ref();
      const labelCol = reactive({
        xs: { span: 24 },
        sm: { span: 5 },
      });
      const wrapperCol = reactive({
        xs: { span: 24 },
        sm: { span: 16 },
      });
      const validatorRules = {
        orderCode: [{ required: true, message: 'Order number不能为空', trigger: 'blur' }],
      };
      const orderMainModel = reactive({
        id: null,
        orderCode: '',
        orderMoney: '',
        ctype: '',
        content: '',
        jeecgOrderCustomerList: {
          name: '',
          telphone: '',
        },
        jeecgOrderTicketList: {
          ticketCode: '',
          tickectDate: '',
        },
      });
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        reset();
        isUpdate.value = !!data?.isUpdate;
        if (unref(isUpdate)) {
          rowId.value = data.record.id;
          Object.assign(orderMainModel, data.record);
          let params = { id: orderMainModel.id };
          const customerList = await orderCustomerList(params);
          //update-begin---author:wangshuai ---date:20220629  for：[VUEN-1484]On the one-to-many example page，Edit a line（Qingdao orderA0001），Customer information无法填入------------
          orderMainModel.jeecgOrderCustomerList = customerList[0]?customerList[0]:{};
          //update-end---author:wangshuai ---date:20220629  for：[VUEN-1484]On the one-to-many example page，Edit a line（Qingdao orderA0001），Customer information无法填入--------------
          const ticketList = await orderTicketList(params);
          //update-begin---author:wangshuai ---date:20220629  for：[VUEN-1484]On the one-to-many example page，Edit a line（Qingdao orderA0001），Customer information无法填入------------
          orderMainModel.jeecgOrderTicketList = ticketList[0]?ticketList[0]:{};
          //update-end---author:wangshuai ---date:20220629  for：[VUEN-1484]On the one-to-many example page，Edit a line（Qingdao orderA0001），Customer information无法填入--------------
        }
      });
      const getTitle = computed(() => (!unref(isUpdate) ? 'New' : 'edit'));

      function reset() {
        orderMainModel.id = null;
        orderMainModel.orderCode = '';
        orderMainModel.orderMoney = '';
        orderMainModel.orderDate = null;
        orderMainModel.ctype = '';
        orderMainModel.content = '';
        orderMainModel.jeecgOrderCustomerList = {};
        orderMainModel.jeecgOrderTicketList = {};
      }
      async function handleSubmit() {
        formRef.value
          .validate()
          .then(async () => {
            try {
              console.log('formData', JSON.stringify(orderMainModel));
              setModalProps({ confirmLoading: true });
              orderMainModel.jeecgOrderCustomerList =
                Object.keys(orderMainModel.jeecgOrderCustomerList).length > 0 ? [orderMainModel.jeecgOrderCustomerList] : [];
              orderMainModel.jeecgOrderTicketList =
                Object.keys(orderMainModel.jeecgOrderTicketList).length > 0 ? [orderMainModel.jeecgOrderTicketList] : [];
              await saveOrUpdate(orderMainModel, unref(isUpdate));
              closeModal();
              emit('success');
            } finally {
              setModalProps({ confirmLoading: false });
            }
          })
          .catch((error: ValidateErrorEntity<any>) => {
            console.log('error', error);
          });
      }

      return { formRef, validatorRules, orderMainModel, registerModal, getTitle, labelCol, wrapperCol, handleSubmit };
    },
  });
</script>
<style scoped>
  .ant-btn {
    padding: 0 10px;
    margin-left: 3px;
  }

  .ant-form-item-control {
    line-height: 0px;
  }

  /** Main form line spacing */
  .ant-form .ant-form-item {
    margin-bottom: 10px;
  }

  /** TabPage line spacing */
  .ant-tabs-content .ant-form-item {
    margin-bottom: 0px;
  }
</style>
