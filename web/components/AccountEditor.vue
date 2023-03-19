<script setup lang="ts">
const account = useFetch('/api/account')
</script>

<template lang="pug">
form
  .avatar
    img
  .fields
    .field
      label Name:
        input(type="text" v-model="account.name")
    .field
      label E-mail:
        input(type="email" v-model="account.email")
    .controls
      button.save Save
</template>