export function promiseOf<T>(call: (callback: (error: any, value?: T) => void) => void): Promise<T> {
  return new Promise((resolve, reject) => {
    call((error, value) => {
      if (value !== undefined) {
        resolve(value)
      } else {
        reject(error || Error('There is no value!'))
      }
    })
  })
}