/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.validation.internal.callable;

import java.util.Map;
import java.util.concurrent.Callable;

import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.internal.ExecutableRule;

/**
 * This class is used to multi-thread the compilation of the rules.
 */
public class RuleCompilingCallable implements Callable<Void> {

    /**
     * Rule to compile.
     */
    private Rule _rule;

    /**
     * Collection of compiled rules.
     */
    private Map<Long, ExecutableRule> _rules;

    /**
     * Constructor.
     * @param rule rule to compile
     * @param rules collection of compiled rules
     */
    public RuleCompilingCallable(Rule rule, Map<Long, ExecutableRule> rules) {
        _rule = rule;
        _rules = rules;
    }

    @Override
    public Void call() throws Exception {
        _rules.put(_rule.getRuleId(), new ExecutableRule(_rule));
        return null;
    }
}
