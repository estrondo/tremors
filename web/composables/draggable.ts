const UNSAVED = {}

export function useDraggable(handler: HTMLElement, target: HTMLElement, storedName?: string) {

  let origin: any, xUpdater: any, yUpdater: any

  const storedPosition = storedName ? useLocalStorage<any>(`${storedName}-draggable-position`, () => UNSAVED) : null

  if (storedPosition && storedPosition.value !== UNSAVED) {
    target.style.left = storedPosition.value.x + "px"
    target.style.bottom = storedPosition.value.y + "px"
  }

  handler.addEventListener('pointerdown', (evt) => {
    evt.stopPropagation()
    origin = { x: evt.pageX, y: evt.pageY }
    const parent = getParent()
    xUpdater = getXUpdater(parent)
    yUpdater = getYUpdater(parent)

    globalThis.addEventListener('pointermove', onPointerMove)
    globalThis.addEventListener('pointerup', onPointerUp)
  })

  function getParent() {
    return target.offsetParent as HTMLElement || document.body
  }

  function onPointerMove(evt: PointerEvent) {
    const dx = evt.pageX - origin.x, dy = evt.pageY - origin.y
    xUpdater(dx)
    yUpdater(dy)
  }

  function onPointerUp(_evt: PointerEvent) {
    globalThis.removeEventListener('pointermove', onPointerMove)
    updateStoredPosition()
  }

  function getX(_parent: HTMLElement): number {
    return target.offsetLeft
  }

  function getXUpdater(_parent: HTMLElement) {
    const x = getX(_parent)
    return (dx: number) => {
      target.style.left = (x + dx) + 'px'
    }
  }

  function getY(_parent: HTMLElement): number {
    return _parent.offsetHeight - target.offsetHeight - target.offsetTop
  }

  function getYUpdater(_parent: HTMLElement) {
    const y = getY(_parent)
    return (dy: number) => {
      target.style.bottom = (y - dy) + 'px'
    }
  }

  function updateStoredPosition() {
    if (storedPosition) {
      const parent = getParent()
      storedPosition.value = {
        x: getX(parent),
        y: getY(parent)
      }
    }
  }
}