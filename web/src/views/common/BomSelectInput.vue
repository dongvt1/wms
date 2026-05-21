<template>
    <a-select v-bind="$attrs" v-model:value="innerValue" :options="options" :loading="loading" show-search allow-clear
        :filter-option="filterOption" :placeholder="placeholder" @change="handleChange" />
</template>

<script lang="ts" setup>
import { ref, watch, onMounted } from 'vue';
import { bomApi, type BomModel } from '/@/api/common/bom';

interface Props {
    /** Giá trị đang được chọn (bom.id) */
    value?: string;
    placeholder?: string;
    /** Nếu cung cấp, chỉ hiển thị BOM của sản phẩm này */
    productId?: string;
    disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    placeholder: 'Chọn BOM',
});

const emit = defineEmits<{
    (e: 'update:value', val: string | undefined): void;
    (e: 'change', val: string | undefined, option: any): void;
}>();

const innerValue = ref(props.value);
const options = ref<{ label: string; value: string }[]>([]);
const loading = ref(false);

watch(() => props.value, (v) => { innerValue.value = v; });
watch(() => props.productId, () => loadOptions());

async function loadOptions() {
    loading.value = true;
    try {
        let list: BomModel[];
        if (props.productId) {
            list = (await bomApi.getByProductId(props.productId)) as any;
        } else {
            list = (await bomApi.listActive()) as any;
        }
        options.value = (list || []).map((b: BomModel) => ({
            label: `${b.bomCode} - ${b.bomName}${b.isDefault ? ' ⭐' : ''}`,
            value: b.id as string,
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
