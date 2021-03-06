/*
 * Copyright (C) 2004 Information Management Services, Inc.
 */
package com.imsweb.validation;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import groovy.lang.Binding;

import com.imsweb.validation.internal.ExtraPropertyEntityHandlerDto;
import com.imsweb.validation.shared.ContextFunctionDocAnnotation;
import com.imsweb.validation.shared.ContextFunctionDocDto;
import com.imsweb.validation.shared.ValidatorLookup;

/**
 * Helper methods made available to the edits.
 * <br/><br/>
 * These functions need to be initialized before executing any edits in the framework. This class only provides basic methods;
 * there are more advanced implementations that extends this class and can be provided in the initialize() method.
 * <br/><br/>
 * To initialize the framework with these basic method, call the following:<br/><br/>
 * <i>ValidatorContextFunctions.initialize(new ValidatorContextFunction())</i>
 * As of version 1.5, the functions are lazily initialized with the default implementation; so if you don't need a special implementation,
 * there is no need to initialize this class anymore.
 * <br/><br/>
 * To add your own methods to the context of the edits, create a class that extends this one, add the methods, and call the following:<br/><br/>
 * <i>ValidatorContextFunctions.initialize(new MyValidatorContextFunction())</i>
 */
public class ValidatorContextFunctions {

    // unique private instance
    private static ValidatorContextFunctions _INSTANCE;

    /**
     * Initializes this class with the passed instance.
     * <br/><br/>
     * If it was already initialized with another instance, the previous one will be overridden with the new one. Use the
     * <i>isInitialized()</i> methods if you don't want this behavior.
     * <p/>
     * Created on Feb 11, 2008 by depryf
     * @param instance a <code>ValidatorContextFunctions</code> instance
     */
    public static synchronized void initialize(ValidatorContextFunctions instance) {
        _INSTANCE = instance;
    }

    /**
     * Returns true if this class has already been initialized, false otherwise.
     * <p/>
     * Created on Mar 6, 2008 by depryf
     * @return true if this class has already been initialized, false otherwise
     */
    public static synchronized boolean isInitialized() {
        return _INSTANCE != null;
    }

    /**
     * Gets current instance of the <code>ValidatorContextFunctions</code>
     * <p/>
     * Created on Feb 11, 2008 by depryf
     * @return a <code>ValidatorContextFunctions</code>
     */
    public static synchronized ValidatorContextFunctions getInstance() {
        if (_INSTANCE == null)
            _INSTANCE = new ValidatorContextFunctions();

        return _INSTANCE;
    }

    /**
     * Returns documentation about any methods available in the context of the edits execution.
     * <p/>
     * Created on Mar 3, 2010 by depryf
     * @return a list of <code>ValidatorContextFunctionDto</code>
     */
    public static synchronized List<ContextFunctionDocDto> getMethodsDocumentation() {
        if (_INSTANCE == null)
            throw new RuntimeException("Validation Context Functions have not been initialized!");

        List<ContextFunctionDocDto> dtos = new ArrayList<>();

        for (Method m : _INSTANCE.getClass().getMethods()) {
            ContextFunctionDocAnnotation annotation = m.getAnnotation(ContextFunctionDocAnnotation.class);
            if (annotation != null) {
                ContextFunctionDocDto dto = new ContextFunctionDocDto();
                dto.setMethodName(m.getName());
                if (!StringUtils.isEmpty(annotation.param1()))
                    dto.getParams().add(annotation.param1());
                if (!StringUtils.isEmpty(annotation.param2()))
                    dto.getParams().add(annotation.param2());
                if (!StringUtils.isEmpty(annotation.param3()))
                    dto.getParams().add(annotation.param3());
                if (!StringUtils.isEmpty(annotation.param4()))
                    dto.getParams().add(annotation.param4());
                if (!StringUtils.isEmpty(annotation.param5()))
                    dto.getParams().add(annotation.param5());
                if (!StringUtils.isEmpty(annotation.param6()))
                    dto.getParams().add(annotation.param6());
                if (!StringUtils.isEmpty(annotation.param7()))
                    dto.getParams().add(annotation.param7());
                if (!StringUtils.isEmpty(annotation.param8()))
                    dto.getParams().add(annotation.param8());
                if (!StringUtils.isEmpty(annotation.param9()))
                    dto.getParams().add(annotation.param9());
                if (!StringUtils.isEmpty(annotation.param10()))
                    dto.getParams().add(annotation.param10());
                if (!StringUtils.isEmpty(annotation.paramName1()))
                    dto.getParamNames().add(annotation.paramName1());
                if (!StringUtils.isEmpty(annotation.paramName2()))
                    dto.getParamNames().add(annotation.paramName2());
                if (!StringUtils.isEmpty(annotation.paramName3()))
                    dto.getParamNames().add(annotation.paramName3());
                if (!StringUtils.isEmpty(annotation.paramName4()))
                    dto.getParamNames().add(annotation.paramName4());
                if (!StringUtils.isEmpty(annotation.paramName5()))
                    dto.getParamNames().add(annotation.paramName5());
                if (!StringUtils.isEmpty(annotation.paramName6()))
                    dto.getParamNames().add(annotation.paramName6());
                if (!StringUtils.isEmpty(annotation.paramName7()))
                    dto.getParamNames().add(annotation.paramName7());
                if (!StringUtils.isEmpty(annotation.paramName8()))
                    dto.getParamNames().add(annotation.paramName8());
                if (!StringUtils.isEmpty(annotation.paramName9()))
                    dto.getParamNames().add(annotation.paramName9());
                if (!StringUtils.isEmpty(annotation.paramName10()))
                    dto.getParamNames().add(annotation.paramName10());
                dto.setDescription(annotation.desc());
                dto.setExample(annotation.example());
                dtos.add(dto);
            }
        }

        return dtos;
    }

