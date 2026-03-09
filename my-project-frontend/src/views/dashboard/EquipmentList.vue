<template>
  <div class="equipment-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备列表</span>
          <el-button type="primary" @click="showSearchDialog = true">搜索</el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col v-for="item in equipmentList" :key="item.id" :span="6">
          <el-card :body-style="{ padding: '0px' }" shadow="hover" class="equipment-card">
            <img :src="item.image || '/placeholder.png'" class="equipment-image" />
            <div class="equipment-info">
              <h3>{{ item.name }}</h3>
              <p class="category">{{ item.category }} - {{ item.brand }}</p>
              <p class="price">¥{{ item.dailyPrice }}/天</p>
              <div class="rating">
                <el-rate v-model="item.avgRating" disabled show-score />
                <span class="evaluation-count">({{ item.evaluationCount }}条评价)</span>
              </div>
              <div class="stock">
                <el-tag :type="item.available > 0 ? 'success' : 'danger'">
                  {{ item.available > 0 ? `可租 ${item.available}/${item.stock}` : '暂无库存' }}
                </el-tag>
              </div>
              <el-button type="primary" @click="viewDetail(item)" style="width: 100%; margin-top: 10px;">
                查看详情
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[8, 16, 24, 32]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 搜索对话框 -->
    <el-dialog v-model="showSearchDialog" title="搜索设备" width="500px">
      <el-form :model="searchForm" label-width="80px">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="设备名称、品牌、型号" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
            <el-option label="电脑设备" value="电脑设备" />
            <el-option label="投影设备" value="投影设备" />
            <el-option label="摄影设备" value="摄影设备" />
            <el-option label="航拍设备" value="航拍设备" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="searchForm.brand" placeholder="设备品牌" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSearchDialog = false">取消</el-button>
        <el-button type="primary" @click="searchEquipment">搜索</el-button>
      </template>
    </el-dialog>
    
    <!-- 设备详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="设备详情" width="800px">
      <div v-if="currentEquipment">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备名称">{{ currentEquipment.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ currentEquipment.category }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ currentEquipment.brand }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ currentEquipment.model }}</el-descriptions-item>
          <el-descriptions-item label="日租金">¥{{ currentEquipment.dailyPrice }}</el-descriptions-item>
          <el-descriptions-item label="押金">¥{{ currentEquipment.deposit }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ currentEquipment.available }}/{{ currentEquipment.stock }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate v-model="currentEquipment.avgRating" disabled show-score />
          </el-descriptions-item>
        </el-descriptions>
        <el-divider>设备描述</el-divider>
        <p>{{ currentEquipment.description }}</p>
        
        <el-divider>配送方式</el-divider>
        <el-form :model="rentalForm" label-width="100px">
          <el-form-item label="配送方式">
            <el-radio-group v-model="rentalForm.deliveryType" @change="handleDeliveryTypeChange">
              <el-radio :label="1">自提（免费）</el-radio>
              <el-radio :label="2">配送到家</el-radio>
              <el-radio :label="3">上门取件</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="rentalForm.deliveryType === 2" label="收件人姓名">
            <el-input v-model="rentalForm.recipientName" placeholder="请输入收件人姓名" />
          </el-form-item>
          <el-form-item v-if="rentalForm.deliveryType === 2" label="联系电话">
            <el-input v-model="rentalForm.recipientPhone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item v-if="rentalForm.deliveryType === 2" label="收件地址">
            <el-input v-model="rentalForm.recipientAddress" type="textarea" :rows="2" placeholder="请输入详细地址" />
          </el-form-item>
          <el-form-item v-if="rentalForm.deliveryType === 3" label="联系人姓名">
            <el-input v-model="rentalForm.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>
          <el-form-item v-if="rentalForm.deliveryType === 3" label="联系电话">
            <el-input v-model="rentalForm.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item v-if="rentalForm.deliveryType === 3" label="取件地址">
            <el-input v-model="rentalForm.pickupAddress" type="textarea" :rows="2" placeholder="请输入取件地址" />
          </el-form-item>
        </el-form>

        <el-divider>租赁</el-divider>
        <el-form :model="rentalForm" label-width="100px">
          <el-form-item label="租赁数量">
            <el-input-number v-model="rentalForm.quantity" :min="1" :max="currentEquipment.available" />
          </el-form-item>
          <el-form-item label="开始日期">
            <el-date-picker
              v-model="rentalForm.startDate"
              type="date"
              placeholder="选择开始日期"
              :disabled-date="disabledStartDate"
            />
          </el-form-item>
          <el-form-item label="结束日期">
            <el-date-picker
              v-model="rentalForm.endDate"
              type="date"
              placeholder="选择结束日期"
              :disabled-date="disabledEndDate"
            />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="rentalForm.remark" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item v-if="rentalDays > 0">
            <el-alert type="info" :closable="false">
              <template #title>
                租赁 {{ rentalDays }} 天，租金 ¥{{ totalRent }}，押金 ¥{{ totalDeposit }}，配送费 ¥{{ deliveryFee }}，合计 ¥{{ totalAmount }}
              </template>
            </el-alert>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">取消</el-button>
        <el-button type="primary" @click="createOrder" :disabled="rentalDays === 0">创建订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getEquipmentList, createOrder as createOrderApi } from '@/net'
