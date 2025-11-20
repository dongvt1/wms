<!-- Basic usage -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" @submit="handleSubmit" style="margin-top: 20px" />
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Custom form fields
  const formSchemas: FormSchema[] = [
    {
      //title name
      label: 'username(Later based onlabelLengthDefined length hidden)',
      //Field
      field: 'username',
      //components
      component: 'Input',
      //title width,Supports numbers and strings
      labelWidth: 150,
      //title length，Hide more than digits
      labelLength: 3,
    },
    {
      label: 'password',
      field: 'password',
      //子title name（after the main title）
      subLabel: '(Numbers and letters)',
      component: 'InputPassword',
      labelWidth: '150px',
    },
  ];

  /**
   * BasicFormBinding registration;
   * useForm It is the core of the entire framework and is used for form rendering.，It encapsulates many public methods;
   * support（schemas: Render form columns，autoSubmitOnEnter：Enter to submit,submitButtonOptions：Customize button text and icons, etc.）；
   * The platform passes this package，Simplified code，support自定义扩展;
   */
  const [registerForm] = useForm({
    //Registration form columns
    schemas: formSchemas,
    //Enter to submit
    autoSubmitOnEnter: true,
    //No reset button shown
    showResetButton: false,
    //Customize submit button text and icon
    submitButtonOptions: { text: 'submit', preIcon: '' },
    //Query column proportion 24represents a line Value range 0-24
    actionColOptions: { span: 17 },
  });

  /**
   * 点击submit按钮的valuevalue
   * @param values
   */
  function handleSubmit(values: any) {
    console.log('submit按钮数据::::', values);
  }
</script>

<style scoped></style>
