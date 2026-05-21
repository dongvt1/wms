<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    @ok="handleSubmit"
    :title="title"
    width="900px"
    wrapClassName="notice-cls-modal"
    :maxHeight="800"
    destroyOnClose
  >
    <BasicForm @register="registerForm">
      <template #msgTemplate="{ model, field }">
        <a-select v-model:value="model[field]" placeholder="Please select message template" :options="templateOption" @change="handleChange" />
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './notice.data';
  import { getTempList, saveOrUpdate } from './notice.api';
  // statementEmits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const record = ref<any>({});
  const templateOption = ref([]);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
    labelWidth: 100,
    baseRowStyle: { marginTop: '10px' },
    baseColProps: { xs: 24, sm: 12, md: 12, lg: 12, xl: 12, xxl: 12 },
  });
  //form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Load template
    await initTemplate();
    //Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      if (data.record.userIds) {
        data.record.userIds = data.record.userIds.substring(0, data.record.userIds.length - 1);
      }
      //form assignment
      await setFieldsValue({
        ...data.record,
      });
      record.value = data.record;
    } else {
      // update-begin--author:liaozhiyang---date:20250807---for：【JHHB-128】forward announcement
      //form assignment
      await setFieldsValue({
        ...data.record,
      });
      // update-end--author:liaozhiyang---date:20250807---for：【JHHB-128】forward announcement
    }
  });
  //Set title
  const title = computed(() => (!unref(isUpdate) ? 'New' : 'edit'));
  //form submission event
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Submit form
      //update-begin-author:liusq---date:20230404--for: [issue#429]New通知公告提交指定用户参数有undefined ---
      if(values.msgType==='ALL'){
        values.userIds = '';
      }else{
        values.userIds += ',';
      }
      //update-end-author:liusq---date:20230404--for: [issue#429]New通知公告提交指定用户参数有undefined ---
      if (isUpdate.value && record.value.sendStatus != '2') {
        values.sendStatus = '0';
      }
      await saveOrUpdate(values, isUpdate.value);
      //Close pop-up window
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
  //Initialize template
  async function initTemplate() {
    const res = await getTempList({ templateCategory: 'notice', pageSize: 100 });
    console.log('res', res);
    if (res.records && res.records.length > 0) {
      templateOption.value = res.records.map((item) => {
        return {
          label: item.templateName,
          value: item.templateCode,
          content: item.templateContent,
        };
      });
    }
  }

  /**
   * Template modification
   * @param val
   */
  function handleChange(val) {
    const content = templateOption.value.find((item: any) => item.value === val)?.content;
    if (content) {
      setFieldsValue({
        msgContent: content,
      });
    }
  }
</script>
<style scoped>
  .notice-cls-modal {
    top: 20px !important;
  }
</style>
