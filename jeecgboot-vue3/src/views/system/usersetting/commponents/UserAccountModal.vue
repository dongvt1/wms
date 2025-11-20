<template>
  <BasicModal v-bind="$attrs" @register="registerModal" width="500px"  :title="title" @ok="handleSubmit" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema } from '../UserSetting.data';
import { ref, unref, defineEmits } from 'vue';
import { userEdit } from "../UserSetting.api";
import { useUserStore } from "/@/store/modules/user";
import { useMessage } from "/@/hooks/web/useMessage";

const userStore = useUserStore();
const { createMessage } = useMessage();
const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
  schemas: formSchema,
  showActionButtonGroup: false,
});
const userDetail = ref<any>({});
const isUpdate = ref<boolean>(false);
const title = ref<string>('');
const emit = defineEmits(['register','success']);
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  await resetFields();
  setModalProps({ confirmLoading: false });
  title.value = 'Edit profile';
  if(data.record.post){
    data.record.post = data.record.post.split(",")
  }
  if(data.record.relTenantIds){
    data.record.relTenantIds = data.record.relTenantIds.split(",")
  }
  userDetail.value = data.record;
  //update-begin---author:wangshuai ---date:20230130  for：[QQYUN-3976]personal information While editing，I just registered and didn’t set my birthday. There is a problem with birthday display------------
  if(data.record.birthday === 'Not filled in'){
    data.record.birthday = undefined;
  }
  //update-end---author:wangshuai ---date:20230130  for：[QQYUN-3976]personal information While editing，I just registered and didn’t set my birthday. There is a problem with birthday display------------
  await setFieldsValue({ ...data.record });
});

/**
 * Data modification and addition
 */
async function handleSubmit() {
  try {
    let values = await validate();
    setModalProps({ confirmLoading: true });
    //Submit form
    await userEdit(values).then((res) =>{
      if(res.success){
        createMessage.success(res.message)
      }else{
        createMessage.warn(res.message)
      }
    });
    //Update cache
    Object.assign(userDetail.value,values)
    userStore.setUserInfo(unref(userDetail));
    emit('success')
    //Close pop-up window
    closeModal();
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>
