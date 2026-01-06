# mod-erm-usage-counter

Copyright (C) 2018-2026 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0. See the
file "[LICENSE](LICENSE)" for more information.

![Development funded by European Regional Development Fund (EFRE)](assets/EFRE_2015_quer_RGB_klein.jpg)

## Introduction

A library containing models, clients and utilities for different versions of the COUNTER/SUSHI
standard.

## Modules

| Module                           | Purpose                                                            | 
|----------------------------------|--------------------------------------------------------------------|
| `mod-erm-usage-counter41`        | COUNTER 4.1 report models, client and utilities                    |
| `mod-erm-usage-counter50`        | COUNTER 5.0 (v5.0.3) report models and utilities                   |
| `mod-erm-usage-counter50-client` | Vert.x HTTP client for COUNTER 5.0 SUSHI API (no validation)       |
| `mod-erm-usage-counter51`        | COUNTER 5.1 (v5.1.0.1) report models and utilities                 |
| `mod-erm-usage-counter51-client` | Vert.x HTTP client for COUNTER 5.1 SUSHI API (validates responses) |
| `mod-erm-usage-counter-common`   | Shared utilities and common code                                   |

### Simplified API for `counter50-client` and `counter51-client`

The client modules expose a simplified API that covers common use cases while hiding complexity:

**Methods (both clients)**: `getReportsTR`, `getReportsIR`, `getReportsDR`, `getReportsPR`

**Additional methods (`counter51-client`)**: `getStatus()`, `getMembers()`, `getReports()`

**Exposed Parameters:**

- `customerId` - Required for authenticated endpoints
- `beginDate`, `endDate` - Required for report endpoints
- `platform` - Optional (for multi-platform providers)

**Hardcoded Values (`counter50-client`):**

| Parameter                     | Value                                                                                    |
|-------------------------------|------------------------------------------------------------------------------------------|
| `attributes_to_show` (TR)     | `Data_Type\|Section_Type\|YOP\|Access_Type\|Access_Method`                               |
| `attributes_to_show` (IR)     | `Authors\|Publication_Date\|Article_Version\|Data_Type\|YOP\|Access_Type\|Access_Method` |
| `attributes_to_show` (DR)     | `Data_Type\|Access_Method`                                                               |
| `attributes_to_show` (PR)     | `Data_Type\|Access_Method`                                                               |
| `include_parent_details` (IR) | `"True"`                                                                                 |

**Hardcoded Values (`counter51-client`):**

| Parameter                     | Value                                                                         |
|-------------------------------|-------------------------------------------------------------------------------|
| `attributes_to_show` (TR)     | `YOP\|Access_Type\|Access_Method`                                             |
| `attributes_to_show` (IR)     | `Authors\|Publication_Date\|Article_Version\|YOP\|Access_Type\|Access_Method` |
| `attributes_to_show` (DR)     | `Access_Method`                                                               |
| `attributes_to_show` (PR)     | `Access_Method`                                                               |
| `include_parent_details` (IR) | `"True"`                                                                      |

**URL Handling (`counter51-client`):**

- The client automatically appends `/r51` to the service URL if not already present
- Trailing slashes are removed before appending the suffix

**Omitted Parameters:**

- COUNTER filter parameters (`metric_type`, `data_type`, `access_type`, `access_method`, `yop`,
  `section_type`, etc.)
- Rationale: Focus on fetching complete reports, use report converter utilities to generate COUNTER
  Standard Views

**Advanced Usage:**

- Subclass client and override `makeRequest()` for custom parameters
- Or filter results after retrieval
- Or use report converter utilities to generate COUNTER Standard Views

## Additional information

### Issue tracker

See project [MODEUSCNT](https://issues.folio.org/browse/MODEUSCNT)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation

Other [modules](https://dev.folio.org/source-code/#server-side) are described, with further FOLIO
Developer documentation at [dev.folio.org](https://dev.folio.org/)

