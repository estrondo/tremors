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

declare type Layer = {
  [name: string]: any
}

// Configrations and descriptions.

type GlobeViewConfiguration = {
  layerGroups: LayerGroupDescription[]
}

type LayerGroupDescription = {
  id: string,
  name: LocaleMessage,
  uiType: UIType,
  layers: LayerDescription[]
}

type Source = Record<string, any>

type LayerDescription = {
  id: string,
  name: LocaleMessage,
  description: LocaleMessage,
  icon: string,
  uiType?: UIType,
  level: string,
  position: number,
  code: string,
  parameters: Record<string, any>
}
