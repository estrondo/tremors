import { CameraTarget } from '~~/utils/itowns-base'
import { NavigationControl } from "./itowns-navigation-control"

interface GlobeViewControlOptions {
  stackSize: number,
  initialTarget?: CameraTarget
}

export class GlobeViewControl {

  private ref: Ref<any>
  private navigationControl?: NavigationControl

  constructor(ref: Ref<GlobeControls>, options: GlobeViewControlOptions) {
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