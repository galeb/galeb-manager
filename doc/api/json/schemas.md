# Project

```
{
  "type" : "object",
  "id" : "urn:jsonschema:io:galeb:manager:entity:Project",
  "properties" : {
    "id" : {
      "type" : "integer",
      "read-only": true
    },
    "_created_at" : {
      "type" : "integer",
      "format" : "UTC_MILLISEC",
      "read-only": true
    },
    "_created_by" : {
      "type" : "string",
      "read-only": true
    },
    "_lastmodified_at" : {
      "type" : "integer",
      "format" : "UTC_MILLISEC",
      "read-only": true
    }
    "_lastmodified_by" : {
      "type" : "string",
      "read-only": true
    },
    "_status" : {
      "type" : "string",
      "enum" : [ "PENDING", "OK", "ERROR", "UNKNOWN", "DISABLED", "ENABLE" ],
      "read-only": true
    },
    "_version" : {
      "type" : "integer",
      "read-only": true
    },
    "name" : {
      "type" : "string",
      "required" : true
    },
    "description" : {
      "type" : "string"
    },
    "hash" : {
      "type" : "integer",
      "read-only": true
    },
    "properties" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "string"
      }
    },
    "teams" : {
      "type" : "array",
      "items" : {
        "type" : "object",
        "id" : "urn:jsonschema:io:galeb:manager:entity:Team",
      }
    },
  }
}
```
                
# Target
        
```
{
  "type" : "object",
  "id" : "urn:jsonschema:io:galeb:manager:entity:Target",
  "properties" : {
    "id" : {
      "type" : "integer",
      "read-only": true
    },
    "_created_at" : {
      "type" : "integer",
      "format" : "UTC_MILLISEC",
      "read-only": true
    },
    "_created_by" : {
      "type" : "string",
      "read-only": true
    },
    "_lastmodified_at" : {
      "type" : "integer",
      "format" : "UTC_MILLISEC",
      "read-only": true
    }
    "_lastmodified_by" : {
      "type" : "string",
      "read-only": true
    },
    "_status" : {
      "type" : "string",
      "enum" : [ "PENDING", "OK", "ERROR", "UNKNOWN", "DISABLED", "ENABLE" ],
      "read-only": true
    },
    "_version" : {
      "type" : "integer",
      "read-only": true
    },
    "name" : {
      "type" : "string",
      "required" : true
    },
    "description" : {
      "type" : "string"
    },
    "hash" : {
      "type" : "integer",
      "read-only": true
    },
    "properties" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "string"
      }
    },
    "parent" : {
      "type" : "object",
      "id" : "urn:jsonschema:io:galeb:manager:entity:Pool",
      "required" : true
    },
    "project" : {
      "type" : "object",
      "$ref" : "urn:jsonschema:io:galeb:manager:entity:Project",
      "required" : true
    },
    "environment" : {
      "type" : "object",
      "id" : "urn:jsonschema:io:galeb:manager:entity:Environment",
      "required" : true
    }
  }
} 
```

# Pool          
        
```
{
  "type" : "object",
  "id" : "urn:jsonschema:io:galeb:manager:entity:Pool",
  "properties" : {
    "id" : {
      "type" : "integer",
      "read-only": true
    },
    "_created_at" : {
      "type" : "integer",
      "format" : "UTC_MILLISEC",
      "read-only": true
    },
    "_created_by" : {
      "type" : "string",
      "read-only": true
    },
    "_lastmodified_at" : {
      "type" : "integer",
      "format" : "UTC_MILLISEC",
      "read-only": true
    }
    "_lastmodified_by" : {
      "type" : "string",
      "read-only": true
    },
    "_status" : {
      "type" : "string",
      "enum" : [ "PENDING", "OK", "ERROR", "UNKNOWN", "DISABLED", "ENABLE" ],
      "read-only": true
    },
    "_version" : {
      "type" : "integer",
      "read-only": true
    },
    "name" : {
      "type" : "string",
      "required" : true
    },
    "description" : {
      "type" : "string"
    },
    "hash" : {
      "type" : "integer",
      "read-only": true
    },
    "properties" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "string"
      }
    },
    "project" : {
      "type" : "object",
      "$ref" : "urn:jsonschema:io:galeb:manager:entity:Project",
      "required" : true
    },
    "environment" : {
      "type" : "object",
      "id" : "urn:jsonschema:io:galeb:manager:entity:Environment",
      "required" : true
    },
    "balancePolicy" : {
      "type" : "object",
      "id" : "urn:jsonschema:io:galeb:manager:entity:BalancePolicy",
      "required" : true
    },
    "targets" : {
      "type" : "array",
      "items" : {
        "type" : "object",
        "id" : "urn:jsonschema:io:galeb:manager:entity:Target"
      }
    }
  }
} 
```

## Pool Properties

