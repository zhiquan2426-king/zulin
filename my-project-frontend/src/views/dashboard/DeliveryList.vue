<template>
  <div class="delivery-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配送管理</span>
          <el-button type="primary" @click="showCreateDialog = true">创建配送单</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" style="margin-bottom: 20px;">
        <el-form-item label="配送单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入配送单号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待配送" :value="1" />
            <el-option label="配送中" :value="2" />
            <el-option label="已签收" :value="3" />
            <el-option label="运输中" :value="4" />
            <el-option label="已完成" :value="5" />
            <el-option label="已取消" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadDeliveries">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="deliveryList" stripe>
        <el-table-column prop="deliveryNo" label="配送单号" width="180" />
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="deliveryTypeText" label="配送方式" width="100" />
        <el-table-column prop="recipientName" label="收件人" width="120" />
        <el-table-column prop="recipientPhone" label="联系电话" width="130" />
        <el-table-column prop="recipientAddress" label="收件地址" width="200" />
        <el-table-column prop="deliveryFee" label="配送费" width="100">
          <template #default="{ row }">¥{{ row.deliveryFee }}</template>
        </el-table-column>
        <el-table-column prop="deliveryStatusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.deliveryStatus)">{{ row.deliveryStatusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="courierName" label="配送员" width="120" />
        <el-table-column label="操作" width="420" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="info" size="small" @click="viewTracking(row)">轨迹</el-button>
            <el-button
              v-if="row.deliveryStatus === 1 && userStore.role === 'system_admin'"
              type="warning"
              size="small"
              @click="assignCourier(row)"
            >
              指派
            </el-button>
            <el-button
              v-if="row.deliveryStatus === 2"
              type="success"
              size="small"
              @click="startDelivery(row)"
            >
              开始配送
            </el-button>
            <el-button
              v-if="row.deliveryStatus === 2"
              type="success"
              size="small"
              @click="completeDelivery(row)"
            >
              完成配送
            </el-button>
            <el-button
              v-if="row.deliveryStatus === 1"
              type="danger"
              size="small"
              @click="cancelDelivery(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.deliveryNo"
              type="primary"
              size="small"
              @click="viewOrder(row)"
            >
              订单
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
          @current-change="loadDeliveries"
        />
      </div>
    </el-card>

    <!-- 创建配送单对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建配送单" width="600px">
      <el-form :model="deliveryForm" label-width="120px" :rules="formRules" ref="formRef">
        <el-form-item label="订单编号" prop="orderNo">
          <el-input v-model="deliveryForm.orderNo" placeholder="请输入订单编号" />
        </el-form-item>
        <el-form-item label="配送方式" prop="deliveryType">
          <el-radio-group v-model="deliveryForm.deliveryType">
            <el-radio :label="1">自提（免费）</el-radio>
            <el-radio :label="2">配送到家</el-radio>
            <el-radio :label="3">上门取件</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="收件人姓名" prop="recipientName">
          <el-input v-model="deliveryForm.recipientName" />
        </el-form-item>
        <el-form-item label="收件人电话" prop="recipientPhone">
          <el-input v-model="deliveryForm.recipientPhone" />
        </el-form-item>
        <el-form-item
          v-if="deliveryForm.deliveryType === 2"
          label="收件地址"
          prop="recipientAddress"
        >
          <el-input
            v-model="deliveryForm.recipientAddress"
            type="textarea"
            :rows="3"
            placeholder="请输入详细地址"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createDelivery">创建</el-button>
      </template>
    </el-dialog>

    <!-- 配送详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="配送详情" width="700px">
      <el-descriptions v-if="currentDelivery" :column="2" border>
        <el-descriptions-item label="配送单号">{{ currentDelivery.deliveryNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ currentDelivery.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="配送方式">{{ currentDelivery.deliveryTypeText }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentDelivery.deliveryStatus)">
            {{ currentDelivery.deliveryStatusText }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收件人">{{ currentDelivery.recipientName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentDelivery.recipientPhone }}</el-descriptions-item>
        <el-descriptions-item label="收件地址" :span="2">{{ currentDelivery.recipientAddress }}</el-descriptions-item>
        <el-descriptions-item label="配送费">¥{{ currentDelivery.deliveryFee }}</el-descriptions-item>
        <el-descriptions-item label="配送距离">{{ currentDelivery.distance }}公里</el-descriptions-item>
        <el-descriptions-item label="配送员">{{ currentDelivery.courierName }}</el-descriptions-item>
        <el-descriptions-item label="配送员电话">{{ currentDelivery.courierPhone }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentDelivery.createTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentDelivery.completeTime }}</el-descriptions-item>
        <el-descriptions-item v-if="currentDelivery.remark" label="备注" :span="2">
          {{ currentDelivery.remark }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 配送轨迹对话框 -->
    <el-dialog v-model="showTrackingDialog" title="配送轨迹" width="600px">
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in trackingList"
          :key="index"
          :type="getTrackingType(item.status)"
          :timestamp="item.time"
          placement="top"
        >
          <h4>{{ item.statusText }}</h4>
          <p>{{ item.description }}</p>
          <p v-if="item.location">位置：{{ item.location }}</p>
          <p v-if="item.operator">操作人：{{ item.operator }}</p>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button @click="showTrackingDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 指派配送员对话框 -->
    <el-dialog v-model="showAssignDialog" title="指派配送员" width="500px">
      <el-table :data="courierList" style="width: 100%">
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="vehicleType" label="车辆" width="100" />
        <el-table-column label="评分" width="80">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="confirmAssign(row)">指派</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getDeliveryList, createDelivery as createDeliveryApi, getDeliveryTracking, getCourierList, assignCourier as assignCourierApi, cancelDelivery as cancelDeliveryApi, startDelivery as startDeliveryApi, completeDelivery as completeDeliveryApi } from '@/net'

const router = useRouter()
const userStore = useUserStore()

const deliveryList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchForm = ref({
  orderNo: '',
  status: null
})

const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showTrackingDialog = ref(false)
const showAssignDialog = ref(false)

const deliveryForm = ref({
  orderNo: '',
  deliveryType: 1,
  recipientName: '',
  recipientPhone: '',
  recipientAddress: ''
})

const formRules = {
  orderNo: [{ required: true, message: '请输入订单编号', trigger: 'blur' }],
  deliveryType: [{ required: true, message: '请选择配送方式', trigger: 'change' }],
  recipientName: [{ required: true, message: '请输入收件人姓名', trigger: 'blur' }],
  recipientPhone: [{ required: true, message: '请输入收件人电话', trigger: 'blur' }]
}

const currentDelivery = ref(null)
const trackingList = ref([])
const courierList = ref([])

const loadDeliveries = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      orderNo: searchForm.value.orderNo || undefined,
      status: searchForm.value.status || undefined
    }
    const res = await getDeliveryList(params)
    deliveryList.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('加载配送列表失败:', error)
    ElMessage.error('加载配送列表失败')
  }
}

const createDelivery = async () => {
  try {
    await createDeliveryApi(deliveryForm.value)
    ElMessage.success('配送单创建成功')
    showCreateDialog.value = false
    loadDeliveries()
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败：' + error.message)
  }
}

const viewDetail = (row) => {
  currentDelivery.value = row
  showDetailDialog.value = true
}

const viewTracking = async (row) => {
  try {
    const res = await getDeliveryTracking(row.deliveryNo)
    trackingList.value = res
    showTrackingDialog.value = true
  } catch (error) {
    console.error('加载轨迹失败:', error)
    ElMessage.error('加载轨迹失败')
  }
}

const assignCourier = async (row) => {
  currentDelivery.value = row
  try {
    const res = await getCourierList()
    courierList.value = res
    showAssignDialog.value = true
  } catch (error) {
    console.error('加载配送员列表失败:', error)
    ElMessage.error('加载配送员列表失败')
  }
}

const confirmAssign = async (courier) => {
  try {
    await assignCourierApi(currentDelivery.value.deliveryNo, courier.id)
    ElMessage.success('指派成功')
    showAssignDialog.value = false
    loadDeliveries()
  } catch (error) {
    console.error('指派失败:', error)
    ElMessage.error('指派失败：' + error.message)
  }
}

const cancelDelivery = async (row) => {
  try {
    await cancelDeliveryApi(row.deliveryNo)
    ElMessage.success('取消成功')
    loadDeliveries()
  } catch (error) {
    console.error('取消失败:', error)
    ElMessage.error('取消失败：' + error.message)
  }
}

const startDelivery = async (row) => {
  try {
    await startDeliveryApi(row.deliveryNo)
    ElMessage.success('配送已开始')
    loadDeliveries()
  } catch (error) {
    console.error('开始配送失败:', error)
    ElMessage.error('开始配送失败：' + error.message)
  }
}

const completeDelivery = async (row) => {
  try {
    await completeDeliveryApi(row.deliveryNo)
    ElMessage.success('配送已完成')
    loadDeliveries()
  } catch (error) {
    console.error('完成配送失败:', error)
    ElMessage.error('完成配送失败：' + error.message)
  }
}

const viewOrder = (row) => {
  if (row.orderNo) {
    router.push(`/dashboard/orders?orderNo=${row.orderNo}`)
  }
}

const getStatusType = (status) => {
  const types = {
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'info',
    5: 'success',
    6: 'danger'
  }
  return types[status] || ''
}

const getTrackingType = (status) => {
  if (status === 6) return 'danger'
  if (status === 5) return 'success'
  return 'primary'
}

onMounted(() => {
  loadDeliveries()
})
</script>

<style scoped>
.delivery-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
