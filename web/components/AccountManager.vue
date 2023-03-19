<script setup lang="ts">
</script>

<template lang="pug">
div(v-if="useIsLogged()")
  AccountEditor
  Logout
Login(v-else)
</template>