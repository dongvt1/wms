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
          <div>
            <a-row type="flex" style="margin-bottom: 10px" :gutter="16">
              <a-col :span="5">Customer name</a-col>
              <a-col :span="5">gender</a-col>
              <a-col :span="6">ID number</a-col>
              <a-col :span="6">Phone number</a-col>
              <a-col :span="2">operate</a-col>
            </a-row>
            <a-row type="flex" style="margin-bottom: 10px" :gutter="16" v-for="(item, index) in orderMainModel.jeecgOrderCustomerList" :key="index">
              <a-col :span="6" style="display: none">
                <a-form-item>
                  <a-input placeholder="id" v-model:value="item.id" />
                </a-form-item>
              </a-col>
              <a-col :span="5">
                <a-form-item>
                  <a-input placeholder="Customer name" v-model:value="item.name" />
                </a-form-item>
              </a-col>
              <a-col :span="5">
                <a-form-item>
                  <a-select placeholder="gender" v-model:value="item.sex">
                    <a-select-option value="1">male</a-select-option>
                    <a-select-option value="2">female</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item
                  :name="['jeecgOrderCustomerList', index, 'idcard']"
                  :rules="[{ required: true, message: 'Please enter your ID number', trigger: 'blur' }]"
                  :key="index"
                >
                  <a-input placeholder="ID number" v-model:value="item.idcard" />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item :name="['jeecgOrderCustomerList', index, 'telphone']">
                  <a-input placeholder="Phone number" v-model:value="item.telphone" />
                </a-form-item>
              </a-col>
              <a-col :span="2">
                <a-form-item>
                  <Icon icon="ant-design:minus-outlined" @click="delRowCustom(index)" style="fontsize: 20px" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-button type="dashed" style="width: 98%; margin-top: 10px" @click="addRowCustom">
              <Icon icon="ph:plus-bold" />
              添加Customer information
            </a-button>
          </div>
        </a-tab-pane>

        <a-tab-pane tab="Air ticket information" key="2" forceRender>
          <a-row type="flex" style="margin-bottom: 10px" :gutter="16">
            <a-col :span="6">flight number</a-col>
            <a-col :span="6">Flight time</a-col>
            <a-col :span="6">operate</a-col>
          </a-row>
          <a-row type="flex" style="margin-bottom: 10px" :gutter="16" v-for="(item, index) in orderMainModel.jeecgOrderTicketList" :key="index">
            <a-col :span="6" style="display: none">
              <a-form-item>
                <a-input placeholder="id" v-model:value="item.id" />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item
                :name="['jeecgOrderTicketList', index, 'ticketCode']"
                :rules="{ required: true, message: '请输入flight number', trigger: 'blur' }"
              >
                <a-input placeholder="flight number" v-model:value="item.ticketCode" />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item>
                <a-date-picker placeholder="Flight time" valueFormat="YYYY-MM-DD" v-model:value="item.tickectDate" />
              </a-form-item>
            </a-col>
            <a-col :span="6">
              <a-form-item>
                <Icon icon="ant-design:minus-outlined" @click="delRowTicket(index)" style="fontsize: 20px" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-button type="dashed" style="width: 98%; margin-top: 10px" @click="addRowTicket">
            <Icon icon="ph:plus-bold" />
            添加Air ticket information
          </a-button>
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
    name: 'tableModal',
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
        jeecgOrderCustomerList: [],
        jeecgOrderTicketList: [],
      });
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false });
        isUpdate.value = !!data?.isUpdate;
        reset();
        if (unref(isUpdate)) {
          rowId.value = data.record.id;
          Object.assign(orderMainModel, data.record);
          let params = { id: orderMainModel.id };
          const customerList = await orderCustomerList(params);
          orderMainModel.jeecgOrderCustomerList = customerList;
          const ticketList = await orderTicketList(params);
          orderMainModel.jeecgOrderTicketList = ticketList;
        }
      });
      const getTitle = computed(() => (!unref(isUpdate) ? 'New' : 'edit'));

      //Add rows dynamically
      function addRowCustom() {
        orderMainModel.jeecgOrderCustomerList.push({});
      }

      //Delete row
      function delRowCustom(index) {
        orderMainModel['jeecgOrderCustomerList'].splice(index, 1);
        orderMainModel.jeecgOrderCustomerList.splice(index, 1);
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
      }
      function addRowTicket() {
        orderMainModel.jeecgOrderTicketList.push({});
      }

      //Delete ticket
      function delRowTicket(index) {
        orderMainModel['jeecgOrderTicketList'].splice(index, 1);
        orderMainModel.jeecgOrderTicketList.splice(index, 1);
      }

      async function handleSubmit() {
        formRef.value
          .validate()
          .then(async () => {
            try {
              console.log('formData', JSON.stringify(orderMainModel));
              setModalProps({ confirmLoading: true });
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

      return {
        formRef,
        validatorRules,
        orderMainModel,
        registerModal,
        getTitle,
        labelCol,
        wrapperCol,
        addRowCustom,
        delRowCustom,
        addRowTicket,
        delRowTicket,
        handleSubmit,
      };
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
