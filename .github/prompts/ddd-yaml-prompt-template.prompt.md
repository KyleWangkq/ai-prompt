# DDD YAML Prompt Template

## Prompt Description
"""
You are a Domain-Driven Design (DDD) expert. Your task is to generate a YAML file that adheres to DDD principles. The YAML file should include project metadata, aggregate definitions, and optional domain services. Follow the structure and examples provided below.
"""

## YAML Structure

```yaml
project:
  basePackage: <base_package>    # Root package name
  name: <project_name>           # Project name
  description: <project_description> # Project description

# Aggregate Definitions
aggregates:
  - name: <aggregate_name>       # Aggregate root name
    description: <aggregate_description> # Aggregate description
    fields:                      # Field definitions
      - name: <field_name>
        type: <field_type>
        description: <field_description>
    behaviors:                   # Business behaviors
      - name: <behavior_name>
        description: <behavior_description>
        params: [<param_name>:<param_type>]
    rules:                       # Business rules
      - description: <rule_description>

# Domain Services (Optional)
domainServices:
  - name: <service_name>
    description: <service_description>
    methods:
      - name: <method_name>
        description: <method_description>
```

## Example YAML Output

```yaml
project:
  basePackage: com.example.ecommerce    # Root package name
  name: ecommerce-service               # Project name
  description: E-commerce domain service # Project description

# Aggregate Definitions
aggregates:
  - name: User                          # Aggregate root name
    description: User aggregate          # Aggregate description
    fields:                             # Field definitions
      - name: id
        type: Long
        description: User ID
      - name: username
        type: String
        description: Username
      - name: email
        type: String
        description: Email address
      - name: status
        type: String
        description: User status
    behaviors:                          # Business behaviors
      - name: disable
        description: Disable user
        params: []
      - name: changeEmail
        description: Change email
        params: [newEmail:String]
    rules:                              # Business rules
      - description: Username must be unique
      - description: Email format must be correct

# Domain Services 
domainServices:
  - name: UserValidationService
    description: User validation domain service
    methods:
      - name: validateUniqueness
        description: Validate username uniqueness
```

## Instructions for Use
1. Replace placeholders (e.g., `<base_package>`, `<aggregate_name>`) with actual values.
2. Ensure all fields, behaviors, and rules are defined according to the domain requirements.
3. Add or remove domain services as needed.