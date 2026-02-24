package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Appointment;
import org.example.springboot.entity.Patient;
import org.example.springboot.service.AppointmentService;
import org.example.springboot.service.PatientService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "预约管理接口")
@RestController
@RequestMapping("/appointment")
public class AppointmentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentController.class);
    
    @Resource
    private AppointmentService appointmentService;
    @Autowired
    private PatientService patientService;

    @Operation(summary = "新增预约")
    @PostMapping
    public Result<?> createAppointment(@RequestBody Appointment appointment) {
        LOGGER.info("新增预约: patientId={}, doctorId={}, scheduleId={}", appointment.getPatientId(), appointment.getDoctorId(), appointment.getScheduleId());
        Appointment newAppointment = appointmentService.createAppointment(appointment);
        return Result.success(newAppointment);
    }
    
    @Operation(summary = "取消预约")
    @PutMapping("/cancel/{id}")
    public Result<?> cancelAppointment(@PathVariable Long id) {
        LOGGER.info("取消预约: id={}", id);
        appointmentService.cancelAppointment(id);
        return Result.success();
    }
    
    @Operation(summary = "完成就诊")
    @PutMapping("/complete/{id}")
    public Result<?> completeAppointment(@PathVariable Long id) {
        LOGGER.info("完成就诊: id={}", id);
        appointmentService.completeAppointment(id);
        return Result.success();
    }
    
    @Operation(summary = "获取预约详情")
    @GetMapping("/{id}")
    public Result<?> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return Result.success(appointment);
    }
    
    @Operation(summary = "分页查询预约列表")
    @GetMapping("/page")
    public Result<?> getAppointmentsByPage(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询预约列表: patientId={}, doctorId={}, patientName={}, doctorName={}, startDate={}, endDate={}, status={}, currentPage={}, size={}", 
                patientId, doctorId, patientName, doctorName, startDate, endDate, status, currentPage, size);
        Page<Appointment> page = appointmentService.getAppointmentsByPage(patientId, doctorId, patientName, doctorName, startDate, endDate, status, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "获取患者预约列表")
    @GetMapping("/patient/{patientId}")
    public Result<?> getAppointmentsByPatient(@PathVariable Long patientId) {
        return Result.success(appointmentService.getAppointmentsByPatient(patientId));
    }
    
    @Operation(summary = "获取当前患者预约列表")
    @GetMapping("/my")
    public Result<?> getMyAppointments() {

        Patient currentPatient = patientService.getCurrentPatient();

        if (currentPatient != null) {
            return Result.success(appointmentService.getAppointmentsByPatient(currentPatient.getId()));
        }else{
            return Result.error("未找到患者信息");
        }


    }
    
    @Operation(summary = "获取医生预约列表")
    @GetMapping("/doctor/{doctorId}")
    public Result<?> getAppointmentsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(appointmentService.getAppointmentsByDoctor(doctorId, date));
    }
    
    @Operation(summary = "更新预约信息")
    @PutMapping("/{id}")
    public Result<?> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        LOGGER.info("更新预约信息: id={}", id);
        appointmentService.updateAppointment(id, appointment);
        return Result.success();
    }
} 