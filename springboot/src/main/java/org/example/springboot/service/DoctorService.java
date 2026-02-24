package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.Department;
import org.example.springboot.entity.Doctor;
import org.example.springboot.entity.User;
import org.example.springboot.entity.Schedule;
import org.example.springboot.entity.Appointment;
import org.example.springboot.entity.MedicalRecord;
import org.example.springboot.entity.Prescription;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.DepartmentMapper;
import org.example.springboot.mapper.DoctorMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.ScheduleMapper;
import org.example.springboot.mapper.AppointmentMapper;
import org.example.springboot.mapper.MedicalRecordMapper;
import org.example.springboot.mapper.PrescriptionMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class DoctorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DoctorService.class);
    
    @Resource
    private DoctorMapper doctorMapper;
    
    @Resource
    private DepartmentMapper departmentMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private ScheduleMapper scheduleMapper;
    
    @Resource
    private AppointmentMapper appointmentMapper;
    
    @Resource
    private MedicalRecordMapper medicalRecordMapper;
    
    @Resource
    private PrescriptionMapper prescriptionMapper;

    /**
     * 创建医生并同时创建用户
     */
    @Transactional
    public Doctor createDoctorWithUser(Doctor doctor, User user) {
        // 先创建用户
        user.setStatus(1); // 设置用户状态为启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 对密码进行加密处理
        // 这里应该有对密码的加密逻辑，如果没有请使用BCryptPasswordEncoder
        
        userMapper.insert(user);
        
        // 将创建的用户ID关联到医生
        doctor.setUserId(user.getId());
        
        // 创建医生
        return createDoctor(doctor);
    }

    /**
     * 创建医生
     */
    @Transactional
    public Doctor createDoctor(Doctor doctor) {
        // 生成医生编号
        if (StringUtils.isBlank(doctor.getDoctorNo())) {
            doctor.setDoctorNo(generateDoctorNo());
        }
        
        // 设置默认状态为在职
        if (doctor.getStatus() == null) {
            doctor.setStatus(1);
        }
        
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        doctor.setCreateTime(now);
        doctor.setUpdateTime(now);
        
        doctorMapper.insert(doctor);
        
        // 如果关联了用户，更新用户状态
        if (doctor.getUserId() != null) {
            bindUserToDoctor(doctor.getId(), doctor.getUserId());
        }
        
        return getDetailById(doctor.getId());
    }

    /**
     * 更新医生信息
     */
    @Transactional
    public void updateDoctor(Long id, Doctor doctor) {
        Doctor existingDoctor = doctorMapper.selectById(id);
        if (existingDoctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        doctor.setId(id);
        doctor.setUpdateTime(LocalDateTime.now());
        doctorMapper.updateById(doctor);
        
        // 处理用户绑定关系变更
        Long oldUserId = existingDoctor.getUserId();
        Long newUserId = doctor.getUserId();
        
        if (!Objects.equals(oldUserId, newUserId)) {
            // 如果旧用户ID不为空，解绑旧用户
            if (oldUserId != null) {
                User oldUser = userMapper.selectById(oldUserId);
                if (oldUser != null) {
                    // 解绑逻辑
                }
            }
            
            // 如果新用户ID不为空，绑定新用户
            if (newUserId != null) {
                bindUserToDoctor(id, newUserId);
            }
        }
    }

    /**
     * 根据ID获取医生详情
     */
    public Doctor getDetailById(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        // 获取科室信息
        if (doctor.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(doctor.getDepartmentId());
            doctor.setDepartment(department);
        }
        
        // 获取用户信息
        if (doctor.getUserId() != null) {
            User user = userMapper.selectById(doctor.getUserId());
            doctor.setUser(user);
        }
        
        return doctor;
    }

    /**
     * 获取所有医生列表
     */
    public List<Doctor> getAllDoctors() {
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Doctor::getCreateTime);
        
        List<Doctor> doctors = doctorMapper.selectList(queryWrapper);
        
        // 获取关联信息
        for (Doctor doctor : doctors) {
            if (doctor.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                doctor.setDepartment(department);
            }
            
            if (doctor.getUserId() != null) {
                User user = userMapper.selectById(doctor.getUserId());
                doctor.setUser(user);
            }
        }
        
        return doctors;
    }

    /**
     * 分页查询医生列表
     */
    public Page<Doctor> getDoctorsByPage(String name, String doctorNo, Long departmentId, 
                                         String title, Integer status, 
                                         Integer currentPage, Integer size) {
        Page<Doctor> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(Doctor::getName, name);
        }
        if (StringUtils.isNotBlank(doctorNo)) {
            queryWrapper.like(Doctor::getDoctorNo, doctorNo);
        }
        if (departmentId != null) {
            queryWrapper.eq(Doctor::getDepartmentId, departmentId);
        }
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like(Doctor::getTitle, title);
        }
        if (status != null) {
            queryWrapper.eq(Doctor::getStatus, status);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Doctor::getCreateTime);
        
        Page<Doctor> resultPage = doctorMapper.selectPage(page, queryWrapper);
        
        // 获取关联信息
        for (Doctor doctor : resultPage.getRecords()) {
            if (doctor.getDepartmentId() != null) {
                Department department = departmentMapper.selectById(doctor.getDepartmentId());
                doctor.setDepartment(department);
            }
            
            if (doctor.getUserId() != null) {
                User user = userMapper.selectById(doctor.getUserId());
                doctor.setUser(user);
            }
        }
        
        return resultPage;
    }

    /**
     * 根据科室ID获取医生列表
     */
    public List<Doctor> getDoctorsByDepartment(Long departmentId) {
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Doctor::getDepartmentId, departmentId);
        queryWrapper.eq(Doctor::getStatus, 1); // 只查询在职医生
        
        return doctorMapper.selectList(queryWrapper);
    }

    /**
     * 更新医生状态
     */
    @Transactional
    public void updateDoctorStatus(Long id, Integer status) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        Doctor updateDoctor = new Doctor();
        updateDoctor.setId(id);
        updateDoctor.setStatus(status);
        updateDoctor.setUpdateTime(LocalDateTime.now());
        
        doctorMapper.updateById(updateDoctor);
    }

    /**
     * 删除医生
     */
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        // 检查医生是否是科室负责人
        LambdaQueryWrapper<Department> departmentQueryWrapper = new LambdaQueryWrapper<>();
        departmentQueryWrapper.eq(Department::getDirectorId, id);
        long departmentCount = departmentMapper.selectCount(departmentQueryWrapper);
        if (departmentCount > 0) {
            throw new ServiceException("该医生是科室负责人，不能删除");
        }
        
        // 检查医生是否有关联排班
        LambdaQueryWrapper<Schedule> scheduleQueryWrapper = new LambdaQueryWrapper<>();
        scheduleQueryWrapper.eq(Schedule::getDoctorId, id);
        long scheduleCount = scheduleMapper.selectCount(scheduleQueryWrapper);
        if (scheduleCount > 0) {
            throw new ServiceException("该医生已有排班记录，不能删除");
        }
        
        // 检查医生是否有关联预约
        LambdaQueryWrapper<Appointment> appointmentQueryWrapper = new LambdaQueryWrapper<>();
        appointmentQueryWrapper.eq(Appointment::getDoctorId, id);
        long appointmentCount = appointmentMapper.selectCount(appointmentQueryWrapper);
        if (appointmentCount > 0) {
            throw new ServiceException("该医生已有预约记录，不能删除");
        }
        
        // 检查医生是否有关联就诊记录
        LambdaQueryWrapper<MedicalRecord> medicalRecordQueryWrapper = new LambdaQueryWrapper<>();
        medicalRecordQueryWrapper.eq(MedicalRecord::getDoctorId, id);
        long medicalRecordCount = medicalRecordMapper.selectCount(medicalRecordQueryWrapper);
        if (medicalRecordCount > 0) {
            throw new ServiceException("该医生已有就诊记录，不能删除");
        }
        
        // 检查医生是否有关联处方
        LambdaQueryWrapper<Prescription> prescriptionQueryWrapper = new LambdaQueryWrapper<>();
        prescriptionQueryWrapper.eq(Prescription::getDoctorId, id);
        long prescriptionCount = prescriptionMapper.selectCount(prescriptionQueryWrapper);
        if (prescriptionCount > 0) {
            throw new ServiceException("该医生已有处方记录，不能删除");
        }
        
        // 如果医生已关联用户，先解绑
        if (doctor.getUserId() != null) {
            User user = userMapper.selectById(doctor.getUserId());
            if (user != null) {
                // 解绑逻辑
            }
        }
        
        doctorMapper.deleteById(id);
    }

    /**
     * 绑定用户到医生
     */
    @Transactional
    public void bindUserToDoctor(Long doctorId, Long userId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 检查用户是否已绑定其他医生
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Doctor::getUserId, userId);
        queryWrapper.ne(Doctor::getId, doctorId);
        Long count = doctorMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException("该用户已绑定其他医生");
        }
        
        // 更新医生关联的用户ID
        Doctor updateDoctor = new Doctor();
        updateDoctor.setId(doctorId);
        updateDoctor.setUserId(userId);
        updateDoctor.setUpdateTime(LocalDateTime.now());
        doctorMapper.updateById(updateDoctor);
    }

    /**
     * 解绑用户
     */
    @Transactional
    public void unbindUserFromDoctor(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new ServiceException("医生不存在");
        }
        
        Doctor updateDoctor = new Doctor();
        updateDoctor.setId(doctorId);
        updateDoctor.setUserId(null);
        updateDoctor.setUpdateTime(LocalDateTime.now());
        
        doctorMapper.updateById(updateDoctor);
    }

    /**
     * 生成医生编号
     */
    private String generateDoctorNo() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePrefix = now.format(formatter);
        
        // 获取当天最大编号
        String likePrefix = "D" + datePrefix;
        LambdaQueryWrapper<Doctor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(Doctor::getDoctorNo, likePrefix);
        queryWrapper.orderByDesc(Doctor::getDoctorNo);
        queryWrapper.last("LIMIT 1");
        
        Doctor lastDoctor = doctorMapper.selectOne(queryWrapper);
        
        int sequence = 1;
        if (lastDoctor != null && lastDoctor.getDoctorNo() != null) {
            String lastNo = lastDoctor.getDoctorNo();
            if (lastNo.length() > 9) {
                try {
                    sequence = Integer.parseInt(lastNo.substring(9)) + 1;
                } catch (NumberFormatException e) {
                    LOGGER.error("解析医生编号序列号异常", e);
                }
            }
        }
        
        return String.format("D%s%03d", datePrefix, sequence);
    }
} 