import { setResponseStatus, H3Event } from 'h3'


export default defineEventHandler(async (event) => {
  const providerName = getRouterParam(event, 'provider')
  if (providerName) {
    return handleProvider(providerName, event)
  } else {
    return setResponseStatus(event, 400, '')
  }
})

async function handleProvider(name: string, event: H3Event): Promise<void> {
  try {
    const client = await getOpenIDClient(name)
    const params = client.callbackParams(event.node.req)
    const tokenSet = await client.callback(getOpenIDRedirectURI(name), params)
    const claims = tokenSet.claims()
    const newAuthInfo = {
      provider: name,
      user: {
        sub: claims.sub,
        name: claims.name,
        email: claims.email,
        emailVerified: claims.email_verified
      },
      token: {
        id: tokenSet.id_token,
        access: tokenSet.access_token,
        refresh: tokenSet.refresh_token,
        expireAt: tokenSet.expires_at
      }
    }

    console.log(tokenSet.id_token)
    await updateSessionAuthInfo(event, newAuthInfo)
    return sendRedirectHome(event)

  } catch (reason) {
    console.error('Unexpected error!', reason)
    return sendRedirectHome(event)
  }
}


