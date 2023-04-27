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
    span {{ $t('account.editor.loading') }}
  div.form.account-editor(v-else)
    .avatar
      img(:src="account.avatar")
    .fields
      .field
        label 
          span {{ $t('account.editor.name') }}:
          input.name(type="text" v-model="account.name")
      .field
        label
          span {{ $t('account.editor.email') }}:
          input.email(type="email" v-model="account.email", disabled)
      .controls
        button.logout(@click.prevent="useRedirectToLogout()") {{ $t('account.logout') }}
        button.save(@click.prevent="save") {{ $t('account.editor.save') }}
</template>