<template>
  <div class="p-4">
    <!--Define table-->
    <BasicTable @register="registerTable">
      <!-- Search area slot custom query -->
      <template #form-email="{ model, field }">
        <a-input placeholder="Please enter your email" v-model:value="model[field]" addon-before="Mail:" addon-after=".com"></a-input>
      </template>
      <!--Action bar-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="basic-table-demo" setup>
  import { ActionItem, BasicColumn, BasicTable, FormSchema, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { defHttp } from '/@/utils/http/axios';
  //Define table列
  const columns: BasicColumn[] = [
    {
      title: 'Name',
      dataIndex: 'name',
      width: 170,
      align: 'left',
      resizable: true,
      sorter: {
        multiple: 1,
      },
    },
    {
      title: 'keywords',
      dataIndex: 'keyWord',
      width: 130,
      resizable: true,
    },
    {
      title: 'Check in time',
      dataIndex: 'punchTime',
      width: 140,
      resizable: true,
    },
    {
      title: 'salary',
      dataIndex: 'salaryMoney',
      width: 140,
      resizable: true,
      sorter: {
        multiple: 2,
      },
    },
    {
      title: 'bonus',
      dataIndex: 'bonusMoney',
      width: 140,
      resizable: true,
    },
    {
      title: 'gender',
      dataIndex: 'sex',
      sorter: {
        multiple: 3,
      },
      filters: [
        { text: 'male', value: '1' },
        { text: 'female', value: '2' },
      ],
      customRender: ({ record }) => {
        return record.sex ? (record.sex == '1' ? 'male' : 'female') : '';
      },
      width: 120,
      resizable: true,
    },
    {
      title: 'Birthday',
      dataIndex: 'birthday',
      width: 120,
      resizable: true,
    },
    {
      title: 'Mail',
      dataIndex: 'email',
      width: 120,
      resizable: true,
    },
  ];
  //form search field
  const searchFormSchema: FormSchema[] = [
    {
      label: 'Name', //showlabel
      field: 'name', //Query field
      component: 'JInput', //Rendered component
      defaultValue: 'Su Rongrun', //Set default value
    },
    {
      label: 'gender',
      field: 'sex',
      component: 'JDictSelectTag',
      componentProps: {
        //Rendered component的props
        dictCode: 'sex',
        placeholder: '请选择gender',
      },
    },
    {
      label: 'Mail',
      field: 'email',
      component: 'JInput',
      slot: 'email',
    },
    {
      label: 'Birthday',
      field: 'birthday',
      component: 'DatePicker',
    },
  ];
  //ajaxaskapiinterface
  const demoListApi = (params) => {
    return defHttp.get({ url: '/test/jeecgDemo/list', params });
  };
  // List page public parameters、method
  const { tableContext } = useListPage({
    designScope: 'basic-table-demo-filter',
    tableProps: {
      title: 'User list',
      api: demoListApi,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      useSearchForm: true,
    },
  });
  //BasicTableBinding registration
  const [registerTable, { getForm }] = tableContext;
  /**
   * Action bar
   */
  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'edit',
        onClick: handleEdit.bind(null, record),
      },
    ];
  }

  function handleEdit(record) {
    let { getFieldsValue } = getForm();
    console.log('Queryformdata', getFieldsValue());
    console.log(record);
  }
</script>

<style scoped></style>
