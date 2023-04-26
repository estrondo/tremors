// https://nuxt.com/docs/api/configuration/nuxt-config

// https://developers.google.com/identity/openid-connect/openid-connect#discovery

export default defineNuxtConfig({
  modules: [
    '@nuxtjs/i18n'
  ],
  i18n: {
    defaultLocale: 'en',
    locales: [
      {
        code: 'en',
        name: 'English'
      },
      {
        code: 'pt',
        name: 'Português'
      }
    ],
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: 'i18n',
      redirectOn: 'root'
    }
  },
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
      redirectUri: 'http://tremors.estrondo.one/openid/callback-[provider]',

      provider: {
        google: {
          endpoint: {
            auto: 'https://accounts.google.com/.well-known/openid-configuration'
          },
          clientId: '<NUXT_OPENID_PROVIDER_GOOGLE_CLIENT_ID>',
          clientSecret: '<NUXT_OPENID_PROVIDER_GOOGLE_CLIENT_SECRET>',
          custom: {
            authorization_endpoint: {
              access_type: "offline"
            }
          }
        },
        microsoft: {
          endpoint: {
            auto: 'https://login.microsoftonline.com/consumers/v2.0/.well-known/openid-configuration'
          },
          clientId: '<NUXT_OPENID_PROVIDER_MICROSOFT_CLIENT_ID>',
          clientSecret: '<NUXT_OPENID_PROVIDER_MICROSOFT_CLIENT_SECRET>',
          custom: {
            authorization_endpoint: {
              scope: 'openid profile email offline_access'
            }
          }
        },
        twitter: {
          endpoint: {
            manual: {
              authorization: 'https://twitter.com/i/oauth2/authorize',
              token: 'https://api.twitter.com/2/oauth2/token',
              issuer: 'https://twitter.com/'
            }
          },
          clientId: '<NUXT_OPENID_PROVIDER_TWITTER_CLIENT_ID>',
          clientSecret: '<NUXT_OPENID_PROVIDER_TWITTER_CLIENT_SECRET>'
        }
      }
    },
    grpc: {
      account: {
        ssl: false,
        address: 'localhost:8080'
      }
    },
    session: {
      password: {
        user: '20f556db-e44c-4a32-bcd7-6c6cbaf535b0',
        general: '570bc251-2703-4c4b-90d3-6c3982481dfc',
        id: 'b12a0921-0c92-431f-9058-08c9e8e76c39',
        refresh: 'de2d669b-3dfd-49d8-9df7-2b4b7b40eafc',
        expireAt: '911525b8-d5df-4b4e-8dae-fbd1d052ba48'
      }
    }
  }
})
