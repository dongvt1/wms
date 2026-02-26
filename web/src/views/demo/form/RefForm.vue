<template>
  <PageWrapper title="RefOperation example">
    <div class="mb-4">
      <a-button @click="setProps({ labelWidth: 150 })" class="mr-2"> ChangelabelWidth </a-button>
      <a-button @click="setProps({ labelWidth: 120 })" class="mr-2"> reductionlabelWidth </a-button>
      <a-button @click="setProps({ size: 'large' })" class="mr-2"> ChangeSize </a-button>
      <a-button @click="setProps({ size: 'default' })" class="mr-2"> reductionSize </a-button>
      <a-button @click="setProps({ disabled: true })" class="mr-2"> Disable form </a-button>
      <a-button @click="setProps({ disabled: false })" class="mr-2"> Undisable </a-button>
      <a-button @click="setProps({ compact: true })" class="mr-2"> compact form </a-button>
      <a-button @click="setProps({ compact: false })" class="mr-2"> reduction正常间距 </a-button>
      <a-button @click="setProps({ actionColOptions: { span: 8 } })" class="mr-2"> Operation button location </a-button>
    </div>
    <div class="mb-4">
      <a-button @click="setProps({ showActionButtonGroup: false })" class="mr-2"> Hide action button </a-button>
      <a-button @click="setProps({ showActionButtonGroup: true })" class="mr-2"> Show action buttons </a-button>
      <a-button @click="setProps({ showResetButton: false })" class="mr-2"> Hide reset button </a-button>
      <a-button @click="setProps({ showResetButton: true })" class="mr-2"> Show reset button </a-button>
      <a-button @click="setProps({ showSubmitButton: false })" class="mr-2"> Hide query button </a-button>
      <a-button @click="setProps({ showSubmitButton: true })" class="mr-2"> Show query button </a-button>
      <a-button
        @click="
          setProps({
            resetButtonOptions: {
              disabled: true,
              text: 'resetNew',
            },
          })
        "
        class="mr-2"
      >
        修改reset按钮
      </a-button>
      <a-button
        @click="
          setProps({
            submitButtonOptions: {
              disabled: true,
              loading: true,
            },
          })
        "
        class="mr-2"
      >
        Modify query button
      </a-button>
    </div>
    <CollapseContainer title="userefExample of calling a form internal function">
      <BasicForm :schemas="schemas" ref="formElRef" :labelWidth="100" @submit="handleSubmit" :actionColOptions="{ span: 24 }" />
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicForm, FormSchema, FormActionType, FormProps } from '/@/components/Form/index';
  import { CollapseContainer } from '/@/components/Container/index';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';

  const schemas: FormSchema[] = [
    {
      field: 'field1',
      component: 'Input',
      label: 'Field1',
      colProps: {
        span: 8,
      },
      componentProps: {
        placeholder: 'Customizeplaceholder',
        onChange: (e: any) => {
          console.log(e);
        },
      },
    },
    {
      field: 'field2',
      component: 'Input',
      label: 'Field2',
      colProps: {
        span: 8,
      },
    },
    {
      field: 'field3',
      component: 'DatePicker',
      label: 'Field3',
      colProps: {
        span: 8,
      },
    },
    {
      field: 'field4',
      component: 'Select',
      label: 'Field4',
      colProps: {
        span: 8,
      },
      componentProps: {
        options: [
          {
            label: 'Options1',
            value: '1',
            key: '1',
          },
          {
            label: 'Options2',
            value: '2',
            key: '2',
          },
        ],
      },
    },
    {
      field: 'field5',
      component: 'CheckboxGroup',
      label: 'Field5',
      colProps: {
        span: 8,
      },
      componentProps: {
        options: [
          {
            label: 'Options1',
            value: '1',
          },
          {
            label: 'Options2',
            value: '2',
          },
        ],
      },
    },
    {
      field: 'field7',
      component: 'RadioGroup',
      label: 'Field7',
      colProps: {
        span: 8,
      },
      componentProps: {
        options: [
          {
            label: 'Options1',
            value: '1',
          },
          {
            label: 'Options2',
            value: '2',
          },
        ],
      },
    },
  ];

  export default defineComponent({
    components: { BasicForm, CollapseContainer, PageWrapper },
    setup() {
      const formElRef = ref<Nullable<FormActionType>>(null);
      const { createMessage } = useMessage();
      return {
        formElRef,
        schemas,
        handleSubmit: (values: any) => {
          createMessage.success('click search,values:' + JSON.stringify(values));
        },
        setProps(props: FormProps) {
          const formEl = formElRef.value;
          if (!formEl) return;
          formEl.setProps(props);
        },
      };
    },
  });
</script>
