<!-- Custom component -->
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
      field: 'custom',
      label: 'Custom component',
      //引入Custom component
      component: 'JInput',
    },
  ];

  /**
   * BasicFormBinding registration;
   */
  const [registerForm] = useForm({
    //Registration form columns
    schemas: formSchemas,
    labelWidth: '150px',
    showActionButtonGroup: false,
  });
</script>

<style scoped></style>
