<script setup lang="ts">
const { data: account, pending } = useFetch('/api/account')


async function save(event: MouseEvent) {
  event.preventDefault()

  try {
    await $fetch('/api/account', { method: 'POST', body: account.value })
  } catch (error: any) {
    alert(error)
  }
}

</script>

<template lang="pug">
div  
  form(v-if="pending")
    span Loading
  div(v-else)
    .avatar
      img
    .fields
      .field
        label Name:
          input(type="text" v-model="account.name")
      .field
        label E-mail:
          input(type="email" v-model="account.email", disabled)
      .controls
        button.save(@click="save") Save
</template>