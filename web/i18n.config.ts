export default defineI18nConfig((nuxt) => ({
  legacy: false,
  messages: reverse({
    toolbox: {
      tip: {
        account: {
          en: 'Account Manager',
          pt: 'Gerenciador de Conta'
        },
        undo: {
          en: 'Undo',
          pt: 'Voltar'
        },
        redo: {
          en: 'Redo',
          pt: 'Avançar'
        },
        zoomIn: {
          en: 'Zoom In',
          pt: 'Ampliar'
        },
        zoomOut: {
          en: 'Zoom Out',
          pt: 'Reduzir'
        },
        zoomReset: {
          en: 'Reset zoom',
          pt: 'Início'
        },
        layer: {
          en: 'Layer Manager',
          pt: 'Gerenciador de Layers'
        },
        search: {
          en: 'Search Events',
          pt: 'Procurar Eventos'
        },
        alert: {
          en: 'Alert Manager',
          pt: 'Gerenciador de Alertas'
        },
        about: {
          en: 'About Tremors',
          pt: 'Sobre Tremors'
        }
      }
    },
    login: {
      'with-google': {
        en: 'Sign in with Google',
        pt: 'Usar conta Google'
      },
      'with-microsoft': {
        en: 'Sign in with Microsoft',
        pt: 'Usar conta Microsoft'
      }
    },
    account: {
      dialog: {
        title: {
          en: 'Account Manager',
          pt: 'Gerenciador de Conta'
        }
      },
      logout: {
        en: 'Logout',
        pt: 'Desconectar'
      },
      editor: {
        name: {
          en: 'Name',
          pt: 'Nome'
        },
        email: {
          en: 'E-mail',
          pt: 'E-mail'
        },
        save: {
          en: 'Save',
          pt: 'Salvar'
        }
      }
    }
  })
}))

import { List } from 'immutable'

function reverse(messages: { [n: string]: any }) {
  const root: { [n: string]: any } = {}

  for (const key in messages) {
    visit(List([key]), messages[key])
  }

  function visit(path: List<string>, value: any) {
    if (typeof value === 'object') {
      for (const key in value) {
        visit(path.push(key), value[key])
      }
    } else {
      add(path.last(), path.pop(), value)
    }
  }

  function add(locale: string, path: List<string>, value: any) {
    if (!root[locale]) {
      root[locale] = {}
    }

    let target = root[locale]
    let current = path.first()
    path = path.shift()

    while (current && !path.isEmpty()) {
      if (target[current] === undefined) [
        target[current] = {}
      ]

      target = target[current]
      current = path.first()
      path = path.shift()
    }

    if (current) {
      target[current] = value
    }
  }
  return root
}