package uk.gov.justice.hmpps.prison.repository;

import lombok.Getter;

@Getter
public enum AccessRoleRepositorySql {

    INSERT_ACCESS_ROLE("  INSERT INTO OMS_ROLES (\n" +
            "        ROLE_ID,\n" +
            "        ROLE_NAME,\n" +
            "        ROLE_CODE,\n" +
            "        ROLE_SEQ,\n" +
            "        PARENT_ROLE_CODE,\n" +
            "        ROLE_TYPE,\n" +
            "        ROLE_FUNCTION,\n" +
            "\t      SYSTEM_DATA_FLAG)\n" +
            "\tVALUES (\n" +
            "\t      ROLE_ID.NEXTVAL,\n" +
            "\t      :roleName,\n" +
            "\t      :roleCode,\n" +
            "\t      1,\n" +
            "\t      :parentRoleCode,\n" +
            "\t      'APP',\n" +
            "\t      :roleFunction,\n" +
            "\t      'Y')"),


    UPDATE_ACCESS_ROLE(" UPDATE OMS_ROLES SET\n" +
            "        ROLE_NAME = :roleName,\n" +
            "                ROLE_FUNCTION = :roleFunction\n" +
            "        WHERE ROLE_CODE = :roleCode"),

    GET_ACCESS_ROLE(" SELECT *\n" +
            "                FROM OMS_ROLES\n" +
            "        WHERE ROLE_CODE = :roleCode"),

    GET_ACCESS_ROLES("        SELECT *\n" +
            "                FROM OMS_ROLES\n" +
            "        WHERE ROLE_TYPE = 'APP'\n"),

    EXCLUDE_ADMIN_ROLES_QUERY_TEMPLATE(" AND OMS_ROLES.ROLE_FUNCTION != 'ADMIN'");

    private final String sql;

    AccessRoleRepositorySql(final String sql) {
        this.sql = sql;
    }

}
