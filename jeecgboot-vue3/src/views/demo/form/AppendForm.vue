<template>
  <PageWrapper title="Form addition and deletion examples">
    <CollapseContainer title="Form addition and deletion">
      <BasicForm @register="register" @submit="handleSubmit">
        <template #add="{ field }">
          <Button v-if="Number(field) === 0" @click="add">+</Button>
          <Button v-if="field > 0" @click="del(field)">-</Button>
        </template>
      </BasicForm>
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { CollapseContainer } from '/@/components/Container/index';
  import { Input } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';
  import { Button } from '/@/components/Button';

  export default defineComponent({
    components: { BasicForm, CollapseContainer, PageWrapper, [Input.name]: Input, Button },
    setup() {
      const [register, { appendSchemaByField, removeSchemaByFiled, validate }] = useForm({
        schemas: [
          {
            field: 'field0a',
            component: 'Input',
            label: 'Field0',
            colProps: {
              span: 8,
            },
            required: true,
          },
          {
            field: 'field0b',
            component: 'Input',
            label: 'Field0',
            colProps: {
              span: 8,
            },
            required: true,
          },
          {
            field: '0',
            component: 'Input',
            label: ' ',
            colProps: {
              span: 8,
            },
            slot: 'add',
          },
        ],
        labelWidth: 100,
        actionColOptions: { span: 24 },
      });

      async function handleSubmit() {
        try {
          const data = await validate();
          console.log(data);
        } catch (e) {
          console.log(e);
        }
      }

      const n = ref(1);

      function add() {
        appendSchemaByField(
          {
            field: `field${n.value}a`,
            component: 'Input',
            label: 'Field' + n.value,
            colProps: {
              span: 8,
            },
            required: true,
          },
          ''
        );
        appendSchemaByField(
          {
            field: `field${n.value}b`,
            component: 'Input',
            label: 'Field' + n.value,
            colProps: {
              span: 8,
            },
            required: true,
          },
          ''
        );

        appendSchemaByField(
          {
            field: `${n.value}`,
            component: 'Input',
            label: ' ',
            colProps: {
              span: 8,
            },
            slot: 'add',
          },
          ''
        );
        n.value++;
      }

      function del(field) {
        removeSchemaByFiled([`field${field}a`, `field${field}b`, `${field}`]);
        n.value--;
      }

      return { register, handleSubmit, add, del };
    },
  });
</script>
