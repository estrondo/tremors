<script setup lang="ts">

const properties = defineProps(['name'])
const globeViewElement = shallowRef<HTMLDivElement>()
const globeViewRef = shallowRef<GlobeView>()
const globeViewControlsRef = shallowRef<GlobeControls>()
const initialCameraTarget = loadConfiguredCameraTarget(properties.name)

provide(ITOWNS_GLOBEVIEW, globeViewRef)
provide(ITOWNS_GLOBEVIEW_CONTROLS, globeViewControlsRef)


onMounted(async () => {

  const [view, placement] = useGlobeView(globeViewElement.value as HTMLDivElement, {
    name: properties.name,
    useStoredPlacement: true
  })

  const controls = new itowns.GlobeControls(view, placement)

  // const orthoSource = new itowns.WMTSSource({
  //   url: 'http://wxs.ign.fr/3ht7xcw6f7nciopo16etuqp2/geoportail/wmts',
  //   crs: 'EPSG:3857',
  //   name: 'ORTHOIMAGERY.ORTHOPHOTOS',
  //   tileMatrixSet: 'PM',
  //   format: 'image/jpeg',
  // });

  // var orthoLayer = new itowns.ColorLayer('Ortho', {
  //   source: orthoSource,
  // });

  // view.addLayer(orthoLayer);

  globeViewRef.value = view
  globeViewControlsRef.value = controls
})

</script>

<template lang="pug">
.tremors-main-map
  .globe-view(ref="globeViewElement")
  GlobeViewToolbox(:initial-camera-target="initialCameraTarget")
</template>