
const THREE = itowns.THREE

export class EarthquakeLayer extends itowns.Layer {

  constructor(id: string, options: Record<string, any>) {
    super(id, {
      source: new EarthquakeSource(options.source),
      object3d: new THREE.Group()
    })

    this._frameAnimation = this.#frameAnimation.bind(this)
  }

  update(context: any, layer: Layer) {
  }

  delete() {
  }

  onAdd(view: GlobeView) {
    this.geometry = new THREE.SphereGeometry(100000)
    this.material = new THREE.MeshBasicMaterial({ color: 0xff0000 })
    this.sphere = new THREE.Mesh(this.geometry, this.material)


    var target = itowns.CameraUtils.getTransformCameraLookingAtTarget(view, view.camera.camera3D).coord;
    this.sphere.altitude = 9000
    this.sphere.position.copy(target.as(view.referenceCrs))
    console.log(this.sphere.position)
    this.sphere.updateMatrixWorld()
    this.object3d.add(this.sphere)
    this.scale = 1

    view.addFrameRequester(itowns.MAIN_LOOP_EVENTS.BEFORE_RENDER, this._frameAnimation)
    this.view = view;
    view.notifyChange()
  }

  onRemove(view: GlobeView) {
    view.removeFrameRequester(itowns.MAIN_LOOP_EVENTS.BEFORE_RENDER, this._frameAnimation)
    this.object3d.remove(this.sphere)
  }

  #frameAnimation() {
    if (this.sphere) {
      this.sphere.scale.set(this.scale, this.scale, this.scale)
      this.sphere.updateMatrixWorld()
      this.scale = (this.scale + 0.3) % 200
      console.log(this.scale)
      this.view.notifyChange()
    }
  }
}

class EarthquakeSource extends itowns.Source {

  constructor(options: Record<string, any>) {
    options = Object.create(options)
    options.crs = "EPSG:4326"
    super(options)
  }
}