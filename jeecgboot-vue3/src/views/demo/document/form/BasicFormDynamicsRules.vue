<!-- Dynamic form validation -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px" @submit="handleSubmit" />
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { duplicateCheck } from '/@/views/system/user/user.api';

  //Custom form字段
  const formSchemas: FormSchema[] = [
    {
      field: 'visitor',
      label: 'Visitors',
      component: 'Input',
      //Automatically trigger inspection，Boolean type
      required: true,
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
      //Dynamic custom rules，values: All values ​​of the current form
      dynamicRules: ({ values }) => {
        //needreturn
        return [
          {
            //Form validation is enabled by default
            required: true,
            // value The value entered by the current mobile phone number
            validator: (_, value) => {
              //needreturn onePromiseobject
              return new Promise((resolve, reject) => {
                if (!value) {
                  reject('Please enter mobile phone number！');
                }
                //Verify that the mobile phone number is correct
                let reg = /^1[3456789]\d{9}$/;
                if (!reg.test(value)) {
                  reject('Please enter the correct mobile phone number！');
                }
                resolve();
              });
            },
          },
        ];
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

  /**
   * submit事件
   */
  function handleSubmit(values: any) {}
</script>

<style scoped></style>
