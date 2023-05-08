export type LevelDescription = [string, boolean] // name, single

type Floor = { position: number, layer: Layer, onForcedRemove?: () => void }

type Level = {
  name: string,
  single: boolean,
  floors: Floor[]
}

export class LayerControl {

  #view: GlobeView
  #levels: Level[]

  constructor(view: GlobeView, descriptions: LevelDescription[]) {
    this.#view = view
    this.#levels = descriptions.map(([name, single]) => {
      return {
        name: name,
        single: single,
        floors: []
      }
    })
  }

  async add(layer: Layer, level: string, position: number, onForcedRemove?: () => void) {
    const [levelObj, previousLayer] = this.#search(layer.id, level)
    if (levelObj) {
      if (levelObj.single) {
        if (levelObj.floors.length) {
          this.#view.removeLayer(levelObj.floors[0].layer)
          executeSafely(levelObj.floors[0].onForcedRemove)
        }

        levelObj.floors = [{ position: 0, layer: layer, onForcedRemove }]
        await this.#view.addLayer(layer)
        this.#updateOrdering()
      } else {
        if (!previousLayer) {
          await this.#view.addLayer(layer)
          levelObj.floors = insertLayer(layer, position, levelObj.floors, onForcedRemove)
          this.#updateOrdering()
        } else {
          console.warn(`There is already a layer with id: ${layer.id}.`)
        }
      }

      this.#callOnAdd(layer)
    } else {
      console.warn(`There is no level: ${level}.`)
    }
  }

  remove(layer: Layer, level: string): void {
    this.#view.removeLayer(layer.id)
    this.#callOnRemove(layer)
    const [levelObj] = this.#search(layer.id, level)
    if (levelObj) {
      if (levelObj.single) {
        levelObj.floors = []
      } else {
        const index = levelObj.floors.findIndex(floor => floor.layer === layer)
        if (index !== -1) {
          levelObj.floors.splice(index, 1)
        }
      }
    }
  }

  removeAll() {
    const removed = new Set<string>()
    for (const level of this.#levels) {
      for (const floor of level.floors) {
        this.#view.removeLayer(floor.layer.id)
        removed.add(floor.layer.id)
        executeSafely(floor.onForcedRemove)
      }

      level.floors = []
    }

    return removed
  }

  #callOnAdd(layer: Layer) {
    executeSafely(() => {
      if (typeof layer.onAdd === 'function') {
        layer.onAdd(this.#view)
      }
    })
  }

  #callOnRemove(layer: Layer) {
    executeSafely(() => {
      if (typeof layer.onRemove === 'function') {
        layer.onRemove(this.#view)
      }
    })
  }

  #search(layerId: string, levelName: string): [Level?, Layer?] {
    let level: Level | undefined = undefined
    let layer: Layer | undefined = undefined

    for (const current of this.#levels) {

      if (!layer) {
        layer = current.floors.find(layer => layer.layer.id === layerId)
      }

      if (!level && current.name === levelName) {
        level = current
      }
    }

    return [level, layer]
  }

  #updateOrdering() {
    const layers = this.#levels.flatMap((level) => {
      return level
        .floors
        .filter(floor => floor.layer.isColorLayer)
        .map(floor => floor.layer)
    })

    let currentIndex = 0
    for (const layer of layers) {
      itowns.ColorLayersOrdering.moveLayerToIndex(this.#view, layer.id, currentIndex++)
    }
  }
}

function insertLayer(layer: Layer, position: number, floors: Floor[], onForcedRemove?: () => void) {
  const ret: Floor[] = []
  const newFloor = { position: position, layer: layer, onForcedRemove }
  let shouldPush = true

  for (const floor of floors) {
    if (shouldPush && position < floor.position) {
      shouldPush = false
      ret.push(newFloor)
    } else if (position == floor.position) {
      throw new Error('There is two layers in the same level with the same position!')
    }

    ret.push(floor)
  }

  if (shouldPush) {
    ret.push(newFloor)
  }

  return ret

}