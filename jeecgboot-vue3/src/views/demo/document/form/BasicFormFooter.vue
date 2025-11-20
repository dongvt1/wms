<!-- Custom footer -->
<template>
  <!-- Custom form -->
  <BasicForm @register="registerForm" style="margin-top: 20px">
    <template #formHeader>
      <div style="margin: 0 auto 20px">
        <span>I am a custom button</span>
      </div>
    </template>
    <template #formFooter>
      <div style="margin: 0 auto">
        <a-button type="primary" @click="save" class="mr-2"> save </a-button>
        <a-button type="primary" @click="saveDraft" class="mr-2"> save草稿 </a-button>
        <a-button type="error" @click="reset" class="mr-2"> reset </a-button>
      </div>
    </template>
  </BasicForm>
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Custom form字段
  const formSchemas: FormSchema[] = [
    {
      label: 'Employee name',
      field: 'name',
      component: 'Input',
    },
    {
      label: 'gender',
      field: 'sex',
      component: 'Select',
      //Fill in the properties of the component
      componentProps: {
        options: [
          { label: 'male', value: 1 },
          { label: 'female', value: 2 },
          { label: 'unknown', value: 3 },
        ],
      },
      //default value
      defaultValue: 3,
    },
    {
      label: 'age',
      field: 'age',
      component: 'Input',
    },
    {
      label: 'Entry time',
      subLabel: '( Optional )',
      field: 'entryTime',
      component: 'TimePicker',
    },
  ];

  /**
   * BasicFormBinding registration;
   */
  const [registerForm, { validate, resetFields }] = useForm({
    schemas: formSchemas,
    labelWidth: '150px',
    //Hide action button
    showActionButtonGroup: false,
  });

  /**
   * save
   */
  async function save() {
    //useuseFormMethod to get form value
    let values = await validate();
    console.log(values);
  }

  /**
   * save草稿
   */
  async function saveDraft() {
    //useuseFormmethodvalidateGet form value
    let values = await validate();
    console.log(values);
  }

  /**
   * reset
   */
  async function reset() {
    //useuseFormmethodresetFieldsClear value
    await resetFields();
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
