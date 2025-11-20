<script lang="tsx">
  import { NamePath, ValidateOptions } from 'ant-design-vue/lib/form/interface';
  import type { PropType, Ref } from 'vue';
  import type { FormActionType, FormProps } from '../types/form';
  import type { FormSchema } from '../types/form';
  import type { ValidationRule } from 'ant-design-vue/lib/form/Form';
  import type { TableActionType } from '/@/components/Table';
  import { defineComponent, computed, unref, toRefs } from 'vue';
  import { Form, Col, Divider } from 'ant-design-vue';
  import { componentMap } from '../componentMap';
  import { BasicHelp } from '/@/components/Basic';
  import { isBoolean, isFunction, isNull } from '/@/utils/is';
  import { getSlot } from '/@/utils/helper/tsxHelper';
  import { createPlaceholderMessage, setComponentRuleType } from '../helper';
  import { upperFirst, cloneDeep } from 'lodash-es';
  import { useItemLabelWidth } from '../hooks/useLabelWidth';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { usePermission } from '/@/hooks/web/usePermission';
  import Middleware from './Middleware.vue';
  import { useLocaleStoreWithOut } from '/@/store/modules/locale';

  export default defineComponent({
    name: 'BasicFormItem',
    inheritAttrs: false,
    props: {
      schema: {
        type: Object as PropType<FormSchema>,
        default: () => ({}),
      },
      formProps: {
        type: Object as PropType<FormProps>,
        default: () => ({}),
      },
      allDefaultValues: {
        type: Object as PropType<Recordable>,
        default: () => ({}),
      },
      formModel: {
        type: Object as PropType<Recordable>,
        default: () => ({}),
      },
      setFormModel: {
        type: Function as PropType<(key: string, value: any) => void>,
        default: null,
      },
      validateFields: {
        type: Function as PropType<(nameList?: NamePath[] | undefined, options?: ValidateOptions) => Promise<any>>,
        default: null,
      },
      tableAction: {
        type: Object as PropType<TableActionType>,
      },
      formActionType: {
        type: Object as PropType<FormActionType>,
      },
      // update-begin--author:liaozhiyang---date:20240605---for：【TV360X-857】Solve the problem of triggering verification in disabled state
      clearValidate: {
        type: Function,
        default: null,
      },
      // update-end-author:liaozhiyang---date:20240605---for：【TV360X-857】Solve the problem of triggering verification in disabled state
      // update-begin--author:liaozhiyang---date:20240625---for：【TV360X-1511】blurNot effective
      formName: {
        type: String,
        default: '',
      },
      // update-end--author:liaozhiyang---date:20240625---for：【TV360X-1511】blurNot effective
      source: {
        type: String,
        default: '',
      },
    },
    setup(props, { slots }) {
      const { t } = useI18n();
      const localeStore = useLocaleStoreWithOut();
      const { schema, formProps } = toRefs(props) as {
        schema: Ref<FormSchema>;
        formProps: Ref<FormProps>;
      };

      const itemLabelWidthProp = useItemLabelWidth(schema, formProps);

      const getValues = computed(() => {
        const { allDefaultValues, formModel, schema } = props;
        const { mergeDynamicData } = props.formProps;
        return {
          field: schema.field,
          model: formModel,
          values: {
            ...mergeDynamicData,
            ...allDefaultValues,
            ...formModel,
          } as Recordable,
          schema: schema,
        };
      });

      const getComponentsProps = computed(() => {
        const { schema, tableAction, formModel, formActionType } = props;
        let { componentProps = {} } = schema;
        if (isFunction(componentProps)) {
          componentProps = componentProps({ schema, tableAction, formModel, formActionType }) ?? {};
        }
        if (schema.component === 'Divider') {
          //update-begin---author:wangshuai---date:2023-09-22---for:【QQYUN-6603】The position of the dividing line title is displayed incorrectly---
          componentProps = Object.assign({ type: 'horizontal',orientation:'left', plain: true, }, componentProps);
          //update-end---author:wangshuai---date:2023-09-22---for:【QQYUN-6603】The position of the dividing line title is displayed incorrectly---
        }
        return componentProps as Recordable;
      });

      const getDisable = computed(() => {
        const { disabled: globDisabled } = props.formProps;
        // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-594】Form global disablingdynamicDisabledNot effective
        if (!!globDisabled) {
          return globDisabled;
        }
        // update-end--author:liaozhiyang---date:20240530---for：【TV360X-594】Form global disablingdynamicDisabledNot effective
        const { dynamicDisabled } = props.schema;
        const { disabled: itemDisabled = false } = unref(getComponentsProps);
        let disabled = !!globDisabled || itemDisabled;
        if (isBoolean(dynamicDisabled)) {
          disabled = dynamicDisabled;
        }
        if (isFunction(dynamicDisabled)) {
          disabled = dynamicDisabled(unref(getValues));
        }
        return disabled;
      });

      // update-begin--author:liaozhiyang---date:20240308---for：【QQYUN-8377】formSchema propsSupport dynamic modification
      const getDynamicPropsValue = computed(() => {
        const { dynamicPropsVal, dynamicPropskey } = props.schema;
        if (dynamicPropskey == null) {
          return null;
        } else {
          const { [dynamicPropskey]: itemValue } = unref(getComponentsProps);
          let value = itemValue;
          if (isFunction(dynamicPropsVal)) {
            value = dynamicPropsVal(unref(getValues));
            return value;
          }
        }
      });
      // update-end--author:liaozhiyang---date:20240308---for：【QQYUN-8377】formSchema propsSupport dynamic modification

      function getShow(): { isShow: boolean; isIfShow: boolean } {
        const { show, ifShow } = props.schema;
        const { showAdvancedButton } = props.formProps;
        const itemIsAdvanced = showAdvancedButton ? (isBoolean(props.schema.isAdvanced) ? props.schema.isAdvanced : true) : true;

        let isShow = true;
        let isIfShow = true;

        if (isBoolean(show)) {
          isShow = show;
        }
        if (isBoolean(ifShow)) {
          isIfShow = ifShow;
        }
        if (isFunction(show)) {
          isShow = show(unref(getValues));
        }
        if (isFunction(ifShow)) {
          isIfShow = ifShow(unref(getValues));
        }
        isShow = isShow && itemIsAdvanced;
        return { isShow, isIfShow };
      }
      // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-434】validatorVerification is performed twice
      let vSwitchArr: any = [],
        prevValidatorArr: any = [];
      const hijackValidator = (rules) => {
        vSwitchArr = [];
        prevValidatorArr = [];
        rules.forEach((item, index) => {
          const fn = item.validator;
          vSwitchArr.push(true);
          prevValidatorArr.push(null);
          if (isFunction(fn)) {
            item.validator = (rule, value, callback) => {
              if (vSwitchArr[index]) {
                vSwitchArr[index] = false;
                setTimeout(() => {
                  vSwitchArr[index] = true;
                }, 100);
                const result = fn(rule, value, callback);
                prevValidatorArr[index] = result;
                return result;
              } else {
                return prevValidatorArr[index];
              }
            };
          }
        });
      };
      // update-end--author:liaozhiyang---date:20240530---for：【TV360X-434】validatorVerification is performed twice
      function handleRules(): ValidationRule[] {
        const { rules: defRules = [], component, rulesMessageJoinLabel, label, dynamicRules, required, auth, field } = props.schema;
        // update-begin--author:liaozhiyang---date:20240605---for：【TV360X-857】Solve the problem of triggering verification in disabled state
        const { disabled: globDisabled } = props.formProps;
        const { disabled: itemDisabled = false } = unref(getComponentsProps);
        if (!!globDisabled || !!itemDisabled) {
          props.clearValidate(field);
          return [];
        }
        // update-end--author:liaozhiyang---date:20240605---for：【TV360X-857】Solve the problem of triggering verification in disabled state
        // update-begin--author:liaozhiyang---date:20240531---for：【TV360X-842】Required fieldsv-auth、showThe form cannot be submitted if it is hidden
        const { hasPermission } = usePermission();
        const { isShow } = getShow();
        if ((auth && !hasPermission(auth)) || !isShow) {
          return [];
        }
        // update-end--author:liaozhiyang---date:20240531---for：【TV360X-842】Required fieldsv-auth、showThe form cannot be submitted if it is hidden
        if (isFunction(dynamicRules)) {
          // update-begin--author:liaozhiyang---date:20240514---for：【issues/1244】Marked required，But the required sign is not displayed
          const ruleArr = dynamicRules(unref(getValues)) as ValidationRule[];
          if (required) {
            ruleArr.unshift({ required: true });
          }
          // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-434】validatorVerification is performed twice
          hijackValidator(ruleArr);
          // update-end--author:liaozhiyang---date:20240530---for：【TV360X-434】validatorVerification is performed twice
          return ruleArr;
          // update-end--author:liaozhiyang---date:20240514---for：【issues/1244】Marked required，But the required sign is not displayed
        }

        let rules: ValidationRule[] = cloneDeep(defRules) as ValidationRule[];
        const { rulesMessageJoinLabel: globalRulesMessageJoinLabel } = props.formProps;

        const joinLabel = Reflect.has(props.schema, 'rulesMessageJoinLabel') ? rulesMessageJoinLabel : globalRulesMessageJoinLabel;
        const defaultMsg = createPlaceholderMessage(component) + `${joinLabel ? label : ''}`;

        function validator(rule: any, value: any) {
          const msg = rule.message || defaultMsg;
          if (value === undefined || isNull(value)) {
            // null value
            return Promise.reject(msg);
          } else if (Array.isArray(value) && value.length === 0) {
            // array type
            return Promise.reject(msg);
          } else if (typeof value === 'string' && value.trim() === '') {
            // empty string
            return Promise.reject(msg);
          } else if (
            typeof value === 'object' &&
            Reflect.has(value, 'checked') &&
            Reflect.has(value, 'halfChecked') &&
            Array.isArray(value.checked) &&
            Array.isArray(value.halfChecked) &&
            value.checked.length === 0 &&
            value.halfChecked.length === 0
          ) {
            // non-associated selectiontreecomponents
            return Promise.reject(msg);
          }
          return Promise.resolve();
        }

        const getRequired = isFunction(required) ? required(unref(getValues)) : required;

        if ((!rules || rules.length === 0) && getRequired) {
          rules = [{ required: getRequired, validator }];
        }

        const requiredRuleIndex: number = rules.findIndex((rule) => Reflect.has(rule, 'required') && !Reflect.has(rule, 'validator'));

        if (requiredRuleIndex !== -1) {
          const rule = rules[requiredRuleIndex];
          const { isShow } = getShow();
          if (!isShow) {
            rule.required = false;
          }
          if (component) {
            //update-begin---author:wangshuai---date:2024-02-01---for:【QQYUN-8176】In edit form,When verification is required,如果components是ApiSelect,When opening the edit page,Even if the field has a value,You will also be prompted to select---
            //https://github.com/vbenjs/vue-vben-admin/pull/3082 githubRepair the original text
            /*if (!Reflect.has(rule, 'type')) {
              rule.type = component === 'InputNumber' ? 'number' : 'string';
            }*/
            //update-end---author:wangshuai---date:2024-02-01---for:【QQYUN-8176】In edit form,When verification is required,如果components是ApiSelect,When opening the edit page,Even if the field has a value,You will also be prompted to select---

            rule.message = rule.message || defaultMsg;

            if (component.includes('Input') || component.includes('Textarea')) {
              rule.whitespace = true;
            }
            const valueFormat = unref(getComponentsProps)?.valueFormat;
            setComponentRuleType(rule, component, valueFormat);
          }
        }

        // Maximum input length rule check
        const characterInx = rules.findIndex((val) => val.max);
        if (characterInx !== -1 && !rules[characterInx].validator) {
          rules[characterInx].message = rules[characterInx].message || t('component.form.maxTip', [rules[characterInx].max] as Recordable);
        }
        // update-begin--author:liaozhiyang---date:20241226---for：【QQYUN-7495】patternChange from string to regular and pass toantd（due to useInputNumberIt is found that the regular expression is invalid）
        rules.forEach((item) => {
          if (typeof item.pattern === 'string') {
            try {
              const reg = new Function('item', `return ${item.pattern}`)(item);
              if (Object.prototype.toString.call(reg) === '[object RegExp]') {
                item.pattern = reg;
              } else {
                item.pattern = new RegExp(item.pattern);
              }
            } catch (error) {
              item.pattern = new RegExp(item.pattern);
            }
          }
        });
        // update-end--author:liaozhiyang---date:20231226---for：【QQYUN-7495】patternChange from string to regular and pass toantd（due to useInputNumberIt is found that the regular expression is invalid）
        // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-434】validatorVerification is performed twice
        hijackValidator(rules);
        // update-end--author:liaozhiyang---date:20240530---for：【TV360X-434】validatorVerification is performed twice
        return rules;
      }

      function renderComponent() {
        const { renderComponentContent, component, field, changeEvent = 'change', valueField, componentProps, dynamicRules, rules:defRules = [] } = props.schema;

        const isCheck = component && ['Switch', 'Checkbox'].includes(component);
        // update-begin--author:liaozhiyang---date:20231013---for：【QQYUN-6679】inputremove spaces
        let isTrim = false;
        if (component === 'Input' && componentProps && componentProps.trim) {
          isTrim = true;
        }
        // update-end--author:liaozhiyang---date:20231013---for：【QQYUN-6679】inputremove spaces
        const eventKey = `on${upperFirst(changeEvent)}`;
        const getRules = (): ValidationRule[] => {
          const dyRules = isFunction(dynamicRules) ? dynamicRules(unref(getValues)) : [];
          return [...dyRules, ...defRules];
        };
        // update-begin--author:liaozhiyang---date:20230922---for：【issues/752】form validationdynamicRules Unable Validate after losing focus using trigger: 'blur'
        const on = {
          [eventKey]: (...args: Nullable<Recordable>[]) => {
            const [e] = args;
            if (propsData[eventKey]) {
              propsData[eventKey](...args);
            }
            const target = e ? e.target : null;
            // update-begin--author:liaozhiyang---date:20231013---for：【QQYUN-6679】inputremove spaces
            let value;
            if (target) {
              if (isCheck) {
                value = target.checked;
              } else {
                value = isTrim ? target.value.trim() : target.value;
              }
            } else {
              value = e;
            }
            // update-end--author:liaozhiyang---date:20231013---for：【QQYUN-6679】inputremove spaces
            props.setFormModel(field, value);
            // update-begin--author:liaozhiyang---date:20240625---for：【TV360X-1511】blurNot effective
            const findItem = getRules().find((item) => item?.trigger === 'blur');
            if (!findItem) {
              // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-341】Required verification will not disappear after there is a value
              props.validateFields([field]).catch((_) => {});
              // update-end--author:liaozhiyang---date:20240625---for：【TV360X-341】Required verification will not disappear after there is a value
            }
            // update-end--author:liaozhiyang---date:20240625---for：【TV360X-1511】blurNot effective
          },
          // onBlur: () => {
          //   props.validateFields([field], { triggerName: 'blur' }).catch((_) => {});
          // },
        };
        // update-end--author:liaozhiyang---date:20230922---for：【issues/752】form validationdynamicRules Unable Validate after losing focus using trigger: 'blur'
        const Comp = componentMap.get(component) as ReturnType<typeof defineComponent>;

        const { autoSetPlaceHolder, size } = props.formProps;
        const propsData: Recordable = {
          allowClear: true,
          getPopupContainer: (trigger: Element) => {

            return trigger?.parentNode;
          },
          size,
          ...unref(getComponentsProps),
          disabled: unref(getDisable),
        };
        // update-begin--author:liaozhiyang---date:20240308---for：【QQYUN-8377】formSchema propsSupport dynamic modification
        const dynamicPropskey = props.schema.dynamicPropskey;
        if (dynamicPropskey) {
          propsData[dynamicPropskey] = unref(getDynamicPropsValue);
        }
        // update-end--author:liaozhiyang---date:20240308---for：【QQYUN-8377】formSchema propsSupport dynamic modification

        // update-begin--author:sunjianlei---date:20240725---for：【TV360X-972】Unify the placeholder content when the control is disabled
        // const isCreatePlaceholder = !propsData.disabled && autoSetPlaceHolder;
        const isCreatePlaceholder = !!autoSetPlaceHolder;
        // update-end----author:sunjianlei---date:20240725---for：【TV360X-972】Unify the placeholder content when the control is disabled

        // RangePicker placeis an array
        if (isCreatePlaceholder && component !== 'RangePicker' && component) {
          //Automatically setplaceholder
          // update-begin--author:liaozhiyang---date:20240724---for：【issues/6908】When switching between multiple languages ​​without refreshing，BasicColumnandFormSchemaThe values ​​inside cannot be switched normally
          let label = isFunction(props.schema.label) ? props.schema.label() : props.schema.label;
          if (localeStore.getLocale === 'en' && !(/^\s/.test(label))) {
            label = ' ' + label;
          }
          // update-end--author:liaozhiyang---date:20240724---for：【issues/6908】When switching between multiple languages ​​without refreshing，BasicColumnandFormSchemaThe values ​​inside cannot be switched normally
          propsData.placeholder = unref(getComponentsProps)?.placeholder || createPlaceholderMessage(component) + label;
        }
        propsData.codeField = field;
        propsData.formValues = unref(getValues);

        const bindValue: Recordable = {
          [valueField || (isCheck ? 'checked' : 'value')]: props.formModel[field],
        };

        const compAttr: Recordable = {
          ...propsData,
          ...on,
          ...bindValue,
        };

        if (!renderComponentContent) {
          return <Comp {...compAttr} />;
        }
        const compSlot = isFunction(renderComponentContent)
          ? { ...renderComponentContent(unref(getValues)) }
          : {
              default: () => renderComponentContent,
            };
        return <Comp {...compAttr}>{compSlot}</Comp>;
      }

      /**
       *renderingLabel
       * @updateBy:zyf
       */
      function renderLabelHelpMessage() {
        //update-begin-author:taoyan date:2022-9-7 for: VUEN-2061【style】onlineform exceeded4indivual .. Omit display
        //labelWidth supports customization
        const { label: itemLabel, helpMessage, helpComponentProps, subLabel, labelLength } = props.schema;
        // update-begin--author:liaozhiyang---date:20240724---for：【issues/6908】When switching between multiple languages ​​without refreshing，BasicColumnandFormSchemaThe values ​​inside cannot be switched normally
        const label = isFunction(itemLabel) ? itemLabel() : itemLabel;
        // update-end--author:liaozhiyang---date:20240724---for：【issues/6908】When switching between multiple languages ​​without refreshing，BasicColumnandFormSchemaThe values ​​inside cannot be switched normally
        let showLabel: string = label + '';
        // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-98】label展示的文字必须andlabelLengthConfiguration consistent
        if (labelLength) {
          showLabel = showLabel.substr(0, labelLength);
        }
        // update-end--author:liaozhiyang---date:20240517---for：【TV360X-98】label展示的文字必须andlabelLengthConfiguration consistent
        const titleObj = { title: label };
        const renderLabel = subLabel ? (
          <span>
            {label} <span class="text-secondary">{subLabel}</span>
          </span>
        ) : labelLength ? (
          <label {...titleObj}>{showLabel}</label>
        ) : (
          label
        );
        //update-end-author:taoyan date:2022-9-7 for: VUEN-2061【style】onlineform exceeded4indivual .. Omit display
        const getHelpMessage = isFunction(helpMessage) ? helpMessage(unref(getValues)) : helpMessage;
        if (!getHelpMessage || (Array.isArray(getHelpMessage) && getHelpMessage.length === 0)) {
          return renderLabel;
        }
        return (
          <span>
            {renderLabel}
            <BasicHelp placement="top" class="mx-1" text={getHelpMessage} {...helpComponentProps} />
          </span>
        );
      }

      function renderItem() {
        const { itemProps, slot, render, field, suffix, suffixCompact, component } = props.schema;
        const { labelCol, wrapperCol } = unref(itemLabelWidthProp);
        const { colon } = props.formProps;

        // update-begin--author:sunjianlei---date:20250613---for：itemProps Properties support functional form
        let getItemProps = itemProps;
        if (typeof getItemProps === 'function') {
          getItemProps = getItemProps(unref(getValues));
        }
        // update-end--author:sunjianlei---date:20250613---for：itemProps Properties support functional form

        if (component === 'Divider') {
          return (
            <Col span={24}>
              <Divider {...unref(getComponentsProps)}>{renderLabelHelpMessage()}</Divider>
            </Col>
          );
        } else {
          const getContent = () => {
            return slot ? getSlot(slots, slot, unref(getValues)) : render ? render(unref(getValues)) : renderComponent();
          };

          const showSuffix = !!suffix;
          const getSuffix = isFunction(suffix) ? suffix(unref(getValues)) : suffix;
          return (
            <Form.Item
              name={field}
              colon={colon}
              class={{ 'suffix-item': showSuffix, 'suffix-compact': showSuffix && suffixCompact }}
              {...(getItemProps as Recordable)}
              label={renderLabelHelpMessage()}
              rules={handleRules()}
              // update-begin--author:liaozhiyang---date:20240514---for：【issues/1244】Marked required，But the required sign is not displayed
              validateFirst = { true }
              // update-end--author:liaozhiyang---date:20240514---for：【issues/1244】Marked required，But the required sign is not displayed
              labelCol={labelCol}
              wrapperCol={wrapperCol}
            >
              <div style="display:flex">
                {/* author: sunjianlei for: 【VUEN-744】Add here width: 100%; 因为要防止components宽度超出 FormItem */}
                {/* update-begin--author:liaozhiyang---date:20240510---for：【TV360X-719】form validation不通过项滚动到可视区内 */}
                <Middleware formName={props.formName} fieldName={field} source={props.source}>{getContent()}</Middleware>
                {/* update-end--author:liaozhiyang---date:20240510---for：【TV360X-719】form validation不通过项滚动到可视区内 */}
                {showSuffix && <span class="suffix">{getSuffix}</span>}
              </div>
            </Form.Item>
          );
        }
      }

      return () => {
        const { colProps = {}, colSlot, renderColContent, component } = props.schema;
        if (!componentMap.has(component)) {
          return null;
        }

        const { baseColProps = {} } = props.formProps;
        // update-begin--author:liaozhiyang---date:20230803---for：【issues-641】Tweak the table search formspanInvalid configuration
        const { getIsMobile } = useAppInject();
        let realColProps;
        realColProps = { ...baseColProps, ...colProps };
        if (colProps['span'] && !unref(getIsMobile)) {
          ['xs', 'sm', 'md', 'lg', 'xl', 'xxl'].forEach((name) => delete realColProps[name]);
        }
        // update-end--author:liaozhiyang---date:20230803---for：【issues-641】Tweak the table search formspanInvalid configuration
        const { isIfShow, isShow } = getShow();
        const values = unref(getValues);

        const getContent = () => {
          return colSlot ? getSlot(slots, colSlot, values) : renderColContent ? renderColContent(values) : renderItem();
        };

        return (
          isIfShow && (
            <Col {...realColProps} v-show={isShow}>
              {getContent()}
            </Col>
          )
        );
      };
    },
  });
</script>
