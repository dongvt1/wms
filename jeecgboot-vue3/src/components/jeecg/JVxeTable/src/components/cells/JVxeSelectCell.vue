<template>
  <a-select :value="innerValue" v-bind="selectProps">
    <template v-if="loading" #notFoundContent>
      <LoadingOutlined />
      <span>&nbsp;loading…</span>
    </template>
    <template v-for="option of selectOptions" :key="option.value">
      <a-select-option :value="option.value" :title="option.text || option.label || option.title" :disabled="option.disabled">
        <span>{{ option.text || option.label || option.title || option.value }}</span>
      </a-select-option>
    </template>
  </a-select>
</template>

<script lang="ts">
  import { ref, computed, defineComponent } from 'vue';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
  import { JVxeComponent, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { dispatchEvent } from '/@/components/jeecg/JVxeTable/utils';
  import { isPromise } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxeSelectCell',
    components: { LoadingOutlined },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, row, originColumn, scrolling, handleChangeCommon, handleBlurCommon } = useJVxeComponent(props);
      const loading = ref(false);
      // Loaded asynchronouslyoptions（Used for multi-level linkage）
      const asyncOptions = ref<any[] | null>(null);
      // drop down box props
      const selectProps = computed(() => {
        let selProps = {
          ...cellProps.value,
          allowClear: true,
          autofocus: true,
          defaultOpen: !scrolling.value,
          style: { width: '100%' },
          filterOption: handleSelectFilterOption,
          onBlur: handleBlur,
          onChange: handleChange,
        };
        // judgeselectWhether to allow input
        let { allowSearch, allowInput } = originColumn.value;
        if (allowInput === true || allowSearch === true) {
          selProps['showSearch'] = true;
          selProps['onSearch'] = handleSearchSelect;
        }
        return selProps;
      });
      // drop down options
      const selectOptions = computed(() => {
        if (asyncOptions.value) {
          return asyncOptions.value;
        }
        let { linkage } = props.renderOptions;
        if (linkage) {
          let { getLinkageOptionsSibling, config } = linkage;
          let res = getLinkageOptionsSibling(row.value, originColumn.value, config, true);
          // when returningPromisehour，It shows that it is multi-level linkage
          if (res instanceof Promise) {
            loading.value = true;
            res
              .then((opt) => {
                asyncOptions.value = opt;
                loading.value = false;
              })
              .catch((e) => {
                console.error(e);
                loading.value = false;
              });
          } else {
            asyncOptions.value = null;
            return res;
          }
        }
        return originColumn.value.options;
      });

      // --------- created ---------

      // Multiple choice、searchtype
      let multipleTypes = [JVxeTypes.selectMultiple, 'list_multi'];
      let searchTypes = [JVxeTypes.selectSearch, 'sel_search'];
      if (multipleTypes.includes(props.type)) {
        // deal withMultiple choice
        let props = originColumn.value.props || {};
        props['mode'] = 'multiple';
        props['maxTagCount'] = 1;
        //update-begin-author:taoyan date:2022-12-5 for: issues/271 Online表单主子表单下拉Multiple choice无法search
        originColumn.value.allowSearch = true;
        //update-end-author:taoyan date:2022-12-5 for: issues/271 Online表单主子表单下拉Multiple choice无法search
        originColumn.value.props = props;
      } else if (searchTypes.includes(props.type)) {
        // deal withsearch
        originColumn.value.allowSearch = true;
      }

      /** deal with change event */
      function handleChange(value) {
        // deal with下级联动
        let linkage = props.renderOptions.linkage;
        if (linkage) {
          linkage.handleLinkageSelectChange(row.value, originColumn.value, linkage.config, value);
        }
        handleChangeCommon(value);
      }

      /** deal withblur失去焦点event */
      function handleBlur(value) {
        let { allowInput, options } = originColumn.value;
        if (allowInput === true) {
          // 删除无用的因search（user input）and the items created
          if (typeof value === 'string') {
            let indexes: number[] = [];
            options.forEach((option, index) => {
              if (option.value.toLocaleString() === value.toLocaleString()) {
                delete option.searchAdd;
              } else if (option.searchAdd === true) {
                indexes.push(index);
              }
            });
            // Flip to delete items in an array
            for (let index of indexes.reverse()) {
              options.splice(index, 1);
            }
          }
        }
        handleBlurCommon(value);
      }

      /** 用于searchdrop down box中的内容 */
      function handleSelectFilterOption(input, option) {
      
        let { allowSearch, allowInput } = originColumn.value;
        if (allowSearch === true || allowInput === true) {
          // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-5806】js增强改变下拉searchoptions (preventoption.titlefornullReport an error)
          if (option.title == null) return false;
          // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-5806】js增强改变下拉searchoptions (preventoption.titlefornullReport an error)
          // update-begin--author:liaozhiyang---date:20230904---for：【issues/5305】JVxeTypes.select 无法按照预期进行search
          return option.title.toLowerCase().indexOf(input.toLowerCase()) >= 0;
          // update-begin--author:liaozhiyang---date:20230904---for：【issues/5305】JVxeTypes.select 无法按照预期进行search
        }
        return true;
      }

      /** select searchhour的event，for dynamic additionoptions */
      function handleSearchSelect(value) {
        let { allowSearch, allowInput, options } = originColumn.value;

        if (allowSearch !== true && allowInput === true) {
          // Whether the corresponding item is found，If not found, add this item
          let flag = false;
          for (let option of options) {
            if (option.value.toLocaleString() === value.toLocaleString()) {
              flag = true;
              break;
            }
          }
          // !!value ：Do not add null values
          if (!flag && !!value) {
            // searchAdd 是否是通过search添加的
            options.push({ title: value, value: value, searchAdd: true });
          }
        }
      }

      return {
        loading,
        innerValue,
        selectProps,
        selectOptions,
        handleChange,
        handleBlur,
      };
    },
    // 【Component enhancement】See notes for details：JVxeComponent.Enhanced
    enhanced: {
      aopEvents: {
        editActived({ $event, row, column }) {
          dispatchEvent({
            $event,
            row,
            column,
            props: this.props,
            instance: this,
            className: '.ant-select .ant-select-selection-search-input',
            isClick: false,
            handler: (el) => el.focus(),
          });
        },
      },
      translate: {
        enabled: true,
        async handler(value, ctx) {
          let { props, context } = ctx!;
          let { row, originColumn } = context;
          let options;
          let linkage = props?.renderOptions.linkage;
          // judge是否是多级联动，If so, translate asynchronously through the interface
          if (linkage) {
            let { getLinkageOptionsSibling, config } = linkage;
            let linkageOptions = getLinkageOptionsSibling(row.value, originColumn.value, config, true);
            options = isPromise(linkageOptions) ? await linkageOptions : linkageOptions;
          } else if (isPromise(originColumn.value.optionsPromise)) {
            options = await originColumn.value.optionsPromise;
          } else {
            options = originColumn.value.options;
          }
          return filterDictText(options, value);
        },
      },
      getValue(value) {
        if (Array.isArray(value)) {
          return value.join(',');
        } else {
          return value;
        }
      },
      setValue(value, ctx) {
        let { context } = ctx!;
        let { originColumn } = context;
        // judge是否是Multiple choice
        if ((originColumn.value.props || {})['mode'] === 'multiple') {
          originColumn.value.props['maxTagCount'] = 1;
        }
        if (value != null && value !== '') {
          if (typeof value === 'string') {
            return value === '' ? [] : value.split(',');
          }
          return value;
        } else {
          return undefined;
        }
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
