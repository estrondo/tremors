import { accountServiceClient } from '../utils/grpc/account'
import { Account__Output } from '../utils/@types/grpc/webapi/Account'

export default defineEventHandler(async (event) => {
  const authInfo = await getSessionAuthInfo(event)

  if (authInfo.token?.access && authInfo.provider) {
    console.log('Loading account data.')
    return await grpcPromiseOf<Account__Output>(callback => accountServiceClient.get({ email: authInfo.user?.email }, callback))
  } else {
    console.log('There is no access token and provider information.')
    return {}
  }
})
