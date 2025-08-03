---
title: Developer Guide
layout: default
---

{% include navigation.html %}


# Developer Guide

From the previous sections, you have learned that JPF has one recurring, major theme: it is not a monolithic system, but rather a configured collection of components that implement different functions like state space search strategies, report generation and much more.

This section includes the following topics which describe the different mechanisms that can be used to extend JPF.

## Core Mechanisms

- [ChoiceGenerators](ChoiceGenerators.html) - Non-deterministic choice handling
- [Partial Order Reduction](Partial-Order-Reduction.html) - State space optimization
- [Slot and Field Attributes](Slot-and-field-attributes.html) - Custom data storage

## Extension Mechanisms

- [Listeners](Listeners.html) - Event-driven JPF extensions
- [Search Strategies](Search-Strategies.html) - Custom exploration algorithms
- [Model Java Interface (MJI)](Model-Java-Interface.html) - Native method modeling
- [Bytecode Factories](Bytecode-Factories.html) - Custom instruction implementations

## Utilities

- [Logging System](Logging-system.html) - JPF's logging infrastructure
- [Reporting System](Reporting-System.html) - Output and result reporting
- [Running JPF from Application](Running-JPF-from-application.html) - Embedding JPF

## Development Process

- [Coding Conventions](Coding-convention.html) - JPF coding standards
- [Creating Projects](create_project.html) - New JPF project setup
- [Modules](modules.html) - JPF module architecture
