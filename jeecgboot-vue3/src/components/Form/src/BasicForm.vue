<template>
  <Form v-bind="getBindValue" :class="getFormClass" ref="formElRef" :model="formModel" @keypress.enter="handleEnterPress">
    <Row v-bind="getRow">
      <slot name="formHeader"></slot>
      <template v-for="schema in getSchema" :key="schema.field">
        <FormItem
          :tableAction="tableAction"
          :formActionType="formActionType"
          :schema="schema"
          :formProps="getProps"
          :allDefaultValues="defaultValueRef"
          :formModel="formModel"
          :formName="getBindValue.name"
          :source="getBindValue.source"
          :setFormModel="setFormModel"
          :validateFields="validateFields"
          :clearValidate="clearValidate"
          v-auth="schema.auth"
        >
          <template #[item]="data" v-for="item in Object.keys($slots)">
            <slot :name="item" v-bind="data || {}"></slot>
          </template>
        </FormItem>
      </template>

      <FormAction v-bind="getFormActionBindProps" @toggle-advanced="handleToggleAdvanced">
        <template #[item]="data" v-for="item in ['resetBefore', 'submitBefore', 'advanceBefore', 'advanceAfter']">
          <slot :name="item" v-bind="data || {}"></slot>
        </template>
      </FormAction>
      <slot name="formFooter"></slot>
    </Row>
  </Form>
