<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="Data rule configuration" width="450px" destroyOnClose>
    <a-tabs defaultActiveKey="1">
      <a-tab-pane tab="Data rules" key="1">
        <a-checkbox-group v-model:value="dataRuleChecked" v-if="dataRuleList.length > 0">
          <a-row>
            <a-col :span="24" v-for="(item, index) in dataRuleList" :key="'dr' + index">
              <a-checkbox :value="item.id">{{ item.ruleName }}</a-checkbox>
            </a-col>

            <a-col :span="24">
              <div style="width: 100%; margin-top: 15px">
                <a-button @click="saveDataRuleForRole" type="primary" size="small"> <Icon icon="ant-design:save-outlined"></Icon>Click save</a-button>
              </div>
            </a-col>
          </a-row>
        </a-checkbox-group>
        <div v-else><h3>No configuration information!</h3></div>
      </a-tab-pane>
    </a-tabs>
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/src/components/Drawer';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { queryDataRule, saveDataRule } from '../role.api';
  // statementEmits
  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  // statementdata
  const functionId = ref('');
  const roleId = ref('');
  const dataRuleList = ref([]);
  const dataRuleChecked = ref([]);

  /**
   * data
   */
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await reset();
    setDrawerProps({ confirmLoading: false });
    //permissionedid
    functionId.value = data.functionId;
    //roleid
    roleId.value = data.roleId;
    //查询data
    const res = await queryDataRule({ functionId: unref(functionId), roleId: unref(roleId) });
    if (res.success) {
      dataRuleList.value = res.result.datarule;
      if (res.result.drChecked) {
        dataRuleChecked.value = res.result.drChecked.split(',');
      }
    }
  });

  /**
   * reset
   */
  function reset() {
    functionId.value = '';
    roleId.value = '';
    dataRuleList.value = [];
    dataRuleChecked.value = [];
  }

  /**
   * submit
   */
  async function saveDataRuleForRole() {
    if (!unref(dataRuleChecked) || unref(dataRuleChecked).length == 0) {
      createMessage.warning('please note，现未勾选任何data权限!');
    }
    let params = {
      permissionId: unref(functionId),
      roleId: unref(roleId),
      dataRuleIds: unref(dataRuleChecked).join(','),
    };
    await saveDataRule(params);
    //Close pop-up window
    closeDrawer();
    //Refresh list
    emit('success');
  }
</script>
