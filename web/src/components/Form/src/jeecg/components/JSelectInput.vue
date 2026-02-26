<template>
  <a-select v-bind="bindProps" @change="onChange" @search="onSearch" />
</template>

<script lang="ts">
  import { propTypes } from '/@/utils/propTypes';
  import { defineComponent, ref, watch, computed } from 'vue';

  // Input drop-down box（This component is currently unused）
  export default defineComponent({
    name: 'JSelectInput',
    props: {
      options: propTypes.array.def(() => []),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, attrs }) {
      // internal options Options
      const options = ref<any[]>([]);
      // Listen to external options change，并覆盖internal options
      watch(
        () => props.options,
        () => {
          options.value = [...props.options];
        },
        { deep: true, immediate: true }
      );
      // merge props and attrs
      const bindProps: any = computed(() =>
        Object.assign(
          {
            showSearch: true,
          },
          props,
          attrs,
          {
            options: options.value,
          }
        )
      );

      function onChange(...args: any[]) {
        deleteSearchAdd(args[0]);
        emit('change', ...args);
        emit('update:value', args[0]);
      }

      function onSearch(value) {
        // Whether the corresponding item is found，If not found, add this item
        let foundIt =
          options.value.findIndex((option) => {
            return option.value.toString() === value.toString();
          }) !== -1;
        // !!value ：Do not add null values
        if (!foundIt && !!value) {
          deleteSearchAdd(value);
          // searchAdd Was it added via search?
          options.value.push({ value: value, searchAdd: true });
          //onChange(value,{ value })
        } else if (foundIt) {
          onChange(value);
        }
      }

      // Remove useless searches（user input）and the items created
      function deleteSearchAdd(value = '') {
        let indexes: any[] = [];
        options.value.forEach((option, index) => {
          if (option.searchAdd) {
            if ((option.value ?? '').toString() !== value.toString()) {
              indexes.push(index);
            }
          }
        });
        // Flip to delete items in an array
        for (let index of indexes.reverse()) {
          options.value.splice(index, 1);
        }
      }

      return {
        bindProps,
        onChange,
        onSearch,
      };
    },
  });
</script>

<style scoped></style>
