import { H3Event } from 'h3'

export function sendRedirectHome(event: H3Event) {
  return sendRedirect(event, '/', 303)
}