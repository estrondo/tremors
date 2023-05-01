import { LayerManager, LayerManagerGroup } from "~/utils/itowns-base"
import { LevelDescription, LayerControl } from "~/utils/itowns-layer-control"

export function useLayerManager(globeViewRef: Ref<GlobeView | undefined>, viewName: string, levelDescriptions: LevelDescription[]): Ref<LayerManager | undefined> {

  const ref = shallowRef<LayerManager | undefined>()

  watch(globeViewRef, (newGlobeView) => {
    if (newGlobeView) {
      const { data, refresh } = useFetch('/api/globe-view-configuration', {
        query: {
          viewName
        }
      })

      const layerControl = new LayerControl(newGlobeView, levelDescriptions)
      let previousRemovedLayers: Set<string> | undefined

      watchEffect(() => {
        if (data.value) {
          ref.value = {
            refresh() {
              previousRemovedLayers = layerControl.removeAll()
              refresh()
            },
            groups: prepareGroups(data.value, newGlobeView, layerControl, previousRemovedLayers)
          }
        }
      })
    } else {
      ref.value = undefined
    }
  })

  return ref
}

function prepareGroups(configuration: GlobeViewConfiguration, globeView: GlobeView, layerControl: LayerControl, shouldAdd?: Set<string>): LayerManagerGroup[] {


  return configuration.layerGroups.map((group) => {
    return {
      name: group.name,
      id: group.id,
      uiType: group.uiType,
      layers: group.layers.map((layer) => {

        const uiType = layer.uiType || group.uiType

        return {
          name: layer.name,
          description: layer.description,
          icon: layer.icon,
          code: layer.code,
          uiType,
          control: createControl(layer, globeView, layerControl, shouldAdd)
        }
      })
    }
  })
}

type LayerInstance = {
  get: () => Promise<Layer>,
  dispose: () => void
}

typeof prepareGroups

function createControl(layerDescription: LayerDescription, globeView: GlobeView, layerControl: LayerControl, shouldAdd?: Set<string>) {
  const factory = LayerFactory.createFactory(layerDescription)
  let instance: Layer | undefined = undefined
  const active = ref(false)

  const LayerInstance: LayerInstance = {
    async get() {
      if (instance) {
        return Promise.resolve(instance)
      } else {
        return instance = await factory()
      }
    },

    dispose() {
      if (instance) {
        instance = undefined
        active.value = false
      }
    }
  }

  const activate = createActivateAction(active, LayerInstance, layerDescription, globeView, layerControl)
  if (shouldAdd?.has(layerDescription.id)) {
    activate()
  }

  return {
    active,
    activate
  }
}

function createActivateAction(active: Ref<boolean>, instance: LayerInstance, layerDescription: LayerDescription,
  globeView: GlobeView, layerControl: LayerControl) {

  let state = 0

  return async () => {
    if (state === 0) {
      state = 1
      try {
        const layer = await instance.get()
        try {
          await layerControl.add(layer, layerDescription.level, layerDescription.position, () => {
            state = 0
            instance.dispose()
          })
          state = 2
        } catch (error) {
          state = 0
          console.error('It was impossible to add the layer %s', layerDescription.id)
          instance.dispose()
        }
      } catch (error) {
        state = 0
        console.error('It was impossible to get the layer %s', layerDescription.id, error)
      }

      active.value = state === 2
    } else if (state === 1) {
      console.debug('It is waiting for a previous action for the layer %s', layerDescription.id)
    } else if (state === 2) {
      state = 1
      try {
        const layer = await instance.get()
        try {
          await layerControl.remove(layer, layerDescription.level)
          state = 0
          instance.dispose()
        } catch (error) {
          state = 2
          console.error('It was impossible to remove the layer %s', layerDescription.id, error)
        }
      } catch (error) {
        console.error('It was impossible to get the layer %s', layerDescription.id, error)
        state = 2
      }

      active.value = state !== 0
    }
  }
}