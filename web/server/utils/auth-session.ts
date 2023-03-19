import { H3Event } from 'h3'

const password = '84071648-1f7f-41e9-8e07-b46434c80d24'
const sessionName = 'auth'
const cookieIsLoggedName = 'auth-isLogged'

export interface AuthInfo {
  provider: string,
  user?: {
    owner?: string,
    name?: string,
    email?: string,
    emailVerified?: boolean
  },
  token?: {
    id?: string,
    access?: string,
    refresh?: string,
    expireAt?: number
  }
}

export async function getSessionAuthInfo(event: H3Event): Promise<AuthInfo> {
  const { data } = await getSession(event)
  return data as AuthInfo
}

export async function removeSessionAuthInfo(event: H3Event) {
  await clearSession(event, {
    sessionHeader: false,
    name: sessionName,
    password: password
  })

  return removeCookieIsLogged(event)
}

export async function updateSessionAuthInfo(event: H3Event, info: AuthInfo) {
  const { update } = await getSession(event)
  await update(info)
  return setCookieIsLogged(event)
}

export function authInfoExpired(info: AuthInfo): boolean | undefined {
  const expireAt = info?.token?.expireAt
  return (expireAt !== undefined) ? expireAt < Math.floor(Date.now() / 1000) : undefined
}

function getSession(event: H3Event) {
  return useSession<AuthInfo>(event, {
    sessionHeader: false,
    name: sessionName,
    password: password
  })
}

function setCookieIsLogged(event: H3Event) {
  return setCookie(event, cookieIsLoggedName, 'true', {
    httpOnly: false
  })
}

function removeCookieIsLogged(event: H3Event) {
  return deleteCookie(event, cookieIsLoggedName)
}