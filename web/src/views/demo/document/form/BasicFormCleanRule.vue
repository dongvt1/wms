<!-- Action disable form -->
<template>
  <div style="margin: 20px auto; text-align: center">
    <!-- all Trigger or clear all validations，visitor Only trigger or clear visitor verification -->
    <a-button @click="triggerFormRule('all')" class="mr-2"> Trigger form validation </a-button>
    <a-button @click="cancelFormRule('all')" class="mr-2"> Clear form validation </a-button>
    <a-button @click="triggerFormRule('visitor')" class="mr-2"> Only verify visitors </a-button>
    <a-button @click="cancelFormRule('visitor')" class="mr-2"> Only clear visitor verification </a-button>
  </div>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px;" />
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Custom formField
  const formSchemas: FormSchema[] = [
    {
      field: 'visitor',
      label: 'Visitors',
      component: 'Input',
      required: true,
    },
    {
      field: 'accessed',
      label: 'Date of visit',
      component: 'DatePicker',
      required: true,
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
   * clearValidate Clear all verifications，Supports de-validation of several fields like clearValidate(['visitor',...])
   * validate Verify all,Supports validation of several fields，validate(['visitor',...])
   * validateFields 只Supports validation of several fields，likevalidateFields(['visitor',...])
   */
  const [registerForm, { clearValidate, validateFields, validate }] = useForm({
    schemas: formSchemas,
    labelWidth: '150px',
    //Hide action button
    showActionButtonGroup: false,
    //Focus on the first one by default，Only supportsinput
    autoFocusFirstItem: true,
  });

  /**
   * Trigger form validation
   * @param type all All verifications visitor Only verify visitors
   */
  async function triggerFormRule(type) {
    if (type == 'all') {
      //触发All verifications
      await validate();
    } else {
      //触发Visitors验证
      //visitor Visitors的对应formSchemas fieldField
      await validateFields(['visitor']);
    }
  }

  /**
   * Trigger form validation
   * @param type all All verifications visitor Only verify visitors
   */
  async function cancelFormRule(type) {
    if (type == 'all') {
      //Cancel all verifications
      await clearValidate();
    } else {
      //只取消Visitors的验证
      //visitor Visitors的对应formSchemas fieldField
      await clearValidate(['visitor']);
    }
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
