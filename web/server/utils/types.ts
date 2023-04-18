export interface AuthSession {
  provider: string,
  user?: {
    sub?: string,
    owner?: string,
    name?: string,
    email?: string,
    emailVerified?: boolean
  },
  token: {
    id?: string,
    refresh?: string,
    expireAt?: number
  }
}