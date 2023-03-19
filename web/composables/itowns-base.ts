export function cameraPlacementToCameraTarget(placement: any): CameraTarget {
  return {
    crs: placement.coord.crs,
    coord: [placement.coord.x, placement.coord.y, placement.coord.z],
    range: placement.range,
    heading: placement.heading,
    tilt: placement.tilt
  }
}

export function eventToCameraTarget(evt: any): CameraTarget {
  return {
    crs: evt.coord.crs,
    coord: [evt.coord.x, evt.coord.y, evt.coord.z],
    range: evt.range,
    heading: evt.heading,
    tilt: evt.tilt
  }
}


export function loadInitialCameraTarget(name: string): CameraTarget {
  return (useAppConfig().map as any)[name].initialTarget
}

export function convertToCameraPlacement(target: CameraTarget): CameraPlacement {
  return {
    coord: new itowns.Coordinates(target.crs, target.coord),
    range: target.range,
    heading: target.heading,
    tilt: target.tilt
  }
}

export interface CameraTarget {
  crs: string,
  coord: [number, number, number],
  range: number,
  heading: number,
  tilt: number
}