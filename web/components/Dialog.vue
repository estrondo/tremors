<script setup lang="ts">

const properties = defineProps(['title', 'storedName'])
const dialog = shallowRef<HTMLDivElement>()
const dialogTitle = shallowRef<HTMLDivElement>()
const closeElement = shallowRef<HTMLElement>()

onMounted(() => {
  useDraggable(dialogTitle.value as HTMLDivElement, dialog.value as HTMLDivElement, properties.storedName, document.body)
  useAutoFocus(dialog.value as HTMLDivElement, closeElement.value)
})

function show() {
  if (dialog.value?.style.display != "block") {
    if (dialog.value) {
      dialog.value.style.display = "block"
    }
  }

  const parent = dialog.value?.parentElement
  if (parent) {
    parent.appendChild(dialog.value!)
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
    div {{ title }}
    button.close(@click="close()", ref="closeElement")

  .dialog-content
    slot
</template>