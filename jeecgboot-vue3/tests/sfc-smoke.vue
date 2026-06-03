<template>
  <div class="hello">Hello {{ name }}</div>
</template>

<script setup lang="ts">
defineProps<{ name: string }>();
</script>
