import { defineStore } from 'pinia'
import request from '@/utils/request'
import { login, register } from '@/api/user'
// import { setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    menus: JSON.parse(localStorage.getItem('menus')) || [],
    doctorInfo: JSON.parse(localStorage.getItem('doctorInfo')) || null,
    patientInfo: JSON.parse(localStorage.getItem('patientInfo')) || null
  }),

  getters: {
    // 判断是否登录
    isLoggedIn: (state) => !!state.token,
    // 判断是否是管理员
    isAdmin: (state) => state.userInfo?.roleCode === 'ADMIN',
    // 判断是否是医生
    isDoctor: (state) => state.userInfo?.roleCode === 'DOCTOR',
    // 判断是否是护士
    isNurse: (state) => state.userInfo?.roleCode === 'NURSE',
    // 判断是否是患者
    isPatient: (state) => state.userInfo?.roleCode === 'PATIENT',
    // 判断是否是普通用户（患者）
    isUser: (state) => state.userInfo?.roleCode === 'PATIENT',
    // 判断是否是医护人员
    isMedicalStaff: (state) => ['DOCTOR', 'NURSE'].includes(state.userInfo?.roleCode)
  },

  actions: {
    // 更新用户信息
    updateUserInfo(data) {
      if (!data) return
      this.userInfo = data
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    
    setUserInfo(data) {
      if (!data) return
      
      this.userInfo = data.userInfo || data
      this.token = data.token
      this.role = data.roleCode
      
      // 存储到 LocalStorage
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      localStorage.setItem('token', this.token || '')
      localStorage.setItem('role', this.role || '')
    },
    clearUserInfo() {
      this.userInfo = null
      this.token = ''
      this.role = ''
      this.menus = []
      this.doctorInfo = null
      this.patientInfo = null
      // 清除 LocalStorage
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('menus')
      localStorage.removeItem('doctorInfo')
      localStorage.removeItem('patientInfo')
    },
    setMenus(menus) {
      if (!menus) return
      this.menus = menus
      localStorage.setItem('menus', JSON.stringify(menus))
    },
    // 获取用户信息和菜单 - 从localStorage恢复
    async getUserInfo() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
      const menus = JSON.parse(localStorage.getItem('menus'))
      
      if (userInfo && menus) {
        this.userInfo = userInfo
        this.menus = menus
        return { userInfo, menus }
      }
      
      // 如果没有缓存的数据，清除状态并抛出错误
      this.clearUserInfo()
      throw new Error('No cached user info')
    },
    
    // 获取医生信息
    async fetchDoctorInfo() {
      if (!this.isDoctor) return null
      
      try {
        const res = await request.get('/doctor/current', {}, {
          showDefaultMsg: false
        })
        
        if (res) {
          this.doctorInfo = res
          localStorage.setItem('doctorInfo', JSON.stringify(res))
        }
        
        return res
      } catch (error) {
        console.error('获取医生信息失败:', error)
        return null
      }
    },
    
    // 获取患者信息
    async fetchPatientInfo() {
      if (!this.isPatient) return null
      
      try {
        const res = await request.get('/patient/current', {}, {
          showDefaultMsg: false,
          onError: (err) => {
            // 如果是未找到患者信息的错误，不显示错误提示
            if (err && err.message && err.message.includes('未找到患者信息')) {
              this.patientInfo = null
              localStorage.removeItem('patientInfo')
              return null
            }
            throw err
          }
        })
        
        if (res) {
          this.patientInfo = res
          localStorage.setItem('patientInfo', JSON.stringify(res))
        }
        
        return res
      } catch (error) {
        console.error('获取患者信息失败:', error)
        return null
      }
    },
    
    // 登录后获取角色相关信息
    async fetchRoleInfo() {
      if (this.isDoctor) {
        await this.fetchDoctorInfo()
      } else if (this.isPatient) {
        await this.fetchPatientInfo()
      }
    },
    
    // 登录
    async login(loginForm) {
      try {
        const res = await request.post('/user/login', loginForm)
        this.setUserInfo(res)
        return res
      } catch (error) {
        this.clearUserInfo()
        throw error
      }
    },
    // 注册

    // 退出登录
    async logout() {
      this.clearUserInfo()
    },
    // 检查登录状态
    checkLoginStatus() {
      return !!this.token
    }
  }
})