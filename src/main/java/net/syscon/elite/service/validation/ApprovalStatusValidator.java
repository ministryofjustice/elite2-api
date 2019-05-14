package net.syscon.elite.service.validation;

import net.syscon.elite.api.model.ApprovalStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ApprovalStatusValidator implements ConstraintValidator<ValidApprovalStatus, ApprovalStatus> {
    @Override
    public void initialize(ValidApprovalStatus constraintAnnotation) {

    }

    @Override
    public boolean isValid(ApprovalStatus approvalStatus, ConstraintValidatorContext context) {
        return approvalStatus.isApproved() && !approvalStatus.hasRefusedReason() ||
                !approvalStatus.isApproved() && approvalStatus.hasRefusedReason();
    }
}
