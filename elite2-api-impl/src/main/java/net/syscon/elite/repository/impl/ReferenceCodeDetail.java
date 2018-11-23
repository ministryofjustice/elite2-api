package net.syscon.elite.repository.impl;

import lombok.Data;

@Data
public class ReferenceCodeDetail {
    private String domain;
    private String code;
    private String description;
    private String parentDomain;
    private String parentCode;
    private String activeFlag;

    private String subDomain;
    private String subCode;
    private String subDescription;
    private String subActiveFlag;
}
