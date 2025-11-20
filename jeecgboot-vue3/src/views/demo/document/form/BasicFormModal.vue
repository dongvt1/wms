<!-- popup form -->
<template>
  <div style="margin: 20px auto">
    <a-button type="primary" @click="openPopup" class="mr-2"> Open pop-up window </a-button>
  </div>
  <!-- Custom popup component -->
  <BasicModal @register="registerModal" title="popup form">
    <!-- Custom form -->
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  //Introduce dependencies
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { BasicModal } from '/@/components/Modal';
  import { useModal } from '/@/components/Modal';

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

  //BasicModalBinding registration;
  const [registerModal, { openModal }] = useModal();

  /**
   * BasicFormBinding registration;
   */
  const [registerForm, { validate, resetFields }] = useForm({
    schemas: formSchemas,
    //Hide action button
    showActionButtonGroup: false,
  });

  /**
   * Open pop-up window
   */
  async function openPopup() {
    //See details BasicModalmodule
    openModal(true, {});
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
