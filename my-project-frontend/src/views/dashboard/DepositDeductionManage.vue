<template>
  <div class="deposit-deduction-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>押金扣除管理</span>
          <div class="header-actions">
            <el-button @click="loadAvailableOrders">查看可扣除订单</el-button>
            <el-button type="primary" @click="showCreateDialog">创建扣除申请</el-button>
          </div>
        </div>
      </template>

      <el-table :data="deductionList" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="equipmentName" label="设备" width="150" />
        <el-table-column prop="deductionTypeText" label="扣除类型" width="100" />
        <el-table-column prop="deductionAmount" label="扣除金额" width="100">
          <template #default="{ row }">¥{{ row.deductionAmount }}</template>
        </el-table-column>
        <el-table-column prop="deductionReason" label="扣除原因" width="200" show-overflow-tooltip />
        <el-table-column prop="statusText" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="success"
              size="small"
              @click="approveDeduction(row.id)"
            >
              批准
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="danger"
              size="small"
              @click="rejectDeduction(row.id)"
            >
              拒绝
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="viewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 可扣除订单对话框 -->
    <el-dialog v-model="showAvailableOrdersDialog" title="可扣除订单列表" width="900px">
      <el-table :data="availableOrders" stripe>
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="equipmentName" label="设备" width="150" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="租赁时间" width="160">
          <template #default="{ row }">
            {{ row.startDate }} 至 {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="deposit" label="押金" width="100">
          <template #default="{ row }">¥{{ row.deposit }}</template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="90" />
        <el-table-column prop="paymentStatusText" label="支付状态" width="90" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="selectOrderForDeduction(row)"
            >
              选择
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showAvailableOrdersDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 创建扣除申请对话框 -->
    <el-dialog v-model="showCreateDeductionDialog" title="创建押金扣除申请" width="600px">
      <el-form :model="deductionForm" label-width="120px">
        <el-form-item label="订单编号">
          <el-input v-model="orderSearchInput" placeholder="输入订单编号搜索">
            <template #append>
              <el-button @click="searchOrder">搜索</el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <template v-if="selectedOrder">
          <el-descriptions :column="2" border class="order-info">
            <el-descriptions-item label="订单编号">{{ selectedOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="用户">{{ selectedOrder.username }}</el-descriptions-item>
            <el-descriptions-item label="设备">{{ selectedOrder.equipmentName }}</el-descriptions-item>
            <el-descriptions-item label="押金">¥{{ selectedOrder.deposit }}</el-descriptions-item>
            <el-descriptions-item label="租赁时间">
              {{ selectedOrder.startDate }} 至 {{ selectedOrder.endDate }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">{{ selectedOrder.statusText }}</el-descriptions-item>
          </el-descriptions>
          
          <el-form-item label="扣除类型" required style="margin-top: 20px;">
            <el-radio-group v-model="deductionForm.deductionType" @change="handleTypeChange">
              <el-radio label="damage">设备损坏</el-radio>
              <el-radio label="overdue">逾期归还</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <template v-if="deductionForm.deductionType === 'damage'">
            <el-form-item label="损坏程度">
              <el-select v-model="deductionForm.damageLevel" @change="calculateDamageDeduction">
                <el-option label="轻微损坏 (扣除20%)" value="light" />
                <el-option label="中度损坏 (扣除50%)" value="moderate" />
                <el-option label="严重损坏 (扣除100%)" value="severe" />
              </el-select>
            </el-form-item>
          </template>
          
          <template v-if="deductionForm.deductionType === 'overdue'">
            <el-form-item label="逾期天数">
              <el-input-number v-model="deductionForm.overdueDays" :min="1" @change="calculateOverdueDeduction" />
            </el-form-item>
            <el-form-item label="每日扣款比例">
              <span>{{ (overdueDailyRate * 100).toFixed(0) }}%</span>
            </el-form-item>
          </template>
          
          <el-form-item label="扣除金额" required>
            <el-input-number v-model="deductionForm.deductionAmount" :min="0.01" :max="selectedOrder.deposit" :precision="2" />
            <div style="font-size: 12px; color: #999; margin-top: 5px;">
              最大可扣除金额：¥{{ selectedOrder.deposit }}
            </div>
          </el-form-item>
          
          <el-form-item label="扣除原因" required>
            <el-input v-model="deductionForm.deductionReason" type="textarea" :rows="4" />
          </el-form-item>
          
          <el-form-item label="损坏图片">
            <el-input v-model="deductionForm.damageImages" placeholder="多个图片URL用逗号分隔" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDeductionDialog = false">取消</el-button>
        <el-button type="primary" @click="submitDeduction" :disabled="!selectedOrder">提交申请</el-button>
      </template>
    </el-dialog>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="扣除详情" width="600px">
      <el-descriptions v-if="currentDeduction" :column="2" border>
        <el-descriptions-item label="扣除记录ID">{{ currentDeduction.id }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ currentDeduction.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentDeduction.username }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentDeduction.userPhone }}</el-descriptions-item>
        <el-descriptions-item label="设备">{{ currentDeduction.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="扣除类型">{{ currentDeduction.deductionTypeText }}</el-descriptions-item>
        <el-descriptions-item label="扣除金额" :span="2">¥{{ currentDeduction.deductionAmount }}</el-descriptions-item>
        <el-descriptions-item label="扣除原因" :span="2">{{ currentDeduction.deductionReason }}</el-descriptions-item>
        <el-descriptions-item v-if="currentDeduction.deductionType === 'damage'" label="损坏程度">
          {{ currentDeduction.damageLevelText }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentDeduction.deductionType === 'overdue'" label="逾期天数">
          {{ currentDeduction.overdueDays }}天
        </el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentDeduction.statusText }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentDeduction.adminName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentDeduction.createTime }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ currentDeduction.handleTime || '未处理' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentDeduction.damageImages" label="损坏图片" :span="2">
          <el-image
            v-for="(img, index) in damageImagesList"
            :key="index"
            :src="img"
            style="width: 100px; height: 100px; margin: 5px;"
            :preview-src-list="damageImagesList"
          />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getAllDeductions, createDeduction, approveDeduction as approveDeductionApi, rejectDeduction as rejectDeductionApi, getOrderByNo, getAllOrders } from '@/net'
import { ElMessage, ElMessageBox } from 'element-plus'

const deductionList = ref([])
const showCreateDeductionDialog = ref(false)
const showDetailDialog = ref(false)
const showAvailableOrdersDialog = ref(false)
const currentDeduction = ref(null)
const selectedOrder = ref(null)
const availableOrders = ref([])
const orderSearchInput = ref('')
const overdueDailyRate = ref(0.1)

const deductionForm = ref({
  orderId: null,
  deductionType: '',
  deductionAmount: null,
  deductionReason: '',
  damageImages: '',
  damageLevel: '',
  overdueDays: null
})

const damageImagesList = computed(() => {
  if (!currentDeduction.value?.damageImages) return []
  return currentDeduction.value.damageImages.split(',').map(img => img.trim()).filter(img => img)
})

const loadDeductions = async () => {
  try {
    const res = await getAllDeductions({ page: 1, size: 100 })
    if (res) {
      deductionList.value = res.records
    }
  } catch (error) {
    ElMessage.error('加载扣除记录失败')
  }
}

const selectOrderForDeduction = (order) => {
  selectedOrder.value = order
  deductionForm.value.orderId = order.id
  orderSearchInput.value = order.orderNo
  showAvailableOrdersDialog.value = false
  showCreateDeductionDialog.value = true
}

const showCreateDialog = () => {
  selectedOrder.value = null
  orderSearchInput.value = ''
  deductionForm.value = {
    orderId: null,
    deductionType: '',
    deductionAmount: null,
    deductionReason: '',
    damageImages: '',
    damageLevel: '',
    overdueDays: null
  }
  showCreateDeductionDialog.value = true
}

const loadAvailableOrders = async () => {
  try {
    const res = await getAllOrders({ page: 1, size: 100 })
    if (res) {
      // 筛选出可以扣除押金的订单
      // 条件：已支付、租赁中、已完成，且押金大于0
      availableOrders.value = res.records.filter(order => {
        return (order.status === 'paid' || order.status === 'renting' || order.status === 'completed') &&
               order.deposit > 0 &&
               order.paymentStatus === 'paid'
      })
      showAvailableOrdersDialog.value = true
    }
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  }
}

const searchOrder = async () => {
  if (!orderSearchInput.value) {
    ElMessage.warning('请输入订单编号')
    return
  }

  try {
    const order = await getOrderByNo(orderSearchInput.value)
    selectedOrder.value = order
    deductionForm.value.orderId = order.id
    ElMessage.success('订单查询成功')
  } catch (error) {
    ElMessage.error('订单查询失败：' + (error.response?.data?.message || error.message))
    selectedOrder.value = null
  }
}

const handleTypeChange = () => {
  deductionForm.value.damageLevel = ''
  deductionForm.value.overdueDays = null
  deductionForm.value.deductionAmount = null
}

const calculateDamageDeduction = () => {
  if (!selectedOrder.value || !deductionForm.value.damageLevel) return
  
  const rates = {
    light: 0.2,
    moderate: 0.5,
    severe: 1.0
  }
  
  const rate = rates[deductionForm.value.damageLevel] || 0
  deductionForm.value.deductionAmount = Math.round(selectedOrder.value.deposit * rate * 100) / 100
}

const calculateOverdueDeduction = () => {
  if (!selectedOrder.value || !deductionForm.value.overdueDays) return
  
  const amount = selectedOrder.value.deposit * overdueDailyRate.value * deductionForm.value.overdueDays
  deductionForm.value.deductionAmount = Math.min(amount, selectedOrder.value.deposit)
}

const submitDeduction = async () => {
  try {
    await createDeduction(deductionForm.value)
    ElMessage.success('扣除申请已提交')
    showCreateDeductionDialog.value = false
    loadDeductions()
  } catch (error) {
    ElMessage.error('提交失败：' + (error.response?.data?.message || error.message))
  }
}

const approveDeduction = async (id) => {
  try {
    await ElMessageBox.confirm('确定要批准此扣除申请吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await approveDeductionApi(id)
    ElMessage.success('已批准')
    loadDeductions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
    }
  }
}

const rejectDeduction = async (id) => {
  try {
    await ElMessageBox.confirm('确定要拒绝此扣除申请吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await rejectDeductionApi(id)
    ElMessage.success('已拒绝')
    loadDeductions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
    }
  }
}

const viewDetail = (deduction) => {
  currentDeduction.value = deduction
  showDetailDialog.value = true
}

const getStatusType = (status) => {
  const types = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return types[status] || ''
}

onMounted(() => {
  loadDeductions()
})
</script>

<style scoped>
.deposit-deduction-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.order-info {
  margin-bottom: 20px;
}
</style>
