#!/usr/bin/env python3
"""
Alibaba Java guidelines checker for quick local validation.

This script performs lightweight static checks for Java source files
based on team conventions and Alibaba/P3C-inspired rules.
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path
from typing import List


UPPER_CAMEL = re.compile(r"^[A-Z][A-Za-z0-9]*$")
LOWER_CAMEL = re.compile(r"^[a-z][A-Za-z0-9]*$")
UPPER_SNAKE = re.compile(r"^[A-Z][A-Z0-9_]*$")

CLASS_DECL = re.compile(r"\bclass\s+([A-Za-z_][A-Za-z0-9_]*)")
CONST_DECL = re.compile(
    r"\b(?:public|protected|private)?\s*static\s+final\s+[A-Za-z0-9_<>\[\], ?]+\s+([A-Za-z_][A-Za-z0-9_]*)\s*="
)
METHOD_DECL = re.compile(
    r"\b(?:public|protected|private)\s+(?:static\s+)?[A-Za-z0-9_<>\[\], ?]+\s+([A-Za-z_][A-Za-z0-9_]*)\s*\("
)

# Heuristics for architecture checks.
MAPPER_INJECT = re.compile(r"@\s*(?:Autowired|Resource)\s+.*\bMapper\b|private\s+\w+Mapper\s+\w+\s*;")
TX_ANNOTATION = re.compile(r"@\s*Transactional\b")


def list_java_files(target: Path) -> List[Path]:
    """Collect Java source files from a single file or recursively from a directory."""
    if target.is_file() and target.suffix == ".java":
        return [target]
    if target.is_dir():
        return sorted(target.rglob("*.java"))
    return []


def check_class_name(content: str, file_path: Path, errors: List[str]) -> None:
    """Validate class naming with UpperCamelCase for declared classes."""
    for match in CLASS_DECL.finditer(content):
        class_name = match.group(1)
        if not UPPER_CAMEL.match(class_name):
            errors.append(f"{file_path}: 类名 `{class_name}` 不符合 UpperCamelCase")


def check_method_name(content: str, file_path: Path, errors: List[str]) -> None:
    """Validate method naming with lowerCamelCase while skipping common Java special methods."""
    ignore = {"equals", "hashCode", "toString", "main"}
    for match in METHOD_DECL.finditer(content):
        method_name = match.group(1)
        if method_name in ignore:
            continue
        if not LOWER_CAMEL.match(method_name):
            errors.append(f"{file_path}: 方法名 `{method_name}` 不符合 lowerCamelCase")


def check_constant_name(content: str, file_path: Path, errors: List[str]) -> None:
    """Validate static final constant names with UPPER_CASE_UNDERSCORE."""
    for match in CONST_DECL.finditer(content):
        constant_name = match.group(1)
        if not UPPER_SNAKE.match(constant_name):
            errors.append(f"{file_path}: 常量名 `{constant_name}` 不符合 UPPER_CASE_UNDERSCORE")


def check_select_star(content: str, file_path: Path, errors: List[str]) -> None:
    """Block SQL SELECT * usage in Java source literals/comments as a quick safety net."""
    if re.search(r"\bSELECT\s+\*\b", content, re.IGNORECASE):
        errors.append(f"{file_path}: 检测到 `SELECT *`，请改为明确字段列表")


def check_spacing_rules(content: str, file_path: Path, errors: List[str]) -> None:
    """Check simple spacing conventions for braces and binary operators."""
    for idx, line in enumerate(content.splitlines(), start=1):
        stripped = line.strip()
        if not stripped or stripped.startswith("//"):
            continue

        # Require whitespace before opening brace in declaration-like lines.
        if re.search(r"\)\{", line):
            errors.append(f"{file_path}:{idx}: `{'){'}` 前应保留空格")

        # Require spaces around common binary operators (heuristic).
        for pattern in [r"\w=[^=]", r"\w\+[^\+]", r"\w-[^>0-9 ]", r"\w\*[^\* ]", r"\w/[^\* ]"]:
            if re.search(pattern, line):
                errors.append(f"{file_path}:{idx}: 操作符周围建议保留空格")
                break


def check_javadoc(content: str, file_path: Path, errors: List[str]) -> None:
    """Require class-level and public-method Javadoc comments with lightweight pattern matching."""
    lines = content.splitlines()
    for idx, line in enumerate(lines):
        if CLASS_DECL.search(line):
            prev = lines[idx - 1].strip() if idx > 0 else ""
            if prev != "*/":
                errors.append(f"{file_path}:{idx + 1}: 类声明前缺少 Javadoc")
            break

    for idx, line in enumerate(lines):
        if re.search(r"\bpublic\b", line) and METHOD_DECL.search(line):
            prev = lines[idx - 1].strip() if idx > 0 else ""
            if prev != "*/":
                errors.append(f"{file_path}:{idx + 1}: public 方法前缺少 Javadoc")


def check_architecture_rules(content: str, file_path: Path, errors: List[str]) -> None:
    """Check layering constraints for Controller/Mapper transaction and injection anti-patterns."""
    lower_name = file_path.name.lower()
    if "controller" in lower_name:
        if MAPPER_INJECT.search(content):
            errors.append(f"{file_path}: Controller 不应直接注入 Mapper")
        if TX_ANNOTATION.search(content):
            errors.append(f"{file_path}: Controller 不应声明 @Transactional")
    if "mapper" in lower_name and TX_ANNOTATION.search(content):
        errors.append(f"{file_path}: Mapper 不应声明 @Transactional")


def check_java_file(file_path: Path) -> List[str]:
    """Run all checks for one Java file and return collected violations."""
    try:
        content = file_path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return [f"{file_path}: 文件编码不是 UTF-8"]

    errors: List[str] = []
    check_class_name(content, file_path, errors)
    check_method_name(content, file_path, errors)
    check_constant_name(content, file_path, errors)
    check_select_star(content, file_path, errors)
    check_spacing_rules(content, file_path, errors)
    check_javadoc(content, file_path, errors)
    check_architecture_rules(content, file_path, errors)
    return errors


def parse_args() -> argparse.Namespace:
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description="Check Java files against Alibaba/P3C-inspired conventions."
    )
    parser.add_argument(
        "--target",
        required=True,
        help="Java file path or directory path to scan.",
    )
    return parser.parse_args()


def main() -> int:
    """Program entry point. Returns process exit code for CI integration."""
    args = parse_args()
    target = Path(args.target).expanduser().resolve()
    java_files = list_java_files(target)

    if not java_files:
        print(f"未找到可检查的 Java 文件: {target}")
        return 1

    all_errors: List[str] = []
    for java_file in java_files:
        all_errors.extend(check_java_file(java_file))

    if all_errors:
        print("代码规范检查未通过：")
        for item in all_errors:
            print(f"- {item}")
        return 1

    print("代码规范检查通过！")
    return 0


if __name__ == "__main__":
    sys.exit(main())
