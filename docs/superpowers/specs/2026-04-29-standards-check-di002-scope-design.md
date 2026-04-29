# DI002 Scope Design

## Background

`standards-check.py` currently reports `DI002` for any Java file that contains `@RequiredArgsConstructor`.
This is broader than the intended rule scope.

## Goal

Restrict `DI002` so it only applies to `service`, `controller`, and their subclasses/family classes.

## Detection Strategy

Use a combined heuristic instead of a full Java parser:

- Path and naming hints:
  - files under `/service/` or `/controller/`
  - file names ending with `Service.java` or `Controller.java`
- Annotation hints:
  - `@Service`
  - `@RestController`
- Inheritance hints:
  - `extends ...Service`
  - `extends ...Controller`

## Change Plan

1. Add a helper such as `is_service_or_controller_family(file_path, content)`.
2. Reuse existing controller detection where practical.
3. Gate the `DI002` violation on the new helper.

## Non-Goals

- No changes to other DI rules such as `DI001`.
- No AST-level parsing.
- No behavior changes for XML checks.
