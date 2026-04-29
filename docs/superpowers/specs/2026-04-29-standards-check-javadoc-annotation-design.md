# Javadoc Annotation Gap Design

## Background

`check_javadoc()` currently checks only whether the previous line before a class or public method declaration is `*/`.
This causes false positives when valid annotations appear between Javadoc and the declaration.

## Goal

Improve Javadoc detection so class and public method declarations are still considered documented when Javadoc is followed by annotations and blank lines.

## Detection Rules

The checker should treat the following lines between Javadoc and a declaration as ignorable:

- blank lines
- annotation start lines such as `@RequestMapping`, `@Operation`, `@Override`
- annotation continuation lines such as `value = "/x"` or `method = RequestMethod.GET`
- annotation closing lines such as `)` or `})`

If backward scanning from a declaration reaches `*/` after skipping only ignorable lines, the declaration is considered to have Javadoc.
If scanning reaches any other meaningful code line first, the declaration should still be reported as missing Javadoc.

## Implementation Approach

Add a small helper for backward scanning instead of checking only the immediately previous line.

Suggested flow:

1. Split file content into lines.
2. For each target declaration, scan upward.
3. Skip ignorable lines.
4. Accept when `*/` is found.
5. Reject when any non-ignorable line is found first.

## Scope

- update class-level Javadoc detection
- update `public` method Javadoc detection
- keep the implementation lightweight without Java AST parsing

## Non-Goals

- no full Java syntax parser
- no changes to unrelated static checks
- no special handling for non-public methods
