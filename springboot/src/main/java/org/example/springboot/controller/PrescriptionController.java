package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Prescription;
import org.example.springboot.entity.PrescriptionDetail;
import org.example.springboot.service.PrescriptionService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "处方管理接口")
@RestController
@RequestMapping("/prescription")
public class PrescriptionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrescriptionController.class);
    
    @Resource
    private PrescriptionService prescriptionService;
    
    @Operation(summary = "新增处方")
    @PostMapping
    public Result<?> createPrescription(@RequestBody Prescription prescription) {
        LOGGER.info("新增处方: patientId={}, doctorId={}, recordId={}", 
                prescription.getPatientId(), prescription.getDoctorId(), prescription.getRecordId());
        Prescription newPrescription = prescriptionService.createPrescription(prescription);
        return Result.success(newPrescription);
    }
    
    @Operation(summary = "更新处方状态")
    @PutMapping("/status/{id}")
    public Result<?> updatePrescriptionStatus(@PathVariable Long id, @RequestParam Integer status) {
        LOGGER.info("更新处方状态: id={}, status={}", id, status);
        prescriptionService.updatePrescriptionStatus(id, status);
        return Result.success();
    }
    
    @Operation(summary = "获取处方详情")
    @GetMapping("/{id}")
    public Result<?> getPrescriptionById(@PathVariable Long id) {
        Prescription prescription = prescriptionService.getPrescriptionById(id);
        return Result.success(prescription);
    }
    
    @Operation(summary = "分页查询处方列表")
    @GetMapping("/page")
    public Result<?> getPrescriptionsByPage(
            @RequestParam(required = false) String prescriptionNo,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) Long recordId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询处方列表: prescriptionNo={}, patientId={}, doctorId={}, patientName={}, doctorName={}, recordId={}, startDate={}, endDate={}, status={}, currentPage={}, size={}", 
                prescriptionNo, patientId, doctorId, patientName, doctorName, recordId, startDate, endDate, status, currentPage, size);
        Page<Prescription> page = prescriptionService.getPrescriptionsByPage(prescriptionNo, patientId, doctorId, patientName, doctorName, recordId, startDate, endDate, status, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "获取患者处方列表")
    @GetMapping("/patient/{patientId}")
    public Result<?> getPrescriptionsByPatient(@PathVariable Long patientId) {
        return Result.success(prescriptionService.getPrescriptionsByPatient(patientId));
    }
    
    @Operation(summary = "获取当前患者处方列表")
    @GetMapping("/my")
    public Result<?> getMyPrescriptions() {
        // 获取当前登录用户的患者ID
        Long userId = JwtTokenUtils.getCurrentUserId();
        return Result.success(prescriptionService.getPrescriptionsByPatient(userId));
    }
    
    @Operation(summary = "获取就诊记录关联的处方列表")
    @GetMapping("/record/{recordId}")
    public Result<?> getPrescriptionsByRecord(@PathVariable Long recordId) {
        return Result.success(prescriptionService.getPrescriptionsByRecord(recordId));
    }
    
    @Operation(summary = "添加处方明细")
    @PostMapping("/detail")
    public Result<?> addPrescriptionDetail(@RequestBody PrescriptionDetail detail) {
        LOGGER.info("添加处方明细: prescriptionId={}, medicineId={}", detail.getPrescriptionId(), detail.getMedicineId());
        PrescriptionDetail newDetail = prescriptionService.addPrescriptionDetail(detail);
        return Result.success(newDetail);
    }
    
    @Operation(summary = "更新处方明细")
    @PutMapping("/detail/{id}")
    public Result<?> updatePrescriptionDetail(@PathVariable Long id, @RequestBody PrescriptionDetail detail) {
        LOGGER.info("更新处方明细: id={}", id);
        prescriptionService.updatePrescriptionDetail(id, detail);
        return Result.success();
    }
    
    @Operation(summary = "删除处方明细")
    @DeleteMapping("/detail/{id}")
    public Result<?> deletePrescriptionDetail(@PathVariable Long id) {
        LOGGER.info("删除处方明细: id={}", id);
        prescriptionService.deletePrescriptionDetail(id);
        return Result.success();
    }
} 