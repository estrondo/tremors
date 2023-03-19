import { H3Event } from 'h3'
import { AuthInfo } from '../../../utils/auth-session'

export default defineEventHandler(async (event) => {
  const authInfo = await getSessionAuthInfo(event)
  const provider = getRouterParam(event, 'provider')

  if (provider) {
    return handleProvider(provider, authInfo, event)
  } else {
    console.log(`There is no provider name.`)
    return sendRedirectHome(event)
  }
})

async function handleProvider(name: string, authInfo: AuthInfo, event: H3Event) {

  async function doLogin() {
    const client = await getOpenIDClient(name)
    return sendRedirect(event, client.authorizationUrl({ scope: 'openid profile email', prompt: 'select_account' }), 303)
  }

  if (!authInfo) {
    console.log(`There is no auth-session data. Logging into ${name}.`)
    return doLogin()
  } else if (authInfo.provider == name) {
    switch (authInfoExpired(authInfo)) {
      case true:
        console.log(`The session is expired. Logging into ${name}.`)
        return doLogin()
      case undefined:
        console.log(`There is no auth-session expireAt. Logging into ${name}.`)
        return doLogin()
      case false:
        console.log('It already is logged.')
        return sendRedirectHome(event)
    }
  } else {
    await removeSessionAuthInfo(event)
    console.log(`Logging into a different provider ${name}.`)
    return await doLogin()
  }
}