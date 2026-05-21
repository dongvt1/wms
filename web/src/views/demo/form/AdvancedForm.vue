<template>
  <PageWrapper title="Collapsible form example">
    <CollapseContainer title="Base Shrink Example">
      <BasicForm @register="register" />
    </CollapseContainer>

    <CollapseContainer title="Exceed5Columns automatically collapse，Keep when folded2OK" class="mt-4">
      <BasicForm @register="register1" />
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form/index';
  import { CollapseContainer } from '/@/components/Container';
  import { PageWrapper } from '/@/components/Page';

  const getSchamas = (): FormSchema[] => {
    return [
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
      // {
      //   field: 'field7',
      //   component: 'RadioGroup',
      //   label: 'Field7',
      //   colProps: {
      //     span: 8,
      //   },
      //   componentProps: {
      //     options: [
      //       {
      //         label: 'Options1',
      //         value: '1',
      //       },
      //       {
      //         label: 'Options2',
      //         value: '2',
      //       },
      //     ],
      //   },
      // },
    ];
  };

  function getAppendSchemas(): FormSchema[] {
    return [
      {
        field: 'field10',
        component: 'Input',
        label: 'Field10',
        colProps: {
          span: 8,
        },
      },
      {
        field: 'field11',
        component: 'Input',
        label: 'Field11',
        colProps: {
          span: 8,
        },
      },
      {
        field: 'field12',
        component: 'Input',
        label: 'Field12',
        colProps: {
          span: 8,
        },
      },
      {
        field: 'field13',
        component: 'Input',
        label: 'Field13',
        colProps: {
          span: 8,
        },
      },
    ];
  }
  export default defineComponent({
    components: { BasicForm, CollapseContainer, PageWrapper },
    setup() {
      const [register] = useForm({
        labelWidth: 120,
        schemas: getSchamas(),
        actionColOptions: {
          span: 24,
        },
        compact: true,
        showAdvancedButton: true,
      });
      const extraSchemas: FormSchema[] = [];
      for (let i = 14; i < 30; i++) {
        extraSchemas.push({
          field: 'field' + i,
          component: 'Input',
          label: 'Field' + i,
          colProps: {
            span: 8,
          },
        });
      }
      const [register1] = useForm({
        labelWidth: 120,
        schemas: [...getSchamas(), ...getAppendSchemas(), { field: '', component: 'Divider', label: '更多Field' }, ...extraSchemas],
        actionColOptions: {
          span: 8,
        },
        compact: true,
        showAdvancedButton: true,
        autoAdvancedCol: 2,
        alwaysShowLines: 1,
      });
      return {
        register,
        register1,
      };
    },
  });
</script>