import { ElMessage } from 'element-plus'

const router = useRouter()

const equipmentList = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const showSearchDialog = ref(false)
const showDetailDialog = ref(false)
const searchForm = ref({
  keyword: '',
  category: '',
  brand: ''
})
const currentEquipment = ref(null)
const rentalForm = ref({
  equipmentId: null,
  quantity: 1,
  startDate: null,
  endDate: null,
  remark: '',
  deliveryType: 1,
  recipientName: '',
  recipientPhone: '',
  recipientAddress: '',
  contactName: '',
  contactPhone: '',
  pickupAddress: ''
})

const rentalDays = computed(() => {
  if (rentalForm.value.startDate && rentalForm.value.endDate) {
    const start = new Date(rentalForm.value.startDate)
    const end = new Date(rentalForm.value.endDate)
    const diffTime = Math.abs(end - start)
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
    return diffDays > 0 ? diffDays : 0
  }
  return 0
})

const totalRent = computed(() => {
  if (currentEquipment.value && rentalDays.value > 0) {
    return (currentEquipment.value.dailyPrice * rentalDays.value * rentalForm.value.quantity).toFixed(2)
  }
  return 0
})

const totalDeposit = computed(() => {
  if (currentEquipment.value) {
    return (currentEquipment.value.deposit * rentalForm.value.quantity).toFixed(2)
  }
  return 0
})

const deliveryFee = computed(() => {
  if (rentalForm.value.deliveryType === 1) {
    return 0
  } else if (rentalForm.value.deliveryType === 2) {
    return 20
  } else if (rentalForm.value.deliveryType === 3) {
    return 15
  }
  return 0
})

const totalAmount = computed(() => {
  return (parseFloat(totalRent.value) + parseFloat(totalDeposit.value) + parseFloat(deliveryFee.value)).toFixed(2)
})

const handleDeliveryTypeChange = () => {
  rentalForm.value.recipientName = ''
  rentalForm.value.recipientPhone = ''
  rentalForm.value.recipientAddress = ''
  rentalForm.value.contactName = ''
  rentalForm.value.contactPhone = ''
  rentalForm.value.pickupAddress = ''
}

const loadEquipment = async () => {
  try {
    const res = await getEquipmentList({
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm.value
    })
    if (res) {
      equipmentList.value = res.records
      total.value = res.total
    }
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  }
}

const searchEquipment = () => {
  currentPage.value = 1
  loadEquipment()
  showSearchDialog.value = false
}

const viewDetail = (item) => {
  currentEquipment.value = item
  rentalForm.value = {
    equipmentId: item.id,
    quantity: 1,
    startDate: null,
    endDate: null,
    remark: '',
    deliveryType: 1,
    recipientName: '',
    recipientPhone: '',
    recipientAddress: '',
    contactName: '',
    contactPhone: '',
    pickupAddress: ''
  }
  showDetailDialog.value = true
}

const disabledStartDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const disabledEndDate = (time) => {
  if (!rentalForm.value.startDate) return false
  return time.getTime() < new Date(rentalForm.value.startDate).getTime()
}

const createOrder = async () => {
  try {
    const orderData = {
      equipmentId: rentalForm.value.equipmentId,
      quantity: rentalForm.value.quantity,
      startDate: rentalForm.value.startDate,
      endDate: rentalForm.value.endDate,
      remark: rentalForm.value.remark,
      deliveryType: rentalForm.value.deliveryType
    }
    
    if (rentalForm.value.deliveryType === 2) {
      if (!rentalForm.value.recipientName || !rentalForm.value.recipientPhone || !rentalForm.value.recipientAddress) {
        ElMessage.warning('请完善配送信息')
        return
      }
      orderData.recipientName = rentalForm.value.recipientName
      orderData.recipientPhone = rentalForm.value.recipientPhone
      orderData.recipientAddress = rentalForm.value.recipientAddress
    } else if (rentalForm.value.deliveryType === 3) {
      if (!rentalForm.value.contactName || !rentalForm.value.contactPhone || !rentalForm.value.pickupAddress) {
        ElMessage.warning('请完善取件信息')
        return
      }
      orderData.contactName = rentalForm.value.contactName
      orderData.contactPhone = rentalForm.value.contactPhone
      orderData.pickupAddress = rentalForm.value.pickupAddress
    }
    
    const res = await createOrderApi(orderData)
    if (res) {
      ElMessage.success('订单创建成功')
      showDetailDialog.value = false
      router.push('/dashboard/orders')
    }
  } catch (error) {
    ElMessage.error('创建订单失败：' + (error.response?.data?.message || error.message))
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadEquipment()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadEquipment()
}

onMounted(() => {
  loadEquipment()
})
</script>

<style scoped>
.equipment-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.equipment-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.equipment-card:hover {
  transform: translateY(-5px);
}

.equipment-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.equipment-info {
  padding: 15px;
}

.equipment-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: bold;
}

.category {
  color: #999;
  font-size: 14px;
  margin: 5px 0;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
  margin: 10px 0;
}

.rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 10px 0;
}

.evaluation-count {
  color: #999;
  font-size: 14px;
}

.stock {
  margin: 10px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
