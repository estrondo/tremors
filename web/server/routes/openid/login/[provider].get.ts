export default defineEventHandler(async (event) => {
  const providerName = getRouterParam(event, 'provider')

  if (providerName) {
    const authorizationUrl = await openIdService.getAuthorizationURL(providerName)
    return sendRedirect(event, authorizationUrl, 303)
  } else {
    console.debug(`The provider name was not provided!`)
    return sendRedirectHome(event)
  }
})