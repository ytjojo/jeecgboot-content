#!/usr/bin/env python3
"""
Spring Boot standards checker for ai-coding-java-springboot skill (lightweight).

This script performs heuristic static checks for Java/XML source files to catch
high-signal violations of the documented standards in:
- .trae/skills/ai-coding-java-springboot/SKILL.md
- .trae/skills/ai-coding-java-springboot/references/*.md
"""

from __future__ import annotations

import argparse
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable, List, Sequence


JAVA_SUFFIX = ".java"
XML_SUFFIX = ".xml"

AUTOWIRED = re.compile(r"@\s*Autowired\b")
REQUIRED_ARGS = re.compile(r"@\s*RequiredArgsConstructor\b")
RESOURCE = re.compile(r"@\s*Resource\b")
TRANSACTIONAL = re.compile(r"@\s*Transactional\b")
SERVICE_ANNOTATION = re.compile(r"@\s*Service\b")
REST_CONTROLLER = re.compile(r"@\s*RestController\b")
EXTENDS_SERVICE = re.compile(r"\bextends\s+\w*Service\b")
EXTENDS_CONTROLLER = re.compile(r"\bextends\s+\w*Controller\b")

MD5_USAGE = re.compile(r"\bmd5\b|DigestUtils\.md5Hex|MessageDigest\.getInstance\(\s*\"MD5\"\s*\)", re.IGNORECASE)

MAPPER_FIELD = re.compile(r"\bprivate\s+[\w<>, ?]+\b(\w+Mapper)\s+\w+\s*;")
SERVICE_FIELD = re.compile(r"\bprivate\s+[\w<>, ?]+\b(\w+Service)\s+\w+\s*;")
BM_SERVICE_FIELD = re.compile(r"\bprivate\s+[\w<>, ?]+\b(\w+BizManageService)\s+\w+\s*;")

SELECT_STAR = re.compile(r"\bSELECT\s+\*\b", re.IGNORECASE)
XML_DOLLAR_PARAM = re.compile(r"\$\{[^}]+\}")
OFFSET_PAGINATION = re.compile(r"\bOFFSET\b", re.IGNORECASE)

FOR_LOOP = re.compile(r"\bfor\s*\(")
DB_CALL_IN_LOOP = re.compile(r"\b(selectById|selectOne|selectList|selectCount|selectPage)\s*\(", re.IGNORECASE)
CLASS_DECL = re.compile(r"\b(class|interface|enum|record)\b")
METHOD_DECL = re.compile(r"\b\w+\s*\([^;{}]*\)\s*(?:\{|throws\b)?")
ANNOTATION_LINE = re.compile(r"^\s*@")
ANNOTATION_CONTINUATION = re.compile(r"^\s*[\w.]+\s*=|^\s*[)\]}]\s*,?\s*$|^\s*[,({].*")


@dataclass(frozen=True)
class Violation:
    file: Path
    line: int | None
    level: str
    code: str
    message: str

    def format(self) -> str:
        loc = f"{self.file}:{self.line}" if self.line else f"{self.file}"
        return f"{loc}: [{self.level}] {self.code} {self.message}"


def list_files(target: Path) -> List[Path]:
    if target.is_file() and target.suffix in {JAVA_SUFFIX, XML_SUFFIX}:
        return [target]
    if target.is_dir():
        files = list(target.rglob(f"*{JAVA_SUFFIX}")) + list(target.rglob(f"*{XML_SUFFIX}"))
        return sorted(set(files))
    return []


def iter_lines(text: str) -> Iterable[tuple[int, str]]:
    for idx, line in enumerate(text.splitlines(), start=1):
        yield idx, line


def is_controller(file_path: Path, content: str) -> bool:
    lower = str(file_path).lower()
    return "/controller/" in lower or file_path.name.endswith("Controller.java") or bool(REST_CONTROLLER.search(content))


def is_service_or_controller_family(file_path: Path, content: str) -> bool:
    lower = str(file_path).lower()
    return (
        "/service/" in lower
        or is_controller(file_path, content)
        or file_path.name.endswith("Service.java")
        or bool(SERVICE_ANNOTATION.search(content))
        or bool(EXTENDS_SERVICE.search(content))
        or bool(EXTENDS_CONTROLLER.search(content))
    )


