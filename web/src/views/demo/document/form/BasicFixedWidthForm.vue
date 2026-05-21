<!-- fixedlabeltitle width -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" @submit="handleSubmit" style="margin-top: 20px" />
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
    showResetButton: false,
    submitButtonOptions: { text: 'submit', preIcon: '' },
    actionColOptions: { span: 17 },
    //uselabelWidthControl title width
    labelWidth: '150px',
    //uselabelCol的样式属性来Control title width
    labelCol: { style: { width: '150px' } },
    //Title alignment（left:left aligned，right：Align right），默认Align right
    labelAlign: 'right',
  });

  /**
   * 点击submit按钮的valuevalue
   * @param values
   */
  function handleSubmit(values: any) {
    console.log('submit按钮数据::::', values);
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
