
export function getOpenIDProperties(name: string): any {
  return (useRuntimeConfig().openid.provider as any)[name]
}

export function getOpenIDRedirectURI(name: string): string {
  const template = useRuntimeConfig().openid.redirectUri
  return template.replace('[provider]', name)
}

import { Issuer, Client } from 'openid-client'
import { createCache } from './cache'

const providers = new Map<string, () => Promise<Client>>()

export function getOpenIDClient(name: string): Promise<Client> {
  let provider = providers.get(name)
  if (provider) {
    return provider()
  } else {
    const properties = getOpenIDProperties(name)
    if (properties) {
      const provider = clientFactory(name, properties)
      providers.set(name, provider)
      return provider()
    } else {
      return Promise.reject(Error(`There is no provider: ${name}.`))
    }
  }
}

function clientFactory(name: string, properties: any): () => Promise<Client> {

  return createCache(`openid-provider-${name}`, async () => {

    const manual = properties.endpoint.manual
    const auto = properties.endpoint.auto

    if (manual) {
      return manualFactory(name, properties, manual)
    } else if (auto) {
      return autoFactory(name, properties, auto)
    } else {
      throw new Error(`There is no auto and manual endpoint configuration for provider ${name}.`)
    }
  })
}

async function autoFactory(name: string, properties: any, auto: any): Promise<Client> {
  const issuer = await Issuer.discover(auto)
  return createClient(issuer, name, properties)
}

async function manualFactory(name: string, properties: any, manual: any): Promise<Client> {
  const issuer = new Issuer({
    authorization_endpoint: manual.authorization,
    token_endpoint: manual.token,
    issuer: 'http://tremors.estrondo.one'
  })

  return createClient(issuer, name, properties)
}

function createClient(issuer: Issuer, name: string, properties: any) {
  return new issuer.Client({
    client_id: properties.clientId,
    client_secret: properties.clientSecret,
    redirect_uris: [getOpenIDRedirectURI(name)]
  })
}