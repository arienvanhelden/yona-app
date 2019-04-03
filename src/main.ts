import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

//partial import bulma, import global, import fonts
import "./sass/libraries/import_bulma.scss"
import './sass/fonts/fonts.scss'
import "./sass/global.scss"

import "../node_modules/tiny-slider/src/tiny-slider.scss"

Vue.config.productionTip = false

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
