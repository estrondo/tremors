import { createColorLayer } from "./itowns/factory/color"
import { createElevationLayer } from "./itowns/factory/elevation"
import { createFeatureGeometry } from "./itowns/factory/feature-geometry"
import { createEarthquakeLayer } from "./itowns/factory/earthquake"

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
  'feature-geometry': createFeatureGeometry,
  'earthquake': createEarthquakeLayer
}