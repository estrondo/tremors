export function useLocalStorage<T>(key: string, factory: () => T): Ref<T> {

  function get(): T {
    const stored = globalThis.localStorage.getItem(key)
    if (stored) {
      return JSON.parse(stored)
    } else {
      return factory()
    }
  }

  function set(value: T): void {
    globalThis.localStorage.setItem(key, JSON.stringify(value))
  }

  const stored = shallowRef(get())

  watch(stored, (newValue) => {
    set(newValue)
  })

  return stored
}