<!-- Field title hints and prefixes -->
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
      field: 'month',
      label: 'current month',
      component: 'Input',
      suffix: 'moon',
    },
    {
      field: 'lateNumber',
      label: 'Number of times late',
      component: 'InputNumber',
      //Help information：You can return directlyString(helpMessage:"Number of times late")，You can also get form values，Dynamic filling
      helpMessage: ({ values }) => {
        return '当前Number of times late' + values.lateNumber + ', Deduction' + values.lateNumber * 50 + 'Yuan';
      },
      defaultValue: 0,
    },
    {
      field: 'lateReason',
      label: 'Reason for being late',
      component: 'Input',
      helpMessage: 'What's the reason for being late for work?',
      //Custom prompt attributes，need to be combinedhelpMessageused together
      helpComponentProps: {
        maxWidth: '200px',
        color: '#66CCFF',
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
