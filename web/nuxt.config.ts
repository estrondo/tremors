// https://nuxt.com/docs/api/configuration/nuxt-config

// https://developers.google.com/identity/openid-connect/openid-connect#discovery

export default defineNuxtConfig({
  ssr: false,
  css: [
    "@/assets/style/main.less"
  ],
  app: {
    head: {
      script: [
        { src: 'lib/itowns.js' }
      ]
    }
  },
  runtimeConfig: {
    openid: {
      redirectUri: 'http://tremors.estrondo.one/openid/callback/[provider]',

      provider: {
        google: {
          endpoint: {
            auto: 'https://accounts.google.com/.well-known/openid-configuration'
          },
          clientId: '<NUXT_OPENID_PROVIDER_GOOGLE_CLIENT_ID>',
          clientSecret: '<NUXT_OPENID_PROVIDER_GOOGLE_CLIENT_SECRET>'
        },
        microsoft: {
          endpoint: {
            auto: 'https://login.microsoftonline.com/consumers/v2.0/.well-known/openid-configuration'
          },
          clientId: '<NUXT_OPENID_PROVIDER_MICROSOFT_CLIENT_ID>',
          clientSecret: '<NUXT_OPENID_PROVIDER_MICROSOFT_CLIENT_SECRET>'
        },
        twitter: {
          endpoint: {
            manual: {
              authorization: 'https://twitter.com/i/oauth2/authorize',
              token: 'https://api.twitter.com/2/oauth2/token',
            }
          },
          clientId: '<NUXT_OPENID_PROVIDER_TWITTER_CLIENT_ID>',
          clientSecret: '<NUXT_OPENID_PROVIDER_TWITTER_CLIENT_SECRET>'
        }
      }
    },
    grpc: {
      account: {
        address: 'localhost:8080'
      }
    }
  }
})
