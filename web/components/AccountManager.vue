<script setup lang="ts">
</script>

<template lang="pug">
div(v-if="useIsLogged()")
  AccountEditor
Login(v-else)
</template>