import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import BackendLayout from '@/layouts/BackendLayout.vue'

// 后台路由
export const backendRoutes = [
  {
    path: '/back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/backend/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/backend/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'department',
        name: 'DepartmentManagement',
        component: () => import('@/views/backend/department/index.vue'),
        meta: { title: '科室管理', icon: 'Office' }
      },
      {
        path: 'doctor',
        name: 'DoctorManagement',
        component: () => import('@/views/backend/doctor/index.vue'),
        meta: { title: '医生管理', icon: 'User' }
      },
      {
        path: 'patient',
        name: 'PatientManagement',
        component: () => import('@/views/backend/patient/index.vue'),
        meta: { title: '患者管理', icon: 'User' }
      },
      {
        path: 'medicine',
        name: 'MedicineManagement',
        component: () => import('@/views/backend/medicine/index.vue'),
        meta: { title: '药品管理', icon: 'FirstAidKit' }
      },
      {
        path: 'medicine-category',
        name: 'MedicineCategoryManagement',
        component: () => import('@/views/backend/medicine-category/index.vue'),
        meta: { title: '药品分类管理', icon: 'Menu' }
      },
      {
        path: 'schedule',
        name: 'ScheduleManagement',
        component: () => import('@/views/backend/schedule/index.vue'),
        meta: { title: '排班管理', icon: 'Calendar' }
      },
      {
        path: 'appointment',
        name: 'AppointmentManagement',
        component: () => import('@/views/backend/appointment/index.vue'),
        meta: { title: '预约管理', icon: 'Calendar' }
      },
      {
        path: 'medical-record',
        name: 'MedicalRecordManagement',
        component: () => import('@/views/backend/medical-record/index.vue'),
        meta: { title: '就诊记录管理', icon: 'Document' }
      },
      {
        path: 'prescription',
        name: 'PrescriptionManagement',
        component: () => import('@/views/backend/prescription/index.vue'),
        meta: { title: '处方管理', icon: 'List' }
      },
      {
        path: 'profile',
        name: 'BackendProfile',
        component: () => import('@/views/backend/user/PersonInfo.vue'),
        meta: { title: '个人信息', icon: 'UserFilled' }
      }
    ]
  }
]

// 前台路由配置
const frontendRoutes = [
  {
    path: '/',
    component: () => import('@/layouts/FrontendLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/frontend/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('@/views/frontend/department/index.vue'),
        meta: { title: '科室介绍' }
      },
      {
        path: 'doctor',
        name: 'Doctor',
        component: () => import('@/views/frontend/doctor/index.vue'),
        meta: { title: '医生介绍' }
      },
      {
        path: 'appointment',
        name: 'Appointment',
        component: () => import('@/views/frontend/appointment/index.vue'),
        meta: { title: '预约挂号', requiresAuth: true }
      },
      {
        path: 'my-appointments',
        name: 'MyAppointments',
        component: () => import('@/views/frontend/appointment/my-appointments.vue'),
        meta: { title: '我的预约', requiresAuth: true }
      },
      {
        path: 'medical-record',
        name: 'MedicalRecord',
        component: () => import('@/views/frontend/medical-record/index.vue'),
        meta: { title: '我的就诊记录', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ] 
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forget',
    name: 'ForgetPassword',
    component: () => import('@/views/auth/ForgetPassword.vue'),
    meta: { title: '忘记密码' }
  }
]

// 错误页面路由
const errorRoutes = [
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...frontendRoutes,
    ...backendRoutes,
    ...errorRoutes
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - XXX系统`
  }

  const userStore = useUserStore()
  console.log("Current route:", to.path)
  console.log("User status:", {
    isLoggedIn: userStore.isLoggedIn,
    isUser: userStore.isUser
  })

  // 检查是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth) && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 已登录用户的路由控制
  if (userStore.isLoggedIn) {
    // 处理登录页面访问
    if (to.path === '/login') {
      next(userStore.isUser ? '/' : '/back/dashboard')
      return
    }

    if (!userStore.isUser) {
      // 非普通用户只能访问后台路由
      if (to.path.startsWith('/back')) {
        next()
      } else {
        next('/back/dashboard')
      }
      return
    } else {
      // 普通用户只能访问前台路由
      if (to.path.startsWith('/back')) {
        next('/')
      } else {
        next()
      }
      return
    }
  } else {
    // 未登录用户
    if (to.path.startsWith('/back')) {
      next('/login')
      return
    }
  }

  next()
})

export default router
