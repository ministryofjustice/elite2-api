CREATE TABLE IMAGES
(
  "IMAGE_ID"                      NUMBER(10, 0) NOT NULL,
  "CAPTURE_DATE"                  DATE NOT NULL,
  "IMAGE_OBJECT_TYPE"             VARCHAR2(12 CHAR) NOT NULL,
  "IMAGE_OBJECT_ID"               NUMBER(10, 0) NOT NULL,
  "IMAGE_OBJECT_SEQ"              NUMBER(6, 0),
  "IMAGE_VIEW_TYPE"               VARCHAR2(12 CHAR),
  "IMAGE_THUMBNAIL"               BLOB,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y',
  "ORIENTATION_TYPE"              VARCHAR2(12 CHAR),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR)
);
