import { Account } from "../utils/@types/grpc/webapi/Account"
import { AccountUpdate } from "../utils/@types/grpc/webapi/AccountUpdate"

export default defineEventHandler(async (event) => {
  const session = await sessionManager.get(event)

  if (session) {
    const payload = await readBody(event) as Account
    const updateAccount: AccountUpdate = {
      email: payload.email,
      newName: payload.name
    }
    return accountService.update(updateAccount, session)
  } else {
    console.error('Unauthenticated request!')
    return {}
  }
})