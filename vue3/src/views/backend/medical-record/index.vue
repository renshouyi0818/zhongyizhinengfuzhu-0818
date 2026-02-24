<template>
  <div class="medical-record-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h3>就诊记录管理</h3>
          <el-button type="primary" @click="handleAdd">新增就诊记录</el-button>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="患者姓名">
          <el-input v-model="searchForm.patientName" placeholder="请输入患者姓名" clearable />
        </el-form-item>
        <el-form-item label="医生姓名">
          <el-input v-model="searchForm.doctorName" placeholder="请输入医生姓名" clearable />
        </el-form-item>
        <el-form-item label="就诊日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 就诊记录列表 -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%">
        <el-table-column prop="recordNo" label="记录编号" width="180" />
        <el-table-column label="患者信息" width="150">
          <template #default="scope">
            {{ scope.row.patient?.name || '未知' }}
            <el-tag size="small" type="info">{{ scope.row.patient?.sex || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="医生信息" width="150">
          <template #default="scope">
            {{ scope.row.doctor?.name || '未知' }}
            <el-tag size="small">{{ scope.row.doctor?.title || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="科室" width="120">
          <template #default="scope">
            {{ scope.row.doctor?.department?.deptName || '未知科室' }}
          </template>
        </el-table-column>
        <el-table-column prop="recordDate" label="就诊日期" width="120" sortable />
        <el-table-column prop="diagnosis" label="诊断结果" width="200">
          <template #default="scope">
            <el-tooltip
              v-if="scope.row.diagnosis"
              :content="scope.row.diagnosis"
              placement="top"
              effect="light"
            >
              <div class="ellipsis">{{ scope.row.diagnosis }}</div>
            </el-tooltip>
            <span v-else>暂无诊断</span>
          </template>
        </el-table-column>
        <el-table-column prop="followUp" label="随访日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" sortable />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button type="success" size="small" @click="handlePrescription(scope.row)">处方</el-button>
            <el-button type="warning" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm
              title="确定删除此就诊记录吗？"
              @confirm="handleDelete(scope.row.id)"
            >
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑就诊记录对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增就诊记录' : dialogType === 'edit' ? '编辑就诊记录' : '查看就诊记录'"
      width="1300px"
      @closed="resetForm"
    >
      <el-form
        ref="recordFormRef"
        :model="recordForm"
        :rules="recordFormRules"
        label-width="100px"
        :disabled="dialogType === 'view'"
      >
        <el-row :gutter="12">
          <el-col :span="4">
            <el-form-item label="患者" prop="patientId">
              <el-select v-model="recordForm.patientId" placeholder="请选择患者" filterable style="width: 100%">
                <el-option
                  v-for="patient in patientOptions"
                  :key="patient.id"
                  :label="patient.name"
                  :value="patient.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="医生" prop="doctorId">
              <el-select v-model="recordForm.doctorId" placeholder="请选择医生" filterable style="width: 100%">
                <el-option
                  v-for="doctor in doctorOptions"
                  :key="doctor.id"
                  :label="`${doctor.name} (${doctor.department?.deptName || ''})`"
                  :value="doctor.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="关联预约">
              <el-select v-model="recordForm.appointmentId" placeholder="请选择预约" filterable clearable style="width: 100%">
                <el-option
                  v-for="appointment in appointmentOptions"
                  :key="appointment.id"
                  :label="`${appointment.appointmentNo} (${appointment.appointmentDate} ${appointment.timeSlot})`"
                  :value="appointment.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="就诊日期" prop="recordDate">
              <el-date-picker
                v-model="recordForm.recordDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="诊次" prop="numberOfVisits">
              <el-select 
                v-model="recordForm.numberOfVisits" 
                placeholder="请选择诊次" 
                style="width: 100%"
              >
                <el-option label="一诊" value="一诊" />
                <el-option label="二诊" value="二诊" />
                <el-option label="三诊" value="三诊" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="症状" prop="symptom">
          <el-input
            v-model="recordForm.symptom"
            type="textarea"
            :rows="3"
            placeholder="请输入患者症状"
            @blur="handleNormalize"
          />
        </el-form-item>
        
        <el-form-item label="标准症状（仅参考）" prop="standardSymptoms">
          <el-input
            v-model="recordForm.standardSymptoms"
            type="textarea"
            :rows="3"
            placeholder="请输入标准症状"
          />
        </el-form-item>
        
        <el-form-item label="中医诊断" prop="traditionalChineseMedicineDiagnosis">
          <el-input
            v-model="recordForm.traditionalChineseMedicineDiagnosis"
            type="textarea"
            :rows="3"
            placeholder="请输入中医诊断结果"
          />
        </el-form-item>
        
        <el-form-item label="西医诊断" prop="westernMedicineDiagnosis">
          <el-input
            v-model="recordForm.westernMedicineDiagnosis"
            type="textarea"
            :rows="3"
            placeholder="请输入西医诊断结果"
          />
        </el-form-item>
        
        <el-form-item label="证型" prop="syndromeType">
          <el-input
            v-model="recordForm.syndromeType"
            type="textarea"
            :rows="3"
            placeholder="请输入证型"
          />
        </el-form-item>
        
        <el-form-item label="治法" prop="treatment">
          <el-input
            v-model="recordForm.treatment"
            type="textarea"
            :rows="3"
            placeholder="请输入治法"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="随访日期">
              <el-date-picker
                v-model="recordForm.followUp"
                type="date"
                placeholder="选择随访日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="医生备注">
          <el-input
            v-model="recordForm.notes"
            type="textarea"
            :rows="2"
            placeholder="请输入医生备注"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button v-if="dialogType !== 'view'" type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 处方管理对话框 -->
    <el-dialog
      v-model="prescriptionDialogVisible"
      title="处方管理"
      width="800px"
    >
      <div v-if="currentRecord.id" class="prescription-container">
        <div class="record-info">
          <h4>就诊信息</h4>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="患者">{{ currentRecord.patient?.name }}</el-descriptions-item>
            <el-descriptions-item label="医生">{{ currentRecord.doctor?.name }}</el-descriptions-item>
            <el-descriptions-item label="就诊日期">{{ currentRecord.recordDate }}</el-descriptions-item>
            <el-descriptions-item label="诊断结果" :span="3">{{ currentRecord.diagnosis || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="prescription-list">
          <div class="section-header">
            <h4>处方列表</h4>
            <el-button type="primary" size="small" @click="handleAddPrescription">新增处方</el-button>
          </div>
          
          <el-table v-loading="prescriptionLoading" :data="prescriptionList" border style="width: 100%">
            <el-table-column prop="prescriptionNo" label="处方编号" width="180" />
            <el-table-column prop="prescriptionDate" label="处方日期" width="120" />
            <el-table-column prop="diagnosis" label="诊断" width="200">
              <template #default="scope">
                <el-tooltip
                  v-if="scope.row.diagnosis"
                  :content="scope.row.diagnosis"
                  placement="top"
                  effect="light"
                >
                  <div class="ellipsis">{{ scope.row.diagnosis }}</div>
                </el-tooltip>
                <span v-else>同就诊记录</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                  {{ scope.row.status === 1 ? '已取药' : '未取药' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleViewPrescription(scope.row)">查看</el-button>
                <el-button 
                  v-if="scope.row.status === 0" 
                  type="success" 
                  size="small" 
                  @click="handleUpdatePrescriptionStatus(scope.row.id, 1)"
                >
                  标记已取药
                </el-button>
                <el-button 
                  v-else
                  type="warning" 
                  size="small" 
                  @click="handleUpdatePrescriptionStatus(scope.row.id, 0)"
                >
                  标记未取药
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

//新增的症状标准化
const handleNormalize = async () => {
  if (!recordForm.symptom) return

  try {
    await request.post('/symptom/normalize', {
      symptom: recordForm.symptom
    }, {
      onSuccess: (res) => {
        recordForm.standardSymptoms = res
      }
    })
  } catch (e) {
    console.error('症状标准化失败', e)
  }
}


// 列表数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  patientName: '',
  doctorName: ''
})

// 日期范围
const dateRange = ref([])

// 患者和医生选项
const patientOptions = ref([])
const doctorOptions = ref([])
const appointmentOptions = ref([])

// 新增/编辑对话框
const dialogVisible = ref(false)
const dialogType = ref('add') // add, edit, view
const recordFormRef = ref(null)
const recordForm = reactive({
  id: null,
  recordNo: '',
  patientId: null,
  doctorId: null,
  appointmentId: null,
  numberOfVisits: '',
  symptom: '',
  standardSymptoms: '',
  traditionalChineseMedicineDiagnosis: '',
  westernMedicineDiagnosis: '',
  syndromeType: '',
  treatment: '',
  recordDate: '',
  notes: '',
  followUp: null
})

// 表单验证规则
const recordFormRules = {
  patientId: [{ required: true, message: '请选择患者', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择就诊日期', trigger: 'change' }],
  diagnosis: [{ required: true, message: '请输入诊断结果', trigger: 'blur' }]
}

// 处方相关
const prescriptionDialogVisible = ref(false)
const prescriptionLoading = ref(false)
const prescriptionList = ref([])
const currentRecord = reactive({})

// 初始化
onMounted(() => {
  fetchPatients()
  fetchDoctors()
  fetchMedicalRecords()
})

// 获取患者列表
const fetchPatients = async () => {
  try {
    await request.get('/patient/list', {}, {
      onSuccess: (res) => {
        patientOptions.value = res || []
      }
    })
  } catch (error) {
    console.error('获取患者列表失败:', error)
  }
}

// 获取医生列表
const fetchDoctors = async () => {
  try {
    await request.get('/doctor/list', {}, {
      onSuccess: (res) => {
        doctorOptions.value = res || []
      }
    })
  } catch (error) {
    console.error('获取医生列表失败:', error)
  }
}

// 获取就诊记录列表
const fetchMedicalRecords = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value
    }

    // 添加日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    // 添加患者姓名和医生姓名
    if (searchForm.patientName) {
      params.patientName = searchForm.patientName
    }
    if (searchForm.doctorName) {
      params.doctorName = searchForm.doctorName
    }

    await request.get('/medical-record/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取就诊记录列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchMedicalRecords()
}

// 重置搜索
const resetSearch = () => {
  searchForm.patientName = ''
  searchForm.doctorName = ''
  dateRange.value = []
  currentPage.value = 1
  fetchMedicalRecords()
}

// 新增就诊记录
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  
  // 设置默认值
  recordForm.recordDate = formatDate(new Date())
}

// 编辑就诊记录
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.keys(recordForm).forEach(key => {
    recordForm[key] = row[key]
  })
  dialogVisible.value = true
}

// 查看就诊记录
const handleView = (row) => {
  dialogType.value = 'view'
  Object.keys(recordForm).forEach(key => {
    recordForm[key] = row[key]
  })
  dialogVisible.value = true
}

// 处理处方
const handlePrescription = async (row) => {
  // 保存当前就诊记录
  Object.assign(currentRecord, row)
  
  // 显示处方对话框
  prescriptionDialogVisible.value = true
  
  // 加载处方列表
  await fetchPrescriptions(row.id)
}

// 获取就诊记录关联的处方列表
const fetchPrescriptions = async (recordId) => {
  prescriptionLoading.value = true
  try {
    await request.get(`/prescription/record/${recordId}`, {}, {
      onSuccess: (res) => {
        prescriptionList.value = res || []
      }
    })
  } catch (error) {
    console.error('获取处方列表失败:', error)
  } finally {
    prescriptionLoading.value = false
  }
}

// 新增处方
const handleAddPrescription = () => {
  // 跳转到处方创建页面，并传递就诊记录信息
  ElMessageBox.confirm(
    '将跳转到处方创建页面，是否继续？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  )
    .then(() => {
      // TODO: 跳转到处方创建页面
      ElMessage.info('功能开发中，敬请期待')
    })
    .catch(() => {})
}

// 查看处方详情
const handleViewPrescription = (prescription) => {
  // TODO: 跳转到处方详情页面
  ElMessageBox.alert(
    `处方编号: ${prescription.prescriptionNo}<br>
     处方日期: ${prescription.prescriptionDate}<br>
     诊断: ${prescription.diagnosis || '同就诊记录'}<br>
     状态: ${prescription.status === 1 ? '已取药' : '未取药'}<br>
     药品数量: ${prescription.details?.length || 0}种`,
    '处方详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

// 更新处方状态
const handleUpdatePrescriptionStatus = async (id, status) => {
  try {
    await request.put(`/prescription/status/${id}`, {
      status
    }, {
      successMsg: status === 1 ? '已标记为已取药' : '已标记为未取药',
      onSuccess: () => {
        // 刷新处方列表
        fetchPrescriptions(currentRecord.id)
      }
    })
  } catch (error) {
    console.error('更新处方状态失败:', error)
  }
}

// 删除就诊记录
const handleDelete = async (id) => {
  try {
    await request.delete(`/medical-record/${id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchMedicalRecords()
      }
    })
  } catch (error) {
    console.error('删除就诊记录失败:', error)
  }
}

// 提交表单
const submitForm = () => {
  recordFormRef.value.validate(async (valid) => {
    if (valid) {
      if (dialogType.value === 'add') {
        // 新增就诊记录
        await request.post('/medical-record', recordForm, {
          successMsg: '新增就诊记录成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchMedicalRecords()
          }
        })
      } else {
        // 编辑就诊记录
        await request.put(`/medical-record/${recordForm.id}`, recordForm, {
          successMsg: '编辑就诊记录成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchMedicalRecords()
          }
        })
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (recordFormRef.value) {
    recordFormRef.value.resetFields()
  }
  Object.keys(recordForm).forEach(key => {
    recordForm[key] = key === 'id' ? null : null
  })
  
  // 清空预约选项
  appointmentOptions.value = []
}

// 监听患者选择变化，加载该患者的预约
const watchPatientId = computed(() => recordForm.patientId)
const watchDoctorId = computed(() => recordForm.doctorId)

// 当患者或医生选择变化时，加载预约
const fetchAppointments = async () => {
  if (!recordForm.patientId || !recordForm.doctorId) {
    appointmentOptions.value = []
    return
  }
  
  try {
    const params = {
      patientId: recordForm.patientId,
      doctorId: recordForm.doctorId,
      status: 1 // 待就诊状态
    }
    
    await request.get('/appointment/page', params, {
      onSuccess: (res) => {
        appointmentOptions.value = res.records || []
      }
    })
  } catch (error) {
    console.error('获取预约列表失败:', error)
  }
}

// 格式化日期
const formatDate = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 分页操作
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchMedicalRecords()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchMedicalRecords()
}
</script>

<style scoped>
.medical-record-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.prescription-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.record-info {
  margin-bottom: 10px;
}
</style> 