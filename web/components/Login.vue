<script setup lang="ts">
</script>

<template lang="pug">
.login-method
  button.google(@click="useRedirectToLogin('google')") Sign in with Google
  button.microsoft(@click="useRedirectToLogin('microsoft')") Sign in with Microsoft

</template>