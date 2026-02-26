<template>
  <div style="min-height: 400px">
    <BasicForm @register="registerForm">
      <template #msgTemplate="{ model, field }">
        <a-select v-model:value="model[field]" placeholder="Please select message template" :options="templateOption" @change="handleChange" />
      </template>
      <template #msgContent="{ model, field }">
        <div v-html="model[field]" class="article-content"></div>
      </template>
    </BasicForm>
    <div class="footer-btn" v-if="!formDisabled">
      <a-button @click="submitForm" pre-icon="ant-design:check" type="primary">carry pay</a-button>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { getBpmFormSchema } from './notice.data';
  import { getTempList, queryById, saveOrUpdate } from './notice.api';
  import { computed, ref } from 'vue';
  // Define properties
  const props = defineProps({
    formData: {
      type: Object,
      default: () => ({}),
    },
  });
  //form disabled
  const formDisabled = computed(() => {
    if (props.formData.disabled === false) {
      return false;
    }
    return true;
  });
  const templateOption = ref([]);
  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: getBpmFormSchema(props.formData),
    showActionButtonGroup: false,
    disabled: formDisabled.value,
    labelWidth: 100,
    baseRowStyle: { marginTop: '10px' },
    baseColProps: { xs: 24, sm: 12, md: 12, lg: 12, xl: 12, xxl: 12 },
  });

  //表单carrypay
  async function submitForm() {
    let values = await validate();
    if (values.msgType === 'ALL') {
      values.userIds = '';
    } else {
      values.userIds += ',';
    }
    console.log('form data', values);
    await saveOrUpdate(values, true);
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
  /**
   * Load data
   */
  async function initFormData() {
    let res = await queryById({ id: props.formData.dataId });
    if (res.success) {
      //Reset form
      await resetFields();
      const record = res.result;
      if (record.userIds) {
        record.userIds = record.userIds.substring(0, record.userIds.length - 1);
      }
      //form assignment
      await setFieldsValue({
        ...record,
      });
    }
  }
  //Load template
  initTemplate();
  //Load data
  initFormData();
</script>
<style lang="less" scoped>
  .footer-btn {
    width: 100%;
    text-align: center;
  }
  .article-content {
    max-width: 100%;
    max-height: 500px;
    overflow-y: auto;
  }
</style>