    /**
     * Forces the given entity (corresponding to the given collection name) to report the given properties when the edit fails.
     * <p/>
     * There can be any number of properties. But if none are provided, then the properties gathered statically from the edit text will be used instead.
     * <p/>
     * Created on Apr 21, 2010 by depryf
     * @param binding groovy binding (cannot be null)
     * @param entity entity on which the properties need to be reported (cannot be null)
     * @param properties properties to report; if none are provided, the failures will be reported for all the static properties
     */
    @SuppressWarnings("unchecked")
    @ContextFunctionDocAnnotation(paramName1 = "binding", param1 = "Groovy binding (always called 'binding')", paramName2 = "entity",
            param2 = "entity to force the failure on; what entity means is application-dependent", paramName3 = "properties",
            param3 = "optional properties (with alias prefix) to report the failure on; if none are provided, the failure will be reported for all properties used in the edit",
            desc = "Forces a failure on the provided entity, this can be useful in situations where an edits iterates over sub-entities and a failure needs to be reported on some of those sub-entities.",
            example = "Functions.forceFailureOnEntity(binding, line)\nFunctions.forceFailureOnEntity(binding, line, 'line.nameLast')\nFunctions.forceFailureOnEntity(binding, line, 'line.nameLast', 'line.nameFirst')")
    public void forceFailureOnEntity(Binding binding, Object entity, String... properties) {
        if (binding == null || entity == null)
            return;

        Set<ExtraPropertyEntityHandlerDto> forcedEntities = (Set<ExtraPropertyEntityHandlerDto>)binding.getVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY);
        if (forcedEntities == null) {
            forcedEntities = new HashSet<>();
            binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_ENTITY_KEY, forcedEntities);
        }
        forcedEntities.add(new ExtraPropertyEntityHandlerDto(entity, properties));
    }

    /**
     * Forces the given properties to be reported if the edit fails.
     * <p/>
     * Created on Apr 27, 2010 by depryf
     * @param binding groovy binding (cannot be null)
     * @param properties properties to report (with alias prefix); if null or empty then the function does nothing
     */
    @SuppressWarnings("unchecked")
    @ContextFunctionDocAnnotation(paramName1 = "binding", param1 = "Groovy binding (always called 'binding')", paramName2 = "properties",
            param2 = "properties (with alias prefix) to report the failure on; if none are provided, this function does nothing",
            desc = "Forces the provided properties to be reported when a failure happens; the properties will be reported on the current entity being validated (what entity means is application-dependent).",
            example = "Functions.forceFailureOnProperty(binding, 'line.nameLast')\nFunctions.forceFailureOnProperty(binding, 'line.nameLast', 'line.nameFirst')")
    public void forceFailureOnProperty(Binding binding, String... properties) {
        if (properties == null || properties.length == 0)
            return;

        Set<String> forcedProperties = (Set<String>)binding.getVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY);
        if (forcedProperties == null) {
            forcedProperties = new HashSet<>();
            binding.setVariable(ValidationEngine.VALIDATOR_FORCE_FAILURE_PROPERTY_KEY, forcedProperties);
        }
        forcedProperties.addAll(Arrays.asList(properties));
    }

    /**
     * Forces the given properties to be ignored if the edit fails.
     * <p/>
     * Created on Apr 27, 2010 by depryf
     * @param binding groovy binding (cannot be null)
     * @param properties properties to ignore (with alias prefix); if null or empty then the function does nothing
     */
    @SuppressWarnings("unchecked")
    @ContextFunctionDocAnnotation(paramName1 = "binding", param1 = "Groovy binding (always called 'binding')", paramName2 = "properties",
            param2 = "properties (with alias prefix) to ignore; if none are provided, this function does nothing", desc = "Ignores the provided properties when reporting a failure for the edit.",
            example = "Functions.ignoreFailureOnProperty(binding, 'line.nameLast')\nFunctions.ignoreFailureOnProperty(binding, 'line.nameLast', 'line.nameFirst')")
    public void ignoreFailureOnProperty(Binding binding, String... properties) {
        if (properties == null || properties.length == 0)
            return;

        Set<String> ignoredProperties = (Set<String>)binding.getVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY);
        if (ignoredProperties == null) {
            ignoredProperties = new HashSet<>();
            binding.setVariable(ValidationEngine.VALIDATOR_IGNORE_FAILURE_PROPERTY_KEY, ignoredProperties);
        }
        ignoredProperties.addAll(Arrays.asList(properties));
    }

    /**
     * Gets the value defined in the context of the passed validator ID, under the passed key
     * <p/>
     * Created on Nov 12, 2007 by depryf
     * @param validatorId validator ID
     * @param contextKey context key
     * @return an object, possibly null
     * @throws ValidationException
     */
    @ContextFunctionDocAnnotation(paramName1 = "validatorId", param1 = "validator ID", paramName2 = "contextKey", param2 = "context key",
            desc = "Returns the value of the requested context key, throws an exception if the context is not found.", example = "Functions.getContext('seer', 'Birthplace_Table')")
    public Object getContext(String validatorId, String contextKey) throws ValidationException {
        if (validatorId == null)
            throw new ValidationException("Group is required when accessing a context entry.");
        if (contextKey == null)
            throw new ValidationException("Context key is required when accessing a context entry.");

        Object context = ValidationEngine.getContext(contextKey, validatorId);
        if (context == null)
            throw new ValidationException("Unknown context key '" + contextKey + "' from group '" + validatorId + "'");

        return context;
    }

    /**
     * Returns the <code>ValidatorLookup</code> corresponding to the passed ID, throws an exception if such a lookup doesn't exist.
     * <p/>
     * Created on Dec 20, 2007 by depryf
     * @param id lookup ID
     * @return a <code>ValidatorLookup</code>, never null
     * @throws ValidationException
     */
    @ContextFunctionDocAnnotation(paramName1 = "id", param1 = "Lookup ID", desc = "Returns the lookup corresponding to the requested ID.\n\n" +
            "The returned object is a ValidatorLookup on which the following methods are available:\n" +
            "    String getId()\n" +
            "    String getByKey(String key)\n" +
            "    String getByKeyWithCase(String key)\n" +
            "    Set<String> getAllByKey(String key)\n" +
            "    Set<String> getAllByKeyWithCase(String key)\n" +
            "    Set<String> getAllKeys()\n" +
            "    String getByValue(String value)\n" +
            "    String getByValueWithCase(String value)\n" +
            "    Set<String> getAllByValue(String value)\n" +
            "    Set<String> getAllByValueWithCase(String value)\n" +
            "    Set<String> getAllValues()\n" +
            "    boolean containsKey(Object key)\n" +
            "    boolean containsKeyWithCase(Object key)\n" +
            "    boolean containsValue(Object value)\n" +
            "    boolean containsValueWithCase(Object value)\n" +
            "    boolean containsPair(String key, String value)\n" +
            "    boolean containsPairWithCase(String key, String value)\n",
            example = "Functions.fetchLookup('lookup_id').containsKey(value)")
    public ValidatorLookup fetchLookup(String id) throws ValidationException {
        if (id == null)
            throw new ValidationException("Unable to load lookup <null>");

        ValidatorLookup lookup = ValidatorServices.getInstance().getLookupById(id);
        if (lookup == null)
            throw new ValidationException("Unable to load lookup '" + id + "'");

        return lookup;
    }

    /**
     * Returns the value corresponding to the passed ID from our configuration files. Returns null if such an ID doesn't exist.
     * <p/>
     * Created on Dec 20, 2007 by depryf
     * @param id configuration variable ID
     * @return corresponding value, null if it doesn't exist
     * @throws ValidationException
     */
    @ContextFunctionDocAnnotation(paramName1 = "id", param1 = "Configuration variable ID", desc = "Returns the value of the requested configuration variable.",
            example = "Functions.fetchConfVariable('id')")
    public Object fetchConfVariable(String id) throws ValidationException {
        if (id == null)
            throw new ValidationException("Unable to fetch configuration variable for null value");

        return ValidatorServices.getInstance().getConfVariable(id);
    }

    /**
     * Utility method that attempts to convert the supplied object to an Integer.
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @param value value
     * @return corresponding <code>Integer</code> value, or null if the conversion fails
     */
    @ContextFunctionDocAnnotation(paramName1 = "value", param1 = "value to convert to an Integer", desc = "Converts the passed value to an Integer, returns null if it can't be converted.",
            example = "Functions.asInt(record.ageAtDx)\nFunctions.asInt(25)\nFunctions.asInt('25')\nFunctions.asInt('whatever') would return null")
    public Integer asInt(Object value) {
        Integer result = null;

        if (value != null) {
            if (value instanceof Integer)
                result = (Integer)value;
            else if (value instanceof Number)
                result = ((Number)value).intValue();
            else {
                String str = value instanceof String ? (String)value : value.toString();
                if (NumberUtils.isDigits(str))
                    result = Integer.valueOf(str);
            }
        }

        return result;
    }

    /**
     * Checks that the passed value is between <code>low</code> and <code>high</code>; both inclusive.
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @param value value to check
     * @param low low limit to check against
     * @param high high limit to check against
     * @return true if value is between low and high, false otherwise
     */
    @ContextFunctionDocAnnotation(paramName1 = "value", param1 = "value to compare", paramName2 = "low", param2 = "low limit", paramName3 = "high", param3 = "high limit",
            desc = "returns true if the value is between the low and hight limit (inclusive), false it is not or cannot be determined",
            example = "Functions.between(2, 1, 3) -> true\nFunctions.between('B', 'A', 'C') -> true")
    public boolean between(Object value, Object low, Object high) {
        if (value == null || low == null || high == null)
            return false;

        if (value instanceof String) {
            String val = (String)value;
            String l = low.toString();
            String h = high.toString();

            // special case, if the three params are numeric string, compare them as numeric, not strings
            if (NumberUtils.isDigits(val) && NumberUtils.isDigits(l) && NumberUtils.isDigits(h)) {
                long lVal = Long.parseLong(val);
                long lLow = Long.parseLong(l);
                long lHigh = Long.parseLong(h);
                return lVal >= lLow && lVal <= lHigh;
            }
            else
                return val.compareTo(l) >= 0 && val.compareTo(h) <= 0;
        }

        if (value instanceof Number && low instanceof Number && high instanceof Number) {
            double val = ((Number)value).doubleValue();
            double l = ((Number)low).doubleValue();
            double h = ((Number)high).doubleValue();

            return val >= l && val <= h;
        }

        return false;
    }

    /**
     * Returns the current day
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @return current day
     */
    @ContextFunctionDocAnnotation(desc = "Returns the current day as an integer.", example = "Functions.getCurrentDay()")
    public int getCurrentDay() {
        return LocalDate.now().getDayOfMonth();
    }

    /**
     * Returns the current month
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @return current month
     */
    @ContextFunctionDocAnnotation(desc = "Returns the current month as an integer.", example = "Functions.getCurrentMonth()")
    public int getCurrentMonth() {
        return LocalDate.now().getMonthValue();
    }

    /**
     * Returns the current year
     * <p/>
     * Created on Dec 27, 2007 by depryf
     * @return current year
     */
    @ContextFunctionDocAnnotation(desc = "Returns the current year as an integer.", example = "Functions.getCurrentYear()")
    public int getCurrentYear() {
        return LocalDate.now().getYear();
    }
}

