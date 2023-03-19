<script setup lang="ts">

const props = defineProps(['initialCameraTarget'])

const globeViewControlsRef = inject<GlobeControls>(ITOWNS_GLOBEVIEW_CONTROLS) as unknown as Ref<GlobeControls>
const globeViewControl = useGlobeViewControl(globeViewControlsRef, props.initialCameraTarget)

const accountDialog = shallowRef()

function showDialog(dialogComponent: any) {
  dialogComponent.show()
}

</script>

<template lang="pug">
.view-toolbox
  button.account(@click='showDialog(accountDialog)')
    span.tooltip Tooltip Text

  button.undo(@click='globeViewControl.undo()')
    span.tooltip Tooltip Text

  button.redo(@click='globeViewControl.redo()')
    span.tooltip Tooltip Text

  button.zoom-in(@click='globeViewControl.zoomIn()')
    span.tooltip Tooltip Text

  button.zoom-out(@click='globeViewControl.zoomOut()')
    span.tooltip Tooltip Text

  button.zoom-reset(@click='globeViewControl.reset()')
    span.tooltip Tooltip Text

  button.layer
    span.tooltip Tooltip Text

  button.alert
    span.tooltip Tooltip Text

  button.search
    span.tooltip Tooltip Text

  button.about
    span.tooltip Tooltip Text

Dialog(title='Account Manager', ref='accountDialog', stored-name='account')
  AccountManager

</template>