<template>
  <div ref="swaggerUiRef" style="height: 100%;"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
// Try importing directly SwaggerUI instead of using * as
import SwaggerUI from 'swagger-ui-dist/swagger-ui-bundle'; // Make sure to use ESM Version
import 'swagger-ui-dist/swagger-ui.css';

import { getOpenApiJson } from './OpenApi.api';

const swaggerUiRef = ref<HTMLElement | null>(null);
const API_DOMAIN = import.meta.env.VITE_GLOB_DOMAIN_URL
onMounted(async () => {
  try {
    const response = await getOpenApiJson();
    const openApiJson = response;
    if (swaggerUiRef.value) {
      SwaggerUI({
        domNode: swaggerUiRef.value,
        spec: openApiJson,
      });
    }
  } catch (error) {
    console.error('Failed to fetch OpenAPI JSON:', error);
  }
});
</script>

<style scoped>
/* Make sure the container has height */
.swagger-ui-container {
  height: 100%;
}
</style>
