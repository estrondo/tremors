declare const itowns: any

declare type CameraPlacement = {
  [name: string]: any
}

declare interface EventDispatcher {

}

declare type GlobeView = {
  [name: string]: any
}

declare interface GlobeControls extends EventDispatcher {
  view: GlobeView,
  addEventListener(type: string, listener: (any) => void)
}