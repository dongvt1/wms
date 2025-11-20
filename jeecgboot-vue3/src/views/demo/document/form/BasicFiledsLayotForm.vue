<!-- Title and field layout -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" @submit="handleSubmit" style="margin: 20px auto"/>
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Custom form字段
  const formSchemas: FormSchema[] = [
    {
      label: 'Name',
      field: 'name',
      component: 'Input',
    },
    {
      label: 'age',
      field: 'password',
      component: 'InputNumber',
    },
    {
      label: 'Birthday',
      field: 'birthday',
      component: 'DatePicker',
    },
    {
      label: 'avatar',
      field: 'avatar',
      component: 'JImageUpload',
    },
  ];

  /**
   * BasicFormBinding registration;
   */
  const [registerForm] = useForm({
    //Registration form columns
    schemas: formSchemas,
    showActionButtonGroup: false,
    actionColOptions: { span: 12 },
    //Control the title width ratio
    labelCol: {
      xs: 2,
      sm: 2,
      md: 2,
      lg: 9,
      xl: 3,
      xxl: 2,
    },
    //Control component width ratio
    wrapperCol: {
      xs: 15,
      sm: 14,
      md: 16,
      lg: 17,
      xl: 19,
      xxl: 20,
    },
  });

  /**
   * Click the submit buttonvaluevalue
   * @param values
   */
  function handleSubmit(values: any) {
    console.log('Submit button data::::', values);
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
