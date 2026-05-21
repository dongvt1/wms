<template>
  <ConfigProvider :locale="getAntdLocale">
    <Modal v-bind="getProps">
      <Spin :spinning="loading">
        <div style="padding: 20px;">
          <div v-html="options.content" style="margin-bottom: 8px"></div>
          <BasicForm @register="registerForm">
            <template #customInput="{ model, field }">
              <Input ref="inputRef" v-model:value="model[field]" :placeholder="placeholder" @pressEnter="onSubmit" @input="onChange" />
            </template>
          </BasicForm>
        </div>
      </Spin>
    </Modal>
  </ConfigProvider>
</template>

<script lang="ts">
  import type { JPromptProps } from './typing';
  import type { ModalProps } from '/@/components/Modal';
  import { ref, defineComponent, computed, unref, onMounted, nextTick } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form';
  import { Modal, Spin, Input, ConfigProvider } from 'ant-design-vue';
  import { useLocale } from '/@/locales/useLocale';

  export default defineComponent({
    name: 'JPrompt',
    components: {
      Modal,
      Spin,
      Input,
      BasicForm,
      ConfigProvider,
    },
    emits: ['register'],
    setup(props, { emit }) {
      const inputRef = ref();
      const { getAntdLocale } = useLocale();
      const visible = ref(false);
      // Is it currently loading?
      const loading = ref(false);
      const options = ref<JPromptProps>({});
      const placeholder = computed(() => options.value.placeholder ?? 'Please enter content');
      // Registration form
      const [registerForm, { clearValidate, setFieldsValue, validate, updateSchema }] = useForm({
        compact: true,
        wrapperCol: { span: 24 },
        schemas: [
          {
            label: '',
            field: 'input',
            component: 'Input',
            slot: 'customInput',
          },
        ],
        showActionButtonGroup: false,
      });

      // Pop-up window finallyprops
      const getProps = computed(() => {
        let opt = options.value;
        let modalProps: Partial<ModalProps> = {
          width: (opt.width ?? 500) as number,
          title: (opt.title ?? 'prompt') as string,
          open: unref(visible),
          confirmLoading: unref(loading),
        };
        let finalProps: Recordable = {
          ...modalProps,
          ...props,
          ...opt,
          onOk: onSubmit,
          onCancel() {
            if (typeof options.value.onCancel === 'function') {
              options.value.onCancel();
            }
            close();
          },
        };
        return finalProps;
      });

      onMounted(() => {
        emit('register', {
          openModal,
          setLoading,
          getVisible: visible,
        });
      });

      /** Pop-up window opens */
      async function openModal(opt: any) {
        document.body.focus();

        options.value = opt;
        visible.value = true;
        await nextTick();
        await updateSchema({
          field: 'input',
          required: options.value.required,
          rules: options.value.rules,
          dynamicRules: options.value.dynamicRules,
        } as any);
        await setFieldsValue({
          input: options.value.defaultValue ?? '',
        });
        await clearValidate();
        inputRef.value?.focus();
      }

      /** Pop-up window closes */
      function close() {
        visible.value = false;
      }

      function onChange() {
        validate()
      }
      
      /** Submit form */
      async function onSubmit() {
        try {
          const { onOk } = options.value;
          // form validation
          let values = await validate();
          setLoading(true);
          if (typeof onOk === 'function') {
            let flag = await onOk(values.input);
            // Only return false To prevent closing the pop-up window
            if (!(flag === false)) {
              close();
            }
          } else {
            close();
          }
        } finally {
          setLoading(false);
        }
      }

      /** Set loading status*/
      function setLoading(flag) {
        loading.value = flag;
      }

      return {
        inputRef,
        getProps,
        loading,
        options,
        placeholder,
        getAntdLocale,
        onChange,
        onSubmit,

        registerForm,
      };
    },
  });
</script>
