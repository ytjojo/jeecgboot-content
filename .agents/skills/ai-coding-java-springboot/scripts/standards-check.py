#!/usr/bin/env python3
"""
Spring Boot standards checker — tree-sitter AST 版本。

用 tree-sitter-java 解析源码 AST，替代正则启发式检测，支持 Java 14+ 语法。

依赖：tree-sitter >= 0.22, tree-sitter-java >= 0.23
"""

from __future__ import annotations

import argparse
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import List, Sequence

try:
    import tree_sitter_java as tsjava
    from tree_sitter import Language, Parser
except ImportError:
    print("缺少依赖，请先安装：pip install tree-sitter tree-sitter-java", file=sys.stderr)
    sys.exit(1)

JAVA_LANG = Language(tsjava.language())
_parser = Parser(JAVA_LANG)

JAVA_SUFFIX = ".java"
XML_SUFFIX = ".xml"

# ── XML 正则（XML 不走 AST，保持正则） ─────────────────────
SELECT_STAR = re.compile(r"\bSELECT\s+\*\b", re.IGNORECASE)
XML_DOLLAR_PARAM = re.compile(r"\$\{[^}]+\}")
OFFSET_PAGINATION = re.compile(r"\bOFFSET\b", re.IGNORECASE)


# ── 数据结构 ──────────────────────────────────────────────

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


def _add(vs: List[Violation], file: Path, line: int | None, level: str, code: str, message: str) -> None:
    vs.append(Violation(file=file, line=line, level=level, code=code, message=message))


# ── tree-sitter 辅助 ──────────────────────────────────────

def _parse_java(source: str):
    """解析 Java 源码，返回 tree。"""
    return _parser.parse(source.encode("utf-8"))


def _text(node, src: bytes) -> str:
    """获取节点对应的源码文本。"""
    return src[node.start_byte:node.end_byte].decode("utf-8")


def _find_all(node, type_name: str):
    """递归查找所有指定类型的节点。"""
    results = []
    if node.type == type_name:
        results.append(node)
    for child in node.children:
        results.extend(_find_all(child, type_name))
    return results


def _find_children(node, type_name: str):
    """查找直接子节点中指定类型的节点。"""
    return [c for c in node.children if c.type == type_name]


def _get_annotation_names(modifiers_node, src: bytes) -> set[str]:
    """从 modifiers 节点提取所有注解名称。"""
    names: set[str] = set()
    for child in modifiers_node.children:
        if child.type in ("marker_annotation", "annotation"):
            for ident in _find_children(child, "identifier"):
                names.add(_text(ident, src))
            for sc in _find_children(child, "scoped_identifier"):
                names.add(_text(sc, src).split(".")[-1])
    return names


def _get_modifiers(modifiers_node) -> set[str]:
    """从 modifiers 节点提取访问修饰符。"""
    mods: set[str] = set()
    for child in modifiers_node.children:
        if child.type in ("public", "private", "protected", "static", "final", "abstract", "synchronized"):
            mods.add(child.type)
    return mods


def _get_class_info(cls_node, src: bytes) -> tuple[str, set[str], str | None]:
    """获取类名、类注解集合、父类名。"""
    name = ""
    annotations: set[str] = set()
    superclass: str | None = None

    for c in cls_node.children:
        if c.type == "identifier":
            name = _text(c, src)
        elif c.type == "modifiers":
            annotations = _get_annotation_names(c, src)
        elif c.type == "superclass":
            # superclass → type_identifier 或 generic_type
            for sc in c.children:
                if sc.type in ("type_identifier", "generic_type"):
                    superclass = _text(sc, src)
                    break

    return name, annotations, superclass


def _get_field_info(field_node, src: bytes) -> tuple[str, str, set[str]]:
    """获取字段名、字段类型、字段注解集合。"""
    annotations: set[str] = set()
    field_type = ""
    field_name = ""

    for c in field_node.children:
        if c.type == "modifiers":
            annotations = _get_annotation_names(c, src)
        elif c.type in ("type_identifier", "generic_type", "integral_type", "boolean_type", "void_type", "array_type"):
            field_type = _text(c, src)
        elif c.type == "variable_declarator":
            for vc in c.children:
                if vc.type == "identifier":
                    field_name = _text(vc, src)

    return field_name, field_type, annotations


def _get_method_info(method_node, src: bytes) -> tuple[str, set[str], set[str]]:
    """获取方法名、方法注解集合、方法修饰符集合。"""
    name = ""
    annotations: set[str] = set()
    mods: set[str] = set()

    for c in method_node.children:
        if c.type == "identifier":
            name = _text(c, src)
        elif c.type == "modifiers":
            annotations = _get_annotation_names(c, src)
            mods = _get_modifiers(c)

    return name, annotations, mods


def _has_javadoc_before(lines: list[str], line_idx: int) -> bool:
    """检查指定行（0-indexed）之前是否有 Javadoc 注释。"""
    cursor = line_idx - 1
    while cursor >= 0:
        stripped = lines[cursor].strip()
        if stripped == "*/":
            return True
        if not stripped:
            cursor -= 1
            continue
        if stripped.startswith("@") or stripped.endswith(",") or stripped.endswith("("):
            cursor -= 1
            continue
        return False
    return False


