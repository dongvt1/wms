<!-- Dynamic increase and decrease forms -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px;" @submit="handleSubmit">
    <!--  Add toinputslot  -->
    <template #addForm="{ field }">
      <a-button v-if="Number(field) === 0" @click="addField" style="width: 50px">+</a-button>
      <a-button v-if="Number(field) > 0" @click="delField(field)" style="width: 50px">-</a-button>
    </template>
  </BasicForm>
  <!--  <div style="margin: 20px auto; text-align: center">
    <a-button @click="addField">Add to表单项</a-button>
  </div>-->
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { CollapseContainer } from '/@/components/Container';
  import { ref } from 'vue';

  //Custom formField
  const formSchemas: FormSchema[] = [
    {
      field: 'name1',
      label: 'Name1',
      component: 'Input',
      // ifShow:false,
      colProps: {
        span: 8,
      },
    },
    {
      field: 'age1',
      label: 'age1',
      component: 'InputNumber',
      // ifShow:false,
      colProps: {
        span: 8,
      },
    },
    {
      field: '0',
      component: 'Input',
      // ifShow:false,
      label: ' ',
      colProps: {
        span: 8,
      },
      slot: 'addForm',
    },
  ];

  /**
   * BasicFormBinding registration;
   * appendSchemaByField:Add form item（Field）
   *
   * removeSchemaByFiled:Reduce form items（Field）
   */
  const [registerForm, { appendSchemaByField, removeSchemaByFiled }] = useForm({
    schemas: formSchemas,
    showResetButton: false,
    labelWidth: '150px',
    // showSubmitButton:false
    submitButtonOptions: { text: 'submit', preIcon: '' },
    actionColOptions: { span: 17 },
  });

  //Number of components
  let n = ref<number>(2);

  /**
   * Add toField
   * appendSchemaByFieldtype: ( schema: FormSchema, prefixField: string | undefined, first?: boolean | undefined ) => Promise<void>
   * illustrate: Insert into specified filed later，If no specified field，then insert to the end,when first = true inserted into the first position
   */
  async function addField() {
    //Add to表单Field，Inside isschemasCorresponding properties，Can be configured by yourself
    await appendSchemaByField(
      {
        field: `name${n.value}`,
        component: 'Input',
        label: 'Field' + n.value,
        colProps: {
          span: 8,
        },
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `sex${n.value}`,
        component: 'InputNumber',
        label: 'Field' + n.value,
        colProps: {
          span: 8,
        },
      },
      ''
    );

    await appendSchemaByField(
      {
        field: `${n.value}`,
        component: 'Input',
        label: ' ',
        colProps: {
          span: 8,
        },
        slot: 'addForm',
      },
      ''
    );
    n.value++;
  }

  /**
   * deleteField
   * type: (field: string | string[]) => Promise<void>
   * illustrate: according to field delete Schema
   * @param field when前Field名称
   */
  function delField(field) {
    //移除指定Field
    removeSchemaByFiled([`name${field}`, `sex${field}`, `${field}`]);
    n.value--;
  }

  /**
   * 点击submit按钮的valuevalue
   * @param values
   */
  function handleSubmit(values: any) {
    console.log('submit按钮数据::::', values);
  }
</script>

<style scoped>
  /** Number input box style */
  :deep(.ant-input-number) {
    width: 100%;
  }
</style>
