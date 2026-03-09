<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <span>用户管理</span>
      </template>
      
      <el-table :data="userList" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="160" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-dropdown @command="(cmd) => handleCommand(cmd, row)">
              <el-button type="primary" size="small">
                操作 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="set-role">修改角色</el-dropdown-item>
                  <el-dropdown-item :command="row.status === 1 ? 'disable' : 'enable'">
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { getUserList, updateUserRole, updateUserStatus } from '@/net'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

const userList = ref([])

const loadUsers = async () => {
  try {
    const res = await getUserList({ page: 1, size: 100 })
    if (res) {
      userList.value = res.records
    }
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

const handleCommand = async (command, row) => {
  if (command === 'set-role') {
    const selectedRole = ref(row.role)
    const roleOptions = [
      { value: 'user', label: '普通用户' },
      { value: 'device_admin', label: '设备管理员' },
      { value: 'system_admin', label: '系统管理员' }
    ]
    
    try {
      await ElMessageBox({
        title: '修改角色',
        message: h('div', { style: 'padding: 10px 0;' }, [
          h('div', { style: 'margin-bottom: 15px; color: #606266;' }, '请选择角色：'),
          h('select', {
            value: selectedRole.value,
            onchange: (e) => { selectedRole.value = e.target.value },
            style: 'width: 100%; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px; font-size: 14px;'
          }, roleOptions.map(opt => 
            h('option', { value: opt.value }, opt.label)
          ))
        ]),
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        showClose: false
      })
      
      if (selectedRole.value !== row.role) {
        await updateUserRole(row.id, selectedRole.value)
        ElMessage.success('角色修改成功')
        loadUsers()
      }
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
      }
    }
  } else if (command === 'disable' || command === 'enable') {
    try {
      await ElMessageBox.confirm(`确定要${command === 'disable' ? '禁用' : '启用'}该用户吗？`, '确认操作', {
        type: 'warning'
      })
      await updateUserStatus(row.id, command === 'disable' ? 0 : 1)
      ElMessage.success('操作成功')
      loadUsers()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
      }
    }
  }
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

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
}
</style>
