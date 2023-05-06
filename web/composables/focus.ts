export function useAutoFocus(element: HTMLElement, ignore?: HTMLElement) {

  let timeoutId: any = undefined

  element.addEventListener('pointerdown', (evt) => {

    if (evt.target !== ignore && (element as any)._DONT === undefined) {
      const parent = element.parentElement
      if (parent) {
        const topElement = parent.children[parent.children.length - 1]
        if (topElement !== element) {
          parent.appendChild(element)
          if (timeoutId) {
            clearTimeout(timeoutId)
          }
          (element as any)._DONT = true
          timeoutId = globalThis.setTimeout(() => {
            delete (element as any)._DONT
            timeoutId = undefined
          }, 1000)
        }
      }
    }
  })
}