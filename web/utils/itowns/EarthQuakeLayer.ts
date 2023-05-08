
const THREE = itowns.THREE

const axis = new THREE.Vector3(1, 0, 0).normalize()

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
    const size = 200000
    this.material = new THREE.MeshBasicMaterial({ color: 0xff0000 })
    this.cubeGeometry = new THREE.BoxGeometry(size, size, size)
    this.cubeMesh = new THREE.Mesh(this.cubeGeometry, this.material)
    this.cubeMesh.translateZ(7000000)

    this.orbit = new THREE.Object3D()
    this.orbit.add(this.cubeMesh)

    this.orbit.updateMatrixWorld()
    this.angle = 0

    this.object3d.add(this.orbit)
    view.addFrameRequester(itowns.MAIN_LOOP_EVENTS.BEFORE_RENDER, this._frameAnimation)
    this.view = view;
    view.notifyChange()
  }

  onRemove(view: GlobeView) {
    view.removeFrameRequester(itowns.MAIN_LOOP_EVENTS.BEFORE_RENDER, this._frameAnimation)
    this.object3d.remove(this.orbit)
  }

  #frameAnimation() {
    this.orbit.rotateX(0.01)
    this.cubeMesh.rotateY(0.01)
    this.angle = (this.angle + 0.01) % Math.PI
    this.orbit.updateMatrixWorld()
    this.view.notifyChange()
  }
}

class EarthquakeSource extends itowns.Source {

  constructor(options: Record<string, any>) {
    options = Object.create(options)
    options.crs = "EPSG:4326"
    super(options)
  }
}