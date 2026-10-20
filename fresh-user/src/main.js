import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant, { Lazyload } from 'vant'
import 'vant/lib/index.css'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Vant)
app.use(Lazyload)
app.mount('#app')
