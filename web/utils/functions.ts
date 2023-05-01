export function ignoreNull<T, R>(fn: (arg: T) => void): (arg: T | null) => void {
  return (value) => {
    if (value)
      return fn(value)
  }
}

export function executeSafely(fn?: () => void) {
  try {
    if (fn) {
      fn()
    }
  } catch (error) {
    console.error('An error happend while executing a function!', error)
  }
}