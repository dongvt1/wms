<template>
  <BasicModal @register="register" title="Modal Title" :helpMessage="['hint1', 'hint2']" :okButtonProps="{ disabled: true }">
    <a-button type="primary" @click="closeModal" class="mr-2"> Close popup from within </a-button>
    <a-button type="primary" @click="setModalProps"> Modify from withintitle </a-button>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  export default defineComponent({
    components: { BasicModal },
    setup() {
      const [register, { closeModal, setModalProps }] = useModalInner();
      return {
        register,
        closeModal,
        setModalProps: () => {
          setModalProps({ title: 'Modal New Title' });
        },
      };
    },
  });
</script>
