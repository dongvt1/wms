<template>
  <PageWrapper title="Dynamic form example">
    <div class="mb-4">
      <a-button @click="changeLabel3" class="mr-2"> Change field3label </a-button>
      <a-button @click="changeLabel34" class="mr-2"> 同时Change field3,4label </a-button>
      <a-button @click="appendField" class="mr-2"> Go to field3Insert fields later10 </a-button>
      <a-button @click="deleteField" class="mr-2"> Delete field11 </a-button>
    </div>
    <CollapseContainer title="Dynamic form example,Dynamically changes based on other values ​​in the form">
      <BasicForm @register="register" />
    </CollapseContainer>

    <CollapseContainer class="mt-5" title="componentPropsdynamic change">
      <BasicForm @register="register1" />
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form/index';
  import { CollapseContainer } from '/@/components/Container/index';
  import { PageWrapper } from '/@/components/Page';
  import { usePermission } from '/@/hooks/web/usePermission';
  const { hasPermission } = usePermission();
  const schemas: FormSchema[] = [
    {
      field: 'field5',
      component: 'Switch',
      label: 'Whether to display fields1(csscontrol)',
      defaultValue: true,
      colProps: {
        span: 12,
      },
      labelWidth: 200,
    },
    {
      field: 'field1',
      component: 'Input',
      label: 'Field1',
      colProps: {
        span: 12,
      },
      show: ({ values }) => {
        return hasPermission('test001');
      },
    },
    {
      field: 'field6',
      component: 'Switch',
      label: 'Whether to display fields2(domcontrol)',
      defaultValue: true,
      colProps: {
        span: 12,
      },
      labelWidth: 200,
    },
    {
      field: 'field2',
      component: 'Input',
      label: 'Field2',
      colProps: {
        span: 12,
      },
      ifShow: ({ values }) => {
        return !!values.field6;
      },
    },
    {
      field: 'field7',
      component: 'Switch',
      label: '是否禁用Field3',
      colProps: {
        span: 12,
      },
      labelWidth: 200,
    },
    {
      field: 'field3',
      component: 'DatePicker',
      label: 'Field3',
      colProps: {
        span: 12,
      },
      dynamicDisabled: ({ values }) => {
        return !!values.field7;
      },
    },
    {
      field: 'field8',
      component: 'Switch',
      label: 'Field4Is it required?',
      colProps: {
        span: 12,
      },
      labelWidth: 200,
    },
    {
      field: 'field4',
      component: 'Select',
      label: 'Field4',
      colProps: {
        span: 12,
      },
      dynamicRules: ({ values }) => {
        return values.field8 ? [{ required: true, message: 'Field必填' }] : [];
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
      field: 'field11',
      component: 'DatePicker',
      label: 'Field11',
      colProps: {
        span: 8,
      },
    },
  ];

  const schemas1: FormSchema[] = [
    {
      field: 'f1',
      component: 'Input',
      label: 'F1',
      colProps: {
        span: 12,
      },
      labelWidth: 200,
      componentProps: ({ formModel }) => {
        return {
          placeholder: 'synchronousf2The value isf1',
          onChange: (e: ChangeEvent) => {
            formModel.f2 = e.target.value;
          },
        };
      },
    },
    {
      field: 'f2',
      component: 'Input',
      label: 'F2',
      colProps: {
        span: 12,
      },
      labelWidth: 200,
      componentProps: { disabled: true },
    },
    {
      field: 'f3',
      component: 'Input',
      label: 'F3',
      colProps: {
        span: 12,
      },
      labelWidth: 200,
      // @ts-ignore
      componentProps: ({ formActionType }) => {
        return {
          placeholder: 'Execute query when value changes,查看control台',
          onChange: async () => {
            const { validate } = formActionType;
            // tableActionOnly applicable to examples where the form is opened within the form
            // const { reload } = tableAction;
            const res = await validate();
            console.log(res);
          },
        };
      },
    },
  ];

  export default defineComponent({
    components: { BasicForm, CollapseContainer, PageWrapper },
    setup() {
      const [register, { setProps, updateSchema, appendSchemaByField, removeSchemaByFiled }] = useForm({
        labelWidth: 120,
        schemas,
        //Disable all components of the form
        disabled: true,
        labelCol: {
          xs: { span: 24 },
          sm: { span: 6 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 18 },
        },
        actionColOptions: {
          span: 24,
        },
      });
      const [register1] = useForm({
        labelWidth: 120,
        schemas: schemas1,
        actionColOptions: {
          span: 24,
        },
      });
      function changeLabel3() {
        updateSchema({
          field: 'field3',
          label: 'Field3 New',
        });
      }
      function changeLabel34() {
        updateSchema([
          {
            field: 'field3',
            label: 'Field3 New++',
          },
          {
            field: 'field4',
            label: 'Field4 New++',
          },
        ]);
      }

      function appendField() {
        appendSchemaByField(
          {
            field: 'field10',
            label: 'Field10',
            component: 'Input',
            colProps: {
              span: 8,
            },
          },
          'field3'
        );
      }
      function deleteField() {
        removeSchemaByFiled('field11');
      }
      return {
        register,
        register1,
        schemas,
        setProps,
        changeLabel3,
        changeLabel34,
        appendField,
        deleteField,
      };
    },
  });
</script>
