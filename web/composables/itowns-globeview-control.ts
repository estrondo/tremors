import { CameraTarget } from './itowns-base'
import { NavigationControl } from './itowns-navigation-control'

export function useGlobeViewControl(ref: Ref<GlobeControls>, initialTarget?: CameraTarget) {
  return new GlobeViewControl(ref, {
    stackSize: 32,
    initialTarget: initialTarget
  })
}

interface GlobeViewControlOptions {
  stackSize: number,
  initialTarget?: CameraTarget
}

class GlobeViewControl {

  private ref: Ref<any>
  private navigationControl?: NavigationControl

  constructor(ref: Ref<any>, options: GlobeViewControlOptions) {
    this.ref = ref

    watchEffect(() => {
      if (ref.value) {
        this.navigationControl = new NavigationControl(ref.value, options.stackSize, options.initialTarget)
      }
    })
  }

  get() {
    return this.ref.value
  }

  getView() {
    return this.get().view
  }

  getZoom() {
    return this.get().getZoom()
  }

  redo() {
    this.navigationControl?.redo()
  }

  reset() {
    this.navigationControl?.reset()
  }

  undo() {
    this.navigationControl?.undo()
  }

  zoomIn() {
    this.get().setZoom(this.getZoom() + 1, true)
  }

  zoomOut() {
    this.get().setZoom(this.getZoom() - 1, true)
  }
}