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
    span.tooltip {{ $t('toolbox.tip.account') }}

  button.undo(@click='globeViewControl.undo()')
    span.tooltip {{ $t('toolbox.tip.undo') }}

  button.redo(@click='globeViewControl.redo()')
    span.tooltip {{ $t('toolbox.tip.redo') }}

  button.zoom-in(@click='globeViewControl.zoomIn()')
    span.tooltip {{ $t('toolbox.tip.zoomIn') }}

  button.zoom-out(@click='globeViewControl.zoomOut()')
    span.tooltip {{ $t('toolbox.tip.zoomOut') }}

  button.zoom-reset(@click='globeViewControl.reset()')
    span.tooltip {{ $t('toolbox.tip.zoomReset') }}

  button.layer
    span.tooltip {{ $t('toolbox.tip.layer') }}

  button.search
    span.tooltip {{ $t('toolbox.tip.search') }}

  button.alert
    span.tooltip {{ $t('toolbox.tip.alert') }}

  button.about
    span.tooltip {{ $t('toolbox.tip.about') }}

Dialog(:title='$t("account.dialog.title")', ref='accountDialog', stored-name='account')
  AccountManager

</template>