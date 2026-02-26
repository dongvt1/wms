<!-- Showing and hiding fields -->
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
      field: 'id',
      label: 'serial number',
      component: 'Input',
      //hideid，css control，will not be deleted dom（Support boolean type trueandfalse。Support dynamic value judgment，For details, please seeifShow）
      show: false,
    },
    {
      field: 'evaluate',
      label: 'Overall evaluation of the company',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: 'satisfy', value: '0' },
          { label: '不satisfy', value: '1' },
        ],
      },
      defaultValue: '0',
    },
    {
      field: 'describe',
      label: '不satisfy原因说明',
      component: 'InputTextArea',
      //ifShowandshowAttributes consistent，Just use one of them，valuesRepresents the value of the current form，js control，will be deleted dom
      ifShow: ({ values }) => {
        return values.evaluate == '1';
      },
    },
    {
      field: 'satisfiedLevel',
      label: 'satisfy度',
      component: 'Slider',
      componentProps: {
        tipFormatter: (value) => {
          return value + '%';
        },
      },
      //Dynamic disable，valuesRepresents the value of the current form，return trueorfalse
      dynamicDisabled: ({ values }) => {
        return values.evaluate == '1';
      },
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
</script>

<style scoped></style>
