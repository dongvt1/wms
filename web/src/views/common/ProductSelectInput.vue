<template>
    <a-select v-bind="$attrs" v-model:value="innerValue" :options="options" :loading="loading" show-search allow-clear
        :filter-option="filterOption" :placeholder="placeholder" @change="handleChange" />
</template>

<script lang="ts" setup>
import { ref, watch, onMounted } from 'vue';
import { productApi, type ProductModel } from '/@/api/common/product';

interface Props {
    /** Giá trị đang được chọn (product.id) */
    value?: string;
    placeholder?: string;
    /** Lọc theo loại: 'product' | 'material' | 'semi' | undefined = lấy tất cả */
    type?: string;
    disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    placeholder: 'Chọn sản phẩm / NVL',
});

const emit = defineEmits<{
    (e: 'update:value', val: string | undefined): void;
    (e: 'change', val: string | undefined, option: any): void;
}>();

const innerValue = ref(props.value);
const options = ref<{ label: string; value: string }[]>([]);
const loading = ref(false);

watch(() => props.value, (v) => { innerValue.value = v; });

async function loadOptions() {
    loading.value = true;
    try {
        const list: any = props.type
            ? await productApi.listByType(props.type)
            : await productApi.listActive();
        options.value = (list || []).map((p: ProductModel) => ({
            label: `${p.code} - ${p.name}`,
            value: p.id as string,
        }));
    } catch { options.value = []; }
    finally { loading.value = false; }
}

function filterOption(input: string, option: any) {
    return option.label.toLowerCase().includes(input.toLowerCase());
}

function handleChange(val: string, opt: any) {
    emit('update:value', val);
    emit('change', val, opt);
}

onMounted(loadOptions);
</script>
