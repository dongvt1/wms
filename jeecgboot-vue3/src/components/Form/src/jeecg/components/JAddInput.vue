<template>
  <div v-for="(param, index) in dynamicInput.params" :key="index" style="display: flex">
    <a-input placeholder="Please enter parameterskey" v-model:value="param.label" style="width: 30%; margin-bottom: 5px" @input="emitChange" />
    <a-input placeholder="Please enter parametersvalue" v-model:value="param.value" style="width: 30%; margin: 0 0 5px 5px" @input="emitChange" />
    <MinusCircleOutlined
      v-if="dynamicInput.params.length > min"
      class="dynamic-delete-button"
      @click="remove(param)"
      style="width: 50px"
    ></MinusCircleOutlined>
  </div>
  <div>
    <a-button type="dashed" style="width: 60%" @click="add">
      <PlusOutlined />
      New
    </a-button>
  </div>
</template>
<script lang="ts">
  import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons-vue';
  import { defineComponent, reactive, ref, UnwrapRef, watchEffect } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  import { isEmpty } from '/@/utils/is';
  import { tryOnMounted, tryOnUnmounted } from '@vueuse/core';
  interface Params {
    label: string;
    value: string;
  }

  export default defineComponent({
    name: 'JAddInput',
    props: {
      value: propTypes.string.def(''),
      //update-begin---author:wangshuai ---date:20220516  for：[VUEN-1043]System coding rules，The last input box cannot be deleted------------
      //How long does it take to customize the delete button to be displayed?
      min: propTypes.integer.def(1),
      //update-end---author:wangshuai ---date:20220516  for：[VUEN-1043]System coding rules，The last input box cannot be deleted--------------
    },
    emits: ['change', 'update:value'],
    setup(props, { emit }) {
      //inputdynamic data
      const dynamicInput: UnwrapRef<{ params: Params[] }> = reactive({ params: [] });
      //deleteInput
      const remove = (item: Params) => {
        let index = dynamicInput.params.indexOf(item);
        if (index !== -1) {
          dynamicInput.params.splice(index, 1);
        }
        emitChange();
      };
      //NewInput
      const add = () => {
        dynamicInput.params.push({
          label: '',
          value: '',
        });
        emitChange();
      };

      //Listen for incoming datavalue
      watchEffect(() => {
        initVal();
      });

      /**
       * initialization value
       */
      function initVal() {
        console.log('props.value', props.value);
        dynamicInput.params = [];
        if (props.value && props.value.indexOf('{') == 0) {
          let jsonObj = JSON.parse(props.value);
          Object.keys(jsonObj).forEach((key) => {
            dynamicInput.params.push({ label: key, value: jsonObj[key] });
          });
        }
      }
      /**
       * value change
       */
      function emitChange() {
        let obj = {};
        if (dynamicInput.params.length > 0) {
          dynamicInput.params.forEach((item) => {
            obj[item['label']] = item['value'];
          });
        }
        emit('change', isEmpty(obj) ? '' : JSON.stringify(obj));
        emit('update:value', isEmpty(obj) ? '' : JSON.stringify(obj));
      }

      return {
        dynamicInput,
        emitChange,
        remove,
        add,
      };
    },
    components: {
      MinusCircleOutlined,
      PlusOutlined,
    },
  });
</script>
<style scoped>
  .dynamic-delete-button {
    cursor: pointer;
    position: relative;
    top: 4px;
    font-size: 24px;
    color: #999;
    transition: all 0.3s;
  }

  .dynamic-delete-button:hover {
    color: #777;
  }

  .dynamic-delete-button[disabled] {
    cursor: not-allowed;
    opacity: 0.5;
  }
</style>
