-- Copy the named Recipe for export.
-- Creates the tempcopy db again, and leaves it there.
-- Used by export-recipe.sh

CREATE DATABASE tempcopy;

-- V4_LIB_MOD_RECIPE
DROP TABLE IF EXISTS tempcopy.V4_LIB_MOD_RECIPE;

CREATE TABLE tempcopy.V4_LIB_MOD_RECIPE AS
  SELECT DISTINCT
    MOD_RECIPE_LIB_ID,
    NAME,
    JAVA_CLASS,
    DESCRIPTION
  FROM
    V4_LIB_MOD_RECIPE
  WHERE
    NAME = ':recipeName';

-- V4_LIB_MOD_RECIPE_ARG
DROP TABLE IF EXISTS tempcopy.V4_LIB_MOD_RECIPE_ARG;

CREATE TABLE tempcopy.V4_LIB_MOD_RECIPE_ARG AS
  SELECT DISTINCT
    AA.MOD_RECIPE_LIB_ID,
    AA.ARG_NAME,
    AA.ARG_ORDER,
    AA.ARG_VALUE
  FROM
    V4_LIB_MOD_RECIPE_ARG AA,
    tempcopy.V4_LIB_MOD_RECIPE AT
  WHERE
    AA.MOD_RECIPE_LIB_ID = AT.MOD_RECIPE_LIB_ID;
