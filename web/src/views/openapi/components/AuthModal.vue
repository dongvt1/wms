<template>
  <!--  <j-modal :title="title" :width="width" :visible="visible" @ok="handleOk" :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }" @cancel="handleCancel" cancelText="closure">-->
  <div style="position: relative;">
    <a-modal
      v-model:open="authDrawerOpen"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      :body-style="{ padding: '20px' }"
      style="color: red"
      :title="title"
      :width="600"
      @after-open-change="authDrawerOpenChange"
      @ok="handleOk"
    >
      <AuthForm ref="registerForm" @ok="submitCallback" :formDisabled="disableSubmit" :formBpm="false"></AuthForm>
    </a-modal>
  </div>
  <!--  </j-modal>-->
</template>

<script lang="ts" setup>
  import { ref, nextTick, defineExpose } from 'vue';
  import AuthForm from './AuthForm.vue';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';

  const title = ref<string>('');
  const width = ref<number>(800);
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
  const registerForm = ref();
  const emit = defineEmits(['register', 'success']);

  const authDrawerOpen = ref(false);
  const authDrawerOpenChange = (val: any) => {
    if(!val)
      registerForm.value.cleanData()
  };

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
   * Authorize
   * @param record
   */
  function edit(record) {
    title.value = disableSubmit.value ? 'Details' : 'Authorize';
    visible.value = true;
    authDrawerOpen.value = true;
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
    authDrawerOpen.value = false;
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
