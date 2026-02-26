<!-- Operation formschemasConfiguration -->
<template>
  <div style="margin: 20px auto; text-align: center">
    <a-button @click="updateFormSchemas" class="mr-2"> Update field properties </a-button>
    <a-button @click="resetFormSchemas" class="mr-2"> Reset field properties </a-button>
  </div>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px" />
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
      componentProps: {
        disabled: true,
      },
    },
    {
      field: 'accessed',
      label: 'Date of visit',
      component: 'DatePicker',
      ifShow: false,
    },
    {
      field: 'phone',
      label: 'Visitor’s mobile phone number',
      component: 'Input',
      required: true,
    },
  ];

  /**
   * BasicFormBinding registration;
   * updateSchema Update field properties，supportschemas里面所有的Configuration
   * updateSchema([{ field: 'visitor', componentProps: { disabled: false },}, ... ]);
   * resetSchema Reset field properties，supportschemas里面所有的Configuration
   * resetSchema([{ field: 'visitor',label: 'Visitors',component: 'Input',},... ]);
   */
  const [registerForm, { updateSchema, resetSchema }] = useForm({
    schemas: formSchemas,
    //Hide action button
    showActionButtonGroup: false,
    labelWidth: '150px',
    //Focus on the first one by default，只supportinput
    autoFocusFirstItem: true,
  });

  /**
   * 清除表单Configuration
   */
  async function resetFormSchemas() {
    await resetSchema([
      {
        //Field required
        field: 'visitor',
        label: 'Visitors',
        component: 'Input',
      },
    ]);
  }

  /**
   * 更新表单Configuration
   */
  async function updateFormSchemas() {
    //support更新schemas里面所有的Configuration
    await updateSchema([
      {
        //Field required
        field: 'visitor',
        componentProps: {
          disabled: false,
        },
      },
      {
        field: 'accessed',
        ifShow: true,
      },
    ]);
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
