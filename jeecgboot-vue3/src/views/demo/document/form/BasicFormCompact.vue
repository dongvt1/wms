<!-- Action disable form -->
<template>
  <div style="margin: 20px auto; text-align: center">
    <!-- passsetProps Can be set userForm properties in -->
    <!-- form size，Default is default   -->
    <a-button @click="setProps({ size: 'large' })" class="mr-2"> Change size to maximum </a-button>
    <a-button @click="setProps({ size: 'default' })" class="mr-2"> restore size </a-button>
    <a-button @click="setProps({ size: 'small' })" class="mr-2"> Change size to smallest </a-button>
    <!--  disabledform disabled  -->
    <a-button @click="setProps({ disabled: true })" class="mr-2"> Disable form </a-button>
    <a-button @click="setProps({ disabled: false })" class="mr-2"> Undisable </a-button>
    <!--  compact Whether it is a compact form  -->
    <a-button @click="setProps({ compact: true })" class="mr-2"> compact form </a-button>
    <a-button @click="setProps({ compact: false })" class="mr-2"> Restore normal spacing </a-button>
  </div>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px;" />
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { CollapseContainer } from '/@/components/Container';

  //Custom form字段
  const formSchemas: FormSchema[] = [
    {
      field: 'visitor',
      label: 'Visitors',
      component: 'Input',
    },
    {
      field: 'accessed',
      label: 'Date of visit',
      component: 'DatePicker',
    },
    {
      field: 'phone',
      label: 'Visitor’s mobile phone number',
      component: 'Input',
    },
  ];

  /**
   * BasicFormBinding registration;
   * setPropsMethods can be set dynamicallyuseFormproperties in
   */
  const [registerForm, { setProps }] = useForm({
    schemas: formSchemas,
    labelWidth: '150px',
    //Hide action button
    showActionButtonGroup: false,
    //Focus on the first one by default，Only supportsinput
    autoFocusFirstItem: true,
  });
</script>

<style scoped></style>
