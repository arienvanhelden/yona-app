import Path from './components/bars'

export default {
  name: 'Bars',

  props: {
    data: {
      type: Array,
      required: true
    },
    autoDraw: Boolean,
    blockSize: {
      type: Number,
      default: 15
    },
    growDuration: {
      type: Number,
      default: 2
    },
    gradient: {
      type: Array,
      default: () => ['#000']
    },
    max: {
      type: Number,
      default: -Infinity
    },
    min: {
      type: Number,
      default: Infinity
    },
    height: Number,
    width: Number,
    padding: {
      type: Number,
      default: 8
    }
  },

  render (h) {
    if (!this.data || this.data.length < 2) return
    const { width, height, padding } = this
    const viewWidth = width || 300
    const viewHeight = height || 75
    const boundary = {
      minX: padding,
      minY: padding,
      maxX: viewWidth - padding,
      maxY: viewHeight - padding
    }
    const props = this.$props

    props.boundary = boundary
    props.id = 'vue-bars-' + this._uid

    return h('svg', {
      attrs: {
        width: width || '100%',
        height: height || '25%',
        viewBox: `0 0 ${viewWidth} ${viewHeight}`
      }
    }, [
      h(Path, {
        props,
        ref: 'path'
      })
    ])
  }
}