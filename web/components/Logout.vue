<template lang="pug">
.logout
  button(@click="useRedirectToLogout()")
    .icon
    .
      {{ $t('account.logout') }}
</template>