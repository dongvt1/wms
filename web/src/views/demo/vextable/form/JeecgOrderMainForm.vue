<template>
  <div class="jeecg-flow-demo">
    <BasicForm @register="registerForm"></BasicForm>
    <a-tabs v-model:activeKey="activeKey" @change="handleChangeTabs">
      <a-tab-pane tab="Customer information" key="jeecgOrderCustomerForm" :forceRender="true">
        <JeecgOrderCustomerForm ref="jeecgOrderCustomerFormRef" :formData="formData"></JeecgOrderCustomerForm>
      </a-tab-pane>

      <a-tab-pane tab="Air ticket information" key="jeecgOrderTicket" :forceRender="true">
        <JVxeTable v-if="ok" ref="jeecgOrderTicketRef" stripe rowSelection keepSource :maxHeight="300" :loading="table2.loading" :columns="table2.columns" :dataSource="table2.dataSource"> </JVxeTable>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script>
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { computed, defineComponent, ref, reactive } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { propTypes } from '/@/utils/propTypes';
  import { getBpmFormSchema, jeecgOrderTicketColumns } from '../data';
  import JeecgOrderCustomerForm from './JeecgOrderCustomerForm.vue';

  export default defineComponent({
    name: 'JeecgOrderMainForm',
    components: {
      BasicForm,
      JeecgOrderCustomerForm,
    },
    props: {
      formData: propTypes.object.def({}),
      formBpm: propTypes.bool.def(true),
    },
    setup(props) {
      const [registerForm, { setFieldsValue, setProps, getFieldsValue, updateSchema }] = useForm({
        labelWidth: 150,
        schemas: getBpmFormSchema(props.formData),
        showActionButtonGroup: false,
        baseColProps: { span: 8 },
      });

      const formDisabled = computed(() => {
        if (props.formData.disabled === false) {
          return false;
        }
        return true;
      });

      const jeecgOrderCustomerFormRef = ref();
      const jeecgOrderTicketRef = ref();
      const ok = ref(false);

      let formData = {};
      const queryByIdUrl = '/test/jeecgOrderMain/queryById';
      async function initFormData() {
        console.log('props.formData', props.formData);
        let params = { id: props.formData.dataId };
        const data = await defHttp.get({ url: queryByIdUrl, params });
        console.log('data', data);
        formData = { ...data };
        //Set form values
        await setFieldsValue(formData);
        await setProps({ disabled: formDisabled.value });

        await jeecgOrderCustomerFormRef.value.initFormData(props.formData.dataId);
        await loadOrderTicketData(props.formData.dataId);
        ok.value = true;
      }

      async function submitForm() {
        let data = getFieldsValue();
        let params = Object.assign({}, formData, data);
        console.log('form data', params);
        await saveOrUpdate(params, true);
      }

      initFormData();

      const activeKey = ref('jeecgOrderCustomerForm');
      function handleChangeTabs() {}
      // Air ticket information
      const table2 = reactive({
        loading: false,
        dataSource: [],
        columns: filterSubTableColnmns(jeecgOrderTicketColumns, 'order:'),
      });

      async function loadOrderTicketData(mainId) {
        const queryByIdUrl = '/test/jeecgOrderMain/queryOrderTicketListByMainId';
        let params = { id: mainId };
        table2.dataSource = [];
        const data = await defHttp.get({ url: queryByIdUrl, params });
        if (data && data.length > 0) {
          table2.dataSource = [...data];
        }
      }

      //Added permission processing method
      function filterSubTableColnmns(columns, pre) {
        let authList = props.formData.permissionList;
        //Notice：If the subtable configuration shows reverse The logic is not dealt with here  That is, hiding cannot be implemented in the process form，Please use global form permissions to achieve
        let temp = columns.filter((item) => {
          let oneAuth = authList.find((auth) => {
            return auth.action === pre + item.key;
          });
          if (!oneAuth) {
            return true;
          }

          //Code is processed rigorously，prevent an authorization identifier，Configure multiple times
          if (oneAuth instanceof Array) {
            oneAuth = oneAuth[0];
          }

          //disable logic
          if (oneAuth.type == '2' && !oneAuth.isAuth) {
            item['disabled'] = true;
            return true;
          }
          //hidden logic logic
          if (oneAuth.type == '1' && !oneAuth.isAuth) {
            return false;
          }
          return true;
        });
        return temp;
      }

      return {
        registerForm,
        formDisabled,
        submitForm,
        jeecgOrderCustomerFormRef,
        activeKey,
        handleChangeTabs,
        table2,
        jeecgOrderTicketRef,
        ok,
      };
    },
  });
</script>

<style lang="less">
  /*.jeecg-flow-demo{
    .vxe-header--row{
        .vxe-header--column .vxe-cell{
            width: 180px !important;
        }
        .vxe-header--column:first-child .vxe-cell{
            width: 40px !important;
        }
    }
}*/
</style>
