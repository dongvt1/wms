<!-- Query area -->
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
      field: 'name',
      label: 'Name',
      component: 'Input',
    },
    {
      field: 'hobby',
      label: 'Hobby',
      component: 'Input',
    },
    {
      field: 'birthday',
      label: 'Birthday',
      component: 'DatePicker',
    },
    {
      field: 'joinTime',
      label: 'Entry time',
      component: 'RangePicker',
      componentProps: {
        valueType: 'Date',
      },
    },
    {
      field: 'workYears',
      label: 'Working year',
      component: 'JRangeNumber',
    },
    {
      field: 'sex',
      label: 'gender',
      component: 'Select',
      componentProps: {
        options: [
          {
            label: 'male',
            value: '1',
          },
          {
            label: 'female',
            value: '2',
          },
        ],
      },
    },
    {
      field: 'marital',
      label: 'Marital status',
      component: 'RadioGroup',
      componentProps: {
        options: [
          {
            label: 'unmarried',
            value: '1',
          },
          {
            label: 'Married',
            value: '2',
          },
        ],
      },
    },
  ];

  /**
   * BasicFormBinding registration;
   */
  const [registerForm] = useForm({
    //Map the values ​​in the time area in the form to 2fields, 'YYYY-MM-DD'date formatting
    fieldMapToTime: [['joinTime', ['joinTime_begin', 'joinTime_end'], 'YYYY-MM-DD']],
    //Registration form columns
    schemas: formSchemas,
    //Whether to display the expand collapse button，defaultfalse
    showAdvancedButton: true,
    //Fold beyond the specified number of rows，default3OK
    autoAdvancedCol: 3,
    //折叠时default显示OK数，default1OK
    alwaysShowLines: 2,

    //Map the values ​​in the numeric type area in the form to 2fields
    fieldMapToNumber: [['workYears', ['workYears_begin', 'workYears_end']]],
    //Proportion of each column，default一OK为24
    baseColProps: { span: 12 },
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
