const UNRESOLVED = {}

export function createCache<T>(name: string, factory: () => Promise<T>, ttl: number = 60000 * 30, attempts: number = 10): () => Promise<T> {

  let timeoutId: any = null
  let cached: any = UNRESOLVED
  let promise: Promise<T> | null

  function clearCache() {
    cached = UNRESOLVED
    console.debug(`cache-cleaned: ${name}.`)
    timeoutId = null
  }

  async function tryResolve(count: number = 0): Promise<T> {
    try {
      cached = await factory()
      promise = null
      scheduleCleaning()
      console.log(`cache-resolved: ${name}.`)
      return cached
    } catch (reason) {
      if (count < attempts) {
        console.error(`cache-missed: ${name}.`, reason)
        return tryResolve(count + 1)
      } else {
        promise = null
        console.error(`cache-failed: ${name}.`, reason)
        throw reason
      }
    }
  }

  function scheduleCleaning() {
    if (timeoutId !== null) {
      clearTimeout(timeoutId)
    }

    timeoutId = setTimeout(clearCache, ttl)
  }

  return () => {
    if (cached !== UNRESOLVED) {
      scheduleCleaning()
      return Promise.resolve(cached)
    } else {
      if (!promise)
        promise = tryResolve()

      return promise
    }
  }
}