##
##ER/Studio 5.1 SQL Code Generation
## Company :      BBNT
## Project :      CSMART Database
## Author :       M. Kappler & J. Berliner
##
## Date Created : Tuesday, July 09, 2002 16:57:59
## Target DBMS : Oracle 8
##


## 
## TABLE: alib_component 
##

CREATE TABLE alib_component(
    COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_NAME       VARCHAR(150)    BINARY DEFAULT NULL,
    COMPONENT_LIB_ID     VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_TYPE       VARCHAR(50)    BINARY DEFAULT NULL,
    CLONE_SET_ID         DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    UNIQUE KEY pk_alib_component (COMPONENT_ALIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: asb_agent 
##

CREATE TABLE asb_agent(
    ASSEMBLY_ID          VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_LIB_ID     VARCHAR(150)    BINARY DEFAULT NULL,
    CLONE_SET_ID         DECIMAL(68,30) DEFAULT NULL,
    COMPONENT_NAME       VARCHAR(150)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_asb_agent (ASSEMBLY_ID, COMPONENT_ALIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: asb_agent_pg_attr 
##

CREATE TABLE asb_agent_pg_attr(
    ASSEMBLY_ID            VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMPONENT_ALIB_ID      VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    PG_ATTRIBUTE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_VALUE        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_ORDER        DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    START_DATE             DATETIME             NOT NULL DEFAULT '0000-00-00 00:00:00',
    END_DATE               DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_asb_agent_pg_attr (ASSEMBLY_ID, COMPONENT_ALIB_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE)
) TYPE=MyISAM 
;


## 
## TABLE: asb_agent_relation 
##

CREATE TABLE asb_agent_relation(
    ASSEMBLY_ID                     VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ROLE                            VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    SUPPORTING_COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    SUPPORTED_COMPONENT_ALIB_ID     VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    START_DATE                      DATETIME             NOT NULL DEFAULT '0000-00-00 00:00:00',
    END_DATE                        DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_asb_agent_relation (ASSEMBLY_ID, ROLE, SUPPORTING_COMPONENT_ALIB_ID, SUPPORTED_COMPONENT_ALIB_ID, START_DATE)
) TYPE=MyISAM 
;



## 
## TABLE: asb_assembly 
##

CREATE TABLE asb_assembly(
    ASSEMBLY_ID      VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ASSEMBLY_TYPE    VARCHAR(50)    BINARY DEFAULT NULL,
    DESCRIPTION      VARCHAR(200)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_asb_assembly (ASSEMBLY_ID)
) TYPE=MyISAM 
;


## 
## TABLE: asb_component_arg 
##

CREATE TABLE asb_component_arg(
    ASSEMBLY_ID          VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    ARGUMENT             VARCHAR(230)    BINARY NOT NULL DEFAULT '',
    ARGUMENT_ORDER       DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    UNIQUE KEY pk_asb_component_arg (ASSEMBLY_ID, COMPONENT_ALIB_ID, ARGUMENT, ARGUMENT_ORDER)
) TYPE=MyISAM 
;


## 
## TABLE: asb_component_hierarchy 
##

CREATE TABLE asb_component_hierarchy(
    ASSEMBLY_ID                 VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMPONENT_ALIB_ID           VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    PARENT_COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    PRIORITY                    VARCHAR(20)    BINARY DEFAULT NULL,
    INSERTION_ORDER             DECIMAL(68,30) DEFAULT NULL,
    UNIQUE KEY pk_asb_component_hierarchy (ASSEMBLY_ID, COMPONENT_ALIB_ID, PARENT_COMPONENT_ALIB_ID)
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
    UNIQUE KEY pk_lib_oplan (OPLAN_ID)
) TYPE=MyISAM 
;


## 
## TABLE: oplan_agent_attr 
##

CREATE TABLE oplan_agent_attr(
    OPLAN_ID             VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ORG_ID               VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_ID         VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    START_CDAY           DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    ATTRIBUTE_NAME       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    END_CDAY             DECIMAL(68,30) DEFAULT NULL,
    ATTRIBUTE_VALUE      VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_oplan_agent_attr (OPLAN_ID, ORG_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME)
) TYPE=MyISAM 
;

## 
## TABLE: asb_oplan 
##

CREATE TABLE asb_oplan(
    ASSEMBLY_ID       VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPLAN_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPERATION_NAME    VARCHAR(50)    BINARY DEFAULT NULL,
    PRIORITY          VARCHAR(50)    BINARY DEFAULT NULL,
    C0_DATE           DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_asb_oplan (ASSEMBLY_ID, OPLAN_ID)
) TYPE=MyISAM 
;


## 
## TABLE: asb_oplan_agent_attr 
##

CREATE TABLE asb_oplan_agent_attr(
    ASSEMBLY_ID          VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    OPLAN_ID             VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMPONENT_ALIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_ID         VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    START_CDAY           DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    ATTRIBUTE_NAME       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    END_CDAY             DECIMAL(68,30) DEFAULT NULL,
    ATTRIBUTE_VALUE      VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_asb_oplan_agent_attr (ASSEMBLY_ID, OPLAN_ID, COMPONENT_ALIB_ID, COMPONENT_ID, START_CDAY, ATTRIBUTE_NAME)
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
## TABLE: cfw_context_plugin_arg 
##

CREATE TABLE cfw_context_plugin_arg(
    CFW_ID           VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ORG_CONTEXT      VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PLUGIN_ARG_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_context_plugin_arg (CFW_ID, ORG_CONTEXT, PLUGIN_ARG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_group 
##

CREATE TABLE cfw_group(
    CFW_GROUP_ID    VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION     VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_group (CFW_GROUP_ID)
) TYPE=MyISAM 
;


## COMMENT ON TABLE cfw_group IS 'CFW_GROUP_ID defines a "Society Template"'

## 
## TABLE: cfw_group_member 
##

CREATE TABLE cfw_group_member(
    CFW_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    CFW_GROUP_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_group_member (CFW_ID, CFW_GROUP_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_group_org 
##

CREATE TABLE cfw_group_org(
    CFW_GROUP_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_group_org (CFW_GROUP_ID, ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_instance 
##

CREATE TABLE cfw_instance(
    CFW_ID         VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(200)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_instance (CFW_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_oplan 
##

CREATE TABLE cfw_oplan(
    CFW_ID            VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPLAN_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPERATION_NAME    VARCHAR(50)    BINARY DEFAULT NULL,
    PRIORITY          VARCHAR(50)    BINARY DEFAULT NULL,
    C0_DATE           DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_cfw_oplan (CFW_ID, OPLAN_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_oplan_activity 
##

CREATE TABLE cfw_oplan_activity(
    CFW_ID           VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPLAN_ID         VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID     VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    START_CDAY       DECIMAL(68,30)         NOT NULL DEFAULT '0.000000000000000000000000000000',
    END_CDAY         DECIMAL(68,30) DEFAULT NULL,
    OPTEMPO          VARCHAR(50)    BINARY DEFAULT NULL,
    ACTIVITY_TYPE    VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_oplan_activity (CFW_ID, OPLAN_ID, ORG_GROUP_ID, START_CDAY)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_oplan_loc 
##

CREATE TABLE cfw_oplan_loc(
    CFW_ID           VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPLAN_ID         VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID     VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    START_CDAY       DECIMAL(68,30)         NOT NULL DEFAULT '0.000000000000000000000000000000',
    END_CDAY         DECIMAL(68,30) DEFAULT NULL,
    LOCATION_CODE    VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_oplan_loc (CFW_ID, OPLAN_ID, ORG_GROUP_ID, START_CDAY)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_oplan_og_attr 
##

CREATE TABLE cfw_oplan_og_attr(
    CFW_ID             VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    OPLAN_ID           VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID       VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    START_CDAY         DECIMAL(68,30)         NOT NULL DEFAULT '0.000000000000000000000000000000',
    ATTRIBUTE_NAME     VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    END_CDAY           DECIMAL(68,30) DEFAULT NULL,
    ATTRIBUTE_VALUE    VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_cfw_oplan_og_attr (CFW_ID, OPLAN_ID, ORG_GROUP_ID, START_CDAY, ATTRIBUTE_NAME)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_group_org_member 
##

CREATE TABLE cfw_org_group_org_member(
    CFW_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID          VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_org_group_org_member (CFW_ID, ORG_GROUP_ID, ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_hierarchy 
##

CREATE TABLE cfw_org_hierarchy(
    CFW_ID             VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID             VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    SUPERIOR_ORG_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_org_hierarchy (CFW_ID, ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_list 
##

CREATE TABLE cfw_org_list(
    CFW_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_org_list (CFW_ID, ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_og_relation 
##

CREATE TABLE cfw_org_og_relation(
    CFW_ID            VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ROLE              VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID            VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID      VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    START_DATE        DATETIME            NOT NULL DEFAULT '0000-00-00 00:00:00',
    END_DATE          DATETIME             DEFAULT NULL,
    RELATION_ORDER    DECIMAL(68,30)         NOT NULL DEFAULT '0.000000000000000000000000000000',
    UNIQUE KEY pk_cfw_org_og_relation (CFW_ID, ROLE, ORG_ID, ORG_GROUP_ID, START_DATE)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_orgtype 
##

CREATE TABLE cfw_org_orgtype(
    CFW_ID        VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_ID        VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORGTYPE_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_org_orgtype (CFW_ID, ORG_ID, ORGTYPE_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_org_pg_attr 
##

CREATE TABLE cfw_org_pg_attr(
    CFW_ID                 VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ORG_ID                 VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PG_ATTRIBUTE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_VALUE        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_ORDER        DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    START_DATE             DATETIME             NOT NULL DEFAULT '0000-00-00 00:00:00',
    END_DATE               DATETIME             DEFAULT NULL,
    UNIQUE KEY pk_cfw_org_pg_attr (CFW_ID, ORG_ID, PG_ATTRIBUTE_LIB_ID, ATTRIBUTE_VALUE, ATTRIBUTE_ORDER, START_DATE)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_orgtype_plugin_grp 
##

CREATE TABLE cfw_orgtype_plugin_grp(
    CFW_ID             VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORGTYPE_ID         VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    PLUGIN_GROUP_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_cfw_orgtype_plugin_grp (CFW_ID, ORGTYPE_ID, PLUGIN_GROUP_ID)
) TYPE=MyISAM 
;


## 
## TABLE: cfw_plugin_group_member 
##

CREATE TABLE cfw_plugin_group_member(
    CFW_ID                VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PLUGIN_GROUP_ID       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PLUGIN_ID             VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    PLUGIN_CLASS_ORDER    DECIMAL(68,30) DEFAULT NULL,
    UNIQUE KEY pk_cfw_plugin_group_member (CFW_ID, PLUGIN_GROUP_ID, PLUGIN_ID)
) TYPE=MyISAM 
;


## 
## TABLE: community_attribute 
##

CREATE TABLE community_attribute(
    ASSEMBLY_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMMUNITY_ID       VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_ID       VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_VALUE    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_community_attribute (ASSEMBLY_ID, COMMUNITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE)
) TYPE=MyISAM 
;


## 
## TABLE: community_entity_attribute 
##

CREATE TABLE community_entity_attribute(
    ASSEMBLY_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    COMMUNITY_ID       VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ENTITY_ID          VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_ID       VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ATTRIBUTE_VALUE    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_community_entity_attribute (ASSEMBLY_ID, COMMUNITY_ID, ENTITY_ID, ATTRIBUTE_ID, ATTRIBUTE_VALUE)
) TYPE=MyISAM 
;


## 
## TABLE: expt_experiment 
##

CREATE TABLE expt_experiment(
    EXPT_ID         VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION     VARCHAR(200)    BINARY DEFAULT NULL,
    NAME            VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    CFW_GROUP_ID    VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_experiment (EXPT_ID)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial 
##

CREATE TABLE expt_trial(
    TRIAL_ID       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    EXPT_ID        VARCHAR(50)    BINARY DEFAULT NULL,
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    NAME           VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial (TRIAL_ID)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial_assembly 
##

CREATE TABLE expt_trial_assembly(
    TRIAL_ID       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ASSEMBLY_ID    VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    EXPT_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(200)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial_assembly (TRIAL_ID, ASSEMBLY_ID)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial_config_assembly 
##

CREATE TABLE expt_trial_config_assembly(
    TRIAL_ID       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ASSEMBLY_ID    VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    EXPT_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(200)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial_config_assembly (TRIAL_ID, ASSEMBLY_ID)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial_mod_recipe 
##

CREATE TABLE expt_trial_mod_recipe(
    TRIAL_ID             VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    MOD_RECIPE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    RECIPE_ORDER         DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    EXPT_ID              VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial_mod_recipe (TRIAL_ID, MOD_RECIPE_LIB_ID, RECIPE_ORDER)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial_org_mult 
##

CREATE TABLE expt_trial_org_mult(
    TRIAL_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    CFW_ID          VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    ORG_GROUP_ID    VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    EXPT_ID         VARCHAR(50)    BINARY DEFAULT NULL,
    MULTIPLIER      DECIMAL(68,30) DEFAULT NULL,
    DESCRIPTION     VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial_org_mult (TRIAL_ID, CFW_ID, ORG_GROUP_ID)
) TYPE=MyISAM 
;


## 
## TABLE: expt_trial_thread 
##

CREATE TABLE expt_trial_thread(
    TRIAL_ID     VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    THREAD_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    EXPT_ID      VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_expt_trial_thread (TRIAL_ID, THREAD_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_activity_type_ref 
##

CREATE TABLE lib_activity_type_ref(
    ACTIVITY_TYPE    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    DESCRIPTION      VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_activity_type_ref (ACTIVITY_TYPE)
) TYPE=MyISAM 
;


## 
## TABLE: lib_agent_org 
##

CREATE TABLE lib_agent_org(
    COMPONENT_LIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    AGENT_LIB_NAME      VARCHAR(50)    BINARY DEFAULT NULL,
    AGENT_ORG_CLASS     VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_agent_org (COMPONENT_LIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_clone_set 
##

CREATE TABLE lib_clone_set(
    CLONE_SET_ID    DECIMAL(68,30)   NOT NULL DEFAULT '0.000000000000000000000000000000',
    UNIQUE KEY pk_lib_clone_set (CLONE_SET_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_component 
##

CREATE TABLE lib_component(
    COMPONENT_LIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    COMPONENT_TYPE      VARCHAR(50)    BINARY DEFAULT NULL,
    COMPONENT_CLASS     VARCHAR(100)    BINARY DEFAULT NULL,
    INSERTION_POINT     VARCHAR(50)    BINARY DEFAULT NULL,
    DESCRIPTION         VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_component (COMPONENT_LIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_component_arg 
##

CREATE TABLE lib_component_arg(
    COMPONENT_LIB_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    ARGUMENT            VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_lib_component_arg (COMPONENT_LIB_ID, ARGUMENT)
) TYPE=MyISAM 
;


## 
## TABLE: lib_mod_recipe 
##

CREATE TABLE lib_mod_recipe(
    MOD_RECIPE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    NAME                 VARCHAR(50)    BINARY DEFAULT NULL,
    JAVA_CLASS           VARCHAR(100)    BINARY DEFAULT NULL,
    DESCRIPTION          VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_mod_recipe (MOD_RECIPE_LIB_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_mod_recipe_arg 
##

CREATE TABLE lib_mod_recipe_arg(
    MOD_RECIPE_LIB_ID    VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ARG_NAME             VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ARG_ORDER            DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    ARG_VALUE            VARCHAR(255)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_mod_recipe_arg (MOD_RECIPE_LIB_ID, ARG_NAME, ARG_ORDER)
) TYPE=MyISAM 
;


## 
## TABLE: lib_org_group 
##

CREATE TABLE lib_org_group(
    ORG_GROUP_ID    VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    DESCRIPTION     VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_org_group (ORG_GROUP_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_organization 
##

CREATE TABLE lib_organization(
    ORG_ID      VARCHAR(50)    BINARY NOT NULL DEFAULT '',
    ORG_NAME    VARCHAR(50)    BINARY DEFAULT NULL,
    UIC         VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_organization (ORG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_orgtype_ref 
##

CREATE TABLE lib_orgtype_ref(
    ORGTYPE_ID     VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_orgtype_ref (ORGTYPE_ID)
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
## TABLE: lib_plugin_arg 
##

CREATE TABLE lib_plugin_arg(
    PLUGIN_ARG_ID     VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    ARGUMENT_ORDER    DECIMAL(68,30)          NOT NULL DEFAULT '0.000000000000000000000000000000',
    PLUGIN_ID         VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ARGUMENT          VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    ARGUMENT_TYPE     VARCHAR(50)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_plugin_arg (PLUGIN_ARG_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_plugin_arg_thread 
##

CREATE TABLE lib_plugin_arg_thread(
    PLUGIN_ARG_ID    VARCHAR(150)    BINARY NOT NULL DEFAULT '',
    THREAD_ID        VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    UNIQUE KEY pk_lib_plugin_arg_thread (PLUGIN_ARG_ID, THREAD_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_plugin_group 
##

CREATE TABLE lib_plugin_group(
    PLUGIN_GROUP_ID       VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    PLUGIN_GROUP_ORDER    DECIMAL(68,30) DEFAULT NULL,
    DESCRIPTION           VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_plugin_group (PLUGIN_GROUP_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_plugin_ref 
##

CREATE TABLE lib_plugin_ref(
    PLUGIN_ID       VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    PLUGIN_CLASS    VARCHAR(100)    BINARY DEFAULT NULL,
    DESCRIPTION     VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_plugin_ref (PLUGIN_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_plugin_thread 
##

CREATE TABLE lib_plugin_thread(
    PLUGIN_ID      VARCHAR(100)    BINARY NOT NULL DEFAULT '',
    THREAD_ID      VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_plugin_thread (PLUGIN_ID, THREAD_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_role_ref 
##

CREATE TABLE lib_role_ref(
    ROLE           VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_role_ref (ROLE)
) TYPE=MyISAM 
;


## 
## TABLE: lib_role_thread 
##

CREATE TABLE lib_role_thread(
    ROLE           VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    THREAD_ID      VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_role_thread (ROLE, THREAD_ID)
) TYPE=MyISAM 
;


## 
## TABLE: lib_thread 
##

CREATE TABLE lib_thread(
    THREAD_ID      VARCHAR(50)     BINARY NOT NULL DEFAULT '',
    DESCRIPTION    VARCHAR(100)    BINARY DEFAULT NULL,
    UNIQUE KEY pk_lib_thread (THREAD_ID)
) TYPE=MyISAM 
;


##
## fdm tables
##

CREATE TABLE fdm_unit (
  ORG_ID varchar(50) default NULL,
  UNIT_IDENTIFIER varchar(6) default NULL,
  MILITARY_ORGANIZATION_CODE varchar(18) default NULL,
  UL_CD char(3) default NULL,
  UN_ABBRD_NM varchar(30) default NULL,
  GELOC_CD varchar(4) default NULL,
  UNT_CD varchar(5) default NULL,
  UN_CMP_CD char(1) default NULL,
  UN_NM varchar(50) default NULL
) TYPE=MyISAM;

CREATE TABLE fdm_unit_equipment (
  ORG_ID varchar(50) default NULL,
  TI_ID varchar(6) default NULL,
  UNIT_IDENTIFIER varchar(6) default NULL,
  UNIT_EQUIPMENT_QTY decimal(38,0) default NULL,
  KEY UNIT_IDENTIFIER (UNIT_IDENTIFIER),
  KEY TI_ID (TI_ID)
) TYPE=MyISAM;

CREATE TABLE fdm_transportable_item (
  TI_ID varchar(6) default NULL,
  SCL_CD char(2) default NULL,
  TI_NM varchar(255) default NULL,
  TI_CGO_CPCTY_QY decimal(126,0) default NULL,
  TI_BLCK_BRC_SRC_ID varchar(4) default NULL,
  FUEL_BURNER char(3) default NULL,
  KEY TI_ID (TI_ID)
) TYPE=MyISAM;

CREATE TABLE fdm_transportable_item_detail (
  TID_ID char(2) default NULL,
  TI_ID varchar(6) default NULL,
  SHPPNG_CNFGRTN_CD char(2) default NULL,
  CGO_TP_CD char(1) default NULL,
  CGO_XTNT_CD char(1) default NULL,
  CGO_CNTZN_CD char(1) default NULL,
  MATERIEL_ITEM_IDENTIFIER varchar(20) default NULL,
  TYPE_PACK_CODE varchar(18) default NULL,
  TID_RDBL_CD char(1) default NULL,
  TID_LG_DM decimal(126,0) default NULL,
  TID_WDTH_DM decimal(126,0) default NULL,
  TID_EQ_TY_CD char(1) default NULL,
  TID_HT_DM decimal(126,0) default NULL,
  TID_WT decimal(126,0) default NULL,
  TID_VL decimal(126,0) default NULL,
  TID_CGO_CPCTY_QY decimal(126,0) default NULL,
  TID_MAX_LDED_HT_DM decimal(126,0) default NULL,
  TID_HVY_LFT_CD char(1) default NULL,
  TID_AR_CGO_LD_CD char(1) default NULL,
  TID_CB_DM decimal(126,0) default NULL,
  TID_MN_GND_CLNC_DM decimal(126,0) default NULL,
  TID_FTPRNT_AR decimal(126,0) default NULL,
  TID_PLZTN_CD char(1) default NULL,
  TID_CGO_CMPT_LG_DM decimal(126,0) default NULL,
  TID_CGO_CMPT_WD_DM decimal(126,0) default NULL,
  TID_CGO_CMPT_HT_DM decimal(126,0) default NULL,
  TID_CGOCMPBD_HT_DM decimal(126,0) default NULL,
  TID_CGO_BED_HT_DM decimal(126,0) default NULL,
  TID_WHL_BS_DM decimal(126,0) default NULL,
  TID_EMTY_LD_CLS_ID varchar(4) default NULL,
  TID_LDED_LD_CLS_ID varchar(4) default NULL,
  TID_MDL_ID varchar(14) default NULL,
  KEY TI_ID (TI_ID),
  KEY TID_ID (TID_ID),
  KEY CGP_TP_CD (CGO_TP_CD),
  KEY MATERIEL_ITEM_IDENTIFIER (MATERIEL_ITEM_IDENTIFIER)
) TYPE=MyISAM;

CREATE TABLE fdm_unfrmd_srvc_occ_rnk_subcat (
  RANK_SUBCATEGORY_CODE varchar(10) default NULL,
  RANK_SUBCATEGORY_TEXT varchar(30) default NULL
) TYPE=MyISAM;

CREATE TABLE fdm_unfrmd_srvc_occptn (
  UNFRMD_SRVC_OCCPTN_CD varchar(8) default NULL,
  SVC_CD char(1) default NULL,
  RANK_SUBCATEGORY_CODE varchar(10) default NULL,
  UNFRMD_SRVC_OCCPTN_TX varchar(120) default NULL,
  KEY UNFRMD_SRVC_OCCPTN_CD (UNFRMD_SRVC_OCCPTN_CD),
  KEY RANK_SUBCATEGORY_CODE (RANK_SUBCATEGORY_CODE)
) TYPE=MyISAM;

CREATE TABLE fdm_unfrmd_srvc_rnk (
  UNFRMD_SRVC_RNK_CD char(2) default NULL,
  SVC_CD char(1) default NULL,
  PAY_GRD_CD varchar(4) default NULL,
  USR_SHRT_NM varchar(6) default NULL,
  USR_OFCR_IND_CD char(1) default NULL
) TYPE=MyISAM;

CREATE TABLE fdm_unit_billet (
  ORG_ID varchar(50) default NULL,
  UNIT_IDENTIFIER varchar(6) default NULL,
  BILLET_ID varchar(18) default NULL,
  SVC_CD char(1) default NULL,
  UNFRMD_SRVC_RNK_CD char(2) default NULL,
  UNFRMD_SRVC_OCCPTN_CD varchar(8) default NULL,
  REQ_SEI_CD varchar(18) default NULL,
  UNT_SVRC_CMPNT_CD char(1) default NULL,
  TO_NUMBER varchar(6) default NULL,
  TO_SUFFIX char(1) default NULL,
  TO_LINE_NUMBER varchar(5) default NULL,
  TO_RANK varchar(6) default NULL,
  TO_STRENGTH decimal(38,0) default NULL,
  KEY UNFRMD_SRVC_OCCPTN_CD (UNFRMD_SRVC_OCCPTN_CD),
  KEY UNIT_UDENTIFIER (UNIT_IDENTIFIER)
) TYPE=MyISAM;

CREATE TABLE geoloc (
  GEOLOC_CODE char(4) NOT NULL default '',
  LOCATION_NAME char(17) default NULL,
  INSTALLATION_TYPE_CODE char(3) default NULL,
  COUNTRY_STATE_CODE char(2) default NULL,
  COUNTRY_STATE_SHORT_NAME char(5) default NULL,
  COUNTRY_STATE_LONG_NAME char(15) default NULL,
  PROVINCE_CODE char(3) default NULL,
  PROVINCE_NAME char(14) default NULL,
  LATITUDE decimal(6,4) NOT NULL default '0.0000',
  LONGITUDE decimal(7,4) NOT NULL default '0.0000',
  LOGISTIC_PLANNING_CODE char(2) default NULL,
  PRIME_GEOLOC_CODE char(4) default NULL,
  RECORD_OWNER_UIC char(6) default NULL,
  CIVIL_AVIATION_CODE char(4) default NULL,
  GSA_STATE_CODE char(2) default NULL,
  GSA_CITY_CODE char(4) default NULL,
  GSA_COUNTY_CODE char(3) default NULL,
  PRIMARY KEY  (GEOLOC_CODE),
  KEY IX_GEOLOC_NAME (LOCATION_NAME),
  KEY IX_GEOLOC_LAT_LONG (LATITUDE,LONGITUDE),
  KEY IX_GEOLOC_CIVIL_AVIATION_CODE (CIVIL_AVIATION_CODE)
) TYPE=MyISAM;