# ── 文件类型判断 ──────────────────────────────────────────

def _is_controller(path: Path, cls_annotations: set[str]) -> bool:
    lower = str(path).lower()
    return "/controller/" in lower or path.name.endswith("Controller.java") or bool({"RestController", "Controller"} & cls_annotations)


def _is_mapper(path: Path) -> bool:
    lower = str(path).lower()
    return "/mapper/" in lower or path.name.endswith("Mapper.java")


def _is_biz_manage_service(path: Path) -> bool:
    lower = str(path).lower()
    return "/biz/" in lower or path.name.endswith("BizManageService.java")


def _is_service_impl(path: Path, superclass: str | None, cls_annotations: set[str]) -> bool:
    lower = str(path).lower()
    return (
        "/service/impl/" in lower
        or path.name.endswith("ServiceImpl.java")
        or (superclass is not None and "ServiceImpl" in superclass)
        or "ServiceImpl" in str(cls_annotations)
    )


def _is_service_or_controller_family(path: Path, cls_annotations: set[str], superclass: str | None) -> bool:
    lower = str(path).lower()
    return (
        "/service/" in lower
        or _is_controller(path, cls_annotations)
        or path.name.endswith("Service.java")
        or "Service" in cls_annotations
        or (superclass is not None and "Service" in superclass)
    )


# ── Java 检测规则 ─────────────────────────────────────────

