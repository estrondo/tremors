<script setup lang="ts">
</script>

<template lang="pug">
.login-method
  button.google(@click="useRedirectToLogin('google')") {{ $t('login.with-google') }}
  button.microsoft(@click="useRedirectToLogin('microsoft')") {{ $t('login.with-microsoft') }}

</template>