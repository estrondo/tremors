import { createColorLayer } from "./itowns-layer-factory/color"
import { createElevationLayer } from "./itowns-layer-factory/elevation"
import { createFeatureGeometry } from "./itowns-layer-factory/feature-geometry"

export const LayerFactory = {

  createFactory(layerDescription: LayerDescription): (() => Promise<Layer>) {
    const factory = SUPPORTED_CODES[layerDescription.code]
    if (factory) {
      return () => factory(layerDescription)
    } else {
      throw new Error(`There is no layer factory for ${layerDescription.code}!`)
    }
  }
}

const SUPPORTED_CODES: Record<string, ((layerDescription: LayerDescription) => Promise<Layer>) | undefined> = {
  'elevation': createElevationLayer,
  'color': createColorLayer,
  'feature-geometry': createFeatureGeometry
}