import Vue from 'vue'
import App from './App.vue'
import router from './router'
import "./utils/validate/validate";
import "./utils/router/hooks";

import store from './store/index'

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
