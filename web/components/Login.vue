<script setup lang="ts">
</script>

<template lang="pug">
.login-method
  button.google(@click="useRedirectToLogin('google')")
    .icon
    span {{ $t('login.with-google') }}
  button.microsoft(@click="useRedirectToLogin('microsoft')")
    .icon
    span {{ $t('login.with-microsoft') }}

</template>