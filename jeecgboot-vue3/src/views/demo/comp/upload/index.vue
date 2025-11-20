<template>
  <PageWrapper title="Upload component example">
    <a-alert message="Basic example" />
    <BasicUpload :maxSize="20" :maxNumber="10" @change="handleChange" :api="uploadApi" class="my-5" :accept="['image/*']" />

    <a-alert message="Embed form,Add form validation" />

    <BasicForm @register="register" class="my-5" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicUpload } from '/@/components/Upload';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form/index';
  import { PageWrapper } from '/@/components/Page';
  import { Alert } from 'ant-design-vue';
  import { uploadApi } from '/@/api/sys/upload';

  const schemas: FormSchema[] = [
    {
      field: 'field1',
      component: 'Upload',
      label: 'Field1',
      colProps: {
        span: 8,
      },
      rules: [{ required: true, message: 'Please select a file to upload' }],
      componentProps: {
        api: uploadApi,
      },
    },
  ];
  export default defineComponent({
    components: { BasicUpload, BasicForm, PageWrapper, [Alert.name]: Alert },
    setup() {
      const { createMessage } = useMessage();
      const [register] = useForm({
        labelWidth: 120,
        schemas,
        actionColOptions: {
          span: 16,
        },
      });
      return {
        handleChange: (list: string[]) => {
          createMessage.info(`File uploaded${JSON.stringify(list)}`);
        },
        uploadApi,
        register,
      };
    },
  });
</script>
