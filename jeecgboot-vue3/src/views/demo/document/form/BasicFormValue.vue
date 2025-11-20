<!-- Action form value -->
<template>
  <div style="margin: 20px auto; text-align: center">
    <a-button @click="getFormValue" class="mr-2"> Get form value </a-button>
    <a-button @click="clearFormValue" class="mr-2"> Clear form values </a-button>
    <a-button @click="updateFormValue" class="mr-2"> Update form values </a-button>
  </div>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px" />
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Custom form字段
  const formSchemas: FormSchema[] = [
    {
      field: 'visitor',
      label: 'Visitors',
      component: 'Input',
      required: true,
    },
    {
      field: 'accessed',
      label: 'Date of visit',
      component: 'DatePicker',
      required: true,
    },
    {
      field: 'phone',
      label: 'Visitor’s mobile phone number',
      component: 'Input',
      required: true,
    },
  ];

  /**
   * BasicFormBinding registration;
   * getFieldsValue Get all form values
   * validate Form value after verification,Supports validation of several fields，validate(['visitor',...])
   * setFieldsValue Update form values，like setFieldsValue({'visitor':'John Doe',...})
   * resetFields Clear all form values
   */
  const [registerForm, { getFieldsValue, setFieldsValue, resetFields, validate }] = useForm({
    schemas: formSchemas,
    //Hide action button
    showActionButtonGroup: false,
    labelWidth: '150px',
    //Focus on the first one by default，Only supportsinput
    autoFocusFirstItem: true,
  });

  /**
   * Get form value
   */
  async function getFormValue() {
    //Get all values
    let fieldsValue = await getFieldsValue();
    console.log('fieldsValue:::', fieldsValue);
    //Get all field values ​​after form verification passes
    fieldsValue = await validate();
    console.log('fieldsValue:::', fieldsValue);
    //form validation`visitorVisitors`The value obtained after passing
    fieldsValue = await validate(['visitor']);
    console.log('fieldsValue:::', fieldsValue);
  }

  /**
   * Clear form values
   */
  async function clearFormValue() {
    await resetFields();
  }

  /**
   * Update form values
   */
  async function updateFormValue() {
    console.log('I'm in');
    await setFieldsValue({ visitor: 'John Doe' });
  }
</script>

<style scoped>
  /** Time and number input box styles */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }
</style>
