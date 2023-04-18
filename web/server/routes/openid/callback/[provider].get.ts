import { setResponseStatus, H3Event } from 'h3'

export default defineEventHandler(async (event) => {
  const providerName = getRouterParam(event, 'provider')
  if (providerName) {
    const session = await openIdService.callback(providerName, event.node.req)
    await sessionManager.update(session, event)
    return sendRedirectHome(event)
  } else {
    return setResponseStatus(event, 400, 'There is no provider name!')
  }
})
