<!-- Action form value -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px" />
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { schemas } from './exampleCustom.data';

  /**
   * BasicFormBinding registration;
   */
  const [registerForm, { getFieldsValue, setFieldsValue, resetFields, validate }] = useForm({
    schemas: schemas,
    labelWidth: '150px',
    //Hide action button
    showActionButtonGroup: false,
  });
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
