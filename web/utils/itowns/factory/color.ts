export async function createColorLayer(description: LayerDescription): Promise<Layer> {

  const source = loadSource(description.parameters.source)
  return new itowns.ColorLayer(description.id, {
    name: description.id,
    source
  })
}

function loadSource(source: any): Promise<Source> {
  switch (source.type) {
    case 'tms': return loadTMSSource(source)
    default: throw new Error(`Unsupported source of type: ${source.type}!`)
  }
}

function loadTMSSource(source: any): Promise<Source> {
  return new itowns.TMSSource(source)
}