</template>
<script lang="ts">
  import type { FormActionType, FormProps, FormSchema } from './types/form';
  import type { AdvanceState } from './types/hooks';
  import type { Ref } from 'vue';

  import { defineComponent, reactive, ref, computed, unref, onMounted, watch, nextTick } from 'vue';
  import { Form, Row } from 'ant-design-vue';
  import FormItem from './components/FormItem.vue';
  import FormAction from './components/FormAction.vue';

  import { dateItemType } from './helper';
  import { dateUtil } from '/@/utils/dateUtil';

  // import { cloneDeep } from 'lodash-es';
  import { deepMerge } from '/@/utils';

  import { useFormValues } from './hooks/useFormValues';
  import useAdvanced from './hooks/useAdvanced';
  import { useFormEvents } from './hooks/useFormEvents';
  import { createFormContext } from './hooks/useFormContext';
  import { useAutoFocus } from './hooks/useAutoFocus';
  import { useModalContext } from '/@/components/Modal';

  import { basicProps } from './props';
  import componentSetting from '/@/settings/componentSetting';

  import { useDesign } from '/@/hooks/web/useDesign';
  import dayjs from 'dayjs';
  import { useDebounceFn } from '@vueuse/core';
  import { isFunction, isObject } from '/@/utils/is';

  export default defineComponent({
    name: 'BasicForm',
    components: { FormItem, Form, Row, FormAction },
    props: basicProps,
    emits: ['advanced-change', 'reset', 'submit', 'register'],
    setup(props, { emit, attrs }) {
      const formModel = reactive<Recordable>({});
      const modalFn = useModalContext();

      const advanceState = reactive<AdvanceState>({
        // The default is the collapsed state
        isAdvanced: false,
        hideAdvanceBtn: true,
        isLoad: false,
        actionSpan: 6,
      });

      const defaultValueRef = ref<Recordable>({});
      const isInitedDefaultRef = ref(false);
      const propsRef = ref<Partial<FormProps>>({});
      const schemaRef = ref<Nullable<FormSchema[]>>(null);
      const formElRef = ref<Nullable<FormActionType>>(null);

      const { prefixCls } = useDesign('basic-form');

      // Get the basic configuration of the form
      const getProps = computed((): FormProps => {
        let mergeProps = { ...props, ...unref(propsRef) } as FormProps;
        //update-begin-author:sunjianlei date:20220923 for: If the user setslabelWidth，Then makelabelColInvalid，solvelabelWidthInvalid settings question
        if (mergeProps.labelWidth) {
          mergeProps.labelCol = undefined;
        }
        //update-end-author:sunjianlei date:20220923 for: If the user setslabelWidth，Then makelabelColInvalid，solvelabelWidthInvalid settings question
        // update-begin--author:liaozhiyang---date:20231017---for：【QQYUN-6566】BasicFormSupport one line display(inline)
        if (mergeProps.layout === 'inline') {
          if (mergeProps.labelCol === componentSetting.form.labelCol) {
            mergeProps.labelCol = undefined;
          }
          if (mergeProps.wrapperCol === componentSetting.form.wrapperCol) {
            mergeProps.wrapperCol = undefined;
          }
        }
        // update-end--author:liaozhiyang---date:20231017---for：【QQYUN-6566】BasicFormSupport one line display(inline)
        return mergeProps;
      });

      const getFormClass = computed(() => {
        return [
          prefixCls,
          {
            [`${prefixCls}--compact`]: unref(getProps).compact,
            'jeecg-form-detail-effect': unref(getProps).disabled
          },
        ];
      });

      // Get uniform row style and Row configuration for the entire form
      const getRow = computed((): Recordable => {
        const { baseRowStyle = {}, rowProps } = unref(getProps);
        return {
          style: baseRowStyle,
          ...rowProps,
        };
      });

      const getBindValue = computed(() => {
        const bindValue = { ...attrs, ...props, ...unref(getProps) } as Recordable;
        // update-begin--author:liaozhiyang---date:20250630---for：【issues/8484】New pop-up windows in the classification dictionarylabelClicking will trigger the query areainput
        if (bindValue.name === undefined && bindValue.source === 'table-query') {
          bindValue.name = 'top-query-form';
        }
        // update-end--author:liaozhiyang---date:20250630---for：【issues/8484】New pop-up windows in the classification dictionarylabelClicking will trigger the query areainput
        return bindValue;
      });

      const getSchema = computed((): FormSchema[] => {
        const schemas: FormSchema[] = unref(schemaRef) || (unref(getProps).schemas as any);
        for (const schema of schemas) {
          const { defaultValue, component, componentProps } = schema;
          // handle date type
          if (defaultValue && dateItemType.includes(component)) {
            //update-begin---author:wangshuai ---date:20230410  for：【issues/435】The date control generated by the code reports an error when assigning a default value------------
            let valueFormat:string = "";
            // update-begin--author:liaozhiyang---date:20250818---for：【issues/8683】DatePickercomponentcomponentPropsThe initial value is incorrectly obtained when using the function form
            if(isObject(componentProps)) {
              valueFormat = componentProps?.valueFormat;
            } else if (isFunction(componentProps)) {
              try {
                // @ts-ignore
                valueFormat = componentProps({schema, tableAction: props.tableAction, formModel})?.valueFormat;
              } catch (error) {
              }
            }
            // update-end--author:liaozhiyang---date:20250818---for【issues/8683】DatePickercomponentcomponentPropsThe initial value is incorrectly obtained when using the function form
            if(!valueFormat){
              console.warn("Not configuredvalueFormat,May cause formatting errors！");
            }
            //update-end---author:wangshuai ---date:20230410  for：【issues/435】The date control generated by the code reports an error when assigning a default value------------
            if (!Array.isArray(defaultValue)) {
              //update-begin---author:wangshuai ---date:20221124  for：[issues/215]List page query box（Date selection box）Set initial time，When entering the page，The background reports that the date conversion type is incorrect.------------
              if(valueFormat){
                // schema.defaultValue = dateUtil(defaultValue).format(valueFormat);
                // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-346 】There is a problem with filling in the default value of the time component
                schema.defaultValue = dateUtil(defaultValue, valueFormat).format(valueFormat);
                // update-end--author:liaozhiyang---date:20240529---for：【TV360X-346 】There is a problem with filling in the default value of the time component
              }else{
                schema.defaultValue = dateUtil(defaultValue);
              }
              //update-end---author:wangshuai ---date:20221124  for：[issues/215]List page query box（Date selection box）Set initial time，When entering the page，The background reports that the date conversion type is incorrect.------------
            } else {
              const def: dayjs.Dayjs[] = [];
              defaultValue.forEach((item) => {
                //update-begin---author:wangshuai ---date:20221124  for：[issues/215]List page query box（Date selection box）Set initial time，When entering the page，The background reports that the date conversion type is incorrect.------------
                if(valueFormat){
                  // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-346 】There is a problem with filling in the default value of the time component
                  def.push(dateUtil(item, valueFormat).format(valueFormat));
                  // update-end--author:liaozhiyang---date:20240529---for：【TV360X-346 】There is a problem with filling in the default value of the time component
                }else{
                  def.push(dateUtil(item));
                }
                //update-end---author:wangshuai ---date:20221124  for：[issues/215]List page query box（Date selection box）Set initial time，When entering the page，The background reports that the date conversion type is incorrect.------------
              });
              // update-begin--author:liaozhiyang---date:20240328---for：【issues/1114】rangepickerWaiting time control reports error（vue3.4There is a problem with the above version）
              def.forEach((item, index) => {
                defaultValue[index] = item;
              });
              // update-end--author:liaozhiyang---date:20240328---for：【issues/1114】rangepickerWaiting time control reports error（vue3.4There is a problem with the above version）
            }
          }
        }
        if (unref(getProps).showAdvancedButton) {
          return schemas.filter((schema) => schema.component !== 'Divider') as FormSchema[];
        } else {
          return schemas as FormSchema[];
        }
      });

      const { handleToggleAdvanced } = useAdvanced({
        advanceState,
        emit,
        getProps,
        getSchema,
        formModel,
        defaultValueRef,
      });

      const { handleFormValues, initDefault } = useFormValues({
        getProps,
        defaultValueRef,
        getSchema,
        formModel,
      });

      useAutoFocus({
        getSchema,
        getProps,
        isInitedDefault: isInitedDefaultRef,
        formElRef: formElRef as Ref<FormActionType>,
      });

      const {
        handleSubmit,
        setFieldsValue,
        clearValidate,
        validate,
        validateFields,
        getFieldsValue,
        updateSchema,
        resetSchema,
        getSchemaByField,
        appendSchemaByField,
        removeSchemaByFiled,
        resetFields,
        scrollToField,
      } = useFormEvents({
        emit,
        getProps,
        formModel,
        getSchema,
        defaultValueRef,
        formElRef: formElRef as Ref<FormActionType>,
        schemaRef: schemaRef as Ref<FormSchema[]>,
        handleFormValues,
      });

      createFormContext({
        resetAction: resetFields,
        submitAction: handleSubmit,
      });

      watch(
        () => unref(getProps).model,
        () => {
          const { model } = unref(getProps);
          if (!model) return;
          setFieldsValue(model);
        },
        {
          immediate: true,
        }
      );

      watch(
        () => unref(getProps).schemas,
        (schemas) => {
          resetSchema(schemas ?? []);
        }
      );

      watch(
        () => getSchema.value,
        (schema) => {
          nextTick(() => {
            //  Solve the problem of modal adaptive height calculation when the form is placed in the modal
            modalFn?.redoModalHeight?.();
          });
          if (unref(isInitedDefaultRef)) {
            return;
          }
          if (schema?.length) {
            initDefault();
            isInitedDefaultRef.value = true;
          }
        }
      );

      async function setProps(formProps: Partial<FormProps>): Promise<void> {
        propsRef.value = deepMerge(unref(propsRef) || {}, formProps);
      }

      //update-begin-author:taoyan date:2022-11-28 for: QQYUN-3121 【optimization】form view issue#scotttest 8、This feature is not implemented
      const onFormSubmitWhenChange = useDebounceFn(handleSubmit, 300);
      function setFormModel(key: string, value: any) {
        formModel[key] = value;
        // update-begin--author:liaozhiyang---date:20230922---for：【issues/752】form validationdynamicRules Unable Validate after losing focus using trigger: 'blur'
        // const { validateTrigger } = unref(getBindValue);
        // if (!validateTrigger || validateTrigger === 'change') {
        //   validateFields([key]).catch((_) => {});
        // }
        // update-end--author:liaozhiyang---date:20230922---for：【issues/752】form validationdynamicRules Unable Validate after losing focus using trigger: 'blur'
        if(props.autoSearch === true){
          onFormSubmitWhenChange();
        }
      }
      //update-end-author:taoyan date:2022-11-28 for: QQYUN-3121 【optimization】form view issue#scotttest 8、This feature is not implemented

      function handleEnterPress(e: KeyboardEvent) {
        const { autoSubmitOnEnter } = unref(getProps);
        if (!autoSubmitOnEnter) return;
        if (e.key === 'Enter' && e.target && e.target instanceof HTMLElement) {
          const target: HTMLElement = e.target as HTMLElement;
          if (target && target.tagName && target.tagName.toUpperCase() == 'INPUT') {
            handleSubmit();
          }
        }
      }

      /**
       * Get componentProps，Handle the case where a function might be
       * @param schema
       */
      function getSchemaComponentProps(schema: FormSchema) {
        if (typeof schema.componentProps === 'function') {
          return schema.componentProps({
            schema,
            tableAction: props.tableAction,
            formActionType,
            formModel,
          })
        }
        return schema.componentProps
      }

      const formActionType: Partial<FormActionType> = {
        getFieldsValue,
        setFieldsValue,
        resetFields,
        updateSchema,
        resetSchema,
        setProps,
        getProps,
        getSchemaByField,
        removeSchemaByFiled,
        appendSchemaByField,
        clearValidate,
        validateFields,
        validate,
        submit: handleSubmit,
        scrollToField: scrollToField,
        getSchemaComponentProps,
      };

      onMounted(() => {
        initDefault();
        emit('register', formActionType);
      });

      return {
        getBindValue,
        handleToggleAdvanced,
        handleEnterPress,
        formModel,
        defaultValueRef,
        advanceState,
        getRow,
        getProps,
        formElRef,
        getSchema,
        formActionType: formActionType as any,
        setFormModel,
        getFormClass,
        getFormActionBindProps: computed((): Recordable => ({ ...getProps.value, ...advanceState })),
        ...formActionType,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-basic-form';

  .@{prefix-cls} {
    .ant-form-item {
      &-label label::after {
        margin: 0 6px 0 2px;
      }

      &-with-help {
        margin-bottom: 0;
      }
      // update-begin--author:liaozhiyang---date:20240514---for：【QQYUN-9241】formMake the space between the top and bottom of the form wider
      //&:not(.ant-form-item-with-help) {
      //  margin-bottom: 24px;
      //}
      // update-begin--author:liaozhiyang---date:20240514---for：【QQYUN-9241】formMake the space between the top and bottom of the form wider
      // update-begin--author:liaozhiyang---date:20240620---for：【TV360X-1420】Flashing during verification
      &-has-error {
        margin-bottom: 24px;
      }
      // update-end--author:liaozhiyang---date:20240620---for：【TV360X-1420】Flashing during verification

      // Form component middleware styles
      .j-form-item-middleware {
        flex: 1;
        width: 100%
      }

      &.suffix-item {
        .ant-form-item-children {
          display: flex;
        }

        .ant-form-item-control {
          margin-top: 4px;
        }

        // 【QQYUN-12876】When compact suffix hour，The form component middleware does not occupy the full width
        &.suffix-compact .j-form-item-middleware {
          flex: unset;
          width: auto;
        }

        .suffix {
          display: inline-flex;
          padding-left: 6px;
          margin-top: 1px;
          line-height: 1;
          align-items: center;
        }
      }
    }
    /*【Beautify the form】formChange the font size of*/
/*    .ant-form-item-label > label{
      font-size: 13px;
    }
    .ant-form-item .ant-select {
      font-size: 13px;
    }
    .ant-select-item-option-selected {
      font-size: 13px;
    }
    .ant-select-item-option-content {
      font-size: 13px;
    }
    .ant-input {
      font-size: 13px;
    }*/
    /*【Beautify the form】formChange the font size of*/
    
    .ant-form-explain {
      font-size: 14px;
    }

    &--compact {
      .ant-form-item {
        margin-bottom: 8px !important;
      }
    }
    // update-begin--author:liaozhiyang---date:20231017---for：【QQYUN-6566】BasicFormSupport one line display(inline)
    &.ant-form-inline {
      & > .ant-row {
        .ant-col { width:auto !important; }
      }
    }
    // update-end--author:liaozhiyang---date:20231017---for：【QQYUN-6566】BasicFormSupport one line display(inline)
  }
</style>
