<template>
  <div class="order-manage">
    <el-card>
      <template #header>
        <span>订单管理</span>
      </template>
      
      <el-table :data="orderList" stripe>
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="equipmentName" label="设备" width="150" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="租赁时间" width="180">
          <template #default="{ row }">
            {{ row.startDate }} 至 {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="租金" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'paid'"
              type="primary"
              size="small"
              @click="completeOrder(row.id)"
            >
              开始租赁
            </el-button>
            <el-button
              v-if="row.status === 'renting' || row.status === 'overdue'"
              type="success"
              size="small"
              @click="returnEquipment(row.id)"
            >
              归还设备
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllOrders, completeOrder as completeOrderApi, returnEquipment as returnEquipmentApi } from '@/net'
import { ElMessage } from 'element-plus'

const orderList = ref([])

const loadOrders = async () => {
  try {
    const res = await getAllOrders({ page: 1, size: 100 })
    if (res) {
      orderList.value = res.records
    }
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  }
}

const completeOrder = async (id) => {
  try {
    await completeOrderApi(id)
    ElMessage.success('租赁已开始')
    loadOrders()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
}

const returnEquipment = async (id) => {
  try {
    await returnEquipmentApi(id)
    ElMessage.success('设备已归还')
    loadOrders()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
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

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-manage {
  padding: 20px;
}
</style>
