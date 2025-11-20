<template>
  <div class="fixed h-full w-full flex flex-col justify-center items-center text-4xl">
    <div class=""> Pages located outside the main frame </div>
    <a-button @click="$router.go(-1)" class="mt-10" type="primary">Back</a-button>
  </div>
</template>
