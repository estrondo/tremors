<script setup lang="ts">

const properties = defineProps(['title', 'storedName'])
const dialog = shallowRef<HTMLDivElement>()
const dialogTitle = shallowRef<HTMLDivElement>()

onMounted(() => {
  useDraggable(dialogTitle.value as HTMLDivElement, dialog.value as HTMLDivElement, properties.storedName)
})

function show() {
  if (dialog.value?.style.display != "block") {
    if (dialog.value)
      dialog.value.style.display = "block"
  }
}

function close() {
  if (dialog.value)
    dialog.value.style.display = "none"
}

defineExpose({
  show() {
    show()
  },
  close() {
    close()
  }
})

</script>

<template lang="pug">
.dialog(ref="dialog" style="display: none;")

  .dialog-title(ref="dialogTitle")
    span {{ title }}
    button.close(@click="close()")

  .dialog-content
    slot
</template>