<template>
  <BasicModal :bodyStyle="{ padding: '20px' }" v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" width="80%" @ok="handleSubmit">
    <a-row :gutter="24">
      <a-col :span="10">
        <BasicForm @register="registerForm" ref="formRef" name="OpenApiForm" />
      </a-col>
      <a-col :span="14">
        <a-row :gutter="24">
          <a-col :span="24" style="margin-top: -0.6em">
            <JVxeTable
              keep-source
              ref="openApiHeader"
              :loading="openApiHeaderTable.loading"
              :columns="openApiHeaderTable.columns"
              :dataSource="openApiHeaderTable.dataSource"
              :height="240"
              :disabled="formDisabled"
              :rowNumber="true"
              :rowSelection="true"
              :toolbar="true"
              size="mini"
            />
          </a-col>
          <a-col :span="24">
            <JVxeTable
              keep-source
              ref="openApiParam"
              :loading="openApiParamTable.loading"
              :columns="openApiParamTable.columns"
              :dataSource="openApiParamTable.dataSource"
              :height="240"
              :disabled="formDisabled"
              :rowNumber="true"
              :rowSelection="true"
              :toolbar="true"
              size="mini"
            />
          </a-col>
        </a-row>
      </a-col>
    </a-row>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { JVxeTable } from '/@/components/jeecg/JVxeTable';
  import { useJvxeMethod } from '/@/hooks/system/useJvxeMethods.ts';
  import { formSchema, openApiHeaderJVxeColumns, openApiParamJVxeColumns } from '../OpenApi.data';
  import { saveOrUpdate, queryOpenApiHeader, queryOpenApiParam, getGenPath } from '../OpenApi.api';
  import { VALIDATE_FAILED } from '/@/utils/common/vxeUtils';
  import { useMessage } from "@/hooks/web/useMessage";

  // Emitsstatement
  const $message = useMessage();
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const formDisabled = ref(false);
  const refKeys = ref(['openApiHeader', 'openApiParam']);
  const activeKey = ref('openApiHeader');
  const openApiHeader = ref();
  const openApiParam = ref();
  const tableRefs = { openApiHeader, openApiParam };
  const openApiHeaderTable = reactive({
    loading: false,
    dataSource: [],
    columns: openApiHeaderJVxeColumns,
  });
  const openApiParamTable = reactive({
    loading: false,
    dataSource: [],
    columns: openApiParamJVxeColumns,
  });
  //Form configuration
  const [registerForm, { setProps, resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
    wrapperCol: { span: 24 },
  });
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await reset();
    setModalProps({ confirmLoading: false, showCancelBtn: data?.showFooter, showOkBtn: data?.showFooter });
    isUpdate.value = !!data?.isUpdate;
    formDisabled.value = !data?.showFooter;
    if (unref(isUpdate)) {
      //form assignment
      await setFieldsValue({
        ...data.record,
      });
      // Request the backend interface to obtain data
      //  requestSubTableData(queryOpenApiHeader, {id:data?.record?.id}, openApiHeaderTable)
      //  requestSubTableData(queryOpenApiParam, {id:data?.record?.id}, openApiParamTable)
      openApiHeaderTable.dataSource = !!data.record.headersJson?JSON.parse(data.record.headersJson):[];
      openApiParamTable.dataSource = !!data.record.paramsJson?JSON.parse(data.record.paramsJson):[];
    } else {
      //  /openapi/genpath
      const requestUrlObj = await getGenPath({});
      await setFieldsValue({
        requestUrl: requestUrlObj.result

      });
    }
    // Disable entire form when hiding bottom
    setProps({ disabled: !data?.showFooter });
  });
  //Method configuration
  const [handleChangeTabs, handleSubmit, requestSubTableData, formRef] = useJvxeMethod(
    requestAddOrEdit,
    classifyIntoFormData,
    tableRefs,
    activeKey,
    refKeys
  );

  //Set title
  const title = computed(() => (!unref(isUpdate) ? 'New' : !unref(formDisabled) ? 'edit' : 'Details'));

  async function reset() {
    await resetFields();
    activeKey.value = 'openApiHeader';
    openApiHeaderTable.dataSource = [];
    openApiParamTable.dataSource = [];
  }
  function classifyIntoFormData(allValues) {
    let main = Object.assign({}, allValues.formValue);
    return {
      ...main, // Expand
      headersJson: allValues.tablesValue[0].tableData,
      paramsJson: allValues.tablesValue[1].tableData,
    };
  }
  //form submission event
  async function requestAddOrEdit(values) {
    let headersJson = !!values.headersJson?JSON.stringify(values.headersJson):null;
    let paramsJson = !!values.headersJson?JSON.stringify(values.paramsJson):null;
    try {
      if (!!values.body){
        try {
          if (typeof JSON.parse(values.body)!='object'){
            $message.createMessage.error("JSONFormat error,Please check the input data");
            return;
          }
        } catch (e) {
          $message.createMessage.error("JSONFormat error,Please check the input data");
          return;
        }
      }
      setModalProps({ confirmLoading: true });
      values.headersJson = headersJson
      values.paramsJson = paramsJson
      //Submit form
      await saveOrUpdate(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  /** Time and number input box styles */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>
