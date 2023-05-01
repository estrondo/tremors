const NOT_DEFINED = {}

export function createDeferred<T>(): { resolve: (value: T) => void, reject: (reason: any) => void, promise: Promise<T> } {

  let _resolve: (value: T) => void
  let _reject: (reason: any) => void
  let _resolved: any = NOT_DEFINED
  let _reason: any = NOT_DEFINED

  const promise = new Promise<T>((resolve, reject) => {
    if (_resolved !== NOT_DEFINED) {
      resolve(_resolved)
    } else if (_reason !== NOT_DEFINED) {
      reject(_reason)
    } else {
      _resolve = resolve
      _reject = reject
    }
  })

  return {
    resolve: (value: T) => {
      if (_resolve) {
        _resolve(value)
      } else if (_resolved === NOT_DEFINED) {
        _resolved = value
      }
    },

    reject: (reason: any) => {
      if (_reject) {
        _reject(reason)
      } else if (_reason === NOT_DEFINED) {
        _reason = reason
      }
    },

    promise
  }
}