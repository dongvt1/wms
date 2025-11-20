<template>
  <j-modal :title="title" :width="width" :maxHeight="200" :visible="visible" @ok="handleOk" :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }" @cancel="handleCancel" cancelText="closure">
    <OpenApiAuthForm ref="registerForm" @ok="submitCallback" :title="title" :formDisabled="disableSubmit" :formBpm="false"></OpenApiAuthForm>
  </j-modal>
</template>

<script lang="ts" setup>
  import { ref, nextTick, defineExpose } from 'vue';
  import OpenApiAuthForm from './OpenApiAuthForm.vue'
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';
  
  const title = ref<string>('');
  const width = ref<number>(800);
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
  const registerForm = ref();
  const emit = defineEmits(['register', 'success']);

  /**
   * New
   */
  function add() {
    title.value = 'New';
    visible.value = true;
    nextTick(() => {
      registerForm.value.add();
    });
  }
  
  /**
   * edit
   * @param record
   */
  function edit(record) {
    title.value = disableSubmit.value ? 'Details' : 'edit';
    visible.value = true;
    nextTick(() => {
      registerForm.value.edit(record);
    });
  }
  
  /**
   * OK button click event
   */
  function handleOk() {
    registerForm.value.submitForm();
  }

  /**
   * formSave callback event
   */
  function submitCallback() {
    handleCancel();
    emit('success');
  }

  /**
   * Cancel button callback event
   */
  function handleCancel() {
    visible.value = false;
  }

  defineExpose({
    add,
    edit,
    disableSubmit,
  });
</script>

<style lang="less">
  /**Hide style-modalOK button */
  .jee-hidden {
    display: none !important;
  }
</style>
<style lang="less" scoped></style>
