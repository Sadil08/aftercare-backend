package com.aftercare.aftercare_portal.enums;

public enum DeathCaseStatus {
    // 1. Case created — awaiting GN initial review
    PENDING_GN_REVIEW,
    // 2a. GN requested medical confirmation, but no doctor was supplied
    PENDING_DOCTOR_ASSIGNMENT,
    // 2b. Doctor assigned — awaiting B-12 medical certification
    PENDING_B12_MEDICAL,
    // 3. Ready for Registrar to issue CR-2
    PENDING_REGISTRAR_REVIEW,
    // Terminal: CR-2 issued successfully
    CR2_ISSUED_CLOSED,
    // Terminal: Doctor declared unnatural death
    REJECTED_UNNATURAL_DEATH
}
