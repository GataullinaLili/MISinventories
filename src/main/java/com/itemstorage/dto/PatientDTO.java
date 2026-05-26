package com.itemstorage.dto;

public class PatientDTO {

    private String misId;
    private String fullName;
    private String birthDate;
    private String medicalCardNumber;
    private String diagnosis;
    private String department;
    private String admissionDate;
    private boolean discharged;
    private String dischargeDate;

    public PatientDTO() {}

    public String getMisId() { return misId; }
    public void setMisId(String misId) { this.misId = misId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getMedicalCardNumber() { return medicalCardNumber; }
    public void setMedicalCardNumber(String medicalCardNumber) { this.medicalCardNumber = medicalCardNumber; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }

    public boolean isDischarged() { return discharged; }
    public void setDischarged(boolean discharged) { this.discharged = discharged; }

    public String getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }
}