def is_mapper(file_path: Path) -> bool:
    lower = str(file_path).lower()
    return "/mapper/" in lower or file_path.name.endswith("Mapper.java")


def is_biz_manage_service(file_path: Path) -> bool:
    lower = str(file_path).lower()
    return "/biz/" in lower or file_path.name.endswith("BizManageService.java")


def is_service_impl(file_path: Path, content: str) -> bool:
    lower = str(file_path).lower()
    return (
        "/service/impl/" in lower
        or file_path.name.endswith("ServiceImpl.java")
        or "extends ServiceImpl" in content
    )


def add(vs: List[Violation], file: Path, line: int | None, level: str, code: str, message: str) -> None:
    vs.append(Violation(file=file, line=line, level=level, code=code, message=message))


def check_java(content: str, file_path: Path) -> List[Violation]:
    vs: List[Violation] = []

    if AUTOWIRED.search(content):
        add(vs, file_path, None, "ERROR", "DI001", "禁止使用 @Autowired，统一使用 @Resource 注入")
    if REQUIRED_ARGS.search(content) and is_service_or_controller_family(file_path, content):
        add(vs, file_path, None, "ERROR", "DI002", "禁止使用 @RequiredArgsConstructor（构造器注入），统一使用 @Resource 注入")

    if MD5_USAGE.search(content):
        add(vs, file_path, None, "ERROR", "SEC001", "疑似使用 MD5/弱哈希存储密码，必须改为 BCrypt 等安全方案")

    if TRANSACTIONAL.search(content):
        if is_controller(file_path, content):
            add(vs, file_path, None, "ERROR", "TX001", "禁止在 Controller 上使用 @Transactional，事务应在 BizManageService（或业务编排层）")
        if is_mapper(file_path):
            add(vs, file_path, None, "ERROR", "TX002", "禁止在 Mapper 上使用 @Transactional")
        if is_service_impl(file_path, content) and not is_biz_manage_service(file_path):
            add(vs, file_path, None, "WARN", "TX003", "建议把跨表/编排事务放在 BizManageService；ServiceImpl 上的事务需确认是否单表写")

    if is_controller(file_path, content):
        if MAPPER_FIELD.search(content):
            add(vs, file_path, None, "ERROR", "LAY001", "Controller 不应注入 Mapper（应调用 BizManageService）")

        has_bm = bool(BM_SERVICE_FIELD.search(content))
        if not has_bm:
            add(vs, file_path, None, "WARN", "LAY003", "Controller 未检测到 BizManageService 依赖注入（若项目采用该分层，应补齐）")

    if is_biz_manage_service(file_path):
        if not SERVICE_ANNOTATION.search(content):
            add(vs, file_path, None, "WARN", "LAY010", "BizManageService 建议标注 @Service")

    if is_service_impl(file_path, content):
        if "extends ServiceImpl" not in content:
            add(vs, file_path, None, "WARN", "MP001", "Service 实现建议继承 MyBatis Plus ServiceImpl")

    for idx, line in iter_lines(content):
        if SELECT_STAR.search(line):
            add(vs, file_path, idx, "WARN", "SQL001", "检测到 SELECT *（建议显式列出字段）")

    in_loop = False
    loop_depth = 0
    for idx, line in iter_lines(content):
        stripped = line.strip()
        if stripped.startswith("//"):
            continue
        if FOR_LOOP.search(line):
            in_loop = True
            loop_depth += 1
        if in_loop and DB_CALL_IN_LOOP.search(line):
            add(vs, file_path, idx, "WARN", "PERF001", "疑似循环内查库（N+1），建议批量查询 + 内存关联")
        if in_loop and "}" in line:
            loop_depth = max(0, loop_depth - line.count("}"))
            if loop_depth == 0:
                in_loop = False

    javadoc_errors: List[str] = []
    check_javadoc(content, file_path, javadoc_errors)
    for error in javadoc_errors:
        _, line_str, message = error.split(":", 2)
        message = message.strip()
        code = "DOC001" if "类声明前缺少 Javadoc" in message else "DOC002"
        add(vs, file_path, int(line_str), "WARN", code, message)

    return vs

