<template>
    <a-range-picker v-model:value="rangeValue" @change="handleChange" :show-time="datetime" :placeholder="placeholder" :valueFormat="valueFormat"/>
</template>

<script>
    import { defineComponent, ref, watch, computed } from 'vue';
    import { propTypes } from '/@/utils/propTypes';
    import { Form } from 'ant-design-vue';

    const placeholder = ['start date', 'end date']
    /**
     * for range queries
     */
    export default defineComponent({
        name: "JRangeDate",
        props:{
            value: propTypes.string.def(''),
            datetime: propTypes.bool.def(false),
            placeholder: propTypes.string.def(''),
        },
        emits:['change', 'update:value'],
        setup(props, {emit}){
            const rangeValue = ref([])
            const formItemContext = Form.useInjectFormItemContext();

            watch(()=>props.value, (val)=>{
                if(val){
                    rangeValue.value = val.split(',')
                }else{
                    rangeValue.value = []
                }
            }, {immediate: true});

            const valueFormat = computed(()=>{
                if(props.datetime === true){
                    return 'YYYY-MM-DD HH:mm:ss'
                }else{
                    return 'YYYY-MM-DD'
                }
            });

            function handleChange(arr){
                let str = ''
                if(arr && arr.length>0){
                  // update-begin--author:liaozhiyang---date:20240710---for：[issues/6368] rangeDateRemove the judgment and allow the starting item or the ending item to be empty and compatibleallowEmpty
                  str = arr.join(',')
                  // update-end--author:liaozhiyang---date:20240710---for：[issues/6368] rangeDateRemove the judgment and allow the starting item or the ending item to be empty and compatibleallowEmpty
                }
                emit('change', str);
                emit('update:value', str);
                formItemContext.onFieldChange();
            }
            return {
                rangeValue,
                placeholder,
                valueFormat,
                handleChange
            }
        }
    });
</script>

<style scoped>

</style>
