<!-- custom rendering -->
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
  import { h } from 'vue';
  import { Input } from 'ant-design-vue';

  //Custom form字段
  const formSchemas: FormSchema[] = [
    {
      field: 'productName',
      label: 'Product name',
      component: 'Input',
    },
    {
      field: 'price',
      label: 'price',
      component: 'InputNumber',
    },
    {
      field: 'buyNums',
      label: 'Purchase quantity',
      component: 'InputNumber',
      //model Single sign form object，field current field
      render: ({ model, field }) => {
        //Render custom components，byInputFor example
        return h(Input, {
          placeholder: '请输入Purchase quantity',
          value: model[field],
          style: { width: '100%' },
          type: 'number',
          onChange: (e: ChangeEvent) => {
            model[field] = e.target.value;
          },
        });
      },
    },
    {
      field: 'describe',
      label: 'describe',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
      //rendering valuesAll values ​​of the current form
      render: ({ values }) => {
        let productName = values.productName?values.productName:'';
        let price = values.price ? values.price : 0;
        let buyNums = values.buyNums ? values.buyNums : 0;
        return '购买Product name：' + productName + ', 总price: ' + price * buyNums + 'Yuan';
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

<style scoped>
  /** Number input box style */
  :deep(.ant-input-number) {
    width: 100%;
  }
</style>
