<script setup lang="ts">


const localTemplate = useLocalTemplate()
const layerManager = inject<ReturnType<typeof useLayerManager>>(ITOWNS_LAYER_MANAGER)!
const currentGroup = shallowRef({ layers: [] })

enum UIType {
  Choice,
  Activation
}

function times(values: any[], times: number) {
  const ret: any[] = []
  for (let i = 0; i < times; i++) {
    for (const v of values) {
      ret.push(v)
    }
  }

  return ret
}

const groups = computed(() => {
  if (layerManager.value?.groups) {
    return layerManager.value.groups
  } else {
    return []
  }
})

function refresh() {
  if (layerManager.value?.refresh) {
    currentGroup.value = { layers: [] }
    layerManager.value.refresh()
  }
}

</script>

<template lang="pug">
.layer-switcher
  .header
    span.title {{ $t('layer.header.title') }}:
    select(v-model='currentGroup')
      option(v-for='group in groups', :value='group')
        .icon(:class="'class-' + group.id")
        span {{ localTemplate(group.name) }}
    button(@click.prevent.stop="refresh") ♺
  .list
    .item(v-for='layer in times(currentGroup.layers, 10)')
      .icon: img(:src="layer.icon")
      .info
        h1.name {{ localTemplate(layer.name) }}
        p.description {{  localTemplate(layer.description) }}
        p {{ layer.control.active }}
      .control
        .choice(v-if='layer.uiType === UIType.Choice')
          input(type='radio', :title='$t("layer.control.choice")', @click='layer.control.activate', :checked='layer.control.active.value')
        .activation(v-else-if='layer.uiType === UIType.Activation')
          input(type='checkbox', :title="$t('layer.control.activation')", @click='layer.control.activate', :checked='layer.control.active.value')
  .controls
    span Controls

</template>