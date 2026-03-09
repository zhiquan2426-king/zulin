<template>
  <div class="profile">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" @click="toggleEdit" v-if="!isEditing">
            编辑资料
          </el-button>
        </div>
      </template>
      
      <!-- 头像区域 -->
      <div class="avatar-section">
        <el-avatar :size="100" :src="userForm.avatar || defaultAvatar" />
        <div v-if="isEditing" class="avatar-upload">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-button size="small" type="primary">更换头像</el-button>
          </el-upload>
        </div>
      </div>

      <el-form :model="userForm" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="!isEditing" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" :disabled="!isEditing" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="userForm.phone" :disabled="!isEditing" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag :type="getRoleType(userForm.role)">{{ getRoleText(userForm.role) }}</el-tag>
        </el-form-item>
        <el-form-item label="注册时间">
          <span>{{ formatTime(userForm.registerTime) }}</span>
        </el-form-item>
        
        <el-form-item v-if="isEditing">
          <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改密码卡片 -->
    <el-card class="password-card">
      <template #header>
        <span>修改密码</span>
      </template>
      <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="changePassword" :loading="changingPassword">修改密码</el-button>
          <el-button @click="resetPasswordForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getCurrentUserProfile, updateCurrentUserProfile, post } from '@/net'

const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const isEditing = ref(false)
const saving = ref(false)
const changingPassword = ref(false)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userForm = ref({
  username: '',
  email: '',
  phone: '',
  avatar: '',
  role: '',
  registerTime: ''
})

const originalForm = ref({})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2到20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const uploadUrl = '/api/file/upload'
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

const getRoleType = (role) => {
  const types = {
    'user': '',
    'device_admin': 'warning',
    'system_admin': 'danger'
  }
  return types[role] || ''
}

const getRoleText = (role) => {
  const texts = {
    'user': '普通用户',
    'device_admin': '设备管理员',
    'system_admin': '系统管理员'
  }
  return texts[role] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  if (typeof time === 'string') return time.replace('T', ' ').substring(0, 19)
  return new Date(time).toLocaleString()
}

const toggleEdit = () => {
  isEditing.value = true
  originalForm.value = { ...userForm.value }
}

const cancelEdit = () => {
  isEditing.value = false
  userForm.value = { ...originalForm.value }
}

const saveProfile = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true
      try {
        const result = await updateCurrentUserProfile({
          username: userForm.value.username,
          email: userForm.value.email,
          phone: userForm.value.phone,
          avatar: userForm.value.avatar
        })
        
        // 更新store
        userStore.updateProfile({
          username: result.username,
          email: result.email,
          phone: result.phone,
          avatar: result.avatar
        })
        
        userForm.value = {
          ...userForm.value,
          ...result
        }
        
        isEditing.value = false
        ElMessage.success('个人信息更新成功')
      } catch (error) {
        ElMessage.error(error || '更新失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    userForm.value.avatar = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true
      try {
        await post('/user/change-password', {
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        // 清除登录状态
        userStore.logout()
        window.location.href = '/welcome/login'
      } catch (error) {
        ElMessage.error(error || '密码修改失败')
      } finally {
        changingPassword.value = false
      }
    }
  })
}

const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordFormRef.value?.resetFields()
}

const loadProfile = async () => {
  try {
    const result = await getCurrentUserProfile()
    userForm.value = {
      username: result.username,
      email: result.email,
      phone: result.phone || '',
      avatar: result.avatar || '',
      role: result.role,
      registerTime: result.registerTime
    }
    
    // 同步更新store
    userStore.updateProfile({
      username: result.username,
      email: result.email,
      phone: result.phone || '',
      avatar: result.avatar || ''
    })
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

onMounted(() => {
  // 先从store加载
  userForm.value = {
    username: userStore.username,
    email: userStore.email,
    phone: userStore.phone || '',
    avatar: userStore.avatar || '',
    role: userStore.role,
    registerTime: userStore.registerTime
  }
  
  // 再从服务器获取最新数据
  loadProfile()
})
</script>

<style scoped>
.profile {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.avatar-upload {
  margin-top: 15px;
}

.password-card {
  margin-top: 20px;
}
</style>
