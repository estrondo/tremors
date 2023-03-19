import { requestCallback as Callback } from '@grpc/grpc-js'

export function grpcPromiseOf<T>(fn: (callback: Callback<T>) => void): Promise<T> {
  return new Promise((resolve, reject) => {
    fn((err, value) => {
      if (err) {
        reject(err)
      } else {
        resolve(value)
      }
    })
  })
}