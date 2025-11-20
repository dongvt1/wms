<template>
  <a-button-group class="j-table-operator">
    <a-button type="primary" @click="setDisabled(0)">enable</a-button>
    <a-button type="primary" @click="setDisabled(1)">Disable</a-button>
    <a-button type="primary" @click="showUploadModal">File pop-up window</a-button>
  </a-button-group>
  <BasicForm @register="register" @submit="handleSubmit" />
  <JUploadModal v-model:value="uploadModalValue" @register="registerModel" />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { FormSchema, useForm, BasicForm } from '/@/components/Form';
  import { UploadTypeEnum } from '/@/components/Form/src/jeecg/components/JUpload';
  import { JUploadModal } from '/@/components/Form/src/jeecg/components/JUpload';
  import { useModal } from '/@/components/Modal';

  const uploadModalValue = ref('');

  const schemas: FormSchema[] = [
    {
      field: 'uploadFile',
      component: 'JUpload',
      helpMessage: 'Unlimited uploads',
      label: 'Upload files',
    },
    {
      field: 'uploadFileMax',
      component: 'JUpload',
      helpMessage: 'Most uploaded3files',
      label: 'Upload files（3）',
      componentProps: { maxCount: 3 },
    },
    {
      field: 'uploadImage',
      component: 'JUpload',
      label: 'Upload pictures',
      helpMessage: 'Unlimited uploads',
      componentProps: {
        fileType: UploadTypeEnum.image,
      },
    },
    {
      field: 'uploadImageMax',
      component: 'JUpload',
      label: 'Upload pictures（1）',
      helpMessage: 'Most uploaded1pictures',
      componentProps: {
        fileType: UploadTypeEnum.image,
        maxCount: 1,
      },
    },
  ];

  const [registerModel, { openModal }] = useModal();

  const [register, { setProps, validate, setFieldsValue }] = useForm({
    labelWidth: 120,
    schemas: schemas,
    actionColOptions: {
      span: 24,
    },
    compact: true,
    showResetButton: false,
    showSubmitButton: false,
    showAdvancedButton: false,
    disabled: false,
  });

  function handleSubmit(values) {
    console.log(values);
  }

  function setDisabled(flag) {
    setProps({ disabled: !!flag });
  }

  function showUploadModal() {
    openModal(true, {
      maxCount: 9,
      fileType: UploadTypeEnum.image,
    });
  }
</script>
