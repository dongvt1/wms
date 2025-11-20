<template>
  <PageWrapper title="Form basic example" contentFullHeight>
    <CollapseContainer title="Basic example">
      <BasicForm
        autoFocusFirstItem
        :labelWidth="200"
        :schemas="schemas"
        :actionColOptions="{ span: 24 }"
        :labelCol="{ span: 8 }"
        @submit="handleSubmit"
        @reset="handleReset"
      >
        <template #jAreaLinkage="{ model, field }">
          <JAreaLinkage v-model:value="model[field]" :showArea="true" :showAll="false" />
        </template>
        <template #localSearch="{ model, field }">
          <ApiSelect
            :api="optionsListApi"
            showSearch
            v-model:value="model[field]"
            optionFilterProp="label"
            resultField="list"
            labelField="name"
            valueField="id"
          />
        </template>
        <template #selectA="{ model, field }">
          <a-select :options="optionsA" mode="multiple" v-model:value="model[field]" @change="valueSelectA = model[field]" allowClear />
        </template>
        <template #selectB="{ model, field }">
          <a-select :options="optionsB" mode="multiple" v-model:value="model[field]" @change="valueSelectB = model[field]" allowClear />
        </template>
        <template #remoteSearch="{ model, field }">
          <ApiSelect
            :api="optionsListApi"
            showSearch
            v-model:value="model[field]"
            :filterOption="false"
            resultField="list"
            labelField="name"
            valueField="id"
            @search="onSearch"
            :params="searchParams"
          />
        </template>
      </BasicForm>
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { computed, defineComponent, unref, ref } from 'vue';
  import { BasicForm, FormSchema, ApiSelect, JAreaLinkage } from '/@/components/Form/index';
  import { CollapseContainer } from '/@/components/Container';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';

  import { optionsListApi } from '/@/api/demo/select';
  import { useDebounceFn } from '@vueuse/core';
  import { treeOptionsListApi } from '/@/api/demo/tree';
  import { Select } from 'ant-design-vue';
  import { cloneDeep } from 'lodash-es';

  const valueSelectA = ref<string[]>([]);
  const valueSelectB = ref<string[]>([]);
  const options = ref<Recordable[]>([]);
  for (let i = 1; i < 10; i++) options.value.push({ label: 'Options' + i, value: `${i}` });

  const optionsA = computed(() => {
    return cloneDeep(unref(options)).map((op) => {
      op.disabled = unref(valueSelectB).indexOf(op.value) !== -1;
      return op;
    });
  });
  const optionsB = computed(() => {
    return cloneDeep(unref(options)).map((op) => {
      op.disabled = unref(valueSelectA).indexOf(op.value) !== -1;
      return op;
    });
  });
  const provincesOptions = [
    {
      id: 'guangdong',
      label: 'Guangdong Province',
      value: '1',
      key: '1',
    },
    {
      id: 'jiangsu',
      label: 'Jiangsu Province',
      value: '2',
      key: '2',
    },
  ];
  const citiesOptionsData = {
    guangdong: [
      {
        label: 'Zhuhai City',
        value: '1',
        key: '1',
      },
      {
        label: 'Shenzhen City',
        value: '2',
        key: '2',
      },
      {
        label: 'Guangzhou City',
        value: '3',
        key: '3',
      },
    ],
    jiangsu: [
      {
        label: 'Nanjing City',
        value: '1',
        key: '1',
      },
      {
        label: 'Wuxi City',
        value: '2',
        key: '2',
      },
      {
        label: 'suzhou city',
        value: '3',
        key: '3',
      },
    ],
  };

  const schemas: FormSchema[] = [
    {
      field: 'divider-basic',
      component: 'Divider',
      label: 'Basic fields',
    },
    {
      field: 'field1',
      component: 'Input',
      label: 'Field1',

      colProps: {
        span: 8,
      },
      // componentProps:{},
      // can func
      componentProps: ({ schema, formModel }) => {
        console.log('form:', schema);
        console.log('formModel:', formModel);
        return {
          placeholder: 'Customizeplaceholder',
          onChange: (e: any) => {
            console.log(e);
          },
        };
      },
      renderComponentContent: () => {
        return {
          prefix: () => 'pSlot',
          suffix: () => 'sSlot',
        };
      },
    },
    {
      field: 'field2',
      component: 'Input',
      label: 'Field2',
      defaultValue: '111',
      colProps: {
        span: 8,
      },
      componentProps: {
        onChange: (e: any) => {
          console.log(e);
        },
      },
      suffix: 'sky',
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
    {
      field: 'field8',
      component: 'Checkbox',
      label: 'Field8',
      colProps: {
        span: 8,
      },
      renderComponentContent: 'Check',
    },
    {
      field: 'field9',
      component: 'Switch',
      label: 'Field9',
      colProps: {
        span: 8,
      },
    },
    {
      field: 'field10',
      component: 'RadioButtonGroup',
      label: 'Field10',
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
      field: 'field11',
      component: 'Cascader',
      label: 'Field11',
      colProps: {
        span: 8,
      },
      componentProps: {
        options: [
          {
            value: 'zhejiang',
            label: 'Zhejiang',
            children: [
              {
                value: 'hangzhou',
                label: 'Hangzhou',
                children: [
                  {
                    value: 'xihu',
                    label: 'West Lake',
                  },
                ],
              },
            ],
          },
          {
            value: 'jiangsu',
            label: 'Jiangsu',
            children: [
              {
                value: 'nanjing',
                label: 'Nanjing',
                children: [
                  {
                    value: 'zhonghuamen',
                    label: 'Zhong Hua Men',
                  },
                ],
              },
            ],
          },
        ],
      },
    },
    {
      field: 'divider-api-select',
      component: 'Divider',
      label: 'Remote drop down demo',
    },
    {
      field: 'field30',
      component: 'ApiSelect',
      label: 'Lazy loading of remote dropdown',
      required: true,
      componentProps: {
        // more details see /src/components/Form/src/components/ApiSelect.vue
        api: optionsListApi,
        params: {
          id: 1,
        },
        resultField: 'list',
        // use name as label
        labelField: 'name',
        // use id as value
        valueField: 'id',
        // not request untill to select
        immediate: false,
        onChange: (e) => {
          console.log('selected:', e);
        },
        // atfer request callback
        onOptionsChange: (options) => {
          console.log('get options', options.length, options);
        },
      },
      colProps: {
        span: 8,
      },
      defaultValue: '0',
    },
    {
      field: 'field311',
      component: 'JAreaLinkage',
      label: 'Province and city selection',
      helpMessage: ['JAreaLinkagecomponents', 'Province and city selection'],
      required: true,
      slot: 'jAreaLinkage',
      colProps: {
        span: 8,
      },
      defaultValue: ['130000', '130200'],
    },
    {
      field: 'field31',
      component: 'Input',
      label: 'Drop down local search',
      helpMessage: ['ApiSelectcomponents', 'Local search of remote data sources', '只发起一次请求获取所有Options'],
      required: true,
      slot: 'localSearch',
      colProps: {
        span: 8,
      },
      defaultValue: '0',
    },
    {
      field: 'field32',
      component: 'Input',
      label: 'Drop down remote search',
      helpMessage: ['ApiSelectcomponents', 'Send keywords to the interface for remote search'],
      required: true,
      slot: 'remoteSearch',
      colProps: {
        span: 8,
      },
      defaultValue: '0',
    },
    {
      field: 'field33',
      component: 'ApiTreeSelect',
      label: 'Remote drop-down tree',
      helpMessage: ['ApiTreeSelectcomponents', '使用接口提供的数据生成Options'],
      required: true,
      componentProps: {
        api: treeOptionsListApi,
        resultField: 'list',
      },
      colProps: {
        span: 8,
      },
    },
    {
      field: 'field34',
      component: 'ApiRadioGroup',
      label: 'remoteRadio',
      helpMessage: ['ApiRadioGroupcomponents', '使用接口提供的数据生成Options'],
      required: true,
      componentProps: {
        api: optionsListApi,
        params: {
          count: 2,
        },
        resultField: 'list',
        // use name as label
        labelField: 'name',
        // use id as value
        valueField: 'id',
      },
      defaultValue: '1',
      colProps: {
        span: 8,
      },
    },
    {
      field: 'field35',
      component: 'ApiRadioGroup',
      label: 'remoteRadio',
      helpMessage: ['ApiRadioGroupcomponents', '使用接口提供的数据生成Options'],
      required: true,
      componentProps: {
        api: optionsListApi,
        params: {
          count: 2,
        },
        resultField: 'list',
        // use name as label
        labelField: 'name',
        // use id as value
        valueField: 'id',
        isBtn: true,
      },
      colProps: {
        span: 8,
      },
    },
    {
      field: 'divider-linked',
      component: 'Divider',
      label: 'Field联动',
    },
    {
      field: 'province',
      component: 'Select',
      label: 'province',
      colProps: {
        span: 8,
      },
      componentProps: ({ formModel, formActionType }) => {
        return {
          options: provincesOptions,
          placeholder: 'province与City联动',
          onChange: (e: any) => {
            // console.log(e)
            let citiesOptions = e == 1 ? citiesOptionsData[provincesOptions[0].id] : citiesOptionsData[provincesOptions[1].id];
            // console.log(citiesOptions)
            if (e === undefined) {
              citiesOptions = [];
            }
            formModel.city = undefined; //  reset city value
            const { updateSchema } = formActionType;
            updateSchema({
              field: 'city',
              componentProps: {
                options: citiesOptions,
              },
            });
          },
        };
      },
    },
    {
      field: 'city',
      component: 'Select',
      label: 'City',
      colProps: {
        span: 8,
      },
      componentProps: {
        options: [], // defalut []
        placeholder: 'province与City联动',
      },
    },
    {
      field: 'divider-selects',
      component: 'Divider',
      label: 'Mutually exclusive multiple selections',
      helpMessage: ['twoSelectShared data source', 'However, you cannot select items that have been selected by the other party.'],
    },
    {
      field: 'selectA',
      component: 'Select',
      label: 'mutually exclusiveSelectA',
      slot: 'selectA',
      defaultValue: [],
      colProps: {
        span: 8,
      },
    },
    {
      field: 'selectB',
      component: 'Select',
      label: 'mutually exclusiveSelectB',
      slot: 'selectB',
      defaultValue: [],
      colProps: {
        span: 8,
      },
    },
    {
      field: 'divider-others',
      component: 'Divider',
      label: 'other',
    },
    {
      field: 'field20',
      component: 'InputNumber',
      label: 'Field20',
      required: true,
      colProps: {
        span: 8,
      },
    },
    {
      field: 'field21',
      component: 'Slider',
      label: 'Field21',
      componentProps: {
        min: 0,
        max: 100,
        range: true,
        marks: {
          20: '20°C',
          60: '60°C',
        },
      },
      colProps: {
        span: 8,
      },
    },
    {
      field: 'field22',
      component: 'Rate',
      label: 'Field22',
      defaultValue: 3,
      colProps: {
        span: 8,
      },
      componentProps: {
        disabled: false,
        allowHalf: true,
      },
    },
  ];

  export default defineComponent({
    components: {
      BasicForm,
      CollapseContainer,
      PageWrapper,
      ApiSelect,
      JAreaLinkage,
      ASelect: Select,
    },
    setup() {
      const check = ref(null);
      const { createMessage } = useMessage();
      const keyword = ref<string>('');
      const searchParams = computed<Recordable>(() => {
        return { keyword: unref(keyword) };
      });

      function onSearch(value: string) {
        keyword.value = value;
      }
      function areaChange(value) {
        alert(value);
      }

      return {
        schemas,
        optionsListApi,
        optionsA,
        optionsB,
        valueSelectA,
        valueSelectB,
        onSearch: useDebounceFn(onSearch, 300),
        searchParams,
        handleReset: () => {
          keyword.value = '';
        },
        handleSubmit: (values: any) => {
          createMessage.success('click search,values:' + JSON.stringify(values));
        },
        check,
      };
    },
  });
</script>
