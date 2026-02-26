<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="40%">
    <BasicForm @register="registerForm">
      <template #pwd="{ model, field }">
        <a-row :gutter="8">
          <a-col :sm="15" :md="16" :lg="17" :xl="19">
            <a-input-password v-model:value="model[field]" placeholder="Please enter password" />
          </a-col>
          <a-col :sm="9" :md="7" :lg="7" :xl="5">
            <a-button type="primary" style="width: 100%" @click="handleTest">test</a-button>
          </a-col>
        </a-row>
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './datasource.data';
  import { saveOrUpdateDataSource, getDataSourceById, testConnection } from './datasource.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  // Emitsstatement
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Form configuration
  const [registerForm, { getFieldsValue, resetFields, validateFields, setFieldsValue, validate }] = useForm({
    // labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //Get details
      data.record = await getDataSourceById({ id: data.record.id });
      //form assignment
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //Set title
  const title = computed(() => (!unref(isUpdate) ? 'Add new data source' : 'Edit data source'));

  async function handleTest() {
    let keys = ['dbType', 'dbDriver', 'dbUrl', 'dbName', 'dbUsername', 'dbPassword'];
    // Get the value of the above field，and clear the verification status
    let fieldsValues = getFieldsValue(keys);
    let setFields = {};
    keys.forEach((key) => (setFields[key] = { value: fieldsValues[key], errors: null }));
    await validateFields(keys).then((values) => {
      let loading = createMessage.loading('Connecting....', 0);
      testConnection(values)
        .then((data) => {
          if (data.success) {
            createMessage.success('Connection successful');
          }
        })
        .catch((error) => {})
        .finally(() => loading());
    });
  }

  //form submission event
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      await saveOrUpdateDataSource(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
