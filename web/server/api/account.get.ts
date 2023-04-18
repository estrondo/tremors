export default defineEventHandler(async (event) => {
  const session = await sessionManager.get(event, current => openIdService.refresh(current))

  if (session) {
    console.debug('Getting account for email %s.', session.user?.email)
    return await accountService.check(session, true)
  } else {
    console.warn('There is no auth information!')
    return {}
  }
})