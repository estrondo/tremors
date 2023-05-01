<script setup lang="ts">

const properties = defineProps(['name'])
const globeViewElement = shallowRef<HTMLDivElement>()
const globeViewRef = shallowRef<GlobeView>()
const globeViewControlsRef = shallowRef<GlobeControls>()
const layerManagerRef = useLayerManager(globeViewRef, properties.name!, [
  ["basemap", true],
  ["elevation", true],
  ["events", false]
])

// const initialCameraTarget = loadConfiguredCameraTarget(properties.name)

provide(ITOWNS_GLOBEVIEW, globeViewRef)
provide(ITOWNS_GLOBEVIEW_CONTROLS, globeViewControlsRef)
provide(ITOWNS_LAYER_MANAGER, layerManagerRef)


onMounted(async () => {

  const [view, placement] = useGlobeView(globeViewElement.value as HTMLDivElement, {
    name: properties.name,
    useStoredPlacement: true
  })

  const controls = new itowns.GlobeControls(view, placement)

  globeViewRef.value = view
  globeViewControlsRef.value = controls
})

</script>

<template lang="pug">
.main-view
  .globe-view(ref="globeViewElement")
  GlobeViewToolbox(:initial-camera-target="initialCameraTarget")
</template>