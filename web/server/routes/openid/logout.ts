export default defineEventHandler(async (event) => {
  await sessionManager.clean(event)
  return sendRedirectHome(event)
})