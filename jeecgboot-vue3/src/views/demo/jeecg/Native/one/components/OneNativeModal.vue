<template>
  <BasicModal
    :title="title"
    :width="width"
    :visible="visible"
    :height="600"
    @ok="handleOk"
    :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }"
    @cancel="handleCancel"
    cancelText="closure"
  >
    <OneNativeForm ref="realForm" @ok="submitCallback" :formDisabled="disableSubmit"></OneNativeForm>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, nextTick } from 'vue';
  import OneNativeForm from './OneNativeForm.vue';
  import { BasicModal } from '/@/components/Modal';
  
  const title = ref<string>('');
  const width = ref<number>(800);
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
  const realForm = ref();
  const emit = defineEmits(['register', 'ok']);

  function add() {
    title.value = 'New';
    visible.value = true;
    nextTick(() => {
      realForm.value.add();
    });
  }

  function edit(record) {
    title.value = disableSubmit.value ? 'Details' : 'edit';
    visible.value = true;
    nextTick(() => {
      realForm.value.edit(record);
    });
  }

  function handleOk() {
    realForm.value.submitForm();
  }

  function submitCallback() {
    handleCancel();
    emit('ok');
  }

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
