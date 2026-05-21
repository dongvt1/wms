<template>
  <PageWrapper title="basic form" contentBackground content=" Form pages are used to collect or verify information from users，basic form常见于数据项较少的表单场景。" contentClass="p-4">
    <BasicForm @register="register" />
  </PageWrapper>
</template>
<script lang="ts">
  import { BasicForm, useForm } from '/@/components/Form';
  import { defineComponent } from 'vue';
  import { schemas } from './data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    name: 'FormBasicPage',
    components: { BasicForm, PageWrapper },
    setup() {
      const { createMessage } = useMessage();
      const [register, { validate, setProps }] = useForm({
        labelCol: {
          span: 8,
        },
        wrapperCol: {
          span: 10,
        },
        schemas: schemas,
        actionColOptions: {
          offset: 8,
          span: 12,
        },
        submitButtonOptions: {
          text: 'submit',
        },
        submitFunc: customSubmitFunc,
      });

      async function customSubmitFunc() {
        try {
          await validate();
          setProps({
            submitButtonOptions: {
              loading: true,
            },
          });
          setTimeout(() => {
            setProps({
              submitButtonOptions: {
                loading: false,
              },
            });
            createMessage.success('submit成功！');
          }, 2000);
        } catch (error) {}
      }

      return { register };
    },
  });
</script>
<style lang="less" scoped>
  .form-wrap {
    padding: 24px;
    background-color: @component-background;
  }
</style>
