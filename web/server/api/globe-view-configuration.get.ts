export default defineEventHandler<GlobeViewConfiguration>(async (event) => {
  const viewName = getQuery(event).viewName
  if (typeof viewName === 'string') {
    try {
      console.debug('Loading globe-view configuration for view %s.', viewName)
      const configuration = await loadYAML<Record<string, GlobeViewConfiguration>>('./workdir/globe-view.yaml')
      const ret = configuration[viewName]
      if (ret) {
        return ret
      } else {
        throw new Error(`There no view configuration for ${viewName}!`)
      }
    } catch (error: any) {
      console.error('Un error has ocurred while loading globe-view configuration!', error)
      throw error
    }
  } else {
    throw new Error('It is missing the viewName parameter!')
  }
})