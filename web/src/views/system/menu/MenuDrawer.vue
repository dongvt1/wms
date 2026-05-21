<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" showFooter :width="adaptiveWidth" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" class="menuForm" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, useAttrs } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema, ComponentTypes } from './menu.data';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { list, saveOrUpdateMenu } from './menu.api';
  import { useDrawerAdaptiveWidth } from '/@/hooks/jeecg/useAdaptiveWidth';
  import { useI18n } from "/@/hooks/web/useI18n";
  // Emits declaration
  const emit = defineEmits(['success', 'register']);
  const { adaptiveWidth } = useDrawerAdaptiveWidth();
  const attrs = useAttrs();
  const isUpdate = ref(true);
  const menuType = ref(0);
  const isButton = (type) => type === 2;
  const [registerForm, { setProps, resetFields, setFieldsValue, updateSchema, validate, clearValidate }] = useForm({
    labelCol: {
      md: { span: 4 },
      sm: { span: 6 },
    },
    wrapperCol: {
      md: { span: 20 },
      sm: { span: 18 },
    },
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    setDrawerProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    menuType.value = data?.record?.menuType;

    //Get dropdown tree information
    const treeData = await list();
    updateSchema([
      {
        field: 'parentId',
        // update-begin--author:liaozhiyang---date:20240306---for：【QQYUN-8379】Menu management page menu internationalization
        componentProps: { treeData: translateMenu(treeData, 'name') },
        // update-end--author:liaozhiyang---date:20240306---for：【QQYUN-8379】Menu management page menu internationalization
      },
      {
        field: 'name',
        label: isButton(unref(menuType)) ? 'Button/Permission' : 'Menu Name',
      },
      {
        field: 'url',
        required: !isButton(unref(menuType)),
        componentProps: {
          onChange: (e) => onUrlChange(e.target.value),
        },
      },
    ]);

    // Whether adding or editing, form values can be set
    if (typeof data.record === 'object') {
      let values = { ...data.record };
      setFieldsValue(values);
      onUrlChange(values.url);
    }
    //In case of button type, clear address validation when editing
    if (menuType.value == 2) {
      clearValidate();
    }
    //Disable form
    setProps({ disabled: !attrs.showFooter });
  });
  //Get modal title
  const getTitle = computed(() => (!unref(isUpdate) ? 'Add Menu' : 'Edit Menu'));
  //Submit event
  async function handleSubmit() {
    try {
      const values = await validate();
      // iframe compatibility
      if (ComponentTypes.IFrame === values.component) {
        values.component = values.frameSrc;
      }
      setDrawerProps({ confirmLoading: true });
      //Submit form
      await saveOrUpdateMenu(values, unref(isUpdate));
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }

  /** When url changes, dynamically set component name placeholder */
  function onUrlChange(url) {
    let placeholder = '';
    let httpUrl = url;
    if (url != null && url != '') {
      if (url.startsWith('/')) {
        url = url.substring(1);
      }
      url = url.replaceAll('/', '-');
      // Special marker
      url = url.replaceAll(':', '@');
      placeholder = `${url}`;
    } else {
      placeholder = 'Please enter component name';
    }
    updateSchema([{ field: 'componentName', componentProps: { placeholder } }]);
    //update-begin---author:wangshuai ---date:20230204  for：[QQYUN-4058]Add intelligent processing to the menu------------
    if (httpUrl != null && httpUrl != '') {
      if (httpUrl.startsWith('http://') || httpUrl.startsWith('https://')) {
        setFieldsValue({ component: httpUrl });
      }
    }
    //update-end---author:wangshuai ---date:20230204  for：[QQYUN-4058]Add intelligent processing to the menu------------
  }

  /**
   * 2024-03-06
   * liaozhiyang
   * Translate menu name
   */
  function translateMenu(data, key) {
    if (data?.length) {
      const { t } = useI18n();
      data.forEach((item) => {
        if (item[key]) {
          if (item[key].includes("t('") && t) {
            item[key] = new Function('t', `return ${item[key]}`)(t);
          }
        }
        if (item.children?.length) {
          translateMenu(item.children, key);
        }
      });
    }
    return data;
  }
</script>
