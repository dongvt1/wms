<template>
  <div class="p-4">
    <div class="p-4 bg-white">
      <a-button-group class="j-table-operator">
        <a-button type="primary" @click="setDis(0)">enable</a-button>
        <a-button type="primary" @click="setDis(1)">Disable</a-button>
        <a-button type="primary" @click="getValues()">Validate the form and get the value</a-button>
        <a-button type="primary" @click="setValues()">Set value</a-button>
      </a-button-group>

      <BasicForm @register="register" @submit="handleSubmit" />
    </div>
  </div>
</template>

<script lang="ts">
  export default {
    title: 'rich text | Markdown',
    name: 'MarkdownDemo',
  };
</script>

<script lang="ts" setup>
  import { FormSchema, useForm, BasicForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage, createSuccessModal } = useMessage();

  const schemas: FormSchema[] = [
    {
      field: 'name',
      component: 'Input',
      label: 'Name',
      required: true,
      defaultValue: 'zhangsan',
    },
    {
      field: 'tinymce',
      component: 'JEditor',
      label: 'rich text',
      defaultValue: 'defaultValue',
      required: true,
    },
    {
      field: 'markdown',
      component: 'JMarkdownEditor',
      label: 'Markdown',
      defaultValue: '# Zhang San',
      required: true,
      componentProps: {
        height: 300,
      },
    },
  ];

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

  function setDis(flag) {
    setProps({ disabled: !!flag });
  }

  async function getValues() {
    try {
      const values = await validate();
      console.log(values);
      createSuccessModal({
        title: 'Verification passed',
        content: `${JSON.stringify(values)}`,
      });
    } catch (error) {
      createMessage.warning('Inspection failed');
    }
  }

  function setValues() {
    setFieldsValue({
      name: 'LiSi',
      markdown: '# John Doe',
      tinymce: '<p><strong><span style="font-size: 18pt;">open<span style="color: #e03e2d;">three</span>rich</span></strong></p>',
    });
  }
</script>

<style scoped></style>
