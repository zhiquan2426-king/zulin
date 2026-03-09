<template>
  <div class="equipment-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备管理</span>
          <el-button type="primary" @click="showAddDialog = true">添加设备</el-button>
        </div>
      </template>
      
      <el-table :data="equipmentList" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="设备图片" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              :preview-src-list="[row.image]"
              fit="cover"
              style="width: 80px; height: 80px; border-radius: 4px;"
            />
            <span v-else style="color: #909399; font-size: 12px;">暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="设备名称" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="brand" label="品牌" width="120" />
        <el-table-column prop="dailyPrice" label="日租金" width="100">
          <template #default="{ row }">¥{{ row.dailyPrice }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="available" label="可租" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editEquipment(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteEquipment(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑设备对话框 -->
    <el-dialog v-model="showAddDialog" :title="editingEquipment ? '编辑设备' : '添加设备'" width="600px">
      <el-form :model="equipmentForm" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="equipmentForm.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="equipmentForm.category" style="width: 100%">
            <el-option label="电脑设备" value="电脑设备" />
            <el-option label="投影设备" value="投影设备" />
            <el-option label="摄影设备" value="摄影设备" />
            <el-option label="航拍设备" value="航拍设备" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="equipmentForm.brand" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="equipmentForm.model" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="equipmentForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="日租金">
          <el-input-number v-model="equipmentForm.dailyPrice" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="押金">
          <el-input-number v-model="equipmentForm.deposit" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存数量">
          <el-input-number v-model="equipmentForm.stock" :min="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="equipmentForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设备图片">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="true"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :limit="1"
            list-type="picture-card"
            :file-list="fileList"
          >
            <el-icon v-if="!imageUrl" class="uploader-icon"><Plus /></el-icon>
            <img v-else :src="imageUrl" class="avatar" />
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEquipment">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getEquipmentList, addEquipment, updateEquipment, deleteEquipment as deleteEquipmentApi } from '@/net'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const equipmentList = ref([])
const showAddDialog = ref(false)
const editingEquipment = ref(null)
const imageUrl = ref('')
const fileList = ref([])
const uploadUrl = 'http://localhost:8080/api/upload'
const baseUrl = 'http://localhost:8080'

const equipmentForm = ref({
  name: '',
  category: '',
  brand: '',
  model: '',
  description: '',
  dailyPrice: 0,
  deposit: 0,
  stock: 1,
  status: 1,
  image: ''
})

const loadEquipment = async () => {
  try {
    const res = await getEquipmentList({ page: 1, size: 100 })
    if (res) {
      console.log('原始设备数据:', res.records)
      equipmentList.value = res.records.map(item => {
        const finalImage = item.image ? (item.image.startsWith('http') ? item.image : baseUrl + item.image) : ''
        console.log('设备ID:', item.id, '原始图片:', item.image, '最终图片:', finalImage)
        return {
          ...item,
          image: finalImage
        }
      })
      console.log('处理后的设备列表:', equipmentList.value)
    }
  } catch (error) {
    ElMessage.error('加载设备列表失败')
  }
}

const editEquipment = (row) => {
  editingEquipment.value = row
  equipmentForm.value = { ...row }
  imageUrl.value = row.image || ''
  fileList.value = row.image ? [{ name: 'image', url: row.image }] : []
  showAddDialog.value = true
}

const beforeUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJpgOrPng) {
    ElMessage.error('上传图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('图片上传成功')
    imageUrl.value = response.data
    equipmentForm.value.image = response.data
  } else {
    ElMessage.error(response.message || '图片上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

const saveEquipment = async () => {
  try {
    if (editingEquipment.value) {
      await updateEquipment(editingEquipment.value.id, equipmentForm.value)
      ElMessage.success('更新成功')
    } else {
      await addEquipment(equipmentForm.value)
      ElMessage.success('添加成功')
    }
    showAddDialog.value = false
    loadEquipment()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
}

const deleteEquipment = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个设备吗？', '确认删除', {
      type: 'warning'
    })
    await deleteEquipmentApi(id)
    ElMessage.success('删除成功')
    loadEquipment()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.response?.data?.message || error.message))
    }
  }
}

onMounted(() => {
  loadEquipment()
})
</script>

<style scoped>
.equipment-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
