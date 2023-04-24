export function useLocalStorage<T>(key: string, factory: () => T): Ref<T> {

  const stored = shallowRef((() => {
    const json = globalThis.localStorage.getItem(key)
    if (json) {
      return JSON.parse(json)
    } else {
      return factory()
    }
  })())

  watch(stored, (newValue) => {
    globalThis.localStorage.setItem(key, JSON.stringify(newValue))
  })

  return stored
}