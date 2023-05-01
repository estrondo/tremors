export default defineAppConfig({
  'globe-view': {
    main: {
      initialTarget: {
        crs: "EPSG:4978",
        coord: [3459135, -4662851, -2636005],
        range: 8000000,
        tilt: 75,
        heading: 5
      },
      levels: [
        ["basemap", true],
        ["shockwave", false],
        ["hypocentre", false]
      ]
    }
  }
})