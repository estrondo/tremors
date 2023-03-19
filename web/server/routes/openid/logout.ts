import { AuthInfo } from '~~/server/utils/auth-session'

export default defineEventHandler(async (event) => {

  const authInfo = await getSessionAuthInfo(event)

  if (authInfo.provider) {
    await removeSessionAuthInfo(event)
  } else {
    console.error(`There is no provider for auth-session!`)
  }
  return sendRedirectHome(event)
})