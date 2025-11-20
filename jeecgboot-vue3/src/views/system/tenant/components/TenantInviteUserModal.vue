<!--Invite user to join tenant pop-up window-->
<template>
  <BasicModal @register="registerModal" :width="500" :title="title" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts">
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { defineComponent, ref } from 'vue';
export default defineComponent({
  name: 'TenantInviteUserModal',
  components: {
    BasicModal,
    BasicForm,
  },
  setup(props, { emit }) {
    const title = ref<string>('Invite members');
    const [registerForm, { resetFields, validate }] = useForm({
      schemas: [
        {
          label: 'Invitation method',
          field: 'invitedMode',
          component: 'RadioButtonGroup',
          defaultValue: 1,
          componentProps: {
            options: [
              { label: 'Phone number', value: 1 },
              { label: 'User account', value: 2 },
            ],
          },
        },
        {
          label: 'Phone number',
          field: 'phone',
          component: 'Input',
          ifShow: ({ values }) => values.invitedMode === 1,
          dynamicRules: ({ values }) => {
            return values.invitedMode === 1
              ? [
                  { required: true, message: '请填写Phone number' },
                  { pattern: /^1[3456789]\d{9}$/, message: 'Phone number码格式有误' },
                ]
              : [{ pattern: /^1[3456789]\d{9}$/, message: 'Phone number码格式有误' }];
          },
        },
        {
          field: 'user',
          component: 'Input',
          label: 'User account',
          ifShow: ({ values }) => values.invitedMode === 2,
          dynamicRules: ({ values }) => {
            return values.invitedMode === 2 ? [{ required: true, message: '请输入User account' }] : [];
          },
        },
      ],
      showActionButtonGroup: false,
    });
    //form assignment
    const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
      //Reset form
      await resetFields();
      setModalProps({ minHeight: 100 });
    });

    /**
     * submit，Return to tenantlistpage
     */
    async function handleSubmit() {
      let values = await validate();
      emit('inviteOk', values.phone, values.user);
      closeModal();
    }

    return {
      title,
      registerModal,
      registerForm,
      handleSubmit,
    };
  },
});
</script>

<style scoped></style>
