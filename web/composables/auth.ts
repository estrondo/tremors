import Cookies from 'js-cookie'

const cookieIsLoggedName = 'auth-isLogged'

export function useRedirectToLogin(provider: string) {
  window.location.replace(`/openid/login/${provider}`)
}

export function useRedirectToLogout() {
  window.location.replace('/openid/logout')
}

export function useIsLogged() {
  return Cookies.get(cookieIsLoggedName) == 'true'
}