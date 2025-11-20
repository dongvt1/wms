<!-- slot -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px">
    <!--  #phoneCorresponding toformSchemascorrespondslot(phone)slot    -->
    <template #phone="{ model, field }">
      <!-- If the component requires two-way binding，modelcurrent form object，fieldCurrent field name  -->
      <a-input v-model:value="model[field]" placeholder="Please enter mobile phone number" />
      <span class="font-color">Please enter your mobile phone number，Make it easier for us to contact you</span>
    </template>
    <template #feedback="{ model, field }">
      <JEditor v-model:value="model[field]" placeholder="Please enter problem feedback" />
      <span class="font-color">Please provide pictures and texts，Facilitate us to understand the problem and provide timely feedback</span>
    </template>
  </BasicForm>
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import JEditor from '/@/components/Form/src/jeecg/components/JEditor.vue';

  //Custom form字段
  const formSchemas: FormSchema[] = [
    {
      field: 'name',
      label: 'Name',
      component: 'Input',
    },
    {
      field: 'phone',
      label: 'Contact information',
      component: 'Input',
      slot: 'phone',
    },
    {
      field: 'feedback',
      label: 'Problem feedback',
      component: 'InputTextArea',
      slot: 'feedback',
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

<style scoped>
  .font-color {
    font-size: 13px;
    color: #a1a1a1;
    margin-bottom: 5px;
  }
</style>
