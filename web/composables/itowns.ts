import { CameraTarget } from "~~/utils/itowns-base"
import { GlobeViewControl } from "~~/utils/itowns-globeview-control"

export const ITOWNS_GLOBEVIEW = "itowns.globeView"
export const ITOWNS_GLOBEVIEW_CONTROLS = "itowns.globeViewControls"

export interface GlobeViewOptions {
  name: string,
  useStoredPlacement: boolean
}

export function useGlobeView(divElement: HTMLDivElement, options: GlobeViewOptions): [GlobeView, CameraPlacement] {

  const placement: any = (options.useStoredPlacement) ? loadStoredCameraPlacement(options.name) :
    toCameraPlacement.fromCameraTarget(loadConfiguredCameraTarget(options.name))

  const placementValue: CameraPlacement = placement.value || placement

  const view = new itowns.GlobeView(divElement, placementValue, { noControls: true })

  if (options.useStoredPlacement) {
    view.addEventListener(itowns.VIEW_EVENTS.CAMERA_MOVED, useThrottle((evt) => {
      placement.value = evt
    }, 250))

    view.addEventListener('camera-placement-changed', useThrottle((evt: any) => {
      placement.value = evt
    }, 250))
  }

  return [view, placementValue]
}

export function useGlobeViewControl(ref: Ref<GlobeControls>, initialTarget?: CameraTarget) {
  return new GlobeViewControl(ref, {
    stackSize: 32,
    initialTarget: initialTarget
  })
}

function loadStoredCameraPlacement(name: string): Ref<CameraPlacement> {
  const storedCameraTarget = useLocalStorage(`${name}-camera-target`, () => {
    return loadConfiguredCameraTarget(name)
  })

  const placement = shallowRef(toCameraPlacement.fromCameraTarget(storedCameraTarget.value))

  watch(placement, (newValue) => {
    storedCameraTarget.value = toCameraTarget.fromCameraPlacement(newValue)
  })

  return placement
}