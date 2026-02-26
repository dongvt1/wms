<template>
  <BasicModal v-bind="$attrs" @register="registerModal" @ok="handleSubmit" :title="title" :width="1200" destroyOnClose>
    <BasicForm @register="registerForm" />

    <a-tabs v-model:activeKey="activeKey" animated>
      <a-tab-pane tab="Local Rules" key="1" :forceRender="true">
        <JVxeTable ref="vTable1" toolbar rowNumber dragSort rowSelection :maxHeight="580" :dataSource="dataSource1" :columns="columns1">
          <template #toolbarAfter>
            <a-alert type="info" showIcon message="Local rules validate in order according to the digits you input" style="margin-bottom: 8px" />
          </template>
        </JVxeTable>
      </a-tab-pane>
      <a-tab-pane tab="Global Rules" key="2" :forceRender="true">
        <JVxeTable
          ref="vTable2"
          toolbar
          rowNumber
          dragSort
          rowSelection
          :maxHeight="580"
          :dataSource="dataSource2"
          :addSetActive="false"
          :columns="columns2"
        >
          <template #toolbarAfter>
            <a-alert type="info" showIcon message="Global rules can validate all characters entered by users; global rules have higher priority than local rules." style="margin-bottom: 8px" />
          </template>
        </JVxeTable>
      </a-tab-pane>
    </a-tabs>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { computed, ref, unref } from 'vue';
  import { formSchema } from './check.rule.data';
  import { saveCheckRule, updateCheckRule } from './check.rule.api';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
  import { pick } from 'lodash-es';

  //Set title
  const title = computed(() => (!unref(isUpdate) ? 'Add New' : 'Edit'));
  // Emits declaration
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);

  //Form configuration
  const [registerForm, { resetFields, setFieldsValue, validate, getFieldsValue }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  const activeKey = ref('1');
  let arr1: any[] = [];
  let dataSource1 = ref(arr1);
  let arr2: any[] = [];
  let dataSource2 = ref(arr2);

  //Form assignment
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Reset form
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    activeKey.value = '1';
    dataSource1.value = [];
    dataSource2.value = [];
    if (unref(isUpdate)) {
      //Form assignment
      await setFieldsValue({
        ...data.record,
      });

      let ruleJson = data.record.ruleJson;
      if (ruleJson) {
        let ruleList = JSON.parse(ruleJson);
        // Filter global rules and local rules
        let global: any[] = [],
          design: any[] = [],
          priority = '1';
        ruleList.forEach((rule) => {
          if (rule.digits === '*') {
            global.push(Object.assign(rule, { priority }));
          } else {
            priority = '0';
            design.push(rule);
          }
        });
        dataSource1.value = design;
        dataSource2.value = global;
      }
    }
  });

  const vTable1 = ref<JVxeTableInstance>();
  const vTable2 = ref<JVxeTableInstance>();

  // Validate table return table data
  function validateMyTable(tableRef, key) {
    return new Promise((resolve, reject) => {
      tableRef.value!.validateTable().then((errMap) => {
        if (errMap) {
          activeKey.value = key;
          reject();
        } else {
          const values = tableRef.value!.getTableData();
          resolve(values);
        }
      });
    });
  }

  //Form submit event
  async function handleSubmit() {
    let mainData;
    let globalValues = [];
    let designValues = [];
    validate()
      .then((formValue) => {
        mainData = formValue;
        return validateMyTable(vTable1, '1');
      })
      .then((tableData1: []) => {
        if (tableData1 && tableData1.length > 0) {
          designValues = tableData1;
        }
        return validateMyTable(vTable2, '2');
      })
      .then((tableData2: []) => {
        if (tableData2 && tableData2.length > 0) {
          globalValues = tableData2;
        }
        // Integrate data from two sub-tables
        let firstGlobal: any[] = [],
          afterGlobal: any[] = [];
        for (let i = 0; i < globalValues.length; i++) {
          let v: any = globalValues[i];
          v.digits = '*';
          if (v.priority === '1') {
            firstGlobal.push(v);
          } else {
            afterGlobal.push(v);
          }
        }
        let concatValues = firstGlobal.concat(designValues).concat(afterGlobal);
        let subValues = concatValues.map((i) => pick(i, 'digits', 'pattern', 'message'));
        // Generate formData for passing to backend
        let ruleJson = JSON.stringify(subValues);
        let formData = Object.assign({}, mainData, { ruleJson });
        saveOrUpdateFormData(formData);
      })
      .catch(() => {
        setModalProps({ confirmLoading: false });
        console.error('Validation failed!');
      });
  }

  // Form submit request
  async function saveOrUpdateFormData(formData) {
    try {
      console.log('Form submit data', formData);
      setModalProps({ confirmLoading: true });
      if (isUpdate.value) {
        await updateCheckRule(formData);
      } else {
        await saveCheckRule(formData);
      }
      //Close modal
      closeModal();
      //Refresh list
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * Validation
   * @param cellValue
   * @param callback
   */
  const validatePatternHandler = ({ cellValue }, callback) => {
    try {
      new RegExp(cellValue);
      callback(true);
    } catch (e) {
      callback(false, 'Please enter a valid regular expression');
    }
  };

  const columns1 = ref<JVxeColumn[]>([
    {
      title: 'Digits',
      key: 'digits',
      type: JVxeTypes.inputNumber,
      minWidth: 180,
      validateRules: [
        { required: true, message: '${title} cannot be empty' },
        { pattern: /^[1-9]\d*$/, message: 'Please enter a positive integer greater than zero' },
      ],
    },
    {
      title: 'Rule (Regular Expression)',
      key: 'pattern',
      minWidth: 320,
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: 'Rule cannot be empty' }, { handler: validatePatternHandler }],
    },
    {
      title: 'Prompt Text',
      key: 'message',
      minWidth: 180,
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: '${title} cannot be empty' }],
    },
  ]);

  const columns2 = ref<JVxeColumn[]>([
    {
      title: 'Priority',
      key: 'priority',
      type: JVxeTypes.select,
      defaultValue: '1',
      options: [
        { title: 'Run First', value: '1' },
        { title: 'Run Last', value: '0' },
      ],
      validateRules: [],
    },
    {
      title: 'Rule (Regular Expression)',
      key: 'pattern',
      width: '40%',
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: 'Rule cannot be empty' }, { handler: validatePatternHandler }],
    },
    {
      title: 'Prompt Text',
      key: 'message',
      width: '20%',
      type: JVxeTypes.input,
      validateRules: [{ required: true, message: '${title} cannot be empty' }],
    },
  ]);
</script>
