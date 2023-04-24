import { CameraTarget } from './itowns-base'

export class NavigationControl {

  private undoStack: Stack<CameraTarget>
  private redoStack: Stack<CameraTarget>
  private controls: any
  private view: any

  private cameraTarget?: CameraTarget
  private initialTarget?: CameraTarget

  constructor(controls: GlobeControls, stackSize: number, initialTarget?: CameraTarget) {
    this.undoStack = new Stack(stackSize)
    this.redoStack = new Stack(stackSize)
    this.controls = controls
    this.view = controls.view
    this.initialTarget = initialTarget

    controls.addEventListener('end', useThrottle(this.onControlEnd.bind(this), 250))
  }

  redo() {
    this.redoStack.apply((target) => {
      this._redo(target)
    })
  }

  _redo(target: CameraTarget) {
    this.undoStack.add(this.popCameraTarget())
    this.lookAt(target);
  }

  reset() {
    if (this.initialTarget)
      this.lookAt(this.initialTarget)
  }

  undo() {
    this.undoStack.apply((target) => {
      this._undo(target)
    })
  }

  _undo(target: CameraTarget) {
    this.redoStack.add(this.popCameraTarget())
    this.lookAt(target);
  }

  private lookAt(target: CameraTarget) {
    this.cameraTarget = target
    this.controls.lookAtCoordinate({
      coord: new itowns.Coordinates(target.crs, target.coord),
      range: target.range,
      heading: target.heading,
      tilt: target.tilt
    })
  }

  private onControlEnd() {
    this.undoStack.add(this.popCameraTarget())
    const controls = this.controls
    const cameraPlacement = itowns.CameraUtils.getTransformCameraLookingAtTarget(controls.view, controls.camera)
    this.cameraTarget = toCameraTarget.fromCameraPlacement(cameraPlacement)
    this.redoStack.clean()
    this.view.dispatchEvent({
      type: 'camera-placement-changed',
      coord: cameraPlacement.coord,
      range: cameraPlacement.range,
      heading: cameraPlacement.heading,
      tilt: cameraPlacement.tilt
    })
  }

  private popCameraTarget() {
    const target = this.cameraTarget
    this.cameraTarget = undefined
    return target
  }
}


class Stack<T> {

  maxSize: number;
  private values: T[] = [];

  constructor(maxSize: number) {
    this.maxSize = maxSize
  }

  add(value?: T) {
    if (value) {
      if (this.values.length < this.maxSize) {
        this.values.push(value)
      } else {
        this.values.shift()
        this.values.push(value)
      }
    }
  }

  apply(fn: (value: any) => void, ifEmptyFn?: () => void) {
    if (this.values.length) {
      fn(this.values.pop())
    } else if (ifEmptyFn) {
      ifEmptyFn()
    }
  }

  clean() {
    if (this.values.length)
      this.values = []
  }
}