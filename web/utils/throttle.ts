export function useThrottle<A>(fn: (a: A) => void, time: number = 100): (a: A) => void {

  let timeoutId: any = null

  return (...args) => {
    if (timeoutId)
      globalThis.clearTimeout(timeoutId)

    timeoutId = globalThis.setTimeout(() => {
      timeoutId = null
      fn.apply(globalThis, args)
    }, time)
  }
}