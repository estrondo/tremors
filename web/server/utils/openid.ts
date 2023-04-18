import { Issuer, Client, TokenSet } from 'openid-client'
import { createCache } from './cache'
import { AuthSession } from './types'
import { IncomingMessage } from 'node:http'

function createIssuer(name: string, configuration: any): Promise<Issuer> {
  const manual = configuration.endpoint.manual
  const auto = configuration.endpoint.auto

  if (manual) {
    return Promise.resolve(new Issuer({
      authorization_endpoint: manual.authorization,
      token_endpoint: manual.token,
      issuer: manual.issuer
    }))
  } else if (auto) {
    return Issuer.discover(auto)
  } else {
    return Promise.reject(new Error(`There is no auto or manual configuration for provider ${name}!`))
  }
}

function convertToAuthSession(providerName: string, tokenSet: TokenSet): AuthSession {
  const claims = tokenSet.claims()
  return {
    provider: providerName,
    user: {
      sub: claims.sub,
      name: claims.name,
      email: claims.email,
      emailVerified: claims.email_verified
    },
    token: {
      id: tokenSet.id_token,
      refresh: tokenSet.refresh_token,
      expireAt: tokenSet.expires_at
    }
  }
}

class OpenIDService {

  #providers: { [name: string]: OpenIDProvider | undefined }

  constructor(providers: { [name: string]: OpenIDProvider | undefined }) {
    this.#providers = providers
  }

  callback(providerName: string, incomingMessage: IncomingMessage): Promise<AuthSession> {
    return this.#getProvider(providerName).callback(incomingMessage)
  }

  async getAuthorizationURL(providerName: string): Promise<string> {
    return this.#getProvider(providerName).getAuthorizationURL()
  }

  async refresh(session: AuthSession): Promise<AuthSession> {
    return this.#getProvider(session.provider).refresh(session)
  }

  #getProvider(providerName: string): OpenIDProvider {
    const provider = this.#providers[providerName]
    if (provider) {
      return provider
    } else {
      throw new Error(`There is provider with name: ${providerName}!`)
    }
  }
}

type OpenIDProviderConfiguration = {
  name: string,
  redirectUri: string,
  additionalParameters: {
    authorization_endpoint?: { [name: string]: any }
  }
}

class OpenIDProvider {

  #client: () => Promise<Client>
  #configuration: OpenIDProviderConfiguration

  constructor(client: () => Promise<Client>, configuration: OpenIDProviderConfiguration) {
    this.#client = client
    this.#configuration = configuration
  }

  async callback(incomingMessage: IncomingMessage): Promise<AuthSession> {
    const client = await this.#client()
    const params = client.callbackParams(incomingMessage)
    const tokenSet = await client.callback(this.#configuration.redirectUri, params)
    return convertToAuthSession(this.#configuration.name, tokenSet)
  }

  async getAuthorizationURL(): Promise<string> {
    const client = await this.#client()
    const parameters: any = { scope: 'openid profile email', prompt: 'select_account', response_type: 'code' }
    const additionalParameters = this.#configuration.additionalParameters.authorization_endpoint

    if (additionalParameters) {
      for (const key in additionalParameters) {
        parameters[key] = additionalParameters[key]
      }
    }

    return client.authorizationUrl(parameters)
  }

  async refresh(session: AuthSession): Promise<AuthSession> {
    const refreshToken = session.token.refresh
    if (refreshToken) {
      const client = await this.#client()
      const tokenSet = await client.refresh(refreshToken)
      return convertToAuthSession(this.#configuration.name, tokenSet)
    } else {
      throw new Error(`There is no refresh token associated for proviver ${this.#configuration.name}!`)
    }
  }
}


export const openIdService = (() => {

  function createClientFactory(name: string, configuration: any, additional: { redirectUri: string }): () => Promise<Client> {

    return createCache(`openid-${name}`, async () => {
      const issuer = await createIssuer(name, configuration)
      return new issuer.Client({
        client_id: configuration.clientId,
        client_secret: configuration.clientSecret,
        redirect_uris: [additional.redirectUri]
      })
    })
  }

  const { provider: configuredProviders, redirectUri } = useRuntimeConfig().openid
  const providers: { [name: string]: OpenIDProvider | undefined } = {}

  for (const providerName in configuredProviders) {
    const configuredProvider = (configuredProviders as any)[providerName]
    const providerRedirectUri = redirectUri.replace('[provider]', providerName)

    const clientFactory = createClientFactory(providerName, configuredProvider, {
      redirectUri: providerRedirectUri
    })

    providers[providerName] = new OpenIDProvider(clientFactory, {
      name: providerName,
      redirectUri: providerRedirectUri,
      additionalParameters: configuredProvider.custom || {}
    })
  }


  return new OpenIDService(providers)
})()