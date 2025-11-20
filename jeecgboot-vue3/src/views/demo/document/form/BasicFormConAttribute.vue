<!-- Control properties -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px" />
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Custom form字段
  const formSchemas: FormSchema[] = [
    {
      label: 'Employee name',
      field: 'name',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
      defaultValue: 'Zhang San',
    },
    {
      label: 'gender',
      field: 'sex',
      component: 'Select',
      //Fill in the properties of the component
      componentProps: {
        options: [
          { label: 'male', value: 1 },
          { label: 'female', value: 2 },
          { label: 'unknown', value: 3 },
        ],
      },
      //default value
      defaultValue: 3,
    },
    {
      label: 'age',
      field: 'age',
      component: 'Input',
    },
    {
      label: 'Entry time',
      subLabel: '( Optional )',
      field: 'entryTime',
      component: 'TimePicker',
    },
  ];

  /**
   * BasicFormBinding registration;
   */
  const [registerForm] = useForm({
    //Registration form columns
    schemas: formSchemas,
    labelWidth: '150px',
    showResetButton: false,
    submitButtonOptions: { text: 'submit', preIcon: '' },
    actionColOptions: { span: 17 },
  });
</script>

<style scoped></style>
