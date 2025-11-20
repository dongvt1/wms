<!-- form layout -->
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
      label: 'Conference name',
      field: 'name',
      component: 'Input',
    },
    {
      label: 'Participating location',
      field: 'meetingLocation',
      component: 'Input',
    },
    {
      label: 'Number of participants',
      field: 'numberOfPart',
      component: 'InputNumber',
    },
    {
      label: 'meeting minutes',
      field: 'meetingMinutes',
      component: 'JUpload',
    },
  ];

  /**
   * BasicFormBinding registration;
   */
  const [registerForm] = useForm({
    //form layout属性，support（vertical,inline），Default isinline
    layout: 'inline',
    //Registration form columns
    schemas: formSchemas,
    //Query and reset buttons are not displayed
    showActionButtonGroup: false,
    //defaultrowline configuration,when layout for inline Take effect
    rowProps: { gutter: 24, justify: 'center', align: 'middle' },
    //overall situationcolColumn ratio(How many digits are displayed in each column?)，andschemasincolPropsAttributes consistent
    baseColProps: { span: 12 },
    //rowrow style
    baseRowStyle: { width: '100%' },
  });
</script>

<style scoped>
  /** 时间and数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }
</style>
