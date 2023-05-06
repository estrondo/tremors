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
        loading: {
          en: 'Loading account data.',
          pt: 'Carregando dados da conta.'
        },
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
    },
    layer: {
      dialog: {
        title: {
          en: 'Layers Manager',
          pt: 'Gerenciador de Layers'
        }
      },
      header: {
        title: {
          en: 'Select the layer type',
          pt: 'Selecione o tipo de camada'
        }
      },
      control: {
        choice: {
          en: 'Select/Unselect',
          pt: 'Usar / Não usar'
        },
        activation: {
          en: 'Activate/Deactivate',
          pt: 'Ativar/Desativar'
        }
      }
    }
  })
}))

// import { List } from 'immutable'

function push(array: string[], element: string): string[] {
  const copy = Array.from(array)
  copy.push(element)
  return copy
}

function last(array: string[]): string {
  return array[array.length - 1]
}

function first(array: string[]): string {
  return array[0]
}

function pop(array: string[]): string[] {
  const copy = Array.from(array)
  copy.pop()
  return copy
}

function shift(array: string[]): string[] {
  const copy = Array.from(array)
  copy.shift()
  return copy
}

function reverse(messages: { [n: string]: any }) {
  const root: Record<string, any> = {}

  for (const key in messages) {
    visit([key], messages[key])
  }

  function visit(path: string[], value: any) {
    if (typeof value === 'object') {
      for (const key in value) {
        visit(push(path, key), value[key])
      }
    } else {
      add(last(path), pop(path), value)
    }
  }

  function add(locale: string, path: string[], value: any) {
    if (!root[locale]) {
      root[locale] = {}
    }

    let target = root[locale]
    let current = first(path)
    path = shift(path)

    while (current && path.length) {
      if (target[current] === undefined) [
        target[current] = {}
      ]

      target = target[current]
      current = first(path)
      path = shift(path)
    }

    if (current) {
      target[current] = value
    }
  }

  return root
}