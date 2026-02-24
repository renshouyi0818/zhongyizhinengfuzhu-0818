package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.MedicalRecord;
import org.example.springboot.entity.Patient;
import org.example.springboot.service.MedicalRecordService;
import org.example.springboot.service.PatientService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "就诊记录管理接口")
@RestController
@RequestMapping("/medical-record")
public class MedicalRecordController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MedicalRecordController.class);
    
    @Resource
    private MedicalRecordService medicalRecordService;
    @Autowired
    private PatientService patientService;

    @Operation(summary = "新增就诊记录")
    @PostMapping
    public Result<?> createMedicalRecord(@RequestBody MedicalRecord medicalRecord) {
        LOGGER.info("新增就诊记录: patientId={}, doctorId={}", medicalRecord.getPatientId(), medicalRecord.getDoctorId());
        MedicalRecord newRecord = medicalRecordService.createMedicalRecord(medicalRecord);
        return Result.success(newRecord);
    }
    
    @Operation(summary = "更新就诊记录")
    @PutMapping("/{id}")
    public Result<?> updateMedicalRecord(@PathVariable Long id, @RequestBody MedicalRecord medicalRecord) {
        LOGGER.info("更新就诊记录: id={}", id);
        medicalRecordService.updateMedicalRecord(id, medicalRecord);
        return Result.success();
    }
    
    @Operation(summary = "获取就诊记录详情")
    @GetMapping("/{id}")
    public Result<?> getMedicalRecordById(@PathVariable Long id) {
        MedicalRecord medicalRecord = medicalRecordService.getMedicalRecordById(id);
        return Result.success(medicalRecord);
    }
    
    @Operation(summary = "分页查询就诊记录")
    @GetMapping("/page")
    public Result<?> getMedicalRecordsByPage(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询就诊记录: patientId={}, doctorId={}, patientName={}, doctorName={}, startDate={}, endDate={}, currentPage={}, size={}", 
                patientId, doctorId, patientName, doctorName, startDate, endDate, currentPage, size);
        Page<MedicalRecord> page = medicalRecordService.getMedicalRecordsByPage(patientId, doctorId, patientName, doctorName, startDate, endDate, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "获取患者就诊记录")
    @GetMapping("/patient/{patientId}")
    public Result<?> getMedicalRecordsByPatient(@PathVariable Long patientId) {
        return Result.success(medicalRecordService.getMedicalRecordsByPatient(patientId));
    }
    
    @Operation(summary = "获取当前患者就诊记录")
    @GetMapping("/my")
    public Result<?> getMyMedicalRecords() {
        // 获取当前登录用户的患者ID
        Patient currentPatient = patientService.getCurrentPatient();
        if(currentPatient != null) {
            return Result.success(medicalRecordService.getMedicalRecordsByPatient(currentPatient.getId()));
        }else{

            return Result.error("未找到患者信息");
        }

    }
    
    @Operation(summary = "获取医生就诊记录")
    @GetMapping("/doctor/{doctorId}")
    public Result<?> getMedicalRecordsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(medicalRecordService.getMedicalRecordsByDoctor(doctorId, date));
    }
    
    @Operation(summary = "删除就诊记录")
    @DeleteMapping("/{id}")
    public Result<?> deleteMedicalRecord(@PathVariable Long id) {
        LOGGER.info("删除就诊记录: id={}", id);
        medicalRecordService.deleteMedicalRecord(id);
        return Result.success();
    }
} 