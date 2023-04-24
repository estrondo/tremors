export interface CameraTarget {
  crs: string,
  coord: [number, number, number],
  range: number,
  heading: number,
  tilt: number
}

export function loadConfiguredCameraTarget(viewName: string): CameraTarget {
  return (useAppConfig()['globe-view'] as any)[viewName].initialTarget
}

export const toCameraTarget = {

  fromCameraPlacement(placement: CameraPlacement): CameraTarget {
    return {
      crs: placement.coord.crs,
      coord: [placement.coord.x, placement.coord.y, placement.coord.z],
      range: placement.range,
      heading: placement.heading,
      tilt: placement.tilt
    }
  },

  fromEvent(evt: any): CameraTarget {
    return {
      crs: evt.coord.crs,
      coord: [evt.coord.x, evt.coord.y, evt.coord.z],
      range: evt.range,
      heading: evt.heading,
      tilt: evt.tilt
    }
  }

}

export const toCameraPlacement = {

  fromCameraTarget(target: CameraTarget): CameraPlacement {
    return {
      coord: new itowns.Coordinates(target.crs, target.coord),
      range: target.range,
      heading: target.heading,
      tilt: target.tilt
    }
  }
}