def check_java(content: str, file_path: Path) -> List[Violation]:
    vs: List[Violation] = []
    tree = _parse_java(content)
    src = content.encode("utf-8")
    lines = content.splitlines()
    root = tree.root_node

    # 获取顶层类
    classes = _find_all(root, "class_declaration")
    if not classes:
        return vs

    cls = classes[0]
    _, cls_annotations, superclass = _get_class_info(cls, src)

    is_ctrl = _is_controller(file_path, cls_annotations)
    is_mapper = _is_mapper(file_path)
    is_biz = _is_biz_manage_service(file_path)
    is_svc_impl = _is_service_impl(file_path, superclass, cls_annotations)
    is_svc_or_ctrl = _is_service_or_controller_family(file_path, cls_annotations, superclass)

    # ── DI001 / DI002: 注入方式检查 ──
    for field in _find_all(cls, "field_declaration"):
        _, ftype, fannots = _get_field_info(field, src)
        line = field.start_point[0] + 1

        if "Autowired" in fannots:
            _add(vs, file_path, line, "ERROR", "DI001", "禁止使用 @Autowired，统一使用 @Resource 注入")

    for method in _find_all(cls, "method_declaration"):
        _, mannots, _ = _get_method_info(method, src)
        line = method.start_point[0] + 1

        if "Autowired" in mannots:
            _add(vs, file_path, line, "ERROR", "DI001", "禁止使用 @Autowired，统一使用 @Resource 注入")
        if "RequiredArgsConstructor" in mannots and is_svc_or_ctrl:
            _add(vs, file_path, line, "ERROR", "DI002", "禁止使用 @RequiredArgsConstructor（构造器注入），统一使用 @Resource 注入")

    # 构造器级别检查
    for ctor in _find_all(cls, "constructor_declaration"):
        ctor_annots: set[str] = set()
        for c in ctor.children:
            if c.type == "modifiers":
                ctor_annots = _get_annotation_names(c, src)
        if "RequiredArgsConstructor" in ctor_annots and is_svc_or_ctrl:
            _add(vs, file_path, ctor.start_point[0] + 1, "ERROR", "DI002", "禁止使用 @RequiredArgsConstructor（构造器注入），统一使用 @Resource 注入")

    # ── SEC001: MD5 使用 ──
    for inv in _find_all(root, "method_invocation"):
        method_name = ""
        for c in inv.children:
            if c.type == "identifier":
                method_name = _text(c, src)
                break
        if method_name in ("md5Hex", "md5"):
            _add(vs, file_path, inv.start_point[0] + 1, "ERROR", "SEC001", "疑似使用 MD5/弱哈希存储密码，必须改为 BCrypt 等安全方案")

    # ── TX001 / TX002 / TX003: 事务检查 ──
    if "Transactional" in cls_annotations:
        if is_ctrl:
            _add(vs, file_path, cls.start_point[0] + 1, "ERROR", "TX001", "禁止在 Controller 上使用 @Transactional，事务应在 BizManageService（或业务编排层）")
        if is_mapper:
            _add(vs, file_path, cls.start_point[0] + 1, "ERROR", "TX002", "禁止在 Mapper 上使用 @Transactional")

    for method in _find_all(cls, "method_declaration"):
        _, mannots, _ = _get_method_info(method, src)
        line = method.start_point[0] + 1

        if "Transactional" not in mannots:
            continue
        if is_ctrl:
            _add(vs, file_path, line, "ERROR", "TX001", "禁止在 Controller 上使用 @Transactional，事务应在 BizManageService（或业务编排层）")
        if is_mapper:
            _add(vs, file_path, line, "ERROR", "TX002", "禁止在 Mapper 上使用 @Transactional")
        if is_svc_impl and not is_biz:
            _add(vs, file_path, line, "WARN", "TX003", "建议把跨表/编排事务放在 BizManageService；ServiceImpl 上的事务需确认是否单表写")

    # ── LAY001 / LAY003: Controller 分层检查 ──
    if is_ctrl:
        has_bm = False
        for field in _find_all(cls, "field_declaration"):
            _, ftype, _ = _get_field_info(field, src)
            if ftype.endswith("Mapper"):
                _add(vs, file_path, field.start_point[0] + 1, "ERROR", "LAY001", "Controller 不应注入 Mapper（应调用 BizManageService）")
            if ftype.endswith("BizManageService"):
                has_bm = True

        if not has_bm:
            _add(vs, file_path, None, "WARN", "LAY003", "Controller 未检测到 BizManageService 依赖注入（若项目采用该分层，应补齐）")

    # ── LAY010: BizManageService 标注 @Service ──
    if is_biz and "Service" not in cls_annotations:
        _add(vs, file_path, cls.start_point[0] + 1, "WARN", "LAY010", "BizManageService 建议标注 @Service")

    # ── MP001: ServiceImpl 继承 ServiceImpl ──
    if is_svc_impl:
        extends_svc_impl = superclass is not None and "ServiceImpl" in superclass
        if not extends_svc_impl:
            _add(vs, file_path, cls.start_point[0] + 1, "WARN", "MP001", "Service 实现建议继承 MyBatis Plus ServiceImpl")

    # ── SQL001: SELECT * ──
    for lit in _find_all(root, "string_literal"):
        text = _text(lit, src).upper()
        if "SELECT" in text and "*" in text:
            _add(vs, file_path, lit.start_point[0] + 1, "WARN", "SQL001", "检测到 SELECT *（建议显式列出字段）")

    # ── PERF001: 循环内查库 ──
    db_call_names = {"selectById", "selectOne", "selectList", "selectCount", "selectPage"}
    for_loop_nodes = _find_all(root, "for_statement") + _find_all(root, "enhanced_for_statement")
    for loop in for_loop_nodes:
        for inv in _find_all(loop, "method_invocation"):
            method_name = ""
            for c in inv.children:
                if c.type == "identifier":
                    method_name = _text(c, src)
                    break
            if method_name in db_call_names:
                _add(vs, file_path, inv.start_point[0] + 1, "WARN", "PERF001", "疑似循环内查库（N+1），建议批量查询 + 内存关联")
                break  # 每个循环只报一次

    # ── DOC001 / DOC002: Javadoc 检查 ──
    cls_line_idx = cls.start_point[0]
    if not _has_javadoc_before(lines, cls_line_idx):
        _add(vs, file_path, cls_line_idx + 1, "WARN", "DOC001", "类声明前缺少 Javadoc")

    for method in _find_all(cls, "method_declaration"):
        _, _, mmods = _get_method_info(method, src)
        if "public" not in mmods:
            continue
        method_line_idx = method.start_point[0]
        if not _has_javadoc_before(lines, method_line_idx):
            _add(vs, file_path, method_line_idx + 1, "WARN", "DOC002", "public 方法前缺少 Javadoc")

    return vs


# ── XML 检测规则（保持正则） ───────────────────────────────

def check_xml(content: str, file_path: Path) -> List[Violation]:
    vs: List[Violation] = []
    for idx, line in enumerate(content.splitlines(), start=1):
        if XML_DOLLAR_PARAM.search(line):
            _add(vs, file_path, idx, "ERROR", "SEC010", "检测到 ${} 字符串拼接，存在 SQL 注入风险；请改为 #{} 或白名单拼接")
        if SELECT_STAR.search(line):
            _add(vs, file_path, idx, "WARN", "SQL010", "检测到 SELECT *（建议显式列出字段）")
        if OFFSET_PAGINATION.search(line):
            _add(vs, file_path, idx, "WARN", "PERF010", "检测到 OFFSET 深分页，建议游标分页/基于 lastId 分页")
    return vs


# ── 文件遍历 ──────────────────────────────────────────────

def list_files(target: Path) -> List[Path]:
    if target.is_file() and target.suffix in {JAVA_SUFFIX, XML_SUFFIX}:
        return [target]
    if target.is_dir():
        files = list(target.rglob(f"*{JAVA_SUFFIX}")) + list(target.rglob(f"*{XML_SUFFIX}"))
        return sorted(set(files))
    return []


def read_text_utf8(file_path: Path) -> str:
    return file_path.read_text(encoding="utf-8")


# ── 入口 ──────────────────────────────────────────────────

def parse_args(argv: Sequence[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Check code against ai-coding-java-springboot standards (tree-sitter AST).")
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
