## 
## TABLE: lib_organization 
##

CREATE TABLE lib_organization(
    ORG_ID      VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_NAME    VARCHAR(50)    BINARY DEFAULT NULL,
    UIC         VARCHAR(50)    BINARY DEFAULT NULL,
    ORG_CLASS   VARCHAR(50)	BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_organization (ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_pg_attribute 
##

CREATE TABLE lib_pg_attribute(
    PG_ATTRIBUTE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    PG_NAME                VARCHAR(50)    BINARY DEFAULT NULL,
    ATTRIBUTE_NAME         VARCHAR(50)    BINARY DEFAULT NULL,
    ATTRIBUTE_TYPE         VARCHAR(50)    BINARY DEFAULT NULL,
    AGGREGATE_TYPE         VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_pg_attribute (PG_ATTRIBUTE_LIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: org_pg_attr 
##

CREATE TABLE org_pg_attr(
    ORG_ID                 VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PG_ATTRIBUTE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_VALUE        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_ORDER        DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    START_DATE             DATETIME             NOT NULL DEFAULT '0000-00-00 00:00:00',
    END_DATE               DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_org_pg_attr (ORG_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE)
) TYPE=MyISAM 
;

## 
## TABLE: org_relation 
##

CREATE TABLE org_relation(
    ROLE                            VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    SUPPORTING_ORG_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    SUPPORTED_ORG_ID     VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    START_DATE                      DATETIME             NOT NULL DEFAULT '0000-00-00 00:00:00',
    END_DATE                        DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_org_relation (ROLE, SUPPORTING_ORG_ID, SUPPORTED_ORG_ID, START_DATE)
) TYPE=MyISAM 
;

## 
## TABLE: oplan_agent_attr 
##

CREATE TABLE oplan_agent_attr(
    OPLAN_ID             VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ORG_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_ID         VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    START_CDAY           DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    ATTRIBUTE_NAME       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    END_CDAY             DECIMAL(68,30) DEFAULT NULL,
    ATTRIBUTE_VALUE      VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_oplan_agent_attr (OPLAN_ID, ORG_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME)
) TYPE=MyISAM 
;


## 
## TABLE: alploc 
##

CREATE TABLE alploc(
    ALPLOC_CODE               VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    LOCATION_NAME             VARCHAR(50)    BINARY DEFAULT NULL,
    LATITUDE                  DECIMAL(68,30) DEFAULT NULL,
    LONGITUDE                 DECIMAL(68,30) DEFAULT NULL,
    INSTALLATION_TYPE_CODE    CHAR(3)       BINARY DEFAULT NULL,
    UNIQUE KEY pk_alploc (ALPLOC_CODE)
) TYPE=MyISAM 
;

## 
## TABLE: lib_oplan 
##

CREATE TABLE lib_oplan(
    OPLAN_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPERATION_NAME    VARCHAR(50)    BINARY DEFAULT NULL,
    PRIORITY          VARCHAR(50)    BINARY DEFAULT NULL,
    C0_DATE           DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_oplan (OPLAN_ID)
) TYPE=MyISAM 
;