| name              | info                   | default          | example          |
|-------------------|------------------------|------------------|------------------|
| hcBody            | healthcheck body       | null             | WORKING          |
| hcPath            | healthcheck path       | /                | /healthcheck     |
| hcStatusCode      | healthcheck path       | (any status)     | 200              |
| hcHost            | force HTTP Header Host | VirtualHost FQDN | test.localdomain |
| loadBalancePolicy | loadBalance Policy     | RoundRobin       | LeastConn        |

# VirtualHost    
        
```
{
  "type" : "object",
  "id" : "urn:jsonschema:io:galeb:manager:entity:VirtualHost",
  "properties" : {
    "id" : {
      "type" : "integer",
      "read-only": true
    },
    "_created_at" : {
      "type" : "integer",
      "format" : "UTC_MILLISEC",
      "read-only": true
    },
    "_created_by" : {
      "type" : "string",
      "read-only": true
    },
    "_lastmodified_at" : {
      "type" : "integer",
      "format" : "UTC_MILLISEC",
      "read-only": true
    }
    "_lastmodified_by" : {
      "type" : "string",
      "read-only": true
    },
    "_status" : {
      "type" : "string",
      "enum" : [ "PENDING", "OK", "ERROR", "UNKNOWN", "DISABLED", "ENABLE" ],
      "read-only": true
    },
    "_version" : {
      "type" : "integer",
      "read-only": true
    },
    "name" : {
      "type" : "string",
      "required" : true
    },
    "description" : {
      "type" : "string"
    },
    "hash" : {
      "type" : "integer",
      "read-only": true
    },
    "properties" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "string"
      }
    },
    "aliases" : {
      "type" : "array",
      "items" : {
        "type" : "string"
      }
    },
    "project" : {
      "type" : "object",
      "$ref" : "urn:jsonschema:io:galeb:manager:entity:Project",
      "required" : true
    },
    "environment" : {
      "type" : "object",
      "id" : "urn:jsonschema:io:galeb:manager:entity:Environment",
      "required" : true
    },
    "rules" : {
      "type" : "array",
      "items" : {
        "$ref" : "urn:jsonschema:io:galeb:manager:entity:Rule"
      }
    },
    "ruleDefault" : {
      "type" : "object",
      "id" : "urn:jsonschema:io:galeb:manager:entity:Rule"
    },
    "rulesOrdered" : {
      "type" : "array",
      "items" : {
        "type" : "object",
        "id" : "urn:jsonschema:io:galeb:manager:entity:RuleOrder",
        "properties" : {
          "ruleOrder" : {
            "type" : "integer"
          },
          "ruleId" : {
            "type" : "integer"
          }
        }
      }
    }
  }
} 
```

# Rule    
        
```
{
  "type" : "object",
  "id" : "urn:jsonschema:io:galeb:manager:entity:Rule",
  "properties" : {
    "id" : {
      "type" : "integer",
      "read-only": true
    },
    "_created_at" : {
      "type" : "integer",
      "format" : "UTC_MILLISEC",
      "read-only": true
    },
    "_created_by" : {
      "type" : "string",
      "read-only": true
    },
    "_lastmodified_at" : {
      "type" : "integer",
      "format" : "UTC_MILLISEC",
      "read-only": true
    }
    "_lastmodified_by" : {
      "type" : "string",
      "read-only": true
    },
    "_status" : {
      "type" : "string",
      "enum" : [ "PENDING", "OK", "ERROR", "UNKNOWN", "DISABLED", "ENABLE" ],
      "read-only": true
    },
    "_version" : {
      "type" : "integer",
      "read-only": true
    },
    "name" : {
      "type" : "string",
      "required" : true
    },
    "description" : {
      "type" : "string"
    },
    "hash" : {
      "type" : "integer",
      "read-only": true
    },
    "properties" : {
      "type" : "object",
      "additionalProperties" : {
        "type" : "string"
      }
    },
    "ruleType" : {
      "type" : "object",
      "id" : "urn:jsonschema:io:galeb:manager:entity:RuleType",
      "required" : true
    },
    "pool" : {
      "type" : "object",
      "id" : "urn:jsonschema:io:galeb:manager:entity:Pool",
      "required" : true
    },
    "global" : {
      "type" : "boolean"
    },
    "defaultIn" : {
      "type" : "array",
      "items" : {
        "$ref" : "urn:jsonschema:io:galeb:manager:entity:VirtualHost"
      }
    },
    "parents" : {
      "type" : "array",
      "items" : {
        "type" : "object",
        "id" : "urn:jsonschema:io:galeb:manager:entity:VirtualHost"
      }
    }
  }
}
```

## Rule properties

| name  | info                     | example          |
|-------|--------------------------|------------------|
| match | uri path (w/o wildcards) | /test            |

# Rule Type
        
    TODO
        
# Balance Policy Type
        
    TODO
        
# Balance Policy
        
    TODO
        
# Team    
        
    TODO
        
# Account
        
    TODO
        
# Farm
        
    TODO
        
# Provider
        
    TODO
        
# Environment
        
    TODO
