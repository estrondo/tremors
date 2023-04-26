<script setup lang="ts">

const { locale, locales, setLocaleCookie } = useI18n()
watch(locale, setLocaleCookie)

</script>

<template lang="pug">
div
  GlobeView(name="main")
  img.logo(src="images/logo.png")
  select.lang-switcher(v-model="locale")
    option(v-for='item in locales', :class='item.code', :value='item.code') {{ item.name }}
  
</template>
