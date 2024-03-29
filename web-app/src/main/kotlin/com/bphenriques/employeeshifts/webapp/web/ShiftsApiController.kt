package com.bphenriques.employeeshifts.webapp.web

import com.bphenriques.employeeshifts.domain.shift.model.Shift
import com.bphenriques.employeeshifts.domain.shift.model.ShiftConstraintEmployeeNotFoundException
import com.bphenriques.employeeshifts.domain.shift.model.ShiftConstraintEndBeforeOrAtStartException
import com.bphenriques.employeeshifts.domain.shift.model.ShiftConstraintOverlappingShiftsException
import com.bphenriques.employeeshifts.domain.shift.model.ShiftUnmappedFailedOperation
import com.bphenriques.employeeshifts.domain.shift.service.ShiftService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import javax.validation.Valid

@RestController
@RequestMapping("shifts")
@Tag(name = "Shifts", description = "Shifts API")
class ShiftApiController(
    private val shiftService: ShiftService
) {

    @Operation(summary = "Create or update Shifts")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful operation"),
            ApiResponse(responseCode = "400", description = "If the shift is not valid", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            ApiResponse(responseCode = "404", description = "If the employee does not exist", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            ApiResponse(responseCode = "409", description = "If the shift conflicts with the existing ones", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
        ]
    )
    @PostMapping
    suspend fun upsert(@Valid @RequestBody shiftsRequests: List<UpsertShiftsRequest>): ResponseEntity<List<ShiftResponse>> {
        val savedShifts = shiftService.upsert(shiftsRequests.map { it.toShift() })
        return ResponseEntity.ok(savedShifts.map { ShiftResponse.fromShift(it) })
    }

    @Operation(summary = "Find Shifts")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful operation")
        ]
    )
    @GetMapping
    suspend fun find(@RequestParam(name = "employee_ids") employeeIds: List<Int>): ResponseEntity<List<ShiftResponse>> {
        val fetchedShifts = shiftService.findByEmployeeIds(employeeIds)
        return ResponseEntity.ok(fetchedShifts.map { ShiftResponse.fromShift(it) })
    }

    @Operation(summary = "Delete Shifts")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successful operation"),
        ]
    )
    @DeleteMapping
    suspend fun delete(@RequestParam(name = "ids") ids: List<Int>): ResponseEntity<Unit> {
        shiftService.delete(ids)
        return ResponseEntity.ok().build()
    }
}

@RestControllerAdvice
class ShiftApiControllerErrorHandling {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(ShiftUnmappedFailedOperation::class)
    fun handleUnexpectedUnmappedConstraintViolation(ex: ShiftUnmappedFailedOperation): ResponseEntity<ErrorResponse> {
        logger.warn(ex.message)
        return ApiError.UNEXPECTED_ERROR.toResponseEntity()
    }

    @ExceptionHandler(ShiftConstraintEmployeeNotFoundException::class)
    fun handleShiftConstraintEmployeeNotFoundException(ex: ShiftConstraintEmployeeNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn(ex.message)
        return ApiError.SHIFT_EMPLOYEE_NOT_FOUND.toResponseEntity()
    }

    @ExceptionHandler(ShiftConstraintEndBeforeOrAtStartException::class)
    fun handleShiftConstraintEndBeforeOrAtStartException(ex: ShiftConstraintEndBeforeOrAtStartException): ResponseEntity<ErrorResponse> {
        logger.warn(ex.message)
        return ApiError.SHIFT_INVALID_START_END_TIMES.toResponseEntity()
    }

    @ExceptionHandler(ShiftConstraintOverlappingShiftsException::class)
    fun handleShiftConstraintOverlappingShiftsException(ex: ShiftConstraintOverlappingShiftsException): ResponseEntity<ErrorResponse> {
        logger.warn(ex.message)
        return ApiError.SHIFT_OVERLAPPING_SHIFTS.toResponseEntity()
    }
}

@Schema(name = "Upsert Shifts Request")
data class UpsertShiftsRequest(
    @Schema(title = "The id of the shift. Omit if creating a new one.", nullable = true)
    val id: Int = 0, // When absent, assume that the user intends to create. Otherwise, update.

    @Schema(title = "The id of an existing Employee.")
    val employeeId: Int,

    @Schema(title = "Start of the shift in ISO-8601 format in UTC", example = "2020-01-01T12:00:00Z")
    val startShift: Instant,

    @Schema(title = "End of the shift in ISO-8601 format in UTC", example = "2020-01-01T13:00:00Z")
    val endShift: Instant
) {
    fun toShift(): Shift = Shift(
        id = id,
        employeeId = employeeId,
        startShift = startShift,
        endShift = endShift
    )
}

@Schema(name = "Shift")
data class ShiftResponse(
    val id: Int,
    val employeeId: Int,
    val startShift: Instant,
    val endShift: Instant
) {
    companion object {
        fun fromShift(shift: Shift): ShiftResponse = ShiftResponse(
            id = shift.id,
            employeeId = shift.employeeId,
            startShift = shift.startShift,
            endShift = shift.endShift,
        )
    }
}
