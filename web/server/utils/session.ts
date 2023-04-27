import { H3Event } from 'h3'
import { AuthSession } from "./types"

type SessionDef = {
  name: string,
  password: string
}

type SessionDefs = {
  cookieName: string,
  user: SessionDef,
  general: SessionDef,
  id: SessionDef,
  refresh: SessionDef,
  expireAt: SessionDef
}

function isExpired(expireAt: number | undefined): boolean | undefined {
  return (expireAt !== undefined) ? expireAt < Math.floor(Date.now() / 1000) : undefined
}

function loadSession(def: SessionDef, event: H3Event) {
  return useSession<any>(event, {
    sessionHeader: false,
    name: def.name,
    password: def.password
  })
}

function removeSession(def: SessionDef, event: H3Event): Promise<void> {
  return clearSession(event, {
    sessionHeader: false,
    name: def.name,
    password: def.password
  })
}

function wrap<T>(value: T): { v?: T } {
  return value ? { v: value } : {}
}

function unwrap<T>(wrapper: any): T | undefined {
  return (wrapper && wrapper.v) ? wrapper.v : undefined
}

class SessionManager {

  #defs: SessionDefs

  constructor(defs: SessionDefs) {
    this.#defs = defs
  }

  async clean(event: H3Event): Promise<void> {

    await Promise.all([
      removeSession(this.#defs.user, event),
      removeSession(this.#defs.general, event),
      removeSession(this.#defs.id, event),
      removeSession(this.#defs.refresh, event),
      removeSession(this.#defs.expireAt, event)
    ])

    return deleteCookie(event, this.#defs.cookieName)
  }

  async get(event: H3Event, update?: (current: AuthSession) => Promise<AuthSession>): Promise<AuthSession | undefined> {
    const [
      { data: wrapperUser },
      { data: wrapperGeneral },
      { data: wrapperId },
      { data: wrapperRefresh },
      { data: wrapperExpireAt }
    ] = await Promise.all([
      loadSession(this.#defs.user, event),
      loadSession(this.#defs.general, event),
      loadSession(this.#defs.id, event),
      loadSession(this.#defs.refresh, event),
      loadSession(this.#defs.expireAt, event)
    ])

    const [
      user,
      general,
      id,
      refresh,
      expireAt
    ] = [
        unwrap<any>(wrapperUser),
        unwrap<any>(wrapperGeneral),
        unwrap<string>(wrapperId),
        unwrap<string>(wrapperRefresh),
        unwrap<number>(wrapperExpireAt)
      ]

    const current: AuthSession = {
      provider: general?.provider,
      user: {
        sub: user?.sub,
        owner: user?.owner,
        name: user?.name,
        email: user?.email,
        emailVerified: user?.emailVerified
      },
      token: {
        id: id,
        refresh: refresh,
        expireAt: expireAt
      }
    }

    if (update && isExpired(expireAt) === true) {
      const newSession = await update(current)
      return this.update(newSession, event)
    } else {
      return current
    }
  }

  async update(session: AuthSession, event: H3Event): Promise<AuthSession> {
    const [
      { update: updateUser },
      { update: updateGeneral },
      { update: updateId },
      { update: updateRefresh },
      { update: updateExpireAt }
    ] = await Promise.all([
      loadSession(this.#defs.user, event),
      loadSession(this.#defs.general, event),
      loadSession(this.#defs.id, event),
      loadSession(this.#defs.refresh, event),
      loadSession(this.#defs.expireAt, event)
    ])

    await Promise.all([
      updateUser(wrap(session.user)),
      updateGeneral(wrap({
        provider: session.provider
      })),
      updateId(wrap(session.token.id)),
      updateRefresh(wrap(session.token.refresh)),
      updateExpireAt(wrap(session.token.expireAt))
    ])

    setCookie(event, this.#defs.cookieName, 'true', {
      httpOnly: false
    })

    return session
  }
}

export const sessionManager = new SessionManager({
  cookieName: 'auth-isLogged',
  user: {
    name: 'us',
    password: useRuntimeConfig().session.password.user
  },
  general: {
    name: 'gs',
    password: useRuntimeConfig().session.password.general
  },
  id: {
    name: 'is',
    password: useRuntimeConfig().session.password.id
  },
  refresh: {
    name: 'rs',
    password: useRuntimeConfig().session.password.refresh
  },
  expireAt: {
    name: 'es',
    password: useRuntimeConfig().session.password.expireAt
  }
})