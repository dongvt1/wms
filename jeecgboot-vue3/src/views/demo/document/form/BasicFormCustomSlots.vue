<!-- slot -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px" @submit="handleSubmit">
      <!--  #nameCorresponding toformSchemascorrespondslot(name)slot    -->
      <template #name="{ model, field }">
        <JInput v-model:value="model[field]" />
      </template>
  </BasicForm>
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  //introduceCustomDemoCustom component
  import JInput from "/@/components/Form/src/jeecg/components/JInput.vue";

  //Custom form字段
  const formSchemas: FormSchema[] = [
    {
      field: 'name',
      label: 'Name',
      component: 'Input',
      slot:'name'
    },
    {
      field: 'phone',
      label: 'Contact information',
      component: 'Input',
    },
    {
      field: 'feedback',
      label: 'Problem feedback',
      component: 'InputTextArea',
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
   * submit信息
   */
  function handleSubmit(values) {
    console.log("values::",values);
  }
</script>

<style scoped>
  .font-color {
    font-size: 13px;
    color: #a1a1a1;
    margin-bottom: 5px;
  }
</style>
