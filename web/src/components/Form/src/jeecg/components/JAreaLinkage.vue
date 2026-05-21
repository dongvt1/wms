<template>
  <Cascader v-bind="attrs" :value="cascaderValue" :options="getOptions" @change="handleChange" />
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, reactive, watchEffect, computed, unref, watch, onMounted } from 'vue';
  import { Cascader } from 'ant-design-vue';
  import { provinceAndCityData, regionData, provinceAndCityDataPlus, regionDataPlus } from '../../utils/areaDataUtil';
  import { useRuleFormItem } from '/@/hooks/component/useFormItem';
  import { propTypes } from '/@/utils/propTypes';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { isArray } from '/@/utils/is';

  export default defineComponent({
    name: 'JAreaLinkage',
    components: {
      Cascader,
    },
    inheritAttrs: false,
    props: {
      value: propTypes.oneOfType([propTypes.object, propTypes.array, propTypes.string]),
      //Whether to display districts and counties
      showArea: propTypes.bool.def(true),
      //Is it all
      showAll: propTypes.bool.def(false),
      // Store data （allhour：What is passed outside is an array；province, city, regionWhat is passed outside is a string）
      saveCode: propTypes.oneOf(['province', 'city', 'region', 'all']).def('all'),
    },
    emits: ['options-change', 'change', 'update:value'],
    setup(props, { emit, refs }) {
      const emitData = ref<any[]>([]);
      const attrs = useAttrs();
      // const [state] = useRuleFormItem(props, 'value', 'change', emitData);
      const cascaderValue = ref([]);
      const getOptions = computed(() => {
        if (props.showArea && props.showAll) {
          return regionDataPlus;
        }
        if (props.showArea && !props.showAll) {
          return regionData;
        }
        if (!props.showArea && !props.showAll) {
          return provinceAndCityData;
        }
        if (!props.showArea && props.showAll) {
          return provinceAndCityDataPlus;
        }
      });
      /**
       * monitorvaluechange
       */
      watchEffect(() => {
        // update-begin--author:liaozhiyang---date:20240612--for：【TV360X-1223】Replacement of new components in provinces and cities
        if (props.value) {
          initValue();
        } else {
          cascaderValue.value = [];
        }
        // update-end--author:liaozhiyang---date:20240612---for：【TV360X-1223】Replacement of new components in provinces and cities
      });

      /**
       * Convert string value to array
       */
      function initValue() {
        let value = props.value ? props.value : [];
        // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-501】Replacement of new components in provinces and cities
        if (value && typeof value === 'string' && value != 'null' && value != 'undefined') {
          const arr = value.split(',');
          cascaderValue.value = transform(arr);
        } else if (isArray(value)) {
          if (value.length) {
            cascaderValue.value = transform(value);
          } else {
            cascaderValue.value = [];
          }
        }
        // update-end--author:liaozhiyang---date:20240607---for：【TV360X-501】Replacement of new components in provinces and cities
      }
      function transform(arr) {
        let result: any = [];
        if (props.saveCode === 'region') {
          const regionCode = arr[0];
          result = [`${regionCode.substring(0, 2)}0000`, `${regionCode.substring(0, 2)}${regionCode.substring(2, 4)}00`, regionCode];
        } else if (props.saveCode === 'city') {
          const cityCode = arr[0];
          result = [`${cityCode.substring(0, 2)}0000`, cityCode];
        } else if (props.saveCode === 'province') {
          const provinceCode = arr[0];
          result = [provinceCode];
        } else {
          result = arr;
        }
        return result;
      }
      /**
       * liaozhiyang
       * 2024-06-17
       * 【TV360X-1224】By default, the values ​​passed to the outside by the province and city components are strings separated by commas.
       * */
      const send = (data) => {
        let result = data;
        if (result) {
          if (props.saveCode === 'all') {
            // An array is passed
          } else {
            // Passed is a string
            result = data.join(',');
          }
        }
        emit('change', result);
        emit('update:value', result);
      };

      function handleChange(arr, ...args) {
        // update-begin--author:liaozhiyang---date:20240607---for：【TV360X-501】Replacement of new components in provinces and cities
        if (arr?.length) {
          let result: any = [];
          if (props.saveCode === 'region') {
            // Maybe only two（选择香港hour，Only provinces）
            result = [arr[arr.length - 1]];
          } else if (props.saveCode === 'city') {
            result = [arr[1]];
          } else if (props.saveCode === 'province') {
            result = [arr[0]];
          } else {
            result = arr;
          }
          send(result);
        } else {
          send(arr);
        }
        // update-end--author:liaozhiyang---date:20240607---for：【TV360X-501】Replacement of new components in provinces and cities
        // emitData.value = args;
        //update-begin-author:taoyan date:2022-6-27 for: VUEN-1424【vue3】tree table、Single table、jvxe、erp 、Embedded subtable provinces, cities and counties Can't choose
        // Changed abovev-model:valueAs a result, the selected data is not displayed
        // state.value = result;
        //update-end-author:taoyan date:2022-6-27 for: VUEN-1424【vue3】tree table、Single table、jvxe、erp 、Embedded subtable provinces, cities and counties Can't choose
      }
      
      return {
        cascaderValue,
        attrs,
        regionData,
        getOptions,
        handleChange,
      };
    },
  });
</script>
