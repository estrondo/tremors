import { EarthquakeLayer } from "~~/utils/itowns/EarthQuakeLayer"

export async function createEarthquakeLayer(description: LayerDescription): Promise<Layer> {
  return new EarthquakeLayer(description.id, description.parameters)
}