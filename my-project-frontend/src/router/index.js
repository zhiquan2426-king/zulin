import { createRouter, createWebHistory } from 'vue-router'
import { unauthorized } from "@/net";
import { useUserStore } from "../stores/user";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }
            ]
        }, {
            path: '/dashboard',
            name: 'dashboard',
            component: () => import('@/views/UserDashboard.vue'),
            children: [
                {
                    path: 'equipment',
                    name: 'dashboard-equipment',
                    component: () => import('@/views/dashboard/EquipmentList.vue')
                }, {
                    path: 'orders',
                    name: 'dashboard-orders',
                    component: () => import('@/views/dashboard/MyOrders.vue')
                }, {
                    path: 'delivery',
                    name: 'dashboard-delivery',
                    component: () => import('@/views/dashboard/DeliveryList.vue')
                }, {
                    path: 'profile',
                    name: 'dashboard-profile',
                    component: () => import('@/views/dashboard/Profile.vue')
                }, {
                    path: 'equipment-manage',
                    name: 'dashboard-equipment-manage',
                    component: () => import('@/views/dashboard/EquipmentManage.vue')
                }, {
                    path: 'order-manage',
                    name: 'dashboard-order-manage',
                    component: () => import('@/views/dashboard/OrderManage.vue')
                }, {
                    path: 'user-manage',
                    name: 'dashboard-user-manage',
                    component: () => import('@/views/dashboard/UserManage.vue')
                }, {
                    path: 'system-manage',
                    name: 'dashboard-system-manage',
                    component: () => import('@/views/dashboard/SystemManage.vue')
                }, {
                    path: 'deduction-manage',
                    name: 'dashboard-deduction-manage',
                    component: () => import('@/views/dashboard/DepositDeductionManage.vue')
                }
            ]
        }
    ]
})

router.beforeEach((to, from, next) => {
    const isUnauthorized = unauthorized()
    if(to.name.startsWith('welcome') && !isUnauthorized) {
        next('/dashboard/equipment')
    } else if(to.fullPath.startsWith('/dashboard') && isUnauthorized) {
        next('/')
    } else {
        next()
    }
})

export default router
