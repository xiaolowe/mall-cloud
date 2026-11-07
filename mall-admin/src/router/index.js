import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/AdminLayout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: () => import('../views/DashboardView.vue'), meta: { title: '数据看板' } },
      { path: 'products', name: 'products', component: () => import('../views/ProductsView.vue'), meta: { title: '商品管理' } },
      { path: 'categories', name: 'categories', component: () => import('../views/CategoriesView.vue'), meta: { title: '分类管理' } },
      { path: 'inventory', name: 'inventory', component: () => import('../views/InventoryView.vue'), meta: { title: '库存管理' } },
      { path: 'orders', name: 'orders', component: () => import('../views/OrdersView.vue'), meta: { title: '订单管理' } },
      { path: 'users', name: 'users', component: () => import('../views/UsersView.vue'), meta: { title: '用户管理' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

export default createRouter({
  history: createWebHashHistory(),
  routes
})
