<template>
  <div class="system-manage">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>系统仪表盘</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="总用户数" :value="dashboardData.totalUsers || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="总设备数" :value="dashboardData.totalEquipment || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="总订单数" :value="dashboardData.totalOrders || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="活跃租赁" :value="dashboardData.activeRentals || 0" />
            </el-col>
          </el-row>
          
          <el-divider />
          
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="总收入" :value="dashboardData.totalRevenue || 0" :precision="2" prefix="¥" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="今日收入" :value="dashboardData.todayRevenue || 0" :precision="2" prefix="¥" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="今日订单" :value="dashboardData.todayOrders || 0" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="今日新增用户" :value="dashboardData.todayNewUsers || 0" />
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快速操作</span>
          </template>
          <el-space wrap>
            <el-button type="primary" @click="showSettingsDialog = true">系统设置</el-button>
            <el-button type="warning" @click="clearLogs">清空日志</el-button>
          </el-space>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统提示</span>
          </template>
          <el-alert
            v-if="dashboardData.lowStockEquipment > 0"
            title="库存不足"
            type="warning"
            :description="`${dashboardData.lowStockEquipment} 个设备库存不足`"
            show-icon
          />
          <el-alert
            v-if="dashboardData.overdueOrders > 0"
            title="逾期订单"
            type="error"
            :description="`${dashboardData.overdueOrders} 个订单已逾期`"
            show-icon
            style="margin-top: 10px"
          />
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 系统设置对话框 -->
    <el-dialog v-model="showSettingsDialog" title="系统设置" width="600px">
      <el-form :model="settingsForm" label-width="150px">
        <el-form-item label="网站名称">
          <el-input v-model="settingsForm['site_name']" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="settingsForm['contact_email']" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="settingsForm['contact_phone']" />
        </el-form-item>
        <el-form-item label="最大租赁天数">
          <el-input-number v-model="settingsForm['max_rental_days']" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSettingsDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDashboardData, getSystemSettings, updateSystemSetting, clearSystemLogs } from '@/net'
import { ElMessage } from 'element-plus'

const dashboardData = ref({})
const showSettingsDialog = ref(false)
const settingsForm = reactive({})

const loadDashboard = async () => {
  try {
    const res = await getDashboardData()
    if (res) {
      dashboardData.value = res
    }
  } catch (error) {
    ElMessage.error('加载仪表盘数据失败')
  }
}

const loadSettings = async () => {
  try {
    const res = await getSystemSettings()
    if (res) {
      res.forEach(item => {
        settingsForm[item.settingKey] = item.settingValue
      })
    }
  } catch (error) {
    ElMessage.error('加载系统设置失败')
  }
}

const saveSettings = async () => {
  try {
    for (const key in settingsForm) {
      await updateSystemSetting({ settingKey: key, settingValue: settingsForm[key] })
    }
    ElMessage.success('设置保存成功')
    showSettingsDialog.value = false
  } catch (error) {
    ElMessage.error('保存失败：' + (error.response?.data?.message || error.message))
  }
}

const clearLogs = async () => {
  try {
    await clearSystemLogs()
    ElMessage.success('日志已清空')
  } catch (error) {
    ElMessage.error('清空失败：' + (error.response?.data?.message || error.message))
  }
}

onMounted(() => {
  loadDashboard()
  loadSettings()
})
</script>

<style scoped>
.system-manage {
  padding: 20px;
}
</style>
