<!-- form validation -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px" @submit="handleSubmit" />
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
      //Automatically trigger inspection，Boolean type
      required: true,
      //Do not add a title when inspecting
      rulesMessageJoinLabel: false,
    },
    {
      field: 'accessed',
      label: 'Date of visit',
      component: 'DatePicker',
      //Supports obtaining current value to determine triggering valuesRepresents the value of the current form
      required: ({ values }) => {
        return !values.accessed;
      },
    },
    {
      field: 'phone',
      label: 'Visitor’s mobile phone number',
      component: 'Input',
      //Support regular expressionspattern and Custom prompt information message
      rules: [{ required: false, message: 'Please enter the correct mobile phone number', pattern: /^1[3456789]\d{9}$/ }],
    },
  ];

  /**
   * BasicFormBinding registration;
   */
  const [registerForm] = useForm({
    //Registration form columns
    schemas: formSchemas,
    showResetButton: false,
    labelWidth: '150px',
    submitButtonOptions: { text: 'submit', preIcon: '' },
    actionColOptions: { span: 17 },
  });

  /**
   * submit事件
   */
  function handleSubmit(values: any) {}
</script>

<style scoped></style>
