const UNSAVED = {}

export function useDraggable(handler: HTMLElement, target: HTMLElement, storedName?: string, limit?: HTMLElement) {

  let origin: any, xUpdater: any, yUpdater: any, xRange: any, yRange: any

  // REMEMBER: The orientation here is: (x: left->right, y: bottom->top)

  const storedPosition = storedName ? useLocalStorage<any>(`${storedName}-draggable-position`, () => UNSAVED) : null

  if (storedPosition && storedPosition.value !== UNSAVED) {
    target.style.left = storedPosition.value.x + 'px'
    target.style.bottom = storedPosition.value.y + 'px'
    if (limit) {
      setTimeout(checkPosition)
    }
  }

  if (limit) {
    globalThis.addEventListener('resize', useThrottle(checkPosition, 250))
  }

  handler.addEventListener('pointerdown', (evt) => {
    evt.stopPropagation()
    origin = { x: evt.pageX, y: evt.pageY }
    const parent = getParent()

    if (limit) {
      const [xRange, yRange] = getRange(target, limit)
      xUpdater = getXUpdater(parent, xRange)
      yUpdater = getYUpdater(parent, yRange)
    } else {
      xUpdater = getXUpdater(parent)
      yUpdater = getYUpdater(parent)
    }

    globalThis.addEventListener('pointermove', onPointerMove)
    globalThis.addEventListener('pointerup', onPointerUp)
  })

  function getParent() {
    return target.offsetParent as HTMLElement || document.body
  }

  function onPointerMove(evt: PointerEvent) {
    const dx = evt.pageX - origin.x, dy = origin.y - evt.pageY
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

  function getXUpdater(_parent: HTMLElement, range?: [number, number]) {
    const x = getX(_parent)
    if (!range) {
      return (dx: number) => {
        target.style.left = (x + dx) + 'px'
      }
    } else {
      const [xMin, xMax] = range
      return (dx: number) => {
        target.style.left = ((dx >= xMin && dx <= xMax) ? x + dx : (dx < xMin ? x + xMin : x + xMax)) + 'px'
      }
    }
  }

  function getY(_parent: HTMLElement): number {
    return _parent.offsetHeight - target.offsetHeight - target.offsetTop
  }

  function getYUpdater(_parent: HTMLElement, range?: [number, number]) {
    const y = getY(_parent)
    console.log('yRange = ', range)

    if (!range) {
      return (dy: number) => {
        target.style.bottom = (y + dy) + 'px'
      }
    } else {
      const [yMin, yMax] = range
      return (dy: number) => {
        target.style.bottom = ((dy >= yMin && dy <= yMax) ? y + dy : (dy < yMin ? y + yMin : y + yMax)) + 'px'
      }
    }
  }

  function checkPosition() {

    if (target.style.display == 'block') {
      const [xMin, xMax, yMin, yMax] = getAbsoluteRange(limit!)
      const [x, y] = getAbsoluteOffset(target)
      const parent = getParent()
      const left = getX(parent), bottom = getY(parent)

      // console.log(left, target.offsetWidth, x, xMax, x - xMax + target.offsetWidth)

      if (x < xMin) {
        target.style.left = (left + (xMin - x)) + 'px'
      } else if ((x + target.offsetWidth) > xMax) {
        target.style.left = (left - (x + target.offsetWidth - xMax)) + 'px'
      }

      if (y < yMin) {
        target.style.bottom = (bottom + (yMin - y)) + 'px'
      } else if ((y + target.offsetHeight) > yMax) {
        target.style.bottom = (bottom - (y + target.offsetHeight - yMax)) + 'px'
      }
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

// returns: [left, bottom]
function getAbsoluteOffset(element: HTMLElement): [number, number] {
  let x = 0, y = 0
  let current = element

  while (current) {
    x += current.offsetLeft
    y += current.offsetTop
    current = current.offsetParent as HTMLElement
  }

  return [x, document.documentElement.scrollHeight - y - element.offsetHeight]
}

// returns: [xMin, xMax, yMin, yMax], remember: left->right, bottom->top
function getAbsoluteRange(element: HTMLElement): [number, number, number, number] {
  const [x, y] = getAbsoluteOffset(element)
  return [x, x + element.offsetWidth, y, y + element.offsetHeight]
}

function getRange(target: HTMLElement, limit: HTMLElement): [[number, number] | undefined, [number, number] | undefined] {
  const [xMin, xMax, yMin, yMax] = getAbsoluteRange(limit)
  const [x, y] = getAbsoluteOffset(target)

  return [
    (x >= xMin && x <= (xMax - target.offsetWidth)) ? [xMin - x, xMax - x - target.offsetWidth] : undefined,
    (y >= yMin && y <= (yMax - target.offsetHeight)) ? [yMin - y, yMax - y - target.offsetHeight] : undefined
  ]
}