# def check_select_star(content: str, file_path: Path, errors: List[str]) -> None:
#     """Block SQL SELECT * usage in Java source literals/comments as a quick safety net."""
#     if re.search(r"\bSELECT\s+\*\b", content, re.IGNORECASE):
#         errors.append(f"{file_path}: 检测到 `SELECT *`，请改为明确字段列表")


def check_javadoc(content: str, file_path: Path, errors: List[str]) -> None:
    """Require class-level and public-method Javadoc comments with lightweight pattern matching."""
    lines = content.splitlines()

    def is_ignorable_javadoc_gap_line(line: str) -> bool:
        stripped = line.strip()
        if not stripped:
            return True
        return bool(ANNOTATION_LINE.search(line) or ANNOTATION_CONTINUATION.search(line))

    def has_javadoc_before_declaration(idx: int) -> bool:
        cursor = idx - 1
        while cursor >= 0:
            stripped = lines[cursor].strip()
            if stripped == "*/":
                return True
            if is_ignorable_javadoc_gap_line(lines[cursor]):
                cursor -= 1
                continue
            return False
        return False

    for idx, line in enumerate(lines):
        if CLASS_DECL.search(line):
            if not has_javadoc_before_declaration(idx):
                errors.append(f"{file_path}:{idx + 1}: 类声明前缺少 Javadoc")
            break

    for idx, line in enumerate(lines):
        if re.search(r"\bpublic\b", line) and METHOD_DECL.search(line):
            if not has_javadoc_before_declaration(idx):
                errors.append(f"{file_path}:{idx + 1}: public 方法前缺少 Javadoc")




def check_xml(content: str, file_path: Path) -> List[Violation]:
    vs: List[Violation] = []

    for idx, line in iter_lines(content):
        if XML_DOLLAR_PARAM.search(line):
            add(vs, file_path, idx, "ERROR", "SEC010", "检测到 ${} 字符串拼接，存在 SQL 注入风险；请改为 #{} 或白名单拼接")
        if SELECT_STAR.search(line):
            add(vs, file_path, idx, "WARN", "SQL010", "检测到 SELECT *（建议显式列出字段）")
        if OFFSET_PAGINATION.search(line):
            add(vs, file_path, idx, "WARN", "PERF010", "检测到 OFFSET 深分页，建议游标分页/基于 lastId 分页")

    return vs


def read_text_utf8(file_path: Path) -> str:
    return file_path.read_text(encoding="utf-8")


def parse_args(argv: Sequence[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Check code against ai-coding-java-springboot standards.")
    parser.add_argument("--target", required=True, help="Java/XML file path or directory path to scan.")
    parser.add_argument("--warn-only", action="store_true", help="Exit with 0 even if errors are found.")
    return parser.parse_args(argv)


def main(argv: Sequence[str]) -> int:
    args = parse_args(argv)
    target = Path(args.target).expanduser().resolve()
    files = list_files(target)
    if not files:
        print(f"未找到可检查的 Java/XML 文件: {target}")
        return 1

    violations: List[Violation] = []
    for fp in files:
        try:
            content = read_text_utf8(fp)
        except UnicodeDecodeError:
            violations.append(Violation(file=fp, line=None, level="ERROR", code="IO001", message="文件编码不是 UTF-8"))
            continue

        if fp.suffix == JAVA_SUFFIX:
            violations.extend(check_java(content, fp))
        elif fp.suffix == XML_SUFFIX:
            violations.extend(check_xml(content, fp))

    if violations:
        errors = [v for v in violations if v.level == "ERROR"]
        warns = [v for v in violations if v.level != "ERROR"]

        print("开发规范检查未通过：")
        for v in errors + warns:
            print(f"- {v.format()}")

        if args.warn_only:
            return 0
        return 1 if errors else 0

    print("开发规范检查通过！")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
