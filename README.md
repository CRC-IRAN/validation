# Java Validation Framework

[![Build Status](https://travis-ci.org/imsweb/validation.svg?branch=master)](https://travis-ci.org/imsweb/validation)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.imsweb/validation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.imsweb/validation)

This framework allows edits to be defined using the Groovy scripting language, and to execute them on different types data.

## Features

* Edits are written in Groovy, a rich java-based scripting language.
* Large tables can be provided to the edits as contexts and shared among several edits.
* Edits can be loaded from an XML file, or defined programmatically.
* Any type of data can be validated; it just needs to implement the *Validatable* interface.
* The validation engine executing edits is thread safe.
* Edits can be dynamically added, modified or removed in the engine.
* The engine supports an edits testing framework with unit tests written in Groovy as well.

## Download

This library will be available in Maven Central soon.

## Usage

### Reading a file of edits

Here is an example of a very simplified XML file:

```xml
<validator id="my-edits">
    <rules>
        <rule id="my-edit" java-path="record">
            <expression>return record.primarySite != 'C809'</expression>
            <message>Primary Site cannot be C809.</message>
        </rule>
    </rules>
</validator>
```

And here is the code that can be used to initialize the validation engine from that file:

```java
File file = new File("my-edits.xml")
Validator v = XmlValidatorFactory.loadValidatorFromXml(file);
ValidationEngine.initialize(v);
```

### Creating an edit programmatically

This example shows how to initialize the validation engine from edits created within the code:

```java
// create the rule
Rule r = new Rule();
r.setRuleId(ValidatorServices.getInstance().getNextRuleSequence());
r.setId("my-edit");
r.setJavaPath("record");
r.setMessage("Primary Site cannot be C809.");
r.setExpression("return record.primarySite != 'C809'");

// create the validator (a wrapper for all the rules that belong together)
Validator v = new Validator();
v.setValidatorId(ValidatorServices.getInstance().getNextValidatorSequence())
v.setId("my-edits");
v.getRules().add(r);
r.setValidator(v);

// initialize the engine
ValidationEngine.initialize(v);
```

### Executing edits on a data file

This example uses the layout framework to read NAACCR files and translate them into maps of fields:

```java
File dataFile = new File("my-data.txd.gz");
Layout layout = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT);
for (<Map<String, String> rec : (RecordLayout)layout.readAllRecords(dataFile)) {

    // this is how the engine knows how to validate the provided object
    Validatable validatable = new SimpleNaaccrLinesValidatable(rec)

    // go through the failures and display them
    Collection<RuleFailure> failures = ValidationEngine.validate(validatable);
    for (RuleFailure failure : failures)
        System.out.println(failure.getMessage());
}
```
