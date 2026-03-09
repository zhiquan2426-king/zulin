<template>
  <div class="my-orders">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的订单</span>
          <el-select v-model="statusFilter" placeholder="订单状态" @change="loadOrders">
            <el-option label="全部" value="" />
            <el-option label="待支付" value="pending" />
            <el-option label="已支付" value="paid" />
            <el-option label="租赁中" value="renting" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </div>
      </template>
      
      <el-table :data="orderList" stripe>
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column label="设备信息" width="180">
          <template #default="{ row }">
            <div class="equipment-info">
              <img :src="row.equipmentImage || '/placeholder.png'" class="equipment-img" />
              <span>{{ row.equipmentName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column label="配送方式" width="100">
          <template #default="{ row }">
            {{ getDeliveryTypeText(row.deliveryType) }}
          </template>
        </el-table-column>
        <el-table-column label="租赁时间" width="140">
          <template #default="{ row }">
            {{ row.startDate }} 至 {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column label="金额" width="130">
          <template #default="{ row }">
            <div>租金: ¥{{ row.totalAmount }}</div>
            <div>押金: ¥{{ row.deposit }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'pending'"
              type="primary"
              size="small"
              @click="payOrder(row.id)"
            >
              支付
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="danger"
              size="small"
              @click="cancelOrder(row.id)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.status === 'completed'"
              type="success"
              size="small"
              @click="showEvaluationDialog(row)"
            >
              评价
            </el-button>
            <el-button
              v-if="row.deliveryNo"
              type="success"
              size="small"
              @click="viewDelivery(row)"
            >
              配送
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
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 评价对话框 -->
    <el-dialog v-model="showEvalDialog" title="评价设备" width="500px">
      <el-form :model="evaluationForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="evaluationForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="evaluationForm.content" type="textarea" :rows="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEvalDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEvaluation">提交评价</el-button>
      </template>
    </el-dialog>
    
    <!-- 订单详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="订单详情" width="600px">
      <el-descriptions v-if="currentOrder" :column="2" border>
        <el-descriptions-item label="订单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ currentOrder.statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentOrder.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentOrder.quantity }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ currentOrder.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ currentOrder.endDate }}</el-descriptions-item>
        <el-descriptions-item label="租赁天数">{{ currentOrder.rentalDays }}天</el-descriptions-item>
        <el-descriptions-item label="日租金">¥{{ currentOrder.dailyPrice }}</el-descriptions-item>
        <el-descriptions-item label="总租金">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="押金">¥{{ currentOrder.deposit }}</el-descriptions-item>
        <el-descriptions-item label="应付金额">¥{{ currentOrder.payableAmount }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ currentOrder.paymentStatusText }}</el-descriptions-item>
        <el-descriptions-item label="押金扣除状态">{{ currentOrder.depositDeductionStatusText || '无扣除' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.deductedAmount && currentOrder.deductedAmount > 0" label="已扣除金额">¥{{ currentOrder.deductedAmount }}</el-descriptions-item>
        <el-descriptions-item label="配送方式">{{ getDeliveryTypeText(currentOrder.deliveryType) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.deliveryNo" label="配送单号">{{ currentOrder.deliveryNo }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.deliveryType === 2" label="收件人">{{ currentOrder.recipientName }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.deliveryType === 2" label="联系电话">{{ currentOrder.recipientPhone }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.deliveryType === 2" label="收件地址" :span="2">{{ currentOrder.recipientAddress }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.deliveryType === 3" label="联系人">{{ currentOrder.contactName }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.deliveryType === 3" label="联系电话">{{ currentOrder.contactPhone }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.deliveryType === 3" label="取件地址" :span="2">{{ currentOrder.pickupAddress }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.paymentTime" label="支付时间" :span="2">{{ currentOrder.paymentTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.returnTime" label="归还时间" :span="2">{{ currentOrder.returnTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentOrder.remark" label="备注" :span="2">{{ currentOrder.remark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyOrders, payOrder as payOrderApi, cancelOrder as cancelOrderApi, addEvaluation } from '@/net'
import { ElMessage } from 'element-plus'

const router = useRouter()

const orderList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')
const showEvalDialog = ref(false)
const showDetailDialog = ref(false)
const currentOrder = ref(null)
const evaluationForm = ref({
  orderId: null,
  rating: 5,
  content: ''
})

const loadOrders = async () => {
  try {
    const res = await getMyOrders({
      page: currentPage.value,
      size: pageSize.value,
      status: statusFilter.value
    })
    if (res) {
      orderList.value = res.records
      total.value = res.total
    }
  } catch (error) {
    ElMessage.error('加载订单失败')
  }
}

const payOrder = async (id) => {
  try {
    await payOrderApi(id)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (error) {
    ElMessage.error('支付失败：' + (error.response?.data?.message || error.message))
  }
}

const cancelOrder = async (id) => {
  try {
    await cancelOrderApi(id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    ElMessage.error('取消失败：' + (error.response?.data?.message || error.message))
  }
}

const showEvaluationDialog = (order) => {
  currentOrder.value = order
  evaluationForm.value = {
    orderId: order.id,
    rating: 5,
    content: ''
  }
  showEvalDialog.value = true
}

const submitEvaluation = async () => {
  try {
    await addEvaluation(evaluationForm.value)
    ElMessage.success('评价成功')
    showEvalDialog.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error('评价失败：' + (error.response?.data?.message || error.message))
  }
}

const viewDetail = (order) => {
  currentOrder.value = order
  showDetailDialog.value = true
}

const getStatusType = (status) => {
  const types = {
    'pending': 'warning',
    'paid': 'primary',
    'renting': 'success',
    'completed': 'info',
    'cancelled': 'danger',
    'overdue': 'danger'
  }
  return types[status] || ''
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadOrders()
}

const getDeliveryTypeText = (deliveryType) => {
  const types = {
    1: '自提',
    2: '配送到家',
    3: '上门取件'
  }
  return types[deliveryType] || '未知'
}

const viewDelivery = (order) => {
  if (order.deliveryNo) {
    router.push(`/dashboard/delivery?orderNo=${order.orderNo}`)
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.my-orders {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.equipment-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.equipment-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
