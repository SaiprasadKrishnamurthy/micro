package com.sai.rules.rulebase;

import java.lang.annotation.*;

/**
 * Created by saipkri on 03/08/17.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RuleLibrary {
    String documentation();

    RuleFamilyType ruleFamily() default RuleFamilyType.NONE;
}
