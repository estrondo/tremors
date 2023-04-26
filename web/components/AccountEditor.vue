<script setup lang="ts">
const { data: account, pending } = useFetch('/api/account')

async function save() {
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
        label {{ $t('account.editor.name') }}:
          input(type="text" v-model="account.name")
      .field
        label {{ $t('account.editor.email') }}:
          input(type="email" v-model="account.email", disabled)
      .controls
        button.save(@click.prevent="save") {{ $t('account.editor.save') }}
</template>