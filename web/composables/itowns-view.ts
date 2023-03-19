import { CameraTarget } from "./itowns-base"

export const ITOWNS_GLOBEVIEW = "itowns.globeView"
export const ITOWNS_GLOBEVIEW_CONTROLS = "itowns.globeViewControls"

export interface GlobeViewOptions {
  name: string,
  useStoredPlacement: boolean
}

export function useGlobeView(divElement: HTMLDivElement, option: GlobeViewOptions): [GlobeView, CameraPlacement] {

  const placement: any = (option.useStoredPlacement) ? useStoredCameraPlacement(option.name) :
    convertToCameraPlacement(loadInitialCameraTarget(option.name))

  const placementValue: CameraPlacement = placement.value || placement

  const view = new itowns.GlobeView(divElement, placementValue, { noControls: true })

  if (option.useStoredPlacement) {
    view.addEventListener(itowns.VIEW_EVENTS.CAMERA_MOVED, useThrottle((evt) => {
      placement.value = evt
    }, 250))

    view.addEventListener('camera-placement-changed', useThrottle((evt: any) => {
      placement.value = evt
    }, 250))
  }

  return [view, placementValue]
}

function useStoredCameraPlacement(name: string): Ref<CameraPlacement> {
  const storedCameraTarget = useLocalStorage(`${name}-camera-target`, () => {
    return loadInitialCameraTarget(name)
  })

  const placement = shallowRef(convertToCameraPlacement(storedCameraTarget.value))

  watch(placement, (newValue) => {
    storedCameraTarget.value = cameraPlacementToCameraTarget(newValue)
  })

  return placement
}