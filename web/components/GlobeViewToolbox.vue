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
    span.tooltip Account Management

  button.undo(@click='globeViewControl.undo()')
    span.tooltip Go to previous

  button.redo(@click='globeViewControl.redo()')
    span.tooltip Go to next

  button.zoom-in(@click='globeViewControl.zoomIn()')
    span.tooltip Zoom in

  button.zoom-out(@click='globeViewControl.zoomOut()')
    span.tooltip Zoom out

  button.zoom-reset(@click='globeViewControl.reset()')
    span.tooltip Reset zoom

  button.layer
    span.tooltip Layers Management

  button.alert
    span.tooltip Alerts Management

  button.search
    span.tooltip Events searching

  button.about
    span.tooltip About Tremors

Dialog(title='Account Manager', ref='accountDialog', stored-name='account')
  AccountManager